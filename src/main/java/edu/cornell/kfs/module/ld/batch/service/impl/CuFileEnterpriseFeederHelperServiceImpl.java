package edu.cornell.kfs.module.ld.batch.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.gl.batch.service.impl.ExceptionCaughtStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconBadLoadAbortedStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconOkLoadOkStatus;
import org.kuali.kfs.gl.batch.service.impl.ReconciliationBlock;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.service.impl.EnterpriseFeederStatusAndErrorMessagesWrapper;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.batch.LaborEnterpriseFeedStep;
import org.kuali.kfs.module.ld.batch.service.impl.FileEnterpriseFeederHelperServiceImpl;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.LaborOriginEntry;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.report.EnterpriseFeederReportData;
import org.kuali.kfs.module.ld.util.LaborOriginEntryFileIterator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.rsmart.kuali.kfs.module.ld.LdConstants;

public class CuFileEnterpriseFeederHelperServiceImpl extends FileEnterpriseFeederHelperServiceImpl {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuFileEnterpriseFeederHelperServiceImpl.class);
    
    @Override
    public void feedOnFile(File doneFile, File dataFile, File reconFile, PrintStream enterpriseFeedPs,
            String feederProcessName, String reconciliationTableId,
            EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors, LedgerSummaryReport ledgerSummaryReport,
            ReportWriterService errorStatisticsReport, EnterpriseFeederReportData feederReportData) {
        LOG.info("Processing done file: " + doneFile.getAbsolutePath());

        List<Message> errorMessages = statusAndErrors.getErrorMessages();
        BufferedReader dataFileReader = null;

        ReconciliationBlock reconciliationBlock = null;
        Reader reconReader = null;
        try {
            reconReader = new FileReader(reconFile);
            reconciliationBlock = reconciliationParserService.parseReconciliationBlock(reconReader, reconciliationTableId);
        }
        catch (IOException e) {
            LOG.error("IO Error occured trying to read the recon file.", e);
            errorMessages.add(new Message("IO Error occured trying to read the recon file.", Message.TYPE_FATAL));
            reconciliationBlock = null;
            statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
            LOG.error("Error occured trying to parse the recon file.", e);
            errorMessages.add(new Message("Error occured trying to parse the recon file.", Message.TYPE_FATAL));
            reconciliationBlock = null;
            statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            throw e;
        }
        finally {
            if (reconReader != null) {
                try {
                    reconReader.close();
                }
                catch (IOException e) {
                    LOG.error("Error occured trying to close recon file: " + reconFile.getAbsolutePath(), e);
                }
            }
        }

        try {
            if (reconciliationBlock == null) {
                errorMessages.add(new Message("Unable to parse reconciliation file.", Message.TYPE_FATAL));
            }
            else {
                dataFileReader = new BufferedReader(new FileReader(dataFile));
                Iterator<LaborOriginEntry> fileIterator = new LaborOriginEntryFileIterator(dataFileReader, false);
                reconciliationService.reconcile(fileIterator, reconciliationBlock, errorMessages);

                fileIterator = null;
                dataFileReader.close();
                dataFileReader = null;
            }

            if (reconciliationProcessSucceeded(errorMessages)) {
                dataFileReader = new BufferedReader(new FileReader(dataFile));
                String line;
                int count = 0;
                    
                String offsetDocTypes = null;
                if(StringUtils.isNotEmpty(parameterService.getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE))) {
                    offsetDocTypes = "," + parameterService.getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE).replace(";", ",").replace("|", ",") + ",";
                }
                while ((line = dataFileReader.readLine()) != null) {
                    try {
                        LaborOriginEntry tempEntry = new LaborOriginEntry();
                        tempEntry.setFromTextFileForBatch(line, count);
                        
                        feederReportData.incrementNumberOfRecordsRead();
                        feederReportData.addToTotalAmountRead(tempEntry.getTransactionLedgerEntryAmount());
                        
                        enterpriseFeedPs.printf("%s\n", line);
                        
                        ledgerSummaryReport.summarizeEntry(tempEntry);
                        feederReportData.incrementNumberOfRecordsWritten();
                        feederReportData.addToTotalAmountWritten(tempEntry.getTransactionLedgerEntryAmount());
                        
                        List<LaborOriginEntry> benefitEntries = generateBenefits(tempEntry, errorStatisticsReport, feederReportData);
                        KualiDecimal benefitTotal = new KualiDecimal (0);
                        KualiDecimal offsetTotal = new KualiDecimal (0);
                        
                        for(LaborOriginEntry benefitEntry : benefitEntries) {
                            benefitEntry.setTransactionLedgerEntryDescription("FRINGE EXPENSE");
                            enterpriseFeedPs.printf("%s\n", benefitEntry.getLine());
                            
                            feederReportData.incrementNumberOfRecordsWritten();
                            feederReportData.addToTotalAmountWritten(benefitEntry.getTransactionLedgerEntryAmount());
                            
                            if(benefitEntry.getTransactionLedgerEntryAmount().isZero())         continue;
                            benefitTotal = benefitTotal.add(benefitEntry.getTransactionLedgerEntryAmount());
                        }
                        
                        if(tempEntry.getFinancialBalanceTypeCode() == null || tempEntry.getFinancialBalanceTypeCode().equalsIgnoreCase("IE")) continue;
                        List<LaborOriginEntry> offsetEntries =  generateOffsets(tempEntry,offsetDocTypes);
                        for(LaborOriginEntry offsetEntry : offsetEntries){
                            if(offsetEntry.getTransactionLedgerEntryAmount().isZero()) continue;
                            enterpriseFeedPs.printf("%s\n", offsetEntry.getLine()); 
                            offsetTotal = offsetTotal.add(offsetEntry.getTransactionLedgerEntryAmount());                           
                        }


                        if(!benefitTotal.equals(offsetTotal)){
                            LOG.info("** count:offsetTotal: benefitTotal="+count +":"+ offsetTotal+""+benefitTotal);
                        }
                        
                    } catch (NullPointerException npe) {
                        LOG.error("NPE encountered");
                        throw new RuntimeException(npe.toString());
                    } catch (Exception e) {
                        throw new IOException(e.toString());
                    }
                    
                    count++;
                    LOG.info("Processed Entry # "+ count);
                }
                
                
                dataFileReader.close();
                dataFileReader = null;
             //   LOG.info("TotalBenifits : " + totalBenefitValue);

                statusAndErrors.setStatus(new FileReconOkLoadOkStatus());
            }
            else {
                statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            }
        }
        catch (Exception e) {
            LOG.error("Caught exception when reconciling/loading done file: " + doneFile, e);
            statusAndErrors.setStatus(new ExceptionCaughtStatus());
            errorMessages.add(new Message("Caught exception attempting to reconcile/load done file: " + doneFile + ".  File contents are NOT loaded", Message.TYPE_FATAL));
            // re-throw the exception rather than returning a value so that Spring will auto-rollback
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            else {
                // Spring only rolls back when throwing a runtime exception (by default), so we throw a new exception
                throw new RuntimeException(e);
            }
        }
        finally {
            if (dataFileReader != null) {
                try {
                    dataFileReader.close();
                }
                catch (IOException e) {
                    LOG.error("IO Exception occured trying to close connection to the data file", e);
                    errorMessages.add(new Message("IO Exception occured trying to close connection to the data file", Message.TYPE_FATAL));
                }
            }
        }
    }
    
    protected List<LaborOriginEntry> generateOffsets(LaborOriginEntry wageEntry,String offsetDocTypes) {
        List<LaborOriginEntry> offsetEntries = new ArrayList<LaborOriginEntry>();
        String benefitRateCategoryCode = laborBenefitsCalculationService.getBenefitRateCategoryCode(wageEntry.getChartOfAccountsCode(), wageEntry.getAccountNumber(), wageEntry.getSubAccountNumber());
        Collection<PositionObjectBenefit> positionObjectBenefits = laborPositionObjectBenefitService.getActivePositionObjectBenefits(wageEntry.getUniversityFiscalYear(), wageEntry.getChartOfAccountsCode(), wageEntry.getFinancialObjectCode());
        
        if (positionObjectBenefits == null || positionObjectBenefits.isEmpty()) {
            return offsetEntries;
        }

        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {

            Map<String, Object> fieldValues = new HashMap<String, Object>();
            fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, wageEntry.getUniversityFiscalYear());
            fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, wageEntry.getChartOfAccountsCode());
            fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
            fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, benefitRateCategoryCode);
            
            BenefitsCalculation benefitsCalculation = (org.kuali.kfs.module.ld.businessobject.BenefitsCalculation) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(BenefitsCalculation.class, fieldValues);
            
            if(ObjectUtils.isNull(benefitsCalculation)) continue;

            LaborOriginEntry offsetEntry = new LaborOriginEntry(wageEntry);
            offsetEntry.setFinancialObjectCode(benefitsCalculation.getPositionFringeBenefitObjectCode());



            // calculate the offsetAmount amount (ledger amt * (benfit pct/100) )
            KualiDecimal fringeBenefitPercent = benefitsCalculation.getPositionFringeBenefitPercent();
            KualiDecimal offsetAmount = fringeBenefitPercent.multiply(
            wageEntry.getTransactionLedgerEntryAmount()).divide(KFSConstants.ONE_HUNDRED.kualiDecimalValue());
            offsetEntry.setTransactionLedgerEntryAmount(offsetAmount.abs());


            offsetEntry.setAccountNumber(benefitsCalculation.getAccountCodeOffset());
            offsetEntry.setFinancialObjectCode(benefitsCalculation.getObjectCodeOffset());
            
            //Set all the fields required to process through the scrubber and poster jobs
            offsetEntry.setUniversityFiscalPeriodCode(wageEntry.getUniversityFiscalPeriodCode());
            offsetEntry.setChartOfAccountsCode(wageEntry.getChartOfAccountsCode());
            offsetEntry.setUniversityFiscalYear(wageEntry.getUniversityFiscalYear());
            offsetEntry.setSubAccountNumber("-----");
            offsetEntry.setFinancialSubObjectCode("---");
            offsetEntry.setOrganizationReferenceId("");
            offsetEntry.setProjectCode("");
            
            offsetEntry.setTransactionLedgerEntryDescription("GENERATED BENEFIT OFFSET");
            
            String originCode = parameterService.getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_ORIGIN_CODE);
            
            offsetEntry.setFinancialSystemOriginationCode(originCode);
            offsetEntry.setDocumentNumber(wageEntry.getDocumentNumber());


            if(!wageEntry.getTransactionDebitCreditCode().equalsIgnoreCase("D")) {
                offsetAmount = offsetAmount.multiply(new KualiDecimal(-1)); 
            }
            
            if(offsetAmount.isGreaterThan(new KualiDecimal(0))) {
                offsetEntry.setTransactionDebitCreditCode("C");
            } else if(offsetAmount.isLessThan(new KualiDecimal(0))) {
                offsetEntry.setTransactionDebitCreditCode("D");
            }
            
 
            String docTypeCode = offsetDocTypes;
            if (offsetDocTypes.contains(",")) {
                String[] splits = offsetDocTypes.split(",");
                for(String split : splits) {
                    if(!StringUtils.isEmpty(split)) {
                        docTypeCode = split;
                        break;
                    }
                }
            }
            offsetEntry.setFinancialDocumentTypeCode(docTypeCode);
            offsetEntries.add(offsetEntry);
        }

        return offsetEntries;
    }


}
