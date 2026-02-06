package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardFundManager;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.module.cg.CuCGPropertyConstants;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassParameterConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.util.RassUtil;
import edu.cornell.kfs.sys.CUKFSConstants;

public class AwardTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAwardEntry, Award> {
    private static final Logger LOG = LogManager.getLogger(AwardTranslationDefinition.class);

    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private ParameterService parameterService;

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @Override
    public String printPrimaryKeyValues(RassXmlAwardEntry xmlAward) {
        return xmlAward.getProposalNumber();
    }

    @Override
    public String printPrimaryKeyValues(Award award) {
        return award.getProposalNumber();
    }

    @Override
    public String getKeyOfPrimaryObjectUpdateToWaitFor(RassXmlAwardEntry xmlAward) {
        return RassUtil.buildClassAndKeyIdentifier(Award.class, xmlAward.getProposalNumber());
    }

    @Override
    public List<String> getKeysOfUpstreamObjectUpdatesToWaitFor(RassXmlAwardEntry xmlAward) {
        List<String> objectsToWaitFor = new ArrayList<>();
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getAgencyNumber()));
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber()));
        if (StringUtils.isNotBlank(xmlAward.getFederalPassThroughAgencyNumber())) {
            objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getFederalPassThroughAgencyNumber()));
        }
        return objectsToWaitFor;
    }

    @Override
    public void processCustomTranslationForBusinessObjectCreate(
            RassXmlAwardEntry xmlAward, Award newAward) {
        String proposalAwardTypeCode = parameterService.getParameterValueAsString(
                RassStep.class, RassConstants.RASS_DEFAULT_PROPOSAL_AWARD_TYPE_PARAMETER);
        newAward.setAwardTypeCode(proposalAwardTypeCode);
        newAward.setAwardEntryDate(dateTimeService.getCurrentSqlDate());
        newAward.getAwardAccounts().add(createDefaultAwardAccount(xmlAward));
        addPrimaryFundManager(xmlAward, newAward);
        updateAwardLastUpdateDate(newAward);
        
        refreshReferenceObject(newAward, KFSPropertyConstants.AGENCY);
    }

    protected AwardAccount createDefaultAwardAccount(RassXmlAwardEntry xmlAward) {
        Map<String, String> defaultAwardAccountAttributes = getDefaultAwardAccountAttributes();
        String directorPrincipalId = parameterService.getParameterValueAsString(RassStep.class, RassParameterConstants.DEFAULT_PROJECT_DIRECTOR);
        if (StringUtils.isBlank(directorPrincipalId)) {
            throw new RuntimeException("Default project director parameter cannot be blank");
        }
        AwardAccount awardAccount = new AwardAccount();
        awardAccount.setProposalNumber(xmlAward.getProposalNumber());
        awardAccount.setPrincipalId(directorPrincipalId);
        awardAccount.setChartOfAccountsCode(defaultAwardAccountAttributes.get(KFSPropertyConstants.CHART));
        awardAccount.setAccountNumber(defaultAwardAccountAttributes.get(KFSPropertyConstants.ACCOUNT_NUMBER));
        awardAccount.setActive(defaultAccountIndicatorIsActive(defaultAwardAccountAttributes.get(KFSPropertyConstants.ACCOUNT_ACTIVE_INDICATOR)));
        return awardAccount;
    }
    
    protected boolean defaultAccountIndicatorIsActive(String rowIndicatorValue) {
        return StringUtils.equals(rowIndicatorValue, KFSConstants.ACTIVE_INDICATOR);
    }

    protected Map<String, String> getDefaultAwardAccountAttributes() {
        String defaultAttributes = parameterService.getParameterValueAsString(RassStep.class, RassParameterConstants.DEFAULT_AWARD_ACCOUNT);
        if (StringUtils.countMatches(defaultAttributes, CUKFSConstants.COLON) != 2) {
            throw new RuntimeException("Default Award Account KFS System Parameter must contain two colon separators.");
        }
        String defaultChart = StringUtils.substringBefore(defaultAttributes, CUKFSConstants.COLON);
        defaultAttributes = StringUtils.substringAfter(defaultAttributes, CUKFSConstants.COLON);
        String defaultAccountNumber = StringUtils.substringBefore(defaultAttributes, CUKFSConstants.COLON);
        String defaultAccountActiveIndicator = StringUtils.substringAfter(defaultAttributes, CUKFSConstants.COLON);
        if (StringUtils.isBlank(defaultChart) || StringUtils.isBlank(defaultAccountNumber) || StringUtils.isBlank(defaultAccountActiveIndicator)) {
            throw new RuntimeException("Default Award Account KFS System Parameter cannot have a blank chart or account or account active indicator.");
        }
        Map<String, String> defaultValuePairs = new HashMap<String, String>();
        defaultValuePairs.put(KFSPropertyConstants.CHART, defaultChart);
        defaultValuePairs.put(KFSPropertyConstants.ACCOUNT_NUMBER, defaultAccountNumber);
        defaultValuePairs.put(KFSPropertyConstants.ACCOUNT_ACTIVE_INDICATOR, defaultAccountActiveIndicator);
        return defaultValuePairs;
    }
    
    protected void addPrimaryFundManager(RassXmlAwardEntry xmlAward, Award award) {
        if (ObjectUtils.isNull(award.getAwardPrimaryFundManager())) {
            LOG.info("addPrimaryFundManager, there is no primary fund manager, so add default fund manager.");
            String fundManagerPrincipalId = parameterService.getParameterValueAsString(
                    RassStep.class, RassParameterConstants.DEFAULT_FUND_MANAGER);
            if (StringUtils.isBlank(fundManagerPrincipalId)) {
                throw new RuntimeException("Default fund manager parameter cannot be blank");
            }
            
            AwardFundManager fundManager = new AwardFundManager();
            fundManager.setProposalNumber(xmlAward.getProposalNumber());
            fundManager.setPrincipalId(fundManagerPrincipalId);
            fundManager.setPrimaryFundManagerIndicator(true);
            award.getAwardFundManagers().add(fundManager);
        } else {
            LOG.info("addPrimaryFundManager, primary fund manager already exists, no need to add default.");
        }
    }

    @Override
    public void processCustomTranslationForBusinessObjectEdit(
            RassXmlAwardEntry xmlAward, Award oldAward, Award newAward) {
        refreshReferenceObject(newAward, KFSPropertyConstants.AGENCY);
        refreshReferenceObject(newAward, CuCGPropertyConstants.AWARD_STATUS);
        refreshReferenceObject(newAward, CuCGPropertyConstants.AWARD_PURPOSE);
        refreshReferenceObject(newAward, CuCGPropertyConstants.GRANT_DESCRIPTION);
        refreshReferenceObject(newAward, KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY);
        addPrimaryFundManager(xmlAward, newAward);
        addFillerFundManagersToOldAwardIfNecessary(oldAward, newAward);
        updateAwardLastUpdateDate(newAward);
    }

    protected void addFillerFundManagersToOldAwardIfNecessary(Award oldAward, Award newAward) {
        List<AwardFundManager> oldFundManagers = oldAward.getAwardFundManagers();
        List<AwardFundManager> newFundManagers = newAward.getAwardFundManagers();
        while (oldFundManagers.size() < newFundManagers.size()) {
            AwardFundManager fillerFundManager = new AwardFundManager();
            oldFundManagers.add(fillerFundManager);
        }
    }

    protected void updateAwardLastUpdateDate(Award newAward) {
        newAward.setAwardLastUpdateDate(dateTimeService.getCurrentTimestamp());
    }

    @Override
    public Award findExistingObject(RassXmlAwardEntry xmlAward) {
        Map<String, String> primaryKeys = new HashMap<String, String>();
        primaryKeys.put(KFSPropertyConstants.PROPOSAL_NUMBER, xmlAward.getProposalNumber());
        return businessObjectService.findByPrimaryKey(Award.class, primaryKeys);
    }

    @Override
    public Class<RassXmlAwardEntry> getXmlObjectClass() {
        return RassXmlAwardEntry.class;
    }

    @Override
    public Class<Award> getBusinessObjectClass() {
        return Award.class;
    }
    
    @Override
    public boolean businessObjectCreateIsPermitted(RassXmlAwardEntry xmlObject) {
        return xmlObject.getTotalAmount() != null && xmlObject.getCostShareRequired() != null
                && (xmlObject.getTotalAmount().isNonZero() || xmlObject.getCostShareRequired());
    }

}
