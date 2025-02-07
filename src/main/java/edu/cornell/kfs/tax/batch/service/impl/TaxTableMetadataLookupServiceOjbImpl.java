package edu.cornell.kfs.tax.batch.service.impl;

import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.service.impl.PersistenceServiceStructureImplBase;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

/*
 * TODO: Remove this class if we intend to go with the "DefaultImpl" implementation instead.
 */
public class TaxTableMetadataLookupServiceOjbImpl
        extends TaxTableMetadataLookupServiceBase<ClassDescriptor> {

    private final TaxPersistenceStructureServiceImpl taxPersistenceStructureService;

    public TaxTableMetadataLookupServiceOjbImpl() {
        this.taxPersistenceStructureService = new TaxPersistenceStructureServiceImpl();
    }

    @Override
    protected ClassDescriptor getMetadataForBusinessObject(final Class<? extends BusinessObject> businessObjectClass) {
        return taxPersistenceStructureService.getClassDescriptor(businessObjectClass);
    }

    @Override
    protected String getTableName(final ClassDescriptor classDescriptor) {
        return classDescriptor.getFullTableName();
    }

    @Override
    protected String getColumnLabel(final TaxDtoFieldEnum fieldMapping, final ClassDescriptor classDescriptor) {
        final FieldDescriptor fieldDescriptor = classDescriptor.getFieldDescriptorByName(fieldMapping.getFieldName());
        return fieldDescriptor.getColumnName();
    }



    /**
     * Declaring this as a nested class because the top-level service is already extending another class,
     * and because we don't want to expose the inherited getClassDescriptor() method globally.
     */
    private static final class TaxPersistenceStructureServiceImpl extends PersistenceServiceStructureImplBase {
        @SuppressWarnings("rawtypes")
        @Override
        protected ClassDescriptor getClassDescriptor(final Class persistableClass) {
            return super.getClassDescriptor(persistableClass);
        }
    }

}
