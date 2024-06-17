package edu.cornell.kfs.sys.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Helper annotation for auto-creating test file directories prior to a unit test and auto-deleting them afterwards.
 * Must specify the shared base path for all the test sub-directories to be created, where the post-test logic
 * will delete all files and directories relative to the base path.
 * 
 * NOTE: All the specified directories are expected to end with a slash!
 * 
 * By default, the directories will be created and deleted at the test's before-each and after-each level.
 * To force the directories to be created and deleted at the before-all and after-all level instead,
 * set the "createBeforeEachTest" flag to false.
 * 
 * The CreateTestDirectoriesExtension class handles the actual creation and deletion of the test directories.
 * This annotation already includes the needed "ExtendWith" annotation for specifying that extension,
 * so there's no need to reference the extension directly on the unit test class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(CreateTestDirectoriesExtension.class)
public @interface CreateTestDirectories {

    String baseDirectory();

    String[] subDirectories();

    boolean createBeforeEachTest() default true;

}
