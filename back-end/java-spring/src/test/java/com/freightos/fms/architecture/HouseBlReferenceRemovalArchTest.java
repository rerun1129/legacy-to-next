package com.freightos.fms.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * HouseBlReference 도메인/JPA 엔티티가 b827e6f 커밋으로 완전 제거됐음을 보장한다.
 * 누군가 이 클래스를 부활시키면 즉시 실패한다.
 */
@AnalyzeClasses(packages = "com.freightos.fms")
class HouseBlReferenceRemovalArchTest {

    @ArchTest
    static final ArchRule noDomainClassNamedHouseBlReference =
            noClasses()
                    .should().haveSimpleName("HouseBlReference");

    @ArchTest
    static final ArchRule noJpaEntityNamedHouseBlReferenceJpaEntity =
            noClasses()
                    .should().haveSimpleName("HouseBlReferenceJpaEntity");

    @ArchTest
    static final ArchRule noClassDependsOnDomainHouseBlReference =
            noClasses()
                    .should().dependOnClassesThat()
                    .haveSimpleName("HouseBlReference");

    @ArchTest
    static final ArchRule noClassDependsOnJpaHouseBlReference =
            noClasses()
                    .should().dependOnClassesThat()
                    .haveSimpleName("HouseBlReferenceJpaEntity");
}
