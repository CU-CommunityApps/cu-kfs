package edu.cornell.kfs.concur.batch.fixture;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.kfs.concur.ConcurKeyConstants;

public class ConcurApplicationPropertiesFixture {

    HashMap<String, String> definedProperties;        

    public ConcurApplicationPropertiesFixture() {
        this.definedProperties = new HashMap<String, String>();
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_NOT_CASH_ADVANCE_DATA_LINE, "Request Extract Detail Line is not a Cash Advance.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_USED_IN_EXPENSE_REPORT, "Cash Advance has been used in expense report.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_REQUEST_ID_INVALID, "Request ID was detected as being NULL or blank.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_EMPLOYEE_ID_NULL_OR_BLANK, "Employee ID was detected as being NULL or blank.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_EMPLOYEE_ID_NOT_FOUND_IN_KFS, "Person for provided Employee ID could not be found in KFS.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_PAYEE_ID_TYPE_INVALID, "Payee ID type was not specified as EMPLOYEE or NON-EMPOYEE.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_REQUEST_AMOUNT_INVALID, "Requested cash advance amount was detected as being NULL or blank.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_DUPLICATE_CASH_ADVANCE_DETECTED, "Duplicate cash advance request detected.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_INVALID_KEYS_FOR_DUPLICATE_CHECK, "Could not check for duplicate cash advance. Requested cash advance was not processed.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HEADER_ROW_COUNT_FAILED, "Header row count validation failed. Header row line count was ({0}) while lines counted in file were ({1}).");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HAS_NO_REQUEST_DETAIL_LINES, "There are no Request Detail lines in the file.");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CONTAINS_BAD_CUSTOMER_PROFILE_GROUP, "File contains Request Detail lines that do not match the employee customer profile group of: {0}");
        this.definedProperties.put(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HEADER_AMOUNT_FILE_AMOUNT_MISMATCH, "Header amount validation failed. Request Extract header line amount was ({0}) while file calculated amount was ({1}).");
    }
    
    public String getPropertyValueAsString(String applicationPropertyKey) {
        return definedProperties.get(applicationPropertyKey);
    }
    
    public Map<String, String> getAllProperties() {
        return definedProperties;
    }

}