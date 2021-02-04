package edu.cornell.kfs.sys.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(AwsSecretServiceCacheInvocationInterceptor.class)
public @interface AwsSecretServiceCacheExtension {
    String awsSecretServiceField();
}
