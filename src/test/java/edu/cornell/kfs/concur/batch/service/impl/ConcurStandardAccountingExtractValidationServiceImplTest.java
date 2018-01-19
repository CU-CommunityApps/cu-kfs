package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.fixture.ConcurParameterConstantsFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurStandardAccountingExtractFileFixture;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;

public class ConcurStandardAccountingExtractValidationServiceImplTest {
    private static final String CORNELL_UPPERCASE_GROUP_ID  = "CORNELL";
    private static final String CORNELL_MIXED_CASE_GROUP_ID  = "Cornell";
    private static final String EXECUTIVES_GROUP_ID  = "Executives";
    private static final String NULL_GROUP_ID  = null;
    private static final String FOO_GROUP_ID  = "foo";
    private static final String PARAM_VALUES_SPLIT_CHAR = ";";
    
    private ConcurStandardAccountingExtractValidationServiceImpl concurStandardAccountingValidationService;
    private ConcurStandardAccountingExtractFile cornellUppercasefile;
    private ConcurStandardAccountingExtractFile cornellMixedcaseFile;
    private ConcurStandardAccountingExtractFile executivesFile;
    private ConcurStandardAccountingExtractFile nullGroupIdFile;
    private ConcurStandardAccountingExtractFile fooGroupIdFile;
    private ConcurStandardAccountingExtractBatchReportData reportData;
    private ConcurBatchUtilityServiceImpl concurBatchUtilityService;
    private ConcurParameterConstantsFixture concurParameterConstantsFixture;
    private ConcurEmployeeInfoValidationServiceImpl concurEmployeeInfoValidationService;
    
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurBatchUtilityService = new TestableConcurBatchUtilityServiceImpl();
        concurEmployeeInfoValidationService = new TestableConcurEmployeeInfoValidationServiceImpl();
        concurStandardAccountingValidationService = new ConcurStandardAccountingExtractValidationServiceImpl();
        concurStandardAccountingValidationService.setConcurBatchUtilityService(concurBatchUtilityService);
        concurStandardAccountingValidationService.setConcurEmployeeInfoValidationService(concurEmployeeInfoValidationService);
        
