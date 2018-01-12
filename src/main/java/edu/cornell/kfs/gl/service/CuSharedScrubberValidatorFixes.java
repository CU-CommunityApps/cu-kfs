package edu.cornell.kfs.gl.service;

import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.service.ScrubberValidator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.Message;
import org.springframework.util.StringUtils;

/**
 * Helper interface for including scrubber validation fixes that need to be shared
 * between the GL and LD versions of the validators.
 */
public interface CuSharedScrubberValidatorFixes extends ScrubberValidator {

    /**
     * This is a fixed implementation of ScrubberValidatorImpl.validateSubObjectCode
     * that is based on the one from the KualiCo 01/11/2018 release.
     */
    default Message validateSubObjectCodeInternal(
            OriginEntryInformation originEntry, OriginEntryInformation workingEntry, AccountingCycleCachingService accountingCycleCachingService) {
        if (!StringUtils.hasText(originEntry.getFinancialSubObjectCode())) {
            workingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            return null;
        }

        if (!KFSConstants.getDashFinancialSubObjectCode().equals(originEntry.getFinancialSubObjectCode())) {
            SubObjectCode originEntrySubObject = accountingCycleCachingService.getSubObjectCode(
                    originEntry.getUniversityFiscalYear(), originEntry.getChartOfAccountsCode(), originEntry.getAccountNumber(),
                    originEntry.getFinancialObjectCode(), originEntry.getFinancialSubObjectCode());
            if (originEntrySubObject != null) {
                if (!originEntrySubObject.isActive()) {
                    workingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                    return null;
                }
            } else {
                workingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                return null;
            }
        }
        workingEntry.setFinancialSubObjectCode(originEntry.getFinancialSubObjectCode());
        return null;
    }

}
