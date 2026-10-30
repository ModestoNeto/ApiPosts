package com.modestoneto.posts.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

// Regras da arquitetura hexagonal. Regra sem nenhuma classe para analisar falha (comportamento padrão do ArchUnit).
@AnalyzeClasses(packages = "com.modestoneto.posts", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule dominioNaoDependeDeFrameworkNemDeOutrasCamadas = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "..application..", "..adapter..");

    // Jackson 3 (tools.jackson) é o padrão do Boot 4; com.fasterxml cobre as anotações que ainda vivem no Jackson 2.
    @ArchTest
    static final ArchRule nucleoNaoUsaJacksonNemSwagger = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "tools.jackson..", "com.fasterxml..", "io.swagger..", "org.springdoc..");

    @ArchTest
    static final ArchRule controllersSoUsamPortasDeEntrada = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAnyPackage("..application.service..", "..application.port.out..");

    @ArchTest
    static final ArchRule adapterInNaoDependeDeAdapterOut = noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..");

    @ArchTest
    static final ArchRule adapterOutNaoDependeDeAdapterIn = noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.in..");

    @ArchTest
    static final ArchRule aplicacaoNaoUsaSpringDataNemJpa = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule nucleoNaoDependeDosAdaptadores = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..");
}
