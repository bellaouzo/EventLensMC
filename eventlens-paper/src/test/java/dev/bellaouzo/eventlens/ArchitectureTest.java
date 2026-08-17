package dev.bellaouzo.eventlens;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ArchitectureTest {

    private static final String JAVA_PLUGIN = "org.bukkit.plugin.java.JavaPlugin";

    private static final JavaClasses CLASSES = new ClassFileImporter().importPackages("dev.bellaouzo.eventlens");

    @ParameterizedTest
    @ValueSource(strings = {"..domain..", "dev.bellaouzo.eventlens.trace..", "..application.."})
    void packageDoesNotDependOnPaper(String packagePattern) {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage(packagePattern)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.bukkit..", "io.papermc..");
        rule.check(CLASSES);
    }

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
