package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.apache.ojb.broker.query.Criteria;
import org.kuali.kfs.krad.dao.impl.LookupDaoOjb;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public class PaymentWorksVendorLookupDaoOjb extends LookupDaoOjb {

    private static final String TO_CHAR_FUNCTION_FORMAT = "TO_CHAR({0})";

    private DescriptorRepository descriptorRepository;

    /*
     * To allow for wildcard searching on the Vendor Number portions, the relevant Vendor header/detail references
     * will be wrapped in a TO_CHAR() function and forcibly converted to database column names. The latter change
     * is needed to prevent OJB from treating the wildcard values as Vendor header/detail integers (since OJB
     * doesn't adjust POJO property datatype expectations for such TO_CHAR() conversions).
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void addCriteria(final String propertyName, final String propertyValue, final Class propertyType,
            final boolean caseInsensitive, final boolean treatWildcardsAndOperatorsAsLiteral,
            final Criteria criteria) {
        if (isVendorNumberProperty(propertyName) && !StringUtils.isNumeric(propertyValue)) {
            final ClassDescriptor pmwVendorDescriptor = getDescriptorRepository()
                    .getDescriptorFor(PaymentWorksVendor.class);
            final FieldDescriptor fieldDescriptor = pmwVendorDescriptor.getFieldDescriptorByName(propertyName);
            if (ObjectUtils.isNull(fieldDescriptor)) {
                throw new IllegalStateException("Could not find PaymentWorksVendor field '" + propertyName
                        + "'; this should NEVER happen!");
            }
            final String columnName = fieldDescriptor.getColumnName();
            final String wrappedName = MessageFormat.format(TO_CHAR_FUNCTION_FORMAT, columnName);
            super.addCriteria(wrappedName, propertyValue, String.class, caseInsensitive,
                    treatWildcardsAndOperatorsAsLiteral, criteria);
        } else {
            super.addCriteria(propertyName, propertyValue, propertyType, caseInsensitive,
                    treatWildcardsAndOperatorsAsLiteral, criteria);
        }
    }

    private boolean isVendorNumberProperty(final String propertyName) {
        return StringUtils.equalsAny(propertyName,
                PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_VENDOR_HEADER_GENERATED_IDENTIFIER,
                PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_VENDOR_DETAIL_ASSIGNED_IDENTIFIER);
    }

    private DescriptorRepository getDescriptorRepository() {
        if (descriptorRepository == null) {
            final MetadataManager metadataManager = MetadataManager.getInstance();
            descriptorRepository = metadataManager.getGlobalRepository();
        }
        return descriptorRepository;
    }

    public void setDescriptorRepository(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

}
