package edu.cornell.kfs.fp.batch;

import org.apache.commons.lang.StringUtils;

import com.mchange.v1.lang.GentleThread;

public class CbcpFlatInputFileType extends ProcurementCardFlatInputFileType {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CbcpFlatInputFileType.class);
    
    @Override
    public String getFileTypeIdentifer() {
        return "cbcpFlatInputFileType";
    }
    
    @Override
    public String getFileName(String principalName, Object parsedFileContents, String userIdentifier) {
        String fileName = "cbcp_" + principalName;
        if (StringUtils.isNotBlank(userIdentifier)) {
            fileName += "_" + userIdentifier;
        }
        
        fileName += "_" + getDateTimeService().toDateTimeStringForFilename(getDateTimeService().getCurrentDate());

        // remove spaces in filename
        fileName = StringUtils.remove(fileName, " ");

        return fileName;
    }
    
    @Override
    protected void handleRecordCountMisMatch() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("There is a discrepancy between the number of transactions counted during the ingestion process.");
        sb.append(" Transactions in header: ");
        sb.append(getHeaderTransactionCount());
        sb.append(" Transactions in footer: ");
        sb.append(getFooterTransactionCount());
        sb.append(" Transactions counted while parsing file: ");
        sb.append(getTransactionCount());
        LOG.error("handleRecordCountMisMatch, there was a record count mis match: " + sb.toString());
    }
    

}
