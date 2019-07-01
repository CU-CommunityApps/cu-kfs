package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardFundManager;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.service.AwardService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.module.cg.CuCGPropertyConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassParameterConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.util.RassUtil;
import edu.cornell.kfs.sys.CUKFSConstants;

public class AwardTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAwardEntry, Award> {

    private AwardService awardService;
    private DateTimeService dateTimeService;
    private ParameterService parameterService;

    public void setAwardService(AwardService awardService) {
        this.awardService = awardService;
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
    public List<String> getKeysOfObjectUpdatesToWaitFor(RassXmlAwardEntry xmlAward) {
        List<String> objectsToWaitFor = new ArrayList<>();
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getAgencyNumber()));
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber()));
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Award.class, xmlAward.getProposalNumber()));
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
        newAward.setProposalAwardTypeCode(proposalAwardTypeCode);
        newAward.setAwardEntryDate(dateTimeService.getCurrentSqlDate());
        newAward.getAwardAccounts().add(createDefaultAwardAccount(xmlAward));
        newAward.getAwardFundManagers().add(createDefaultFundManager(xmlAward));
        updateFinalFinancialReportRequiredFlag(xmlAward, newAward);
        updateAwardLastUpdateDate(newAward);
        
        refreshReferenceObject(newAward, KFSPropertyConstants.PROPOSAL);
        refreshReferenceObject(newAward, KFSPropertyConstants.AGENCY);
    }

    protected AwardAccount createDefaultAwardAccount(RassXmlAwardEntry xmlAward) {
        Pair<String, String> defaultChartAndAccount = getChartAndAccountForDefaultAwardAccount();
        String directorPrincipalId = parameterService.getParameterValueAsString(
                RassStep.class, RassParameterConstants.DEFAULT_PROJECT_DIRECTOR);
        if (StringUtils.isBlank(directorPrincipalId)) {
            throw new RuntimeException("Default project director parameter cannot be blank");
        }
        
        AwardAccount awardAccount = new AwardAccount();
        awardAccount.setProposalNumber(xmlAward.getProposalNumber());
        awardAccount.setPrincipalId(directorPrincipalId);
        awardAccount.setChartOfAccountsCode(defaultChartAndAccount.getLeft());
        awardAccount.setAccountNumber(defaultChartAndAccount.getRight());
        return awardAccount;
    }

    protected Pair<String, String> getChartAndAccountForDefaultAwardAccount() {
        String defaultChartAndAccount = parameterService.getParameterValueAsString(
                RassStep.class, RassParameterConstants.DEFAULT_AWARD_ACCOUNT);
        if (StringUtils.countMatches(defaultChartAndAccount, CUKFSConstants.COLON) != 1) {
            throw new RuntimeException("Default chart-and-account parameter must contain one colon separator");
        }
        
        String defaultChart = StringUtils.substringBefore(defaultChartAndAccount, CUKFSConstants.COLON);
        String defaultAccount = StringUtils.substringAfter(defaultChartAndAccount, CUKFSConstants.COLON);
        if (StringUtils.isBlank(defaultChart) || StringUtils.isBlank(defaultAccount)) {
            throw new RuntimeException("Default chart-and-account parameter cannot have a blank chart or account");
        }
        return Pair.of(defaultChart, defaultAccount);
    }

    protected AwardFundManager createDefaultFundManager(RassXmlAwardEntry xmlAward) {
        String fundManagerPrincipalId = parameterService.getParameterValueAsString(
                RassStep.class, RassParameterConstants.DEFAULT_FUND_MANAGER);
        if (StringUtils.isBlank(fundManagerPrincipalId)) {
            throw new RuntimeException("Default fund manager parameter cannot be blank");
        }
        
        AwardFundManager fundManager = new AwardFundManager();
        fundManager.setProposalNumber(xmlAward.getProposalNumber());
        fundManager.setPrincipalId(fundManagerPrincipalId);
        fundManager.setPrimaryFundManagerIndicator(true);
        return fundManager;
    }

    @Override
    public void processCustomTranslationForBusinessObjectEdit(
            RassXmlAwardEntry xmlAward, Award oldAward, Award newAward) {
        refreshReferenceObject(newAward, KFSPropertyConstants.PROPOSAL);
        refreshReferenceObject(newAward, KFSPropertyConstants.AGENCY);
        refreshReferenceObject(newAward, CuCGPropertyConstants.AWARD_STATUS);
        refreshReferenceObject(newAward, CuCGPropertyConstants.AWARD_PURPOSE);
        refreshReferenceObject(newAward, CuCGPropertyConstants.GRANT_DESCRIPTION);
        refreshReferenceObject(newAward, KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY);
        updateFinalFinancialReportRequiredFlag(xmlAward, newAward);
        updateAwardLastUpdateDate(newAward);
    }

    protected void updateFinalFinancialReportRequiredFlag(RassXmlAwardEntry xmlAward, Award newAward) {
        AwardExtendedAttribute newExtension = (AwardExtendedAttribute) newAward.getExtension();
        boolean finalFinancialReportRequired = ObjectUtils.isNotNull(xmlAward.getFinalReportDueDate());
        newExtension.setFinalFinancialReportRequired(finalFinancialReportRequired);
    }

    protected void updateAwardLastUpdateDate(Award newAward) {
        newAward.setAwardLastUpdateDate(dateTimeService.getCurrentTimestamp());
    }

    @Override
    public Award findExistingObject(RassXmlAwardEntry xmlAward) {
        return awardService.getByPrimaryId(xmlAward.getProposalNumber());
    }

    @Override
    public Class<RassXmlAwardEntry> getXmlObjectClass() {
        return RassXmlAwardEntry.class;
    }

    @Override
    public Class<Award> getBusinessObjectClass() {
        return Award.class;
    }

}