        KualiDecimal[] debits = {new KualiDecimal(100.75), new KualiDecimal(-50.45)};
        KualiDecimal[] credits  = {};
        cornellUppercasefile = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits, CORNELL_UPPERCASE_GROUP_ID);
        cornellMixedcaseFile = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits, CORNELL_MIXED_CASE_GROUP_ID);
        executivesFile = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits, EXECUTIVES_GROUP_ID);
        nullGroupIdFile = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits, NULL_GROUP_ID);
        fooGroupIdFile = ConcurStandardAccountingExtractFileFixture.buildConcurStandardAccountingExtractFile(debits, credits, FOO_GROUP_ID);
        reportData = new ConcurStandardAccountingExtractBatchReportData();
        concurParameterConstantsFixture = new ConcurParameterConstantsFixture();
        
    }

    @After
    public void tearDown() throws Exception {
        concurStandardAccountingValidationService = null;
        cornellUppercasefile = null;
        cornellMixedcaseFile = null;
        executivesFile = null;
        nullGroupIdFile = null;
        fooGroupIdFile = null;
        reportData = null;
    }
    
    @Test
    public void validateDetailCountGood() {
        assertTrue("The counts should be been the same.", concurStandardAccountingValidationService.validateDetailCount(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateDetailCountIncorrectMatch() {
        setBadRecordCount();
        assertFalse("The counts should NOT be been the same.", concurStandardAccountingValidationService.validateDetailCount(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateAmountsGood() {
        assertTrue("The amounts should equal.", concurStandardAccountingValidationService.validateAmountsAndDebitCreditCode(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateAmountsAmountMismatch() {
        setBadJournalTotal();
        assertFalse("The amounts should NOT be equal.", concurStandardAccountingValidationService.validateAmountsAndDebitCreditCode(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateAmountsIncorrectDebitCredit() {
        setBadDebitCredit();
        assertFalse("The should throw an error due to incorrect debitCredit field", concurStandardAccountingValidationService.validateAmountsAndDebitCreditCode(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateDateGood() {
        Date testDate = new Date(Calendar.getInstance().getTimeInMillis());
        assertTrue("The date should be valid.", concurStandardAccountingValidationService.validateDate(testDate));
    }
    
    @Test
    public void validateDateBad() {
        Date testDate = null;
        assertFalse("The date should NOT be valid.", concurStandardAccountingValidationService.validateDate(testDate));
    }
    
    @Test
    public void validateGeneralValidationGood() {
        assertTrue("General Validation should be good.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateGeneralValidationBadDate() {
        cornellUppercasefile.setBatchDate(null);
        assertFalse("General validation should be false, bad date.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateGeneralValidationBadAmount() {
        setBadJournalTotal();
        assertFalse("General validation should be false, bad journal total.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateGeneralValidationBadDebitCredit() {
        setBadDebitCredit();
        assertFalse("General validation should be false, bad debit credit field.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(cornellUppercasefile, reportData));
    }
    
    @Test
    public void validateGeneralValidationBadCount() {
        setBadRecordCount();
        assertFalse("General validation should be false, bad line count.", concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(cornellUppercasefile, reportData));
    }
    
    @Test 
    public void validateEmployeeGroupIdUppercaseIsGood() {
        assertTrue("This should be a good group", 
                concurStandardAccountingValidationService.validateEmployeeGroupId(cornellUppercasefile.getConcurStandardAccountingExtractDetailLines().get(0), reportData));
    }
    
    @Test 
    public void validateEmployeeGroupIdMixedcaseIsGood() {
        assertTrue("This should be a good group", 
                concurStandardAccountingValidationService.validateEmployeeGroupId(cornellMixedcaseFile.getConcurStandardAccountingExtractDetailLines().get(0), reportData));
    }
    
    @Test 
    public void validateExecutivesEmployeeGroupIdGood() {
        assertTrue("Executives should be a good group", 
                concurStandardAccountingValidationService.validateEmployeeGroupId(executivesFile.getConcurStandardAccountingExtractDetailLines().get(0), reportData));
    }
    
    @Test 
    public void validateEmployeeGroupIdNull() {
        assertFalse("This should be a bad group", 
                concurStandardAccountingValidationService.validateEmployeeGroupId(nullGroupIdFile.getConcurStandardAccountingExtractDetailLines().get(0), reportData));
    }
    
    @Test 
    public void validateEmployeeGroupIdWrongValue() {
        assertFalse("This should be a bad group", 
                concurStandardAccountingValidationService.validateEmployeeGroupId(fooGroupIdFile.getConcurStandardAccountingExtractDetailLines().get(0), reportData));
    }
    
    @Test
    public void validateGeneralValidationBadEmployeeGroup() {
        cornellUppercasefile.getConcurStandardAccountingExtractDetailLines().get(0).setEmployeeGroupId("testMe");
        assertFalse("General validation should be false, bad employee group id.", 
                concurStandardAccountingValidationService.validateConcurStandardAccountExtractFile(cornellUppercasefile, reportData));
    }
    
    private void setBadJournalTotal() {
        cornellUppercasefile.getConcurStandardAccountingExtractDetailLines().get(0).setJournalAmount(new KualiDecimal(200));
    }
    
    private void setBadDebitCredit() {
        cornellUppercasefile.getConcurStandardAccountingExtractDetailLines().get(0).setJounalDebitCredit("foo");
    }

    private void setBadRecordCount() {
        cornellUppercasefile.setRecordCount(new Integer(5));
    }
    
    private class TestableConcurEmployeeInfoValidationServiceImpl extends ConcurEmployeeInfoValidationServiceImpl {
        @Override
        public boolean isEmployeeGroupIdValid(String employeeGroupId) {
            if(StringUtils.isNotBlank(employeeGroupId)){
                String parameterValue = concurParameterConstantsFixture.getValueForConcurParameter(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID);              
                if(StringUtils.isNotBlank(parameterValue) && StringUtils.contains(parameterValue, PARAM_VALUES_SPLIT_CHAR)){
                    List<String> parameterValues = Arrays.asList(parameterValue.split(PARAM_VALUES_SPLIT_CHAR));
                    return parameterValues.stream().filter(acceptedValue -> acceptedValue.equalsIgnoreCase(employeeGroupId)).count() > 0;                 
                }
            }
            return false;
        }
    }
    
    private class TestableConcurBatchUtilityServiceImpl extends ConcurBatchUtilityServiceImpl {
        @Override
        public String getConcurParameterValue(String parameterName) {
            return concurParameterConstantsFixture.getValueForConcurParameter(parameterName);          
        }
    }
    
}
