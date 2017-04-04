package edu.cornell.kfs.concur.batch.businessobject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * BO serialization field util subclass for serializing OriginEntryFull BOs
 * into Collector flat-file origin entry lines.
 * 
 * For sub-account numbers, sub-object codes or project codes that contain
 * the dash-only values (indicating that they don't have a value defined),
 * they will be forcibly converted to empty Strings for cleanliness purposes.
 * KFS's existing code will convert them back to dash-only values as needed
 * once parsed.
 */
public class OriginEntrySerializerFieldUtil extends CollectorSerializerFieldUtilsWithDate {

    protected static final String SEQUENCE_NUMBER_LEFT_PAD_DIGIT = "0";

    protected int sequenceNumberLength;

    @Override
    protected Map<String,Function<Object,String>> getCustomSerializerFunctions() {
        Map<String,Function<Object,String>> customFunctions = new HashMap<>();
        
        customFunctions.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, this::makeEmptyIfDashOnlyValue);
        customFunctions.put(KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, this::makeEmptyIfDashOnlyValue);
        customFunctions.put(KFSPropertyConstants.PROJECT_CODE, this::makeEmptyIfDashOnlyValue);
        customFunctions.put(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER, this::convertTransactionEntrySequenceNumber);
        customFunctions.put(KFSPropertyConstants.TRANSACTION_DATE, this::formatSqlDateForCollectorFile);
        customFunctions.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_REVERSAL_DATE, this::formatSqlDateForCollectorFile);
        
        return customFunctions;
    }

    protected String makeEmptyIfDashOnlyValue(Object propertyValue) {
        String stringValue = defaultConversionToString(propertyValue);
        if (StringUtils.containsOnly(stringValue, KFSConstants.DASH)) {
            stringValue = StringUtils.EMPTY;
        }
        return stringValue;
    }

    protected String convertTransactionEntrySequenceNumber(Object sequenceNumber) {
        if (sequenceNumber == null) {
            return StringUtils.EMPTY;
        }
        return StringUtils.leftPad(sequenceNumber.toString(), getSequenceNumberLength(), SEQUENCE_NUMBER_LEFT_PAD_DIGIT);
    }

    protected int getSequenceNumberLength() {
        if (sequenceNumberLength == 0) {
            Map<String,Integer> fieldLengthMap = parserFieldUtils.getFieldLengthMap();
            sequenceNumberLength = fieldLengthMap.get(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER).intValue();
        }
        return sequenceNumberLength;
    }

}
