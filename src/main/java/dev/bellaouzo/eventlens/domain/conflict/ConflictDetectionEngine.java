package dev.bellaouzo.eventlens.domain.conflict;

import dev.bellaouzo.eventlens.domain.diff.BandChange;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiffEngine;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.List;

public final class ConflictDetectionEngine {

    private ConflictDetectionEngine() {}

    public static List<DispatchConflict> detect(TraceDispatchRecord dispatch, long slowThresholdNanos) {
        List<DispatchConflict> conflicts = new ArrayList<>();
        List<BandChange> bandChanges =
                SnapshotDiffEngine.computeBandChanges(dispatch.priorityCheckpoints(), dispatch.listenerChain(), false);

        conflicts.addAll(SnapshotConflictRules.detectCancellationFight(dispatch.sequence(), bandChanges));
        conflicts.addAll(SnapshotConflictRules.detectMultiPluginChanges(dispatch.sequence(), bandChanges));
        conflicts.addAll(
                SnapshotConflictRules.detectPropertyReverts(dispatch.sequence(), dispatch.priorityCheckpoints()));
        conflicts.addAll(ListenerTimingConflictRules.detectPostCancelListeners(dispatch, bandChanges));
        conflicts.addAll(SnapshotConflictRules.detectMonitorMutations(dispatch.sequence(), bandChanges));
        conflicts.addAll(ConflictDetectionSupport.tagSequence(
                ListenerChainAnalyzer.detectChainIssues(dispatch.listenerChain()), dispatch.sequence()));
        conflicts.addAll(ListenerTimingConflictRules.detectListenerExceptions(dispatch));
        conflicts.addAll(ListenerTimingConflictRules.detectSlowChain(dispatch, slowThresholdNanos));

        return ConflictDetectionSupport.dedupe(conflicts);
    }
}
