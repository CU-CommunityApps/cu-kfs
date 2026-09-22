package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableFactory;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableMaps;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardPropertyConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardAllocationDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardBudgetDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardFileSubmitAwardTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardHeaderDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyAccountSubAccountDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLineDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardSpecialConditionDataBo;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardAllocationDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardBudgetDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardFileSubmitAwardTabRowBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardHeaderDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardLegacyNovelutionBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardLineDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.batch.factory.CemiAwardSpecialConditionDataBoFactory;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CemiAwardFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiAwardFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected DateTimeService dateTimeService;
    protected CemiAwardExtractOrmDao cemiAwardExtractOrmDao;
    protected CemiAwardExtractDao cemiAwardExtractDao;
    protected final boolean maskSensitiveData;
    
    protected CemiAwardTranslateTableMaps allAwardTranslateTableMaps;
    
    public CemiAwardFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiAwardExtractOrmDao cemiAwardExtractOrmDao,
            final CemiAwardExtractDao cemiAwardExtractDao,
            final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiAwardFileSubmitAwardTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null in CemiAwardFileExtractDataBuilderDefaultImpl");
        Validate.notNull(cemiAwardExtractOrmDao, "cemiAwardExtractOrmDao cannot be null in CemiAwardFileExtractDataBuilderDefaultImpl");
        Validate.notNull(cemiAwardExtractDao, "cemiAwardExtractDao cannot be null in CemiAwardFileExtractDataBuilderDefaultImpl");
        this.dateTimeService = dateTimeService;
        this.cemiAwardExtractOrmDao = cemiAwardExtractOrmDao;
        this.cemiAwardExtractDao = cemiAwardExtractDao;
        this.maskSensitiveData = maskSensitiveData;
        populateAllAwardTranslateTableMaps();
    }
    
    
    @Override
    public void writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage(final Iterator<Award> awards) {
        int awardCounter = 0;
        int totalRowsWritten = 0;
        
        for (final Award award : IteratorUtils.asIterable(awards)) {
            awardCounter++;
            if (awardCounter % 1000 == 0) {
                LOG.info("writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage, Processed {} "
                        + "Awards for Submit Award and counting...", awardCounter);
            }
            
            // Gather all the data specific to the award being converted. 
            AwardExtendedAttribute awardExtendedAttribute = (AwardExtendedAttribute) award.getExtension();
            String currentProposalNumber = award.getProposalNumber();
            String awardOrgCode = cemiAwardExtractDao.findOrganizationCodeForInScopeAward(currentProposalNumber);
            
            // Novelution data attribute retreival for the award
            CemiAwardLegacyNovelutionBo awardNovelutionAttributes = obtainAssociatedNovelutionData(award.getProposalNumber());
            if (ObjectUtils.isNull(awardNovelutionAttributes)) {
                // problem encountered retrieving Novelution data for award
                // set to empty business object to prevent downstream processing from failing
                awardNovelutionAttributes = CemiAwardLegacyNovelutionBoFactory.createEmptyCemiAwardLegacyNovelutionBo();
            }
            
            // Account and Sub-Account data retreival for the award
            Collection<CemiAwardLegacyAccountSubAccountDataBo> awardAccountsSubAccountsCollection = obtainLegacyAccountSubAccountsFor(currentProposalNumber);
            
            //Database table storage of data extract
            totalRowsWritten += createAndStoreAwardFileSubmitAwardTabRowsFor(award, awardExtendedAttribute, awardOrgCode,
                    awardAccountsSubAccountsCollection, awardNovelutionAttributes, jobRunDateString);
        }
        LOG.info("writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage, Finished writing "
                + "{} Submit Award data rows for {} Awards.", totalRowsWritten, awardCounter);
    }
    
    
    protected Collection<CemiAwardLegacyAccountSubAccountDataBo> obtainLegacyAccountSubAccountsFor(final String proposalNumber) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(CemiAwardPropertyConstants.PROPOSAL_NUMBER, proposalNumber);
        Collection<CemiAwardLegacyAccountSubAccountDataBo> allAccountsWithSubAccounts = 
                        businessObjectService.findMatchingOrderBy(CemiAwardLegacyAccountSubAccountDataBo.class,
                                fieldValues, CemiAwardPropertyConstants.ACCOUNT_NUMBER, true);
        
        // Business object service does not allow for multiple property result set sorting. Must sort manually.
        // Construct the sort order 
        List<String> defaultSortOrder = new ArrayList<String>();
        defaultSortOrder.add(0, CemiAwardPropertyConstants.PROPOSAL_NUMBER);
        defaultSortOrder.add(1, CemiAwardPropertyConstants.ACCOUNT_NUMBER);
        defaultSortOrder.add(2, CemiAwardPropertyConstants.SUB_ACCOUNT_NUMBER);
        
        allAccountsWithSubAccounts = new ArrayList<CemiAwardLegacyAccountSubAccountDataBo>(allAccountsWithSubAccounts);
        
        Collections.sort((List) allAccountsWithSubAccounts, new BeanPropertyComparator(defaultSortOrder, true));
        
        return allAccountsWithSubAccounts;
    }
    
    
    protected int createAndStoreAwardFileSubmitAwardTabRowsFor(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute, final String awardOrgCode,
            final Collection<CemiAwardLegacyAccountSubAccountDataBo> awardAccountsSubAccountsCollection,
            final CemiAwardLegacyNovelutionBo awardNovelutionAttributes, final String jobRunDateString) {
        
        int numAwardFileLinesGeneratedForThisAward = 0;
        
        // FIXME TODO find contract control account to use its attributes at the award header level pass 
        // into headerBo factory, collection used in routine input parameter so multiple iterators can be created
        final CemiAwardHeaderDataBo headerBo = CemiAwardHeaderDataBoFactory.createAwardHeaderDataBoFrom(award,
                awardExtendedAttribute, awardAccountsSubAccountsCollection.iterator(), awardOrgCode,
                awardNovelutionAttributes, jobRunDateString, dateTimeService, cemiAwardExtractDao,
                allAwardTranslateTableMaps, maskSensitiveData);
        
        // Setup iterator prior to loop for award lines associated with the current award being processed
        Iterator<CemiAwardLegacyAccountSubAccountDataBo> accountsSubAccountsBeingProcessed = awardAccountsSubAccountsCollection.iterator();

        boolean useSubAccount = true;
        boolean useAccount = !useSubAccount;
        
        int awardLineDataRowIdCounter = 0;  //start with a value of 1 and increment by 1 until the next award, then return to a value of 1 (carry all of the data down for the columns to the left when populating row id of 2, 3 etc)
        int awardLineDataLineNumberCounter = 0; //TODO FIXME QUESTION ?same as awardLineDataRowIdCounter?? : ITH: Incremental number for each additional award line data - check in legacy system for line number
        int specialConditionDataRowIdCounter = 0; //Increment by 1 starting at a value of 1 for each additional special condition by Award, return to a value of 1 for the next Award
        int budgetDataRowIdCounter = 1; // hardcoded to 1 do not increment
        
        String currentAccountBeingProcessed;
        String previousAccountProcessed = CemiBaseConstants.EMPTY_STRING;
        
        for (final CemiAwardLegacyAccountSubAccountDataBo processingAccountSubAccount : IteratorUtils.asIterable(accountsSubAccountsBeingProcessed)) {
            
            currentAccountBeingProcessed = processingAccountSubAccount.getAccountNumber();
            
            if (!currentAccountBeingProcessed.equalsIgnoreCase(previousAccountProcessed)) {
                awardLineDataRowIdCounter++;
                awardLineDataLineNumberCounter++;
                specialConditionDataRowIdCounter++;
                budgetDataRowIdCounter = 1;
                numAwardFileLinesGeneratedForThisAward++;
                
                // write out account row first
                final CemiAwardLineDataBo awardLineAccountBo = CemiAwardLineDataBoFactory.createCemiAwardLineDataBoFrom(
                    award, awardExtendedAttribute, processingAccountSubAccount, awardOrgCode, useAccount,
                    awardLineDataRowIdCounter, awardLineDataLineNumberCounter, jobRunDateString, dateTimeService,
                    allAwardTranslateTableMaps, maskSensitiveData);
                    
                    
                // FIXME TODO: Adjust these factory calls when TBD mappings are completed
                final CemiAwardSpecialConditionDataBo specialConditionBo = CemiAwardSpecialConditionDataBoFactory
                    .createCemiAwardSpecialConditionDataBoFrom(award, processingAccountSubAccount,
                            useAccount, awardLineDataRowIdCounter, awardLineDataLineNumberCounter,
                            specialConditionDataRowIdCounter, jobRunDateString, maskSensitiveData);
            
                // FIXME TODO: Adjust this factory call from an EMPTY BO to actual BO creation when TBD mappings are completed
                final CemiAwardBudgetDataBo budgetBo = CemiAwardBudgetDataBoFactory
                    .createCemiAwardBudgetDataBoFrom(award, processingAccountSubAccount, useAccount, 
                            awardLineDataRowIdCounter, awardLineDataLineNumberCounter, specialConditionDataRowIdCounter,
                            budgetDataRowIdCounter, jobRunDateString, maskSensitiveData);
            
                // FIXME TODO: Adjust this factory call from an EMPTY BO to actual BO creation when TBD mappings are completed
                final CemiAwardAllocationDataBo allocationBo = CemiAwardAllocationDataBoFactory
                    .createCemiAwardAllocationDataBoFrom(award, processingAccountSubAccount, useAccount, 
                            awardLineDataRowIdCounter, awardLineDataLineNumberCounter, specialConditionDataRowIdCounter,
                            budgetDataRowIdCounter, jobRunDateString, maskSensitiveData);
            
                createAndStoreAwardFileSubmitAwardTabRowBo(headerBo, awardLineAccountBo, specialConditionBo, budgetBo, allocationBo);
                previousAccountProcessed = currentAccountBeingProcessed;
            }
            
            if (StringUtils.isNotBlank(processingAccountSubAccount.getSubAccountNumber())) {
                awardLineDataRowIdCounter++;
                awardLineDataLineNumberCounter++;
                specialConditionDataRowIdCounter++;
                budgetDataRowIdCounter = 1;
                numAwardFileLinesGeneratedForThisAward++;
                // account has a sub-account, write out sub-account row
                // FIXME TODO: Adjust these factory calls when TBD mappings are completed
                final CemiAwardLineDataBo subAccountAwardLineSubAccountBo =
                        CemiAwardLineDataBoFactory.createCemiAwardLineDataBoFrom(award, awardExtendedAttribute,
                                processingAccountSubAccount, awardOrgCode, useSubAccount, awardLineDataRowIdCounter,
                                awardLineDataLineNumberCounter, jobRunDateString, dateTimeService,
                                allAwardTranslateTableMaps, maskSensitiveData);
                
             // FIXME TODO: Adjust these factory calls when TBD mappings are completed
                final CemiAwardSpecialConditionDataBo subAccountSpecialConditionBo = CemiAwardSpecialConditionDataBoFactory
                        .createCemiAwardSpecialConditionDataBoFrom(award, processingAccountSubAccount,
                                useSubAccount, awardLineDataRowIdCounter, awardLineDataLineNumberCounter,
                                specialConditionDataRowIdCounter, jobRunDateString, maskSensitiveData);
                
                // FIXME TODO: Adjust this factory call from an EMPTY BO to actual BO creation when TBD mappings are completed
                final CemiAwardBudgetDataBo subAccountBudgetBo = CemiAwardBudgetDataBoFactory
                        .createCemiAwardBudgetDataBoFrom(award, processingAccountSubAccount, useSubAccount, 
                                awardLineDataRowIdCounter, awardLineDataLineNumberCounter, specialConditionDataRowIdCounter,
                                budgetDataRowIdCounter, jobRunDateString, maskSensitiveData);
        
                // FIXME TODO: Adjust this factory call from an EMPTY BO to actual BO creation when TBD mappings are completed
                final CemiAwardAllocationDataBo subAccountAllocationBo = CemiAwardAllocationDataBoFactory
                        .createCemiAwardAllocationDataBoFrom(award, processingAccountSubAccount, useSubAccount, 
                                awardLineDataRowIdCounter, awardLineDataLineNumberCounter, specialConditionDataRowIdCounter,
                                budgetDataRowIdCounter, jobRunDateString, maskSensitiveData);
        
                createAndStoreAwardFileSubmitAwardTabRowBo(headerBo, subAccountAwardLineSubAccountBo, 
                        subAccountSpecialConditionBo, subAccountBudgetBo, subAccountAllocationBo);
            }
        }
        return numAwardFileLinesGeneratedForThisAward;
    }
    
    
    private CemiAwardLegacyNovelutionBo obtainAssociatedNovelutionData(final String awardProposalNumber) {
        String spreadsheetKeyToSearchFor = 
                MessageFormat.format(CemiAwardConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);
        
        CemiAwardLegacyNovelutionBo novelutionDataRow = 
                getAwardNovelutionAttributesForCemiAwardExtractBySpreadsheetKey(spreadsheetKeyToSearchFor);
        
        return novelutionDataRow;
    }
    
    private void createAndStoreAwardFileSubmitAwardTabRowBo(
            final CemiAwardHeaderDataBo headerBo, final CemiAwardLineDataBo awardLineBo,
            final CemiAwardSpecialConditionDataBo specialConditionBo, final CemiAwardBudgetDataBo budgetBo,
            final CemiAwardAllocationDataBo allocationBo) {
        final CemiAwardFileSubmitAwardTabRowBo tabRowBo = CemiAwardFileSubmitAwardTabRowBoFactory.createTabRowBoFrom(headerBo, awardLineBo, specialConditionBo, budgetBo, allocationBo);
        storeSheetRow(tabRowBo);
    }
    
    private void populateAllAwardTranslateTableMaps() {
        allAwardTranslateTableMaps = new CemiAwardTranslateTableMaps();
        
        allAwardTranslateTableMaps.setSponsorAwardTypesMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.SPONSOR_AWARD_TYPES_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardPurposeMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_PURPOSE_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardLineLifecycleStatusMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_LINE_LIFECYCLE_STATUS_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardLineTypesMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_LINE_TYPES_QUERY, cemiAwardExtractDao));
    }
    
    private CemiAwardLegacyNovelutionBo getAwardNovelutionAttributesForCemiAwardExtractBySpreadsheetKey(String spreadsheetKeyToSearchFor) {
        return businessObjectService.findBySinglePrimaryKey(CemiAwardLegacyNovelutionBo.class, spreadsheetKeyToSearchFor);
        
    }
}
