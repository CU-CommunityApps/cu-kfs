package edu.cornell.kfs.sys.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Helper extension for auto-creating test file directories prior to a unit test and auto-deleting them afterwards.
 * 
 * The "CreateTestDirectories" annotation handles the setup and configuration of this extension;
 * see that annotation for further info.
 */
public class CreateTestDirectoriesExtension
        implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private CreateTestDirectories testDirectoriesAnnotation;

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        testDirectoriesAnnotation = getAndValidateTestAnnotation(context);

        if (!testDirectoriesAnnotation.createBeforeEachTest()) {
            createDirectories();
        }
    }

    private CreateTestDirectories getAndValidateTestAnnotation(final ExtensionContext context) throws Exception {
        final Class<?> testClass = context.getRequiredTestClass();
        final CreateTestDirectories annotation = testClass.getAnnotation(CreateTestDirectories.class);
        if (annotation == null) {
            throw new IllegalStateException("Test class " + testClass + " does not use the "
                    + CreateTestDirectories.class.getSimpleName() + " annotation");
        }

        final String baseDirectory = annotation.baseDirectory();
        final String[] subDirectories = annotation.subDirectories();
        if (StringUtils.isBlank(annotation.baseDirectory())) {
            throw new IllegalStateException("Test class " + testClass
                    + " does not define a base directory representing the root of the listed sub-directories");
        } else if (subDirectories == null || subDirectories.length == 0) {
            throw new IllegalStateException("Test class " + testClass
                    + " does not define any sub-directories to be created");
        } else if (Arrays.stream(subDirectories).anyMatch(StringUtils::isBlank)) {
            throw new IllegalStateException("Test class " + testClass
                    + " defines one or more blank sub-directories");
        } else if (!StringUtils.endsWith(baseDirectory, CUKFSConstants.SLASH)) {
            throw new IllegalStateException("Test class " + testClass
                    + " defines a base directory that does not end with a slash");
        } else if (Arrays.stream(subDirectories).anyMatch(
                subDirectory -> !StringUtils.endsWith(subDirectory, CUKFSConstants.SLASH))) {
            throw new IllegalStateException("Test class " + testClass
                    + " defines one more sub-directories that do not end with a slash");
        } else if (Arrays.stream(subDirectories).anyMatch(
                subDirectory -> !StringUtils.startsWithIgnoreCase(subDirectory, baseDirectory))) {
            throw new IllegalStateException("Test class " + testClass
                    + " defines one or more sub-directories that do not reside under the given base directory");
        }

        return annotation;
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        if (testDirectoriesAnnotation.createBeforeEachTest()) {
            createDirectories();
        }
    }

    private void createDirectories() throws IOException {
        final File baseDirectory = new File(testDirectoriesAnnotation.baseDirectory());
        FileUtils.forceMkdir(baseDirectory);
        for (final String subDirectoryString : testDirectoriesAnnotation.subDirectories()) {
            final File subDirectory = new File(subDirectoryString);
            FileUtils.forceMkdir(subDirectory);
        }
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        if (testDirectoriesAnnotation.createBeforeEachTest()) {
            deleteDirectories();
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        if (!testDirectoriesAnnotation.createBeforeEachTest()) {
            deleteDirectories();
        }
        testDirectoriesAnnotation = null;
    }

    private void deleteDirectories() throws IOException {
        final File baseDirectory = new File(testDirectoriesAnnotation.baseDirectory());
        if (baseDirectory.exists() && baseDirectory.isDirectory()) {
            FileUtils.forceDelete(baseDirectory.getAbsoluteFile());
        }
    }

}
