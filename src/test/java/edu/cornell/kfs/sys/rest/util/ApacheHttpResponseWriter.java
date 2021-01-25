package edu.cornell.kfs.sys.rest.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicHeader;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

public class ApacheHttpResponseWriter implements ContainerResponseWriter, Closeable {
    private HttpResponse httpResponse;
    private ByteArrayOutputStream outputStream;
    private ScheduledExecutorService scheduledExecutorService;
    private AtomicReference<ScheduledTimeoutHandler> scheduledTimeoutReference;

    public ApacheHttpResponseWriter(HttpResponse httpResponse, ScheduledExecutorService scheduledExecutorService) {
        this.httpResponse = Objects.requireNonNull(httpResponse);
        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService);
        this.scheduledTimeoutReference = new AtomicReference<>();
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext)
            throws ContainerException {
        StatusType statusType = responseContext.getStatusInfo();
        int statusCode = statusType.getStatusCode();
        String reasonPhrase = statusType.getReasonPhrase();
        if (StringUtils.isBlank(reasonPhrase)) {
            reasonPhrase = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, Locale.US);
        }
        
        httpResponse.setStatusCode(statusCode);
        httpResponse.setReasonPhrase(reasonPhrase);
        
        responseContext.getStringHeaders().entrySet()
                .stream()
                .flatMap(this::flatMapMultiValuedHeaderToApacheHeaders)
                .forEach(httpResponse::addHeader);
        
        return outputStream;
    }

    private Stream<Header> flatMapMultiValuedHeaderToApacheHeaders(
            Map.Entry<String, List<String>> multiValuedHeader) {
        String headerName = multiValuedHeader.getKey();
        return multiValuedHeader.getValue().stream()
                .map(headerValue -> new BasicHeader(headerName, headerValue));
    }

    @Override
    public boolean suspend(long timeout, TimeUnit timeUnit, ContainerResponseWriter.TimeoutHandler timeoutHandler) {
        ScheduledTimeoutHandler newHandler = new ScheduledTimeoutHandler(
                scheduledExecutorService, this, timeoutHandler);
        boolean isInitialSuspend = scheduledTimeoutReference.compareAndSet(null, newHandler);
        if (isInitialSuspend) {
            newHandler.rescheduleTimeout(timeout, timeUnit);
        }
        return isInitialSuspend;
    }

    @Override
    public void setSuspendTimeout(long timeout, TimeUnit timeUnit) throws IllegalStateException {
        ScheduledTimeoutHandler currentHandler = scheduledTimeoutReference.get();
        if (currentHandler == null) {
            throw new IllegalStateException("Response has not been suspended yet");
        }
        currentHandler.rescheduleTimeout(timeout, timeUnit);
    }

    @Override
    public void commit() {
        byte[] responseContent = outputStream.toByteArray();
        httpResponse.setEntity(new ByteArrayEntity(responseContent));
    }

    @Override
    public void failure(Throwable error) {
        if (httpResponse.getEntity() == null) {
            httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpResponse.setReasonPhrase(error.getMessage());
        }
    }

    @Override
    public boolean enableResponseBuffering() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close() throws IOException {
        httpResponse = null;
        scheduledExecutorService = null;
        
        if (scheduledTimeoutReference != null) {
            ScheduledTimeoutHandler currentHandler = scheduledTimeoutReference.getAndSet(null);
            scheduledTimeoutReference = null;
            if (currentHandler != null) {
                IOUtils.closeQuietly(currentHandler);
            }
        }
        
        IOUtils.closeQuietly(outputStream);
        outputStream = null;
    }

    private static class ScheduledTimeoutHandler implements Closeable {
        private ScheduledExecutorService scheduledExecutorService;
        private ContainerResponseWriter responseWriter;
        private ContainerResponseWriter.TimeoutHandler timeoutHandler;
        private AtomicReference<ScheduledFuture<?>> scheduledEventReference;

        public ScheduledTimeoutHandler(ScheduledExecutorService scheduledExecutorService,
                ContainerResponseWriter responseWriter, ContainerResponseWriter.TimeoutHandler timeoutHandler) {
            this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService);
            this.responseWriter = Objects.requireNonNull(responseWriter);
            this.timeoutHandler = Objects.requireNonNull(timeoutHandler);
            this.scheduledEventReference = new AtomicReference<>();
        }

        public void rescheduleTimeout(long timeout, TimeUnit timeUnit) {
            boolean previousTimeoutWasAbsentOrCanceled = cancelAndReplaceCurrentTimeout(null);
            if (previousTimeoutWasAbsentOrCanceled) {
                long actualTimeout = convertToActualTimeoutValue(timeout);
                TimeUnit actualTimeUnit = convertTimeUnitIfNecessary(actualTimeout, timeUnit);
                ScheduledFuture<?> newEvent = scheduledExecutorService.schedule(
                        this::runTimeoutHandler, actualTimeout, actualTimeUnit);
                cancelAndReplaceCurrentTimeout(newEvent);
            }
        }

        private boolean cancelAndReplaceCurrentTimeout(ScheduledFuture<?> newEvent) {
            ScheduledFuture<?> currentEvent = scheduledEventReference.getAndSet(newEvent);
            if (currentEvent != null) {
                return currentEvent.cancel(true);
            }
            return true;
        }

        private void runTimeoutHandler() {
            try {
                scheduledEventReference.set(null);
                timeoutHandler.onTimeout(responseWriter);
            } catch (Exception e) {
                
            }
        }

        private long convertToActualTimeoutValue(long timeout) {
            return timeout <= 0L ? Long.MAX_VALUE : timeout;
        }

        private TimeUnit convertTimeUnitIfNecessary(long convertedTimeout, TimeUnit timeUnit) {
            return convertedTimeout == Long.MAX_VALUE ? TimeUnit.SECONDS : timeUnit;
        }

        @Override
        public void close() throws IOException {
            if (scheduledEventReference != null) {
                scheduledEventReference.set(null);
                scheduledEventReference = null;
            }
            scheduledExecutorService = null;
            responseWriter = null;
            timeoutHandler = null;
        }
    }

}
