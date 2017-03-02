package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractValidationServiceImpl implements ConcurStandardAccountingExtractValidationService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class);

    @Override
    public void validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException {
        Integer numberOfDetailsInHeader = concurStandardAccountingExtractFile.getRecordCount();
        int actualNumberOfDetails = concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().size();
        if (numberOfDetailsInHeader.intValue() != actualNumberOfDetails) {
            String message = "The header said there were " + numberOfDetailsInHeader + " the but the actual number of details was " + actualNumberOfDetails;
            LOG.debug("validateDetailCount, " + message);
            throw new ValidationException(message);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("validateDetailCount, Number of detail lines is what we expected.");
            }
        }
    }

    @Override
    public void validateAmounts(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException {
        KualiDecimal journalTotal = concurStandardAccountingExtractFile.getJournalAmountTotal();
        KualiDecimal detailTotal = KualiDecimal.ZERO;

        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile
                .getConcurStandardAccountingExtractDetailLines()) {
            validateDebitCreditField(line.getJounalDebitCredit());
            detailTotal = detailTotal.add(line.getJournalAmount());

        }
        if (journalTotal.doubleValue() != detailTotal.doubleValue()) {
            String message = "The journal total (" + journalTotal + ") does not equal the detail line total (" + detailTotal + ")";
            LOG.debug("validateAmounts, " + message);
            throw new ValidationException(message);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("validateAmounts, jornal total: " + journalTotal.doubleValue() + " and detailTotal: " + detailTotal.doubleValue() + " do match.");
            }
        }

    }

    @Override
    public void validateDebitCreditField(String debitCredit) throws ValidationException {
        if(!StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.CREDIT) && 
                !StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.DEBIT)) {
            LOG.debug("validateDebitCreditField, invalid debit or credit: " + debitCredit);
            throw new ValidationException(debitCredit + " is not a valid valuee for the debit or credit field.");
        } else {
            LOG.debug("validateDebitCreditField, found a valid debit/credit.");
        }
    }

    @Override
    public void validateDate(Date date) throws ValidationException {
        if (date == null) { 
            String message = "The Date must not be null.";
            LOG.debug("validateDate, " + message);
            throw new ValidationException(message);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("validateDate, found a valid date: " + date);
            }
        }
    }

}
