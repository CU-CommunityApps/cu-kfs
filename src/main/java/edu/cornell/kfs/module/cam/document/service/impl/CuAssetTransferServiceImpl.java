package edu.cornell.kfs.module.cam.document.service.impl;

import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlpeSourceDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.document.AssetTransferDocument;
import org.kuali.kfs.module.cam.document.service.impl.AssetTransferServiceImpl;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.cam.CuCamsConstants;

public class CuAssetTransferServiceImpl extends AssetTransferServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetTransferServiceImpl.class);

    protected ParameterService parameterService;

    /**
     * Overridden to forcibly set the sub-account number on the generated postable object to null, if the account number
     * is flagged as being one that should not have a sub-account number defined on related source details.
     * The "ASSET_TRANSFER_PLANT_ACCOUNTS_FOR_SUB_ACCOUNTS" parameter governs whether such clearing behavior should be applied;
     * see shouldPreserveSubAccount() for details.
     * 
     * @see org.kuali.kfs.module.cam.document.service.impl.AssetTransferServiceImpl#createAssetGlpePostable(
     * org.kuali.kfs.module.cam.document.AssetTransferDocument, org.kuali.kfs.coa.businessobject.Account,
     * org.kuali.kfs.module.cam.businessobject.AssetPayment, boolean, org.kuali.kfs.module.cam.document.service.impl.AssetTransferServiceImpl.AmountCategory)
     */
    @Override
    protected AssetGlpeSourceDetail createAssetGlpePostable(
            AssetTransferDocument document, Account plantAccount, AssetPayment assetPayment, boolean isSource, AmountCategory amountCategory) {
        AssetGlpeSourceDetail postable = super.createAssetGlpePostable(document, plantAccount, assetPayment, isSource, amountCategory);
        clearSubAccountIfNecessary(postable);
        return postable;
    }

    /**
     * Clears out the sub-account number on the source detail if the account number matches a particular pattern.
     * In the event that a badly-formatted pattern causes an exception while matching, the source detail will be left as-is.
     * 
     * This was separated into its own method for unit testing convenience. 
     * 
     * @param postable The GLPE source detail whose sub-account number may need to be cleared based on account number.
     */
    protected void clearSubAccountIfNecessary(AssetGlpeSourceDetail postable) {
        try {
            if (shouldClearSubAccount(postable)) {
                postable.setSubAccountNumber(null);
            }
        } catch (PatternSyntaxException e) {
            LOG.warn("Invalid account number pattern found in parameter, this will cause the GLPE Source Detail's sub-account number to remain as-is", e);
        }
    }

    /**
     * Determines whether the sub-account number on a GLPE source detail should be cleared out,
     * based upon the configuration of the "ASSET_TRANSFER_PLANT_ACCOUNTS_FOR_SUB_ACCOUNTS" parameter.
     * The parameter contains a list of patterns that will be matched against the detail's account number,
     * and its ALLOW/DISALLOW operator controls whether the patterns represent a whitelist or blacklist.
     * If the patterns do not match for whitelist mode or if a pattern does match for blacklist mode,
     * then the sub-account should be cleared out.
     * 
     * @param postable The GLPE source detail whose account number should be checked.
     * @return true if the parameter is non-null and its ALLOW/DISALLOW operator doesn't line up with the match/no-match account check result, false otherwise.
     * @throws PatternSyntaxException if the parameter exists and has a non-empty value but contains one or more invalid search expressions.
     */
    protected boolean shouldClearSubAccount(AssetGlpeSourceDetail postable) {
        Parameter accountPatternsParameter = parameterService.getParameter(
                Asset.class, CuCamsConstants.Parameters.ASSET_TRANSFER_PLANT_ACCOUNTS_FOR_SUB_ACCOUNTS);
        if (ObjectUtils.isNull(accountPatternsParameter)) {
            return false;
        }
        boolean isWhitelist = EvaluationOperator.ALLOW.equals(accountPatternsParameter.getEvaluationOperator());
        boolean foundMatch = accountMatchesAtLeastOnePattern(
                postable.getAccountNumber(), StringUtils.split(accountPatternsParameter.getValue(), ';'));
        return isWhitelist != foundMatch;
    }

    /**
     * Determines whether the given account number matches any of the given patterns.
     * The pattern strings will have the "^" and "$" boundaries added automatically,
     * and any "*" wildcards will be converted into ".*".
     * 
     * @param accountNumber The account number to check.
     * @param patterns The patterns to match against; may contain "*" wildcards to be converted into the ".*" pattern.
     * @return true if at least one pattern is specified and the account number matches at least one pattern, false otherwise.
     * @throws PatternSyntaxException if any of the checked patterns is not a valid regex even after the automatic boundary and wildcard setup.
     */
    protected boolean accountMatchesAtLeastOnePattern(String accountNumber, String... patterns) {
        if (patterns != null && patterns.length > 0) {
            for (String pattern : patterns) {
                String adjustedPattern = "^" + pattern.replace("*", ".*") + "$";
                if (accountNumber.matches(adjustedPattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
