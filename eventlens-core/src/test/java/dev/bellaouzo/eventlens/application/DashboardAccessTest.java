package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DashboardAccessTest {

    @Test
    void prefersBearerThenQueryThenCookie() {
        assertEquals(
                "from-header",
                DashboardAccess.presentedToken(
                                "Bearer from-header", "eventlens_dashboard=from-cookie", "token=from-query")
                        .orElseThrow());
        assertEquals(
                "from-query",
                DashboardAccess.presentedToken(null, "eventlens_dashboard=from-cookie", "token=from-query")
                        .orElseThrow());
        assertEquals(
                "from-cookie",
                DashboardAccess.presentedToken(null, "other=1; eventlens_dashboard=from-cookie", null)
                        .orElseThrow());
        assertTrue(DashboardAccess.presentedToken(null, null, null).isEmpty());
    }

    @Test
    void decodesQueryToken() {
        assertEquals(
                "a b", DashboardAccess.presentedToken(null, null, "token=a+b").orElseThrow());
        assertTrue(DashboardAccess.sessionCookie("secret token").contains("eventlens_dashboard="));
    }
}
