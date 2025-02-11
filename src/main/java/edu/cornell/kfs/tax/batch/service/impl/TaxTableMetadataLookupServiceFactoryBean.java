package edu.cornell.kfs.tax.batch.service.impl;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;

/**
 * The current OJB implementation of TaxTableMetadataLookupService relies upon a base financials class
 * that makes a call to a static OJB method within its constructor. To make it easier to mock/override
 * the service's OJB setup for unit/integration tests, this factory bean was introduced so that subclasses
 * can inject Mockito's static mocking functionality into the instantiation process.
 * 
 * The default implementation creates the service bean by calling the default constructor on the given
 * service class.
 * 
 * TODO: If we're not implementing the OJB variant of the lookup service at this time, then remove this class.
 */
public class TaxTableMetadataLookupServiceFactoryBean
        extends AbstractFactoryBean<TaxTableMetadataLookupService> {

    private Class<? extends TaxTableMetadataLookupService> serviceClass;

    @Override
    protected TaxTableMetadataLookupService createInstance() throws Exception {
        Validate.validState(serviceClass != null, "serviceClass was not initialized");
        return serviceClass.getConstructor().newInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return TaxTableMetadataLookupService.class;
    }

    public void setServiceClass(final Class<? extends TaxTableMetadataLookupService> serviceClass) {
        this.serviceClass = serviceClass;
    }

}
