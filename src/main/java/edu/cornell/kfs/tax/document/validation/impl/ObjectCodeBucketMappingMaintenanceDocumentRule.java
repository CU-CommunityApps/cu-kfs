package edu.cornell.kfs.tax.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxPropertyConstants;

public class ObjectCodeBucketMappingMaintenanceDocumentRule extends MaintenanceDocumentRuleBase {

    /**
     * Overridden so that when checking if a new bucket mapping has the same primary keys as an existing one,
     * searches using the "*" (Any/None) payment reason will treat the asterisk as a literal instead of a wildcard.
     */
    @Override
    protected boolean primaryKeyCheck(MaintenanceDocument document) {
        if (!document.isNew()) {
            return super.primaryKeyCheck(document);
        }
        
        Class<?> boClass = document.getNewMaintainableObject().getDataObjectClass();
        Object newBo = document.getNewMaintainableObject().getDataObject();
        Map<String, ?> primaryKeyValues = getDataObjectMetaDataService().getPrimaryKeyFieldValues(newBo);
        Map<String, ?> convertedValues = convertFieldValuesForExactPaymentReasonMatch(primaryKeyValues);
        
        Collection<?> existingObjects = boService.findMatching(
                boClass.asSubclass(PersistableBusinessObject.class), convertedValues);
        if (!existingObjects.isEmpty()) {
            putDocumentError(KRADConstants.DOCUMENT_ERRORS,
                    KFSKeyConstants.ERROR_DOCUMENT_MAINTENANCE_KEYS_ALREADY_EXIST_ON_CREATE_NEW,
                    getHumanReadablePrimaryKeyFieldNames(boClass));
            return false;
        } else {
            return true;
        }
    }

    private Map<String, ?> convertFieldValuesForExactPaymentReasonMatch(Map<String, ?> fieldValues) {
        String paymentReasonCode = (String) fieldValues.get(CUTaxPropertyConstants.DV_PAYMENT_REASON_CODE);
        if (StringUtils.equalsIgnoreCase(CUTaxConstants.ANY_OR_NONE_PAYMENT_REASON, paymentReasonCode)) {
            Map<String, Object> convertedValues = new HashMap<>(fieldValues);
            convertedValues.put(CUTaxPropertyConstants.DV_PAYMENT_REASON_CODE, List.of(paymentReasonCode));
            return convertedValues;
        } else {
            return fieldValues;
        }
    }

}
