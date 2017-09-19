package edu.cornell.kfs.paymentworks.service;

import java.util.Map;

import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksFieldChangesDTO;

public interface PaymentWorksUtilityService {

    String getGlobalErrorMessage();

    String getAutoPopulatingErrorMessages(Map<String, AutoPopulatingList<ErrorMessage>> errorMap);

    String trimFieldToMax(String field, String fieldName);

    String convertPhoneNumber(String phoneNumber);

    /**
     * Utility method to convert custom fields into a map for ease of access
     * @param customFields
     * @return
     */
    Map<String, String> convertFieldArrayToMap(PaymentWorksCustomFieldsDTO customFields);

    Map<String, String> convertFieldArrayToMap(PaymentWorksFieldChangesDTO fieldChanges);

    Map<String, String> convertFieldArrayToMapFromValues(PaymentWorksFieldChangesDTO fieldChanges);

    /**
     * Converts a pojo object into a json string
     * @param object
     * @return
     */
    String pojoToJsonString(Object object);

    boolean shouldVendorBeSentToPaymentWorks(VendorDetail vendorDetail);

    boolean shouldVendorBeSentToPaymentWorks(PaymentWorksVendor paymentWorksVendor);
}
