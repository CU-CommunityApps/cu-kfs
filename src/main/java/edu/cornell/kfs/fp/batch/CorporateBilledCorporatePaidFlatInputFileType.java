package edu.cornell.kfs.fp.batch;

import org.apache.commons.lang.StringUtils;

public class CorporateBilledCorporatePaidFlatInputFileType extends ProcurementCardFlatInputFileType {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidFlatInputFileType.class);
    
    @Override
    public String getFileTypeIdentifer() {
        return "corpoateBilledCorporatePaidFlatInputFileType";
    }
    
    @Override
    public String getFileName(String principalName, Object parsedFileContents, String userIdentifier) {
        String fileName = "cbcp_" + principalName;
        if (StringUtils.isNotBlank(userIdentifier)) {
            fileName += "_" + userIdentifier;
        }
        fileName += "_" + getDateTimeService().toDateTimeStringForFilename(getDateTimeService().getCurrentDate());
        fileName = StringUtils.remove(fileName, " ");
        return fileName;
    }

}
