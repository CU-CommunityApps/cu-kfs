package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.io.IOException;
import java.sql.Date;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.cg.CemiAwardScheduleConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleBoSequence;
import edu.cornell.kfs.cemi.module.cg.batch.dto.CemiAwardSchedule;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleDataBuilder;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public abstract class CemiAwardScheduleDataBuilderBase implements CemiAwardScheduleDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected final CemiOutputDefinition outputDefinition;
    protected final LocalDateTime jobRunDate;
    protected final boolean maskSensitiveData;
    protected int awardScheduleCount;
   
    protected CemiAwardScheduleDao cemiAwardScheduleDao;
    protected DateTimeService dateTimeService;
    protected BusinessObjectService businessObjectService;

    protected CemiAwardScheduleDataBuilderBase(final CemiOutputDefinition outputDefinition,
            CemiAwardScheduleDao cemiAwardScheduleDao, DateTimeService dateTimeService, BusinessObjectService businessObjectService, 
            final LocalDateTime jobRunDate, boolean maskSensitiveData) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(cemiAwardScheduleDao, "cemiAwardScheduleDao cannot be null");
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(businessObjectService, "businessObjectService cannot be null");
        Validate.notNull(jobRunDate, "jobRunDate cannot be null");
        this.outputDefinition = outputDefinition;
        this.cemiAwardScheduleDao = cemiAwardScheduleDao;
        this.dateTimeService = dateTimeService;
        this.businessObjectService = businessObjectService;
        this.jobRunDate = jobRunDate;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writeAwardScheduleDataToIntermediateStorage(final Iterator<Award> awards,
                final LocalDateTime jobRunDate) throws IOException {
        CemiAwardScheduleBoSequence awardScheduleTabTableSequence = new CemiAwardScheduleBoSequence();
        
        for (final Award award : IteratorUtils.asIterable(awards)) {
            awardScheduleCount++;
            if (awardScheduleCount % 1000 == 0) {
                LOG.info("writeAwardScheduleDataToIntermediateStorage, Writing {} Award Schedule and counting...", awardScheduleCount);
            }

            //Award Schedule Tab
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            String awardIntervalStartDate = determineFormattedDate(awardExtendedAttribute.getBudgetBeginningDate());
            String awardIntervalEndDate = determineFormattedDate(awardExtendedAttribute.getBudgetEndingDate());
            String spreadsheetKey = buildSpreadsheetKey(award.getProposalNumber());
            String awardScheduleReferenceId = buildAwardScheduleReferenceId(award.getProposalNumber());
            final CemiAwardSchedule awardSchedule = new CemiAwardSchedule(award, spreadsheetKey, 
                    awardScheduleReferenceId, awardIntervalStartDate, awardIntervalEndDate, maskSensitiveData);
            //Database table storage of data extract
            saveAwardScheduleRowToTable(awardSchedule, award.getProposalNumber(), jobRunDate, awardScheduleTabTableSequence);
            
            //csv storage of data extract
            writeAwardScheduleRowToFiles(awardSchedule);
            
            //Record identifier associations for Award Schedule extract file based upon batch job run date
            recordAwardScheduleIdentifiersInLegacyAssociationTable(spreadsheetKey, awardScheduleReferenceId, jobRunDate);
        }
        LOG.info("writeAwardScheduleDataToIntermediateStorage, Finished writing {} Awards for Award Schedule", awardScheduleCount);
    }
    
    protected void saveAwardScheduleRowToTable(CemiAwardSchedule awardSchedule, String proposalNumberUsed,
            LocalDateTime jobRunDate, CemiAwardScheduleBoSequence awardScheduleTabTableSequence) {
        CemiAwardScheduleBo dataToSave = new CemiAwardScheduleBo(awardSchedule, proposalNumberUsed, jobRunDate, awardScheduleTabTableSequence);
         dataToSave = getBusinessObjectService().save(dataToSave);
    }
    
    private static String buildSpreadsheetKey(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardScheduleConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
    }
    
    private static String buildAwardScheduleReferenceId(final String awardProposalNumber) {
        return MessageFormat.format(CemiAwardScheduleConstants.AWARD_SCHEDULE_REFERENCE_ID_FORMAT, awardProposalNumber);
    }
    
    private String determineFormattedDate(Date dateToFormat) {
        return ObjectUtils.isNotNull(dateToFormat)
                ? dateTimeService.toString(dateToFormat, CemiBaseConstants.DATE_FORMAT_yyyy_MM_dd)
                        : KFSConstants.EMPTY_STRING;
    }
    
    protected void writeAwardScheduleRowToFiles(final CemiAwardSchedule awardSchedule) throws IOException {
        writeDataToIntermediateStorage(CemiAwardScheduleConstants.AwardScheduleExtractSheets.AWARD_SCHEDULE, awardSchedule);
    }
    
    protected void recordAwardScheduleIdentifiersInLegacyAssociationTable(final String spreadsheetKey,
            final String  awardProposalNumber, final LocalDateTime jobRunDate) {
        getCemiAwardScheduleDao().storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(spreadsheetKey,
                awardProposalNumber, jobRunDate);
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

    public CemiAwardScheduleDao getCemiAwardScheduleDao() {
        return cemiAwardScheduleDao;
    }

    public void setCemiAwardScheduleDao(CemiAwardScheduleDao cemiAwardScheduleDao) {
        this.cemiAwardScheduleDao = cemiAwardScheduleDao;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
