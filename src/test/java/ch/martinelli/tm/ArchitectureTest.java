package ch.martinelli.tm;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ArchitectureTest {

	// Packages

	public static final String PACKAGE_ROOT = "ch.martinelli.tm";

	public static final String UI_PACKAGE = "..ui..";

	public static final String SECURITY_PACKAGE = "..security..";

	public static final String DOMAIN_PACKAGE = "..domain..";

	public static final String DB_PACKAGE = "ch.martinelli.tm.db..";

	// Layers

	private static final String UI_LAYER = "UI";

	private static final String SECURITY_LAYER = "Security";

	private static final String DOMAIN_LAYER = "Domain";

	private static final String DB_LAYER = "Database";

	// Modules

	private static final String CORE_MODULE = "..core..";

	private static final String[] FEATURE_MODULES = { "ch.martinelli.tm.task..", "ch.martinelli.tm.project..",
			"ch.martinelli.tm.dashboard..", "ch.martinelli.tm.user.." };

	private final JavaClasses classes = new ClassFileImporter().importPackages(PACKAGE_ROOT);

	@Test
	void layered_architecture_check() {
		layeredArchitecture().consideringAllDependencies()

			.layer(UI_LAYER)
			.definedBy(UI_PACKAGE)
			.layer(SECURITY_LAYER)
			.definedBy(SECURITY_PACKAGE)
			.layer(DB_LAYER)
			.definedBy(DB_PACKAGE)
			.layer(DOMAIN_LAYER)
			.definedBy(DOMAIN_PACKAGE)

			// the security configuration references the login view
			.whereLayer(UI_LAYER)
			.mayOnlyBeAccessedByLayers(SECURITY_LAYER)
			// the generated jOOQ code references domain types through forced types
			.whereLayer(DOMAIN_LAYER)
			.mayOnlyBeAccessedByLayers(UI_LAYER, SECURITY_LAYER, DB_LAYER)

			.check(classes);
	}

	@Test
	void module_check_core_may_not_access_feature_modules() {
		noClasses().that()
			.resideInAPackage(CORE_MODULE)
			.should()
			.accessClassesThat()
			.resideInAnyPackage(FEATURE_MODULES)
			.check(classes);
	}

	@Test
	void verify_that_only_the_ui_layer_and_security_config_is_using_vaadin() {
		noClasses().that()
			.resideOutsideOfPackages(UI_PACKAGE, SECURITY_PACKAGE)
			.should()
			.accessClassesThat()
			.resideInAnyPackage("com.vaadin..")
			.check(classes);
	}

}
