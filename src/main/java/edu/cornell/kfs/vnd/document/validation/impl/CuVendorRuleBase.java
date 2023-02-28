package edu.cornell.kfs.vnd.document.validation.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorKeyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.validation.impl.VendorRule;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.CuPredicateFactory;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.vnd.CUVendorKeyConstants;

/**
 * Custom rule subclass of VendorRule that contains overrides
 * of VendorRule methods with default (package-private) visibility.
 * That is why this particular subclass is in the same package
 * as the VendorRule class.
 */
public abstract class CuVendorRuleBase extends VendorRule {

    /**
     * Overridden so that when an existing vendor tax number is found when trying to add a new parent vendor,
     * the error message will include the existing vendor detail's ID (in payee ID format).
     * 
     * @see org.kuali.kfs.vnd.document.validation.impl.VendorRule#validateParentVendorTaxNumber(org.kuali.kfs.vnd.businessobject.VendorDetail)
     */
    @SuppressWarnings("deprecation")
    @Override
    protected boolean validateParentVendorTaxNumber(VendorDetail vendorDetail) {
        boolean isParent = vendorDetail.isVendorParentIndicator();

        // ==== CU Customization: Use criteria API instead, due to limited BO service methods with negative criteria. ====
        List<Predicate> criteria = new ArrayList<Predicate>();
        if (ObjectUtils.isNotNull(vendorDetail.getVendorHeader().getVendorTaxTypeCode()) && ObjectUtils.isNotNull(vendorDetail.getVendorHeader().getVendorTaxNumber())) {
        	criteria.add(PredicateFactory.equal(
        	        VendorPropertyConstants.VENDOR_TAX_TYPE_CODE, vendorDetail.getVendorHeader().getVendorTaxTypeCode()));
        	criteria.add(PredicateFactory.equal(
        	        VendorPropertyConstants.VENDOR_TAX_NUMBER, vendorDetail.getVendorHeader().getVendorTaxNumber()));
        	criteria.add(PredicateFactory.equal(KFSPropertyConstants.ACTIVE_INDICATOR, true));
        } else {
        	return true;
        }

        // ==== CU Customization: Use the actual vendor details, not just the count. ====
        List<VendorDetail> existingVendors;

        // If this is editing an existing vendor, we have to include the current vendor's header generated id in the
        // negative criteria so that the current vendor is excluded from the search
        if (ObjectUtils.isNotNull(vendorDetail.getVendorHeaderGeneratedIdentifier())) {
            // ==== CU Customization: Use CriteriaLookupService instead, since BO service doesn't allow negative criteria in non-count methods. ====
            criteria.add(CuPredicateFactory.notEqual(VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID,
                    vendorDetail.getVendorHeaderGeneratedIdentifier().toString()));
            existingVendors = SpringContext.getBean(CriteriaLookupService.class).lookup(
                    VendorDetail.class, QueryByCriteria.Builder.fromPredicates(criteria.toArray(new Predicate[criteria.size()]))).getResults();
        } else {
            // If this is creating a new vendor, we can't include the header generated id
            // in the negative criteria because it's null, so we'll only look for existing
            // vendors with the same tax # and tax type regardless of the vendor header generated id.
            // ==== CU Customization: Use CriteriaLookupService instead, since BO service doesn't allow negative criteria in non-count methods. ====
            existingVendors = SpringContext.getBean(CriteriaLookupService.class).lookup(
                    VendorDetail.class, QueryByCriteria.Builder.fromPredicates(criteria.toArray(new Predicate[criteria.size()]))).getResults();
        }

        if (!existingVendors.isEmpty()) {
            if (isParent) {
                // ==== CU Customization: Print a different error message that also includes the existing vendor's ID. ====
                if (KFSConstants.SYSTEM_USER.equals(GlobalVariables.getUserSession().getActualPerson().getPrincipalName())) {
                    // Only print new message when the actual KFS system user is in place (and not someone backdoored), to keep it specific to batch runs.
                    VendorDetail existingVendor = existingVendors.get(0);
                    // Parent vendor takes precedence when printing the error message.
                    for (int i = 1; !existingVendor.isVendorParentIndicator() && i < existingVendors.size(); i++) {
                        existingVendor = existingVendors.get(i);
                    }
                    putFieldError(VendorPropertyConstants.VENDOR_TAX_NUMBER,
                            CUVendorKeyConstants.ERROR_VENDOR_TAX_TYPE_AND_NUMBER_COMBO_EXISTS_AND_PRINT_EXISTING, new String[] {
                                existingVendor.getVendorHeaderGeneratedIdentifier().toString(),
                                existingVendor.getVendorDetailAssignedIdentifier().toString()
                            });
                } else {
                    // Just use standard KFS-delivered message for general users who are trying to submit a regular vendor maintenance document.
                    putFieldError(VendorPropertyConstants.VENDOR_TAX_NUMBER,
                            VendorKeyConstants.ERROR_VENDOR_TAX_TYPE_AND_NUMBER_COMBO_EXISTS);
                }
            } else {
                putFieldError(VendorPropertyConstants.VENDOR_TAX_NUMBER,
                        VendorKeyConstants.ERROR_VENDOR_PARENT_NEEDS_CHANGED);
            }
            return false;
        }

        return true;
    }
}
