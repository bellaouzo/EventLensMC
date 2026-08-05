package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.report.TraceReportEnvironment;
import java.util.Set;

public interface EnvironmentPort {

    TraceReportEnvironment capture(Set<String> relevantPluginNames, long generatedAtMillis);
}
