package edu.cornell.kfs.sys.dataaccess.xml;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;

/**
 * Convenience factory bean for creating mock DescriptorRepository instances that are sourced from
 * one or more filtered OJB XML files.
 * 
 * The directory for holding the filtered XML files will be auto-generated if needed, but the caller
 * is responsible for cleaning up the directory afterwards.
 */
public class MockFilteredDescriptorRepositoryFactoryBean extends AbstractFactoryBean<DescriptorRepository> {

    private CUMarshalService cuMarshalService;
    private List<String> ojbFilesToFilter;
    private String outputDirectoryForFilteredXmlFiles;
    private Object itemContainingXmlFilterAnnotation;

    @Override
    protected DescriptorRepository createInstance() throws Exception {
        Validate.validState(cuMarshalService != null, "cuMarshalService cannot be null");
        Validate.validState(CollectionUtils.isNotEmpty(ojbFilesToFilter), "ojbFilesToFilter cannot be null or empty");
        Validate.validState(StringUtils.isNotBlank(outputDirectoryForFilteredXmlFiles),
                "outputDirectoryForFilteredXmlFiles cannot be blank");
        Validate.validState(itemContainingXmlFilterAnnotation != null,
                "Neither itemContainingXmlFilterAnnotation nor classContainingXmlFilterAnnotation were specified");

        final File ojbXmlOutputDirectory = new File(outputDirectoryForFilteredXmlFiles);
        FileUtils.forceMkdir(ojbXmlOutputDirectory);

        final List<String> filteredOjbFiles = CuXMLUnitTestUtils.filterXml(
                outputDirectoryForFilteredXmlFiles, itemContainingXmlFilterAnnotation, ojbFilesToFilter);

        return TestOjbMetadataXmlUtils.readAndCombineOjbRepositories(cuMarshalService, filteredOjbFiles);
    }

    @Override
    public Class<?> getObjectType() {
        return DescriptorRepository.class;
    }

    public void setCuMarshalService(final CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public void setOjbFilesToFilter(final List<String> ojbFilesToFilter) {
        this.ojbFilesToFilter = ojbFilesToFilter;
    }

    public void setOutputDirectoryForFilteredXmlFiles(final String outputDirectoryForFilteredXmlFiles) {
        this.outputDirectoryForFilteredXmlFiles = outputDirectoryForFilteredXmlFiles;
    }

    public void setItemContainingXmlFilterAnnotation(final Object itemContainingXmlFilterAnnotation) {
        this.itemContainingXmlFilterAnnotation = itemContainingXmlFilterAnnotation;
    }

    public void setClassContainingXmlFilterAnnotation(final Class<?> classContainingXmlFilterAnnotation) {
        setItemContainingXmlFilterAnnotation(classContainingXmlFilterAnnotation);
    }

}
