package edu.cornell.kfs.tax.batch.service.impl;

import org.apache.ojb.broker.metadata.DescriptorRepository;

import edu.cornell.kfs.sys.dataaccess.util.TestOjbMetadataUtils;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

public class TestTaxTableMetadataLookupServiceFactoryBean extends TaxTableMetadataLookupServiceFactoryBean {

    private DescriptorRepository descriptorRepository;

    @Override
    protected TaxTableMetadataLookupService createInstance() throws Exception {
        return TestOjbMetadataUtils.doWithMockMetadataManagerInstance(descriptorRepository, super::createInstance);
    }

    public void setDescriptorRepository(final DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

}
