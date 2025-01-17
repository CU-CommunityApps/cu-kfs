package edu.cornell.kfs.sys.util;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Convenience class for using the "builder" strategy to create a mocked object (or spied object).
 * The first argument to each "withXXX()" method should be a lambda expression that takes
 * the mocked/spied object and performs the method invocation that you want to mock/stub.
 * (The lambda can still specify argument-matching calls such as Mockito.any(), Mockito.eq(), etc.)
 */
public final class CuMockBuilder<E> {

    private final E mockedObject;

    public CuMockBuilder(final Class<E> objectClass) {
        this.mockedObject = Mockito.mock(objectClass);
    }

    public CuMockBuilder(final E objectToSpy) {
        this.mockedObject = Mockito.spy(objectToSpy);
    }

    public <R, T extends Throwable> CuMockBuilder<E> withReturn(final FailableFunction<E, R, T> methodCall,
            final R returnValue) throws T {
        final E methodCallStubber = Mockito.doReturn(returnValue).when(mockedObject);
        methodCall.apply(methodCallStubber);
        return this;
    }

    public <R, T extends Throwable> CuMockBuilder<E> withAnswer(final FailableConsumer<E, T> methodCall,
            final Answer<R> methodHandler) throws T {
        final E methodCallStubber = Mockito.doAnswer(methodHandler).when(mockedObject);
        methodCall.accept(methodCallStubber);
        return this;
    }

    public <T extends Throwable> CuMockBuilder<E> withNoOp(final FailableConsumer<E, T> methodCall) throws T {
        final E methodCallStubber = Mockito.doNothing().when(mockedObject);
        methodCall.accept(methodCallStubber);
        return this;
    }

    public E build() {
        return mockedObject;
    }

}
