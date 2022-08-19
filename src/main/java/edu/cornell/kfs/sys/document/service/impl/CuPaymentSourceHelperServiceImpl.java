package edu.cornell.kfs.sys.document.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.sys.document.service.impl.PaymentSourceHelperServiceImpl;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;

public class CuPaymentSourceHelperServiceImpl extends PaymentSourceHelperServiceImpl {
    
    @Override
    public List<PaymentNoteText> buildNotesForCheckStubText(String text, int previousLineCount) {
        PaymentNoteText pnt = null;
        List<PaymentNoteText> pnts = new ArrayList<PaymentNoteText>();
        final String maxNoteLinesParam = parameterService.getParameterValueAsString(KfsParameterConstants.PRE_DISBURSEMENT_ALL.class, PdpParameterConstants.MAX_NOTE_LINES);

        int maxNoteLines;
        try {
            maxNoteLines = Integer.parseInt(maxNoteLinesParam);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid Max Notes Lines parameter, value: " + maxNoteLinesParam +
                    " cannot be converted to an integer");
        }

        // The WordUtils should be sufficient for the majority of cases.  This method will
        // word wrap the whole string based on the MAX_NOTE_LINE_SIZE, separating each wrapped
        // word by a newline character.  The 'wrap' method adds line feeds to the end causing
        // the character length to exceed the max length by 1, hence the need for the replace
        // method before splitting.
        String   wrappedText = WordUtils.wrap(text, CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE);
        String[] noteLines   = wrappedText.replaceAll("[\r]", "").split("\\n");

        // Loop through all the note lines.
        for (String noteLine : noteLines) {
            if (previousLineCount < maxNoteLines - 3 && StringUtils.isNotEmpty(noteLine)) {
                // This should only happen if we encounter a word that is greater than the max length.
                // The only concern I have for this occurring is with URLs/email addresses.
                if (noteLine.length() >CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE) {
                    for (String choppedWord : chopWord(noteLine, CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE)) {
                        // Make sure we're still under the maximum number of note lines.
                        if (previousLineCount < maxNoteLines - 3 && StringUtils.isNotEmpty(choppedWord)) {
                            pnt = new PaymentNoteText();
                            pnt.setCustomerNoteLineNbr(new KualiInteger(previousLineCount++));
                            pnt.setCustomerNoteText(
                                    CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER + choppedWord.replaceAll("\\n", "").trim());
                        } else {
                            // We can't add any additional note lines, or we'll exceed the maximum, therefore
                            // just break out of the loop early - there's nothing left to do.
                            break;
                        }
                    }
                } else {
                    // This should be the most common case.  Simply create a new PaymentNoteText, add the line at the
                    // correct line location.
                    pnt = new PaymentNoteText();
                    pnt.setCustomerNoteLineNbr(new KualiInteger(previousLineCount++));
                    pnt.setCustomerNoteText(
                            CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER + noteLine.replaceAll("\\n", "").trim());
                }

                if (pnt != null) {
                    pnts.add(pnt); // This should never be null at this point, but...
                }
            }
        }
        return pnts;
    }

}
