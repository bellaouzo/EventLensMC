package dev.bellaouzo.eventlens.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChatPaginationTest {

    @Test
    void buildNavigationReturnsEmptyForSinglePage() {
        assertTrue(ChatPagination.buildNavigation(1, 1, "/prev", "/next").isEmpty());
    }

    @Test
    void buildNavigationPresentWhenMultiplePagesExist() {
        assertTrue(ChatPagination.buildNavigation(1, 3, null, "/next").isPresent());
        assertTrue(ChatPagination.buildNavigation(2, 3, "/prev", "/next").isPresent());
        assertTrue(ChatPagination.buildNavigation(3, 3, "/prev", null).isPresent());
    }

    @Test
    void buildNavigationOmitsPreviousWhenCommandMissing() {
        assertFalse(ChatPagination.buildNavigation(2, 3, null, "/next").isEmpty());
    }

    @Test
    void buildNavigationOmitsNextWhenCommandMissing() {
        assertFalse(ChatPagination.buildNavigation(2, 3, "/prev", null).isEmpty());
    }
}
