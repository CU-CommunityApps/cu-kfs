package edu.cornell.kfs.cemi.pdp.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionConstants;
import edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionGroupTwoBo;
import edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionGroupTwoBoSequence;
import edu.cornell.kfs.cemi.pdp.batch.dto.CemiGroupTwo;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionDataBuilder;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;

public abstract class CemiPaymentElectionDataBuilderBase implements CemiPaymentElectionDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected final CemiOutputDefinition outputDefinition;
    protected final LocalDateTime jobRunDate;
    protected final boolean maskSensitiveData;
    protected int paymentElectionCount;
   
    protected CemiPaymentElectionDao cemiPaymentElectionDao;
    protected AchBankService achBankService;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;

    protected CemiPaymentElectionDataBuilderBase(final CemiOutputDefinition outputDefinition,
            CemiPaymentElectionDao cemiPaymentElectionDao, AchBankService achBankService,
            DateTimeService dateTimeService, BusinessObjectService businessObjectService,
            final LocalDateTime jobRunDate, boolean maskSensitiveData) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(cemiPaymentElectionDao, "cemiPaymentElectionDao cannot be null");
        Validate.notNull(achBankService, "achBankService cannot be null");
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(businessObjectService, "businessObjectService cannot be null");
        Validate.notNull(jobRunDate, "jobRunDate cannot be null");
        this.outputDefinition = outputDefinition;
        this.cemiPaymentElectionDao = cemiPaymentElectionDao;
        this.achBankService = achBankService;
        this.dateTimeService = dateTimeService;
        this.businessObjectService = businessObjectService;
        this.jobRunDate = jobRunDate;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writePaymentElectionDataToIntermediateStorage(final Iterator<PayeeACHAccount> payeeACHAcounts,
                final LocalDateTime jobRunDate) throws IOException {
        CemiPaymentElectionGroupTwoBoSequence paymentElectionGroupTwoBoSequence = new CemiPaymentElectionGroupTwoBoSequence();
        
        for (final PayeeACHAccount payeeAchAccount : IteratorUtils.asIterable(payeeACHAcounts)) {
            paymentElectionCount++;
            if (paymentElectionCount % 1000 == 0) {
                LOG.info("writePaymentElectionDataToIntermediateStorage, Writing {} Payment Election and counting...", paymentElectionCount);
            }
            
            KualiInteger achAccountGeneratedIdentifier = payeeAchAccount.getAchAccountGeneratedIdentifier();
            String employeeId = determineEmployeeId(payeeAchAccount);
            String achBankName = determineAchBankName(payeeAchAccount.getBankRoutingNumber());
            final CemiGroupTwo groupTwo = new CemiGroupTwo(payeeAchAccount, employeeId, achBankName, maskSensitiveData);
            
            //Database table storage of data extract
            savePaymentElectionRowToTable(groupTwo, employeeId, achAccountGeneratedIdentifier, jobRunDate, paymentElectionGroupTwoBoSequence);
            
            //csv storage of data extract
            writePaymentElectionRowToFiles(groupTwo);
            
            //Record identifier associations for Payment Election extract file based upon batch job run date
            recordPaymentElectionIdentifiersInLegacyAssociationTable(employeeId, achAccountGeneratedIdentifier, jobRunDate);
        }
        LOG.info("writePaymentElectionDataToIntermediateStorage, Finished writing {} PayeeACHAccount for Payment Election", paymentElectionCount);
    }
    
    protected void savePaymentElectionRowToTable(CemiGroupTwo groupTwo, String employeeId, KualiInteger achAccountGeneratedIdentifierUsed,
            LocalDateTime jobRunDate, CemiPaymentElectionGroupTwoBoSequence paymentElectionGroupTwoBoSequence) {
        CemiPaymentElectionGroupTwoBo dataToSave = new CemiPaymentElectionGroupTwoBo(groupTwo, employeeId,
                achAccountGeneratedIdentifierUsed, jobRunDate, paymentElectionGroupTwoBoSequence);
        dataToSave = getBusinessObjectService().save(dataToSave);
    }
    
    private static String determineEmployeeId(final PayeeACHAccount payeeAchAccount) {
        return payeeAchAccount.getPayeeIdNumber();
    }
    
    protected void writePaymentElectionRowToFiles(final CemiGroupTwo groupTwo) throws IOException {
        writeDataToIntermediateStorage(CemiPaymentElectionConstants.PaymentElectionExtractSheets.GROUP_TWO, groupTwo);
    }
    
    protected void recordPaymentElectionIdentifiersInLegacyAssociationTable(final String employeeId,
            final KualiInteger achAccountGeneratedIdentifier, final LocalDateTime jobRunDate) {
        getCemiPaymentElectionDao().storeEmployeeIdAchAccountGeneratedIdPaymentElectionExtractRunDate(
                employeeId, achAccountGeneratedIdentifier, jobRunDate);
    }
    
    protected String determineAchBankName(String bankRoutingNumber) {
        ACHBank bankToUseForName = achBankService.getByPrimaryId(bankRoutingNumber);
        return (ObjectUtils.isNull(bankToUseForName) ? KFSConstants.EMPTY_STRING : bankToUseForName.getBankName());
    }

    /*
     * The subclass that writes the award schedule data to the temp tables needs to implement this method.
     * If desired, the implementation can keep connections/files/etc. open until close() is called.
     * See the CSV implementation for an example.
     */
    protected abstract void writeDataToIntermediateStorage(
            final String sheetName, final Object rowObject) throws IOException;

    // The temp table implementation can use (or override) this method to retrieve the column value to be inserted.
    protected String getFieldValue(final CemiFieldDefinition field, final Object rowObject) {
        switch (field.getType()) {
            case STATIC:
                return field.getValue();

            case STRING:
                return (String) ObjectUtils.getPropertyValue(rowObject, field.getKey());

            default:
                throw new IllegalStateException("Unknown field type: " + field.getType());
        }
    }

    public CemiPaymentElectionDao getCemiPaymentElectionDao() {
        return cemiPaymentElectionDao;
    }

    public void setCemiPaymentElectionDao(CemiPaymentElectionDao cemiPaymentElectionDao) {
        this.cemiPaymentElectionDao = cemiPaymentElectionDao;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
