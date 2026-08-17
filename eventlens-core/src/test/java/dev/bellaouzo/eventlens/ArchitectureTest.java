package dev.bellaouzo.eventlens;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ArchitectureTest {

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
}
