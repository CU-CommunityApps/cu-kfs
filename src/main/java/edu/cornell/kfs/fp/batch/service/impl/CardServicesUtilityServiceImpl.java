package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.service.CardServicesUtilityService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CardServicesUtilityServiceImpl implements CardServicesUtilityService {

    public String changeFormatFromYYYYMMDDToSlashedMMDDYYYY(String dateAsYYYYMMDD) {
        String slashedFormattedDateString = dateAsYYYYMMDD.substring(4, 6) 
                + CUKFSConstants.SLASH + dateAsYYYYMMDD.substring(6) 
                + CUKFSConstants.SLASH + dateAsYYYYMMDD.substring(0, 4);
        return slashedFormattedDateString;
    }
    
    public KualiDecimal generateKualiDecimal(String stringToConvert) {
        if (StringUtils.isNotEmpty(stringToConvert)) {
            return new KualiDecimal(stringToConvert);
        } else {
            return KualiDecimal.ZERO;
        }
    }
    
    public void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(
                    dataFileName, CUKFSConstants.DELIMITER) + CUKFSConstants.FileExtensions.DONE);
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

}
