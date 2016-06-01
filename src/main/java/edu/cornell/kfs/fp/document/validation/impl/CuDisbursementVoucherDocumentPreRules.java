/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.fp.document.validation.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherDocumentPreRules;
import org.kuali.kfs.fp.service.AccountingDocumentPreRuleService;
import org.kuali.kfs.pdp.PdpParameterConstants;
import org.kuali.kfs.pdp.businessobject.PaymentNoteText;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiInteger;

import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.service.impl.CuDisbursementVoucherExtractionHelperServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.document.Document;

/**
 * Checks warnings and prompt conditions for dv document.
 */
public class CuDisbursementVoucherDocumentPreRules extends DisbursementVoucherDocumentPreRules {

  /**
   * Executes pre-rules for Disbursement Voucher Document
   *
   * @param document submitted document
   * @return true if pre-rules execute successfully
   * @see org.kuali.rice.kns.rules.PromptBeforeValidationBase#doRules(org.kuali.rice.kns.document.MaintenanceDocument)
   */
  @Override
  public boolean doPrompts(Document document) {
    boolean preRulesOK = super.doPrompts(document);

    DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) document;
    checkSpecialHandlingIndicator(dvDocument);

    // Handle custom pre-rules:
    preRulesOK &= this.checkCheckStubTextOverflow(dvDocument);

