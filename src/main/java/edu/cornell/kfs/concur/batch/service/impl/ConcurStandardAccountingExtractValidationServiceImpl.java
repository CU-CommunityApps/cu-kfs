package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractValidationServiceImpl implements ConcurStandardAccountingExtractValidationService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class);
    
    @Override
    public boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        boolean valid = validateDetailCount(concurStandardAccountingExtractFile);
        valid = validateAmountsAndDebitCreditCode(concurStandardAccountingExtractFile) && valid;
        valid = validateDate(concurStandardAccountingExtractFile.getBatchDate()) && valid;
        if (LOG.isDebugEnabled() && valid) {
            LOG.debug("validateConcurStandardAccountExtractFile, passed file level validation, the record counts, batch date, and journal totals are all correct.");
        }
        return valid;
    }

    @Override
    public boolean validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        Integer numberOfDetailsInHeader = concurStandardAccountingExtractFile.getRecordCount();
        int actualNumberOfDetails = concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().size();
        
        boolean valid = numberOfDetailsInHeader.intValue() == actualNumberOfDetails;
        
        if (valid) {
            LOG.debug("validateDetailCount, Number of detail lines is what we expected: " + actualNumberOfDetails);
        } else {
            LOG.error("validateDetailCount, The header said there were (" + numberOfDetailsInHeader + 
                    ") detail lines expected, but the actual number of details were (" + actualNumberOfDetails + ")");
        }
        
        return valid;
    }

    @Override
    public boolean validateAmountsAndDebitCreditCode(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        KualiDecimal journalTotal = concurStandardAccountingExtractFile.getJournalAmountTotal();
        KualiDecimal detailTotal = KualiDecimal.ZERO;
        boolean debbitCreditValid = true;
        
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile
                .getConcurStandardAccountingExtractDetailLines()) {
            detailTotal = detailTotal.add(line.getJournalAmount());
            debbitCreditValid &= validateDebitCreditField(line.getJounalDebitCredit());
        }
        
        boolean journalTotalValidation = journalTotal.equals(detailTotal);
        if (journalTotalValidation) {
            LOG.debug("validateAmounts, journal total: " + journalTotal.doubleValue() + " and detailTotal: " + detailTotal.doubleValue() + " do match.");
        } else {
            LOG.error("validateAmounts, The journal total (" + journalTotal + ") does not equal the detail line total (" + detailTotal + ")");
        }
        return journalTotalValidation && debbitCreditValid;
    }

    @Override
    public boolean validateDebitCreditField(String debitCredit) {
        boolean valid = StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.CREDIT) || 
                StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.DEBIT);
        
        if (valid) {
            LOG.debug("validateDebitCreditField, found a valid debit/credit.");
        } else {
            LOG.error("validateDebitCreditField, invalid debit or credit: " + debitCredit);
        }
        return valid;
    }

    @Override
    public boolean validateDate(Date date) {
        boolean valid = date != null;
        if (valid) {
            LOG.debug("validateDate, found a valid date: " + date);
        } else {
            LOG.error("validateDate, found a a null date.");
        }
        return valid;
    }

}
