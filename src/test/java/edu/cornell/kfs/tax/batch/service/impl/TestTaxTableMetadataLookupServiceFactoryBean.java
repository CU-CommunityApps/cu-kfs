package edu.cornell.kfs.tax.batch.service.impl;

import org.apache.commons.lang3.Validate;
import org.apache.ojb.broker.metadata.DescriptorRepository;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

/**
 * Test-only subclass of TaxTableMetadataLookupServiceFactoryBean that can handle instantiating
 * the OJB service implementation in a unit-test-friendly manner. Requires specifying an explicit mock
 * descriptor repository for the service to use when it accesses a mocked OJB MetadataManager.
 */
public class TestTaxTableMetadataLookupServiceFactoryBean extends TaxTableMetadataLookupServiceFactoryBean {

    private DescriptorRepository descriptorRepository;

    @Override
    protected TaxTableMetadataLookupService createInstance() throws Exception {
        Validate.validState(descriptorRepository != null, "descriptorRepository was not initialized");
        return TestOjbMetadataUtils.doWithMockMetadataManagerInstance(descriptorRepository, super::createInstance);
    }

    public void setDescriptorRepository(final DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

}

