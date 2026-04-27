package com.freightos.fms.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchRule;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.freightos.fms")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_adapters =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..application..",
                "org.springframework..", "jakarta.persistence..", "org.hibernate..");

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapter_impl =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule jpa_entities_only_in_persistence_adapter =
        classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("..adapter.out.persistence..");
}
