package dev.bellaouzo.eventlens.setup.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FolderPickerTest {

    @Test
    void parseOutputUsesLastNonEmptyLine() {
        Optional<Path> path = FolderPicker.parseOutput("Add-Type noise\nC:\\server\\plugins\n");
        assertEquals(Path.of("C:\\server\\plugins"), path.orElseThrow());
    }

    @Test
    void parseOutputIgnoresBlank() {
        assertTrue(FolderPicker.parseOutput("   \n").isEmpty());
        assertTrue(FolderPicker.parseOutput(null).isEmpty());
    }
}
