package edu.cornell.kfs.module.bc.util;

import org.kuali.rice.krad.util.ObjectUtils;

/**
 * This class has utility methods to be used in the BC exports.
 */
public class ExportUtil {

    /**
     * Gets as input a String value and a boolean parameter: isNumeric and returns the
     * empty string if the input value is the word "null" and the isNumeric parameter is false
     * or returns "0" if the input value is the word "null" and the isNumeric parameter is true.
     * If the input value is not the word "null" then it returns the inputValue as is without
     * any modification.
     * 
     * @param inputValue the input value
     * @param isNumeric true if the inputValue is an numeric value, false otherwise
     * 
     * @return returns the empty string if the input value is the word "null" and the isNumeric 
     * parameter is false.  Returns "0" if the input value is the word "null" and the isNumeric 
     * parameter is true.  Returns the inputValue if the inputValue is not the word "null".
     */
    public String removeNulls(String inputValue, boolean isNumeric) {

        String returnValue = inputValue;
        
        if (ObjectUtils.isNull(inputValue) && !isNumeric)
            returnValue = "";
        else if (ObjectUtils.isNull(inputValue) && isNumeric)
	            returnValue = "0";
        
        return returnValue;
    }
}
