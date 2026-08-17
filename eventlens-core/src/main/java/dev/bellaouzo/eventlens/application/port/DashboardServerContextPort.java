package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.dashboard.DashboardServerContext;

public interface DashboardServerContextPort {

    DashboardServerContext capture();
}
