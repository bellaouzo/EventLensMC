package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.correlation.CorrelationActionKind;
import dev.bellaouzo.eventlens.domain.correlation.CorrelationChannelCodec;
import dev.bellaouzo.eventlens.domain.correlation.CorrelationPair;
import dev.bellaouzo.eventlens.domain.report.TraceReportDocument;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TraceCorrelateService {

    private final TraceSessionManager sessionManager;
    private final ExportCommandService exportCommandService;

    public TraceCorrelateService(TraceSessionManager sessionManager, ExportCommandService exportCommandService) {
        this.sessionManager = sessionManager;
        this.exportCommandService = exportCommandService;
    }

    public CorrelateResult correlate(String leftSessionId, String rightSessionId) {
        Optional<TraceSessionDetail> left = sessionManager.getSessionDetail(leftSessionId);
        if (left.isEmpty()) {
            return CorrelateResult.notFound(leftSessionId);
        }
        Optional<TraceSessionDetail> right = sessionManager.getSessionDetail(rightSessionId);
        if (right.isEmpty()) {
            Optional<TraceReportDocument> report = exportCommandService.buildReport(
                    rightSessionId, dev.bellaouzo.eventlens.domain.report.ExportRedactionMode.FULL);
            if (report.isEmpty()) {
                return CorrelateResult.notFound(rightSessionId);
            }
            return CorrelateResult.ok(
                    pair(left.get().records(), report.get().dispatches(), leftSessionId, rightSessionId));
        }
        List<CorrelationPair> pairs = pair(left.get().records(), right.get().records(), leftSessionId, rightSessionId);
        stampPairs(leftSessionId, rightSessionId, pairs);
        return CorrelateResult.ok(pairs);
    }

    public static List<CorrelationPair> pair(
            List<TraceDispatchRecord> left,
            List<TraceDispatchRecord> right,
            String leftSessionId,
            String rightSessionId) {
        List<CorrelationPair> pairs = new ArrayList<>();
        boolean[] used = new boolean[right.size()];
        for (TraceDispatchRecord leftRecord : left) {
            Optional<String> leftKey = leftRecord.correlation().correlationKey();
            Optional<String> leftKind = leftRecord.correlation().actionKind();
            if (leftKey.isEmpty() || leftKind.isEmpty()) {
                continue;
            }
            for (int index = 0; index < right.size(); index++) {
                if (used[index]) {
                    continue;
                }
                TraceDispatchRecord rightRecord = right.get(index);
                if (matches(leftRecord, rightRecord)) {
                    used[index] = true;
                    pairs.add(new CorrelationPair(
                            leftSessionId,
                            leftRecord.sequence(),
                            rightSessionId,
                            rightRecord.sequence(),
                            leftKey.get()));
                    break;
                }
            }
        }
        return List.copyOf(pairs);
    }

    private static boolean matches(TraceDispatchRecord left, TraceDispatchRecord right) {
        Optional<String> leftKey = left.correlation().correlationKey();
        Optional<String> rightKey = right.correlation().correlationKey();
        if (leftKey.isPresent() && leftKey.equals(rightKey)) {
            return true;
        }
        Optional<CorrelationActionKind> leftKind =
                left.correlation().actionKind().flatMap(value -> parseKind(value));
        Optional<CorrelationActionKind> rightKind =
                right.correlation().actionKind().flatMap(TraceCorrelateService::parseKind);
        if (leftKind.isEmpty() || rightKind.isEmpty() || !leftKind.get().pairsWith(rightKind.get())) {
            return false;
        }
        return sameBucket(left, right) && samePlayer(left, right);
    }

    private static Optional<CorrelationActionKind> parseKind(String value) {
        try {
            return Optional.of(CorrelationActionKind.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static boolean sameBucket(TraceDispatchRecord left, TraceDispatchRecord right) {
        long leftBucket = left.startedAtMillis() / 100L;
        long rightBucket = right.startedAtMillis() / 100L;
        return Math.abs(leftBucket - rightBucket) <= 1L;
    }

    private static boolean samePlayer(TraceDispatchRecord left, TraceDispatchRecord right) {
        return left.playerName().isEmpty()
                || right.playerName().isEmpty()
                || left.playerName().equals(right.playerName());
    }

    public Optional<CorrelationChannelCodec.Reply> acceptHello(
            String clientSessionId, long clientSequence, String correlationKey) {
        if (correlationKey == null || correlationKey.isBlank()) {
            return Optional.empty();
        }
        for (var summary : sessionManager.listSessions()) {
            Optional<CorrelationChannelCodec.Reply> reply =
                    matchHello(summary.sessionId(), clientSessionId, clientSequence, correlationKey);
            if (reply.isPresent()) {
                return reply;
            }
        }
        return Optional.empty();
    }

    private Optional<CorrelationChannelCodec.Reply> matchHello(
            String serverSessionId, String clientSessionId, long clientSequence, String correlationKey) {
        Optional<TraceSessionDetail> detail = sessionManager.getSessionDetail(serverSessionId);
        if (detail.isEmpty()) {
            return Optional.empty();
        }
        for (TraceDispatchRecord dispatch : detail.get().records()) {
            if (dispatch.correlation()
                    .correlationKey()
                    .filter(correlationKey::equals)
                    .isPresent()) {
                sessionManager.stampPeer(serverSessionId, correlationKey, clientSessionId, clientSequence);
                return Optional.of(new CorrelationChannelCodec.Reply(
                        serverSessionId, dispatch.sequence(), clientSessionId, clientSequence));
            }
        }
        return Optional.empty();
    }

    private void stampPairs(String leftSessionId, String rightSessionId, List<CorrelationPair> pairs) {
        for (CorrelationPair pair : pairs) {
            sessionManager.stampPeer(leftSessionId, pair.correlationKey(), rightSessionId, pair.rightSequence());
            sessionManager.stampPeer(rightSessionId, pair.correlationKey(), leftSessionId, pair.leftSequence());
        }
    }

    public record CorrelateResult(boolean found, String missingSessionId, List<CorrelationPair> pairs) {
        public static CorrelateResult ok(List<CorrelationPair> pairs) {
            return new CorrelateResult(true, "", pairs);
        }

        public static CorrelateResult notFound(String sessionId) {
            return new CorrelateResult(false, sessionId, List.of());
        }
    }
}
