package edu.cornell.kfs.sys.util;

import java.util.function.Consumer;

import org.easymock.EasyMock;

public class MockObjectUtils {

    public static <T> T buildMockObject(Class<T> mockObjectClass, Consumer<T> mockObjectConfigurer) {
        T mockObject = EasyMock.createMock(mockObjectClass);
        mockObjectConfigurer.accept(mockObject);
        EasyMock.replay(mockObject);
        return mockObject;
    }

    public static <T> T buildMockObjectWithExceptionProneSetup(
            Class<T> mockObjectClass, ExceptionProneConsumer<T> mockObjectConfigurer) throws Exception {
        T mockObject = EasyMock.createMock(mockObjectClass);
        mockObjectConfigurer.accept(mockObject);
        EasyMock.replay(mockObject);
        return mockObject;
    }

    @FunctionalInterface
    public static interface ExceptionProneConsumer<T> {
        void accept(T value) throws Exception;
    }

}
