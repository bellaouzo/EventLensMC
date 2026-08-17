package dev.bellaouzo.eventlens;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class PaperArchitectureTest {

    private static final String JAVA_PLUGIN = "org.bukkit.plugin.java.JavaPlugin";

    private static final JavaClasses CLASSES = new ClassFileImporter().importPackages("dev.bellaouzo.eventlens");

    @Test
    void onlyEventLensExtendsJavaPlugin() {
        var javaPluginSubclasses = CLASSES.stream()
                .filter(javaClass -> javaClass.getAllRawSuperclasses().stream()
                        .anyMatch(superClass -> superClass.getName().equals(JAVA_PLUGIN)))
                .toList();

        assertEquals(1, javaPluginSubclasses.size());
        assertEquals("EventLens", javaPluginSubclasses.getFirst().getSimpleName());
    }
}
