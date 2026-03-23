/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.fp.document.JournalVoucherDocument;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ====
 * CU Customization: Allow for invoking the "changeBalanceType" methodToCall from a browser onchange event.
 * ====
 * 
 * This class is the Struts specific form object that works in conjunction with the pojo utilities to build the UI for
 * the Journal Voucher Document. This class is unique in that it leverages a helper data structure called the
 * VoucherAccountingLineHelper because the Journal Voucher, under certain conditions, presents the user with a debit
 * and credit column for amount entry. In addition, this form class must keep track of the changes between the old
 * and new balance type selection so that the corresponding action class and make decisions based upon the
 * differences. New accounting lines use specific credit and debit amount fields b/c the new line is explicitly known;
 * however, already existing accounting lines need to exist within a list with ordering that matches the accounting
 * lines source list.
 */
public class JournalVoucherForm extends VoucherForm {

    protected List balanceTypes;
    protected String originalBalanceType;
    protected BalanceType selectedBalanceType;

    public JournalVoucherForm() {
        super();
        selectedBalanceType = new BalanceType(KFSConstants.BALANCE_TYPE_ACTUAL);
        originalBalanceType = "";
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "JV";
    }

    /**
     * Overrides the parent to call super.populate and then to call the two methods that are specific to loading the
     * two select lists on the page. In addition, this also makes sure that the credit and debit amounts are filled in
     * for situations where validation errors occur and the page reposts.
     */
    @Override
    public void populate(final HttpServletRequest request) {
        super.populate(request);
        populateBalanceTypeListForRendering();
    }

    /**
     * Override the parent, to push the chosen accounting period and balance type down into the source accounting
     * line object. In addition, check the balance type to see if it's the "External Encumbrance" balance and alter
     * the encumbrance update code on the accounting line appropriately.
     */
    @Override
    public void populateSourceAccountingLine(
            final SourceAccountingLine sourceLine, final String accountingLinePropertyName,
            final Map parameterMap) {
        super.populateSourceAccountingLine(sourceLine, accountingLinePropertyName, parameterMap);
        populateSourceAccountingLineEncumbranceCode(sourceLine);
    }

    /**
     * Sets the encumbrance code of the line based on the balance type.
     *
     * @param sourceLine line to set code on
     */
    protected void populateSourceAccountingLineEncumbranceCode(final SourceAccountingLine sourceLine) {
        BalanceType selectedBalanceType = getSelectedBalanceType();
        if (ObjectUtils.isNotNull(selectedBalanceType)) {
            selectedBalanceType.refresh();
            sourceLine.setBalanceTyp(selectedBalanceType);
            sourceLine.setBalanceTypeCode(selectedBalanceType.getCode());
        } else {
            // it's the first time in, the form will be empty the first time in set up default selection value
            selectedBalanceType = getPopulatedBalanceTypeInstance(KFSConstants.BALANCE_TYPE_ACTUAL);
            setSelectedBalanceType(selectedBalanceType);
            setOriginalBalanceType(selectedBalanceType.getCode());

            sourceLine.setEncumbranceUpdateCode(null);
        }
    }

    // CU Customization: Allow for invoking the "changeBalanceType" methodToCall from a browser onchange event.
    @Override
    public boolean shouldMethodToCallParameterBeUsed(
            final String methodToCallParameterName,
            final String methodToCallParameterValue, final HttpServletRequest request) {
        if (StringUtils.isNotBlank(originalBalanceType)
                && StringUtils.startsWith(methodToCallParameterName, KRADConstants.DISPATCH_REQUEST_PARAMETER)) {
            final String changeBalanceTypeMethodToCall = StringUtils.join(KRADConstants.DISPATCH_REQUEST_PARAMETER
                    + KFSConstants.DELIMITER + KFSConstants.CHANGE_JOURNAL_VOUCHER_BALANCE_TYPE_METHOD);
            if (StringUtils.equals(methodToCallParameterName, changeBalanceTypeMethodToCall)
                    && StringUtils.equals(methodToCallParameterValue, changeBalanceTypeMethodToCall)) {
                return true;
            }
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName, methodToCallParameterValue, request);
    }
    // End CU Customization

