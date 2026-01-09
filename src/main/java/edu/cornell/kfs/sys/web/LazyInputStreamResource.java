package edu.cornell.kfs.sys.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableSupplier;
import org.springframework.core.io.AbstractResource;

/**
 * Custom Spring Resource implementation that lazily creates the InputStream on demand,
 * rather than using a pre-initialized InputStream like in Spring's InputStreamResource class.
 * This provides better flexibility by allowing the InputStream to be created multiple times,
 * and for letting Spring automatically handle the InputStream's lifecycle.
 * 
 * In addition, the content length can optionally be pre-defined for better efficiency.
 * 
 * This is similar to one of the workarounds mentioned in the following Spring Framework issue:
 * 
 * https://github.com/spring-projects/spring-framework/issues/32802
 */
public class LazyInputStreamResource extends AbstractResource {

    private final FailableSupplier<InputStream, IOException> inputStreamSupplier;
    private final FailableSupplier<Long, IOException> contentLengthSupplier;
    private final String description;

    public LazyInputStreamResource(final FailableSupplier<InputStream, IOException> inputStreamSupplier,
            final Optional<Long> knownContentLength, final String description) {
        Objects.requireNonNull(inputStreamSupplier, "inputStreamSupplier cannot be null");
        Objects.requireNonNull(knownContentLength, "knownContentLength wrapper object cannot be null");
        Validate.notBlank(description, "description cannot be blank");
        this.inputStreamSupplier = inputStreamSupplier;
        this.contentLengthSupplier = knownContentLength.isPresent() ? knownContentLength::get : super::contentLength;
        this.description = description;
    }

    @Override
    public long contentLength() throws IOException {
        return contentLengthSupplier.get();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStreamSupplier.get();
    }

}
