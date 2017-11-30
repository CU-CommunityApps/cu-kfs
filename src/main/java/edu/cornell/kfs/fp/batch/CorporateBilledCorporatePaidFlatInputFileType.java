package edu.cornell.kfs.fp.batch;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.batch.service.CorporateBilledCorporatePaidCreateDocumentService;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransaction;
import edu.cornell.kfs.fp.businessobject.CorporateBilledCorporatePaidTransactionExtendedAttribute;
import edu.cornell.kfs.fp.businessobject.ProcurementCardTransactionExtendedAttribute;

public class CorporateBilledCorporatePaidFlatInputFileType extends ProcurementCardFlatInputFileType {
    protected CorporateBilledCorporatePaidCreateDocumentService corporateBilledCorporatePaidCreateDocumentService;
    
    @Override
    protected ProcurementCardTransaction buildProcurementCardTransaction() {
        return new CorporateBilledCorporatePaidTransaction();
    }
    
    @Override
    protected ProcurementCardTransactionExtendedAttribute buildProcurementCardTransactionExtendedAttribute() {
        return new CorporateBilledCorporatePaidTransactionExtendedAttribute();
    }
    
    @Override
    public String getFileTypeIdentifer() {
        return CuFPConstants.CORPORATE_BILLED_CORPORATE_PAID_FLAT_INPUT_FILE_TYPE;
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
    
    @Override
    protected void parseAccountingInformation(String line, ProcurementCardTransaction child) throws java.text.ParseException {
        child.setChartOfAccountsCode(corporateBilledCorporatePaidCreateDocumentService.getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_CHART_PARAMETER_NAME));
        child.setAccountNumber(corporateBilledCorporatePaidCreateDocumentService.getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_ACCOUNT_PARAMETER_NAME));
        child.setFinancialObjectCode(corporateBilledCorporatePaidCreateDocumentService.getCorporateBilledCorporatePaidDocumentParameter(
                CuFPParameterConstants.CorporateBilledCorporatePaidDocument.DEFAULT_AMOUNT_OWED_OBJECT_CODE_PARAMETER_NAME));
    }

    public void setCorporateBilledCorporatePaidCreateDocumentService(
            CorporateBilledCorporatePaidCreateDocumentService corporateBilledCorporatePaidCreateDocumentService) {
        this.corporateBilledCorporatePaidCreateDocumentService = corporateBilledCorporatePaidCreateDocumentService;
    }

}