    return preRulesOK;
  }

  /**
   * This method returns true if the state of all the tabs is valid, false otherwise.
   *
   * @param dvDocument submitted disbursement voucher document
   * @return Returns true if the state of all the tabs is valid, false otherwise.
   */
  @SuppressWarnings("deprecation")
  protected boolean checkWireTransferTabState(DisbursementVoucherDocument dvDocument) {
    boolean tabStatesOK = true;

    PaymentSourceWireTransfer dvWireTransfer = dvDocument.getWireTransfer();

    // if payment method is CHECK and wire tab contains data, ask user to clear tab
    if ((StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, dvDocument.getDisbVchrPaymentMethodCode()) || StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT, dvDocument.getDisbVchrPaymentMethodCode())) && hasWireTransferValues(dvWireTransfer)) {
      String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_CLEAR_UNNEEDED_WIRE_TAB);

      boolean clearTab = super.askOrAnalyzeYesNoQuestion(KFSConstants.DisbursementVoucherDocumentConstants.CLEAR_WIRE_TRANSFER_TAB_QUESTION_ID, questionText);
      if (clearTab) {
        // NOTE: Can't replace with new instance because Foreign Draft uses same object
        clearWireTransferValues(dvWireTransfer);
      } else {
        // return to document if the user doesn't want to clear the Wire Transfer tab
        super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
        tabStatesOK = false;
      }
    }

    return tabStatesOK;
  }

  /**
   * This method notifies the user if the Check Stub Text will break into more lines than those
   * allowed in the FastTrack file format and optionally allows the user to go back and fix the issue.
   * If the user chooses to fix the issue, it will return false, otherwise true.
   *
   * @param dvDocument submitted disbursement voucher document
   * @return Returns true if the user wishes to correct the Check Stub Text, false otherwise.
   */
  @SuppressWarnings("deprecation")
  protected boolean checkCheckStubTextOverflow(DisbursementVoucherDocument dvDocument) {
    boolean checkStubTextOK = true;
    int maxCheckStubLines = 4; // This is the number allowed in the FastTrack file format

    List<PaymentNoteText> pntList = buildNotesForCheckStubText(dvDocument.getDisbVchrCheckStubText());

    if(pntList.size() > maxCheckStubLines) {
      String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.QUESTION_OVERSIZED_CHECK_STUB_TEXT);
      boolean useOversizedCheckStubText = super.askOrAnalyzeYesNoQuestion(CuFPConstants.USE_OVERSIZED_CHECK_STUB_TEXT_QUESTION_ID, questionText);

      if(!useOversizedCheckStubText) {
        // return to document if the user doesn't want to use the oversized Check Stub Text
        super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
        checkStubTextOK = false;
      }

    }

    return checkStubTextOK;
  }

  /* This is borrowed mostly from
   * edu.cornell.kfs.document.service.impl.CuDisbursementVoucherExtractionHelperServiceImpl#buildNoteForCheckStubText
   * with the modification of removing the setting for the previous line count (as we don't have previous lines, and
   * we're really only interested in how the existing Check Stub Text lines will be broken up).
   */
  private List<PaymentNoteText> buildNotesForCheckStubText(String text) {
    PaymentNoteText pnt = null;
    List<PaymentNoteText> pnts = new ArrayList<PaymentNoteText>();
    String maxNoteLinesParam = SpringContext.getBean(ParameterService.class)
        .getParameterValueAsString(KfsParameterConstants.PRE_DISBURSEMENT_ALL.class,
            PdpParameterConstants.MAX_NOTE_LINES);
    int previousLineCount = 0;
    int maxNoteLines;
    try {
      maxNoteLines = Integer.parseInt(maxNoteLinesParam);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Invalid Max Notes Lines parameter, value: "+maxNoteLinesParam+" cannot be converted to an integer");
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
      if (previousLineCount < (maxNoteLines - 3) && !StringUtils.isEmpty(noteLine)) {

        // This should only happen if we encounter a word that is greater than the max length.
        // The only concern I have for this occurring is with URLs/email addresses.
        if (noteLine.length() > CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE) {
          for (String choppedWord : chopWord(noteLine, CuDisbursementVoucherConstants.DV_EXTRACT_MAX_NOTE_LINE_SIZE)) {

            // Make sure we're still under the maximum number of note lines.
            if (previousLineCount < (maxNoteLines - 3) && !StringUtils.isEmpty(choppedWord)) {
              pnt = new PaymentNoteText();
              pnt.setCustomerNoteLineNbr(new KualiInteger(previousLineCount++));
              pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER + choppedWord.replaceAll("\\n", "").trim());
            }
            // We can't add any additional note lines, or we'll exceed the maximum, therefore
            // just break out of the loop early - there's nothing left to do.
            else {
              break;
            }
          }
        }
        // This should be the most common case.  Simply create a new PaymentNoteText,
        // add the line at the correct line location.
        else {
          pnt = new PaymentNoteText();
          pnt.setCustomerNoteLineNbr(new KualiInteger(previousLineCount++));
          pnt.setCustomerNoteText(CuDisbursementVoucherConstants.DV_EXTRACT_TYPED_NOTE_PREFIX_IDENTIFIER + noteLine.replaceAll("\\n", "").trim());
        }

        if(pnt != null) {
          pnts.add(pnt); // This should never be null at this point, but...
        }
      }
    }
    return pnts;
  }

  /**
   * This method will take a word and simply chop into smaller
   * text segments that satisfy the limit requirements.  All words
   * brute force chopped, with no regard to preserving whole words.
   *
   * For example:
   *
   *      "Java is a fun programming language!"
   *
   * Might be chopped into:
   *
   *      "Java is a fun prog"
   *      "ramming language!"
   *
   * @param word The word that needs chopping
   * @param limit Number of character that should represent a chopped word
   * @return String [] of chopped words
   */
  private String[] chopWord(String word, int limit) {
    StringBuilder builder = new StringBuilder();
    if (word != null && word.trim().length() > 0) {

      char[] chars = word.toCharArray();
      int index = 0;

      // First process all the words that fit into the limit.
      for (int i = 0; i < chars.length/limit; i++) {
        builder.append(String.copyValueOf(chars, index, limit));
        builder.append("\n");

        index += limit;
      }

      // Not all words will fit perfectly into the limit amount, so
      // calculate the modulus value to determine any remaining characters.
      int modValue =  chars.length%limit;
      if (modValue > 0) {
        builder.append(String.copyValueOf(chars, index, modValue));
      }

    }

    // Split the chopped words into individual segments.
    return builder.toString().split("\\n");
  }

}