    public List getBalanceTypes() {
        return balanceTypes;
    }

    public BalanceType getSelectedBalanceType() {
        return selectedBalanceType;
    }

    public void setSelectedBalanceType(final BalanceType selectedBalanceType) {
        this.selectedBalanceType = selectedBalanceType;
    }

    public void setBalanceTypes(final List balanceTypes) {
        this.balanceTypes = balanceTypes;
    }

    public JournalVoucherDocument getJournalVoucherDocument() {
        return (JournalVoucherDocument) getTransactionalDocument();
    }

    public void setJournalVoucherDocument(final JournalVoucherDocument journalVoucherDocument) {
        setDocument(journalVoucherDocument);
    }

    public String getOriginalBalanceType() {
        return originalBalanceType;
    }

    public void setOriginalBalanceType(final String changedBalanceType) {
        originalBalanceType = changedBalanceType;
    }

    /**
     * Using the selected accounting period to determine university fiscal year and look up all the encumbrance
     * balance type - check if the selected balance type is for encumbrance
     *
     * @return true/false  true if it is an encumbrance balance type
     */
    public boolean getIsEncumbranceBalanceType() {
        //get encumbrance balance type list
        final BalanceTypeService balanceTypeService = SpringContext.getBean(BalanceTypeService.class);
        final List<String> encumbranceBalanceTypes = balanceTypeService.getEncumbranceBalanceTypes(getSelectedPostingYear());
        return encumbranceBalanceTypes.contains(selectedBalanceType.getCode());
    }

    /**
     * This method retrieves all of the balance types in the system and prepares them to be rendered in a dropdown UI
     * component.
     */
    protected void populateBalanceTypeListForRendering() {
        // grab the list of valid balance types
        final ArrayList balanceTypes = SpringContext.getBean(BalanceTypeService.class).getAllBalanceTypes()
                .stream()
                .filter(balanceType -> balanceType.isActive())
                .collect(Collectors.toCollection(ArrayList::new));
        // set into the form for rendering
        setBalanceTypes(balanceTypes);

        String selectedBalanceTypeCode = getSelectedBalanceType().getCode();
        if (StringUtils.isBlank(selectedBalanceTypeCode)) {
            selectedBalanceTypeCode = KFSConstants.BALANCE_TYPE_ACTUAL;
        }

        setSelectedBalanceType(getPopulatedBalanceTypeInstance(selectedBalanceTypeCode));
        getJournalVoucherDocument().setBalanceTypeCode(selectedBalanceTypeCode);
    }

    /**
     * This method will fully populate a balance type given the passed in code, by calling the business object
     * service that retrieves the rest of the instances' information.
     *
     * @param balanceTypeCode
     * @return BalanceTyp
     */
    protected BalanceType getPopulatedBalanceTypeInstance(final String balanceTypeCode) {
        // now we have to get the code and the name of the original and new balance types
        final BalanceTypeService bts = SpringContext.getBean(BalanceTypeService.class);

        final BalanceType balanceType = bts.getBalanceTypeByCode(balanceTypeCode);
        balanceType.setCode(balanceTypeCode);
        return balanceType;
    }

    /**
     * If the balance type is an offset generation balance type, then the user is able to enter the amount as either
     * a debit or a credit, otherwise, they only need to deal with the amount field in this case we always need to
     * update the underlying bo so that the debit/credit code along with the amount, is properly set.
     */
    @Override
    protected void populateCreditAndDebitAmounts() {
        if (isSelectedBalanceTypeFinancialOffsetGenerationIndicator()) {
            super.populateCreditAndDebitAmounts();
        }
    }

    /**
     * This is a convenience helper method that is used several times throughout this action class to determine if
     * the selected balance type contained within the form instance is a financial offset generation balance type or
     * not.
     *
     * @return boolean True if it is an offset generation balance type, false otherwise.
     */
    protected boolean isSelectedBalanceTypeFinancialOffsetGenerationIndicator() {
        return getPopulatedBalanceTypeInstance(getSelectedBalanceType().getCode()).isFinancialOffsetGenerationIndicator();
    }
}
