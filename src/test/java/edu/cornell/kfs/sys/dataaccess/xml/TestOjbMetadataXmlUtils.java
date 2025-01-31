package edu.cornell.kfs.sys.dataaccess.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.ojb.broker.metadata.DescriptorRepository;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

public final class TestOjbMetadataXmlUtils {

    public static DescriptorRepository readAndCombineOjbRepositories(final CUMarshalService cuMarshalService,
            final Collection<String> sourceFiles) {
        return sourceFiles.stream()
                .map(sourceFile -> readOjbRepositoryDto(cuMarshalService, sourceFile))
                .collect(Collectors.collectingAndThen(
                        Collectors.toUnmodifiableList(),
                        TestDescriptorRepositoryDto::createCombinedOjbDescriptorRepository));
    }

    public static DescriptorRepository readOjbRepository(final CUMarshalService cuMarshalService,
            final String sourceFile) {
        final TestDescriptorRepositoryDto repositoryDto = readOjbRepositoryDto(cuMarshalService, sourceFile);
        return repositoryDto.toOjbDescriptorRepository();
    }

    public static TestDescriptorRepositoryDto readOjbRepositoryDto(final CUMarshalService cuMarshalService,
            final String sourceFile) {
        try (final InputStream inputStream = CuXMLUnitTestUtils.getXmlInputStream(sourceFile)) {
            return cuMarshalService.unmarshalStream(inputStream, TestDescriptorRepositoryDto.class);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
