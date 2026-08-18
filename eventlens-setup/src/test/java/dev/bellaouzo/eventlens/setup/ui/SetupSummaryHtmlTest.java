package dev.bellaouzo.eventlens.setup.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SetupSummaryHtmlTest {

    @Test
    void colorsCopiedUpdatedAndNextSteps() {
        assertEquals("#18753C", SetupSummaryHtml.color("Copied EventLens-1.12.0.jar to plugins"));
        assertEquals("#1A5FA8", SetupSummaryHtml.color("Updated C:/server/user_jvm_args.txt"));
        assertEquals(
                "#8A5A00", SetupSummaryHtml.color("Skipped Java agent. /eventlens status will say dispatch-only."));
        assertEquals("#3D4654", SetupSummaryHtml.color("Next: fully restart the server or quit the launcher."));
    }

    @Test
    void renderEscapesHtmlAndKeepsMarkers() {
        String html = SetupSummaryHtml.render(List.of("Copied a > b", "Next: restart"));
        assertTrue(html.contains("Copied a &gt; b"));
        assertTrue(html.contains("✓"));
        assertTrue(html.contains("→"));
        assertTrue(html.contains("#18753C"));
    }
}
