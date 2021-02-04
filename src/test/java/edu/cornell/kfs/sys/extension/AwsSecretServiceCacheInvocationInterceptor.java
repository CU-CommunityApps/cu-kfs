package edu.cornell.kfs.sys.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import edu.cornell.kfs.sys.service.AwsSecretService;

public class AwsSecretServiceCacheInvocationInterceptor implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        invokeWithAwsSecretsCachingEnabledIfNecessary(invocation, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(
            Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        invokeWithAwsSecretsCachingEnabledIfNecessary(invocation, extensionContext);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        invokeWithAwsSecretsCachingEnabledIfNecessary(invocation, extensionContext);
    }

    private void invokeWithAwsSecretsCachingEnabledIfNecessary(
            Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        if (shouldSkipAwsSecretsCacheSetup(extensionContext)) {
            invocation.proceed();
        } else {
            AwsSecretService awsSecretService = getAwsSecretServiceFromFieldOnTestClass(extensionContext);
            awsSecretService.doWithAwsSecretsCachingEnabled(() -> {
                invocation.proceed();
                return null;
            });
        }
    }

    private boolean shouldSkipAwsSecretsCacheSetup(ExtensionContext extensionContext) throws Throwable {
        return extensionContext.getTestMethod()
                .filter(this::testMethodIsAnnotatedToExcludeSecretsCacheSetup)
                .isPresent();
    }

    private boolean testMethodIsAnnotatedToExcludeSecretsCacheSetup(Method testMethod) {
        return testMethod.getAnnotation(ExcludeAwsSecretsCacheSetup.class) != null;
    }

    private AwsSecretService getAwsSecretServiceFromFieldOnTestClass(
            ExtensionContext extensionContext) throws Throwable {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        Object testInstance = extensionContext.getRequiredTestInstance();
        
        AwsSecretServiceCacheExtension awsCacheAnnotation = testClass.getAnnotation(
                AwsSecretServiceCacheExtension.class);
        if (awsCacheAnnotation == null) {
            throw new IllegalStateException("AwsSecretServiceCacheExtension annotation was not present on test class");
        }
        
        String awsSecretServiceFieldName = awsCacheAnnotation.awsSecretServiceField();
        if (StringUtils.isBlank(awsSecretServiceFieldName)) {
            throw new IllegalStateException(
                    "AwsSecretServiceCacheExtension annotation cannot specify a blank AwsSecretService field");
        }
        
        Field awsSecretServiceField = testClass.getDeclaredField(awsSecretServiceFieldName);
        awsSecretServiceField.setAccessible(true);
        AwsSecretService awsSecretService = (AwsSecretService) awsSecretServiceField.get(testInstance);
        if (awsSecretService == null) {
            throw new IllegalStateException("The test instance did not initialize its AwsSecretService field "
                    + "prior to the invocation of the test case");
        }
        return awsSecretService;
    }

}
