/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.ar.document.service;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceObjectCode;
import org.kuali.kfs.module.ar.businessobject.CostCategory;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceBill;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.InvoiceMilestone;
import org.kuali.kfs.module.ar.businessobject.InvoiceTemplate;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.validation.SuspensionCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class defines all the service methods for Contracts & Grants invoice Document.
 */
//CU customization: backport FINP-8642, this file cab be removed when we upgrade to the 07/21/2022 version of financials
public interface ContractsGrantsInvoiceDocumentService {

    /**
     * This method creates Source Accounting lines enabling the creation of GLPEs in the document.
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice document
     * @param awardAccounts                  award accounts to populate as accounting lines
     */
    void createSourceAccountingLines(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument,
            List<ContractsAndGrantsBillingAwardAccount> awardAccounts);

    /**
     * Finds CGB Inv Object code
     *
     * There are 3 levels that the CGB Inv Object code can be defined at:
     * First the method looks for the CGB Inv Object code defined for subFundGroup.subFundGroupCode - subFundGroup level
     * If not found it looks for one defined for subFundGroup.subFundGroupTypeCode - subFundGroupType level
     * If not found it looks for one defined for subFundGroup.fundGroup.fundGroupCode - fundGroup level
     *
     * @param subFundGroup used to find ContractsGrantsInvoiceObjectCode
     * @param chartOfAccountsCode used to find ContractsGrantsInvoiceObjectCode
     * @return ContractsGrantsInvoiceObjectCode corresponding to the subFundGroup
     */
    ContractsGrantsInvoiceObjectCode contractGrantsInvoiceObjectCodeForSubFundGroup(SubFundGroup subFundGroup,
            String chartOfAccountsCode);

    /**
     * Determines the total amount for the given invoice document, based on billing frequency of the award
     *
     * @param invoice the contracts & grants invoice to find a total amount for
     * @return the total amount of the invoice document
     */
    KualiDecimal getTotalAmountForInvoice(ContractsGrantsInvoiceDocument invoice);

    /**
     * Calculates the total of bill amounts if the given CINV doc is a pre-determined billing kind of CINV doc
     *
     * @param contractsGrantsInvoiceDocument the document to total bills on
     * @return the total from the bills, or 0 if no bills exist or the CINV is not a pre-determined billing kind of CINV
     */
    KualiDecimal getBillAmountTotal(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Calculates the total of milestones on the given CINV, assuming the CINV is a milestone kind of CINV
     *
     * @param contractsGrantsInvoiceDocument the document to find total milestones on
     * @return the total of the milestones, or 0 if no milestones exist or if the CINV is not a milestone schedule CINV
     */
    KualiDecimal getInvoiceMilestoneTotal(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * This method recalculates the new total billed amount on the Contracts & Grants Invoice document.
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice document
     */
    void recalculateTotalAmountBilledToDate(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Recalculates the Total Expenditures for the Invoice due to reaching limit of the total award.
     *
     * @param contractsGrantsInvoiceDocument
     */
    void prorateBill(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Add the Total Billed amount from each invoiceDetailAccountObjectCodes to the corresponding Award Account Object Code.
     *
     * @param invoiceDetailAccountObjectCodes List account object codes to process
     */
    void addToAccountObjectCodeBilledTotal(List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes);

    /**
     * @param proposalNumber
     * @param chartOfAccountsCode
     * @param accountNumber
     * @return the billed to date amount for the given Proposal Number, Chart and Account for Milestones.
     */
    KualiDecimal getMilestonesBilledToDateAmount(String proposalNumber, String chartOfAccountsCode,
            String accountNumber);

    /**
     * @param proposalNumber
     * @param chartOfAccountsCode
     * @param accountNumber
     * @return the billed to date amount for the given Proposal Number, Chart and Account for Predetermined Billing.
     */
    KualiDecimal getPredeterminedBillingBilledToDateAmount(String proposalNumber, String chartOfAccountsCode,
            String accountNumber);

    /**
     * Returns the total amount billed to date for an Award.
     *
     * @param proposalNumber for the award
     * @return billed to date amount for the award with the passed in proposalNumber
     */
    KualiDecimal getAwardBilledToDateAmount(String proposalNumber);

    /**
     * Returns the total amount billed to date for an Award for all invoice documents except the one matching the
     * documentNumber parameter.
     *
     * @param proposalNumber for the award
     * @param documentNumber invoice document to exclude from the billed to date amount
     * @return billed to date amount for the award with the passed in proposalNumber for all invoice documents except
     * the one matching the documentNumber parameter.
     */
    KualiDecimal getAwardBilledToDateAmountExcludingDocument(String proposalNumber, String documentNumber);

    /**
     * @param fieldValues
     * @return CG invoice documents that match the given field values
     */
    Collection<ContractsGrantsInvoiceDocument> retrieveAllCGInvoicesByCriteria(Map fieldValues);

    /**
     * @param balance the balance to calculate the cumulative amount for
     * @return the cumulative amount for the balance
     */
    KualiDecimal calculateCumulativeBalanceAmount(Balance balance);

    /**
     * Determines if today, the document creation date, occurs within the first fiscal period
     *
     * @return true if it is the first fiscal period, false otherwise
     */
    boolean isFirstFiscalPeriod();

    /**
     * @return true if period 13 should be included in Period 01 calculations for invoice details and invoice account
     * details, false otherwise
     */
    boolean includePeriod13InPeriod01Calculations();

    /**
     * This method serves as a create and update. When it is first called, the List&lt;InvoiceSuspensionCategory&gt; is
     * empty. This list then gets populated with invoiceSuspensionCategories where the test fails. Each time the
     * document goes through validation, and this method gets called, it will update the list by adding or removing the
     * suspension categories
     *
     * @param contractsGrantsInvoiceDocument
     */
    void updateSuspensionCategoriesOnDocument(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * This method calculates and returns the total payments applied to date for an award.
     *
     * @param award used to calculate total payments
     * @return total payments to date for the award
     */
    KualiDecimal calculateTotalPaymentsToDateByAward(ContractsAndGrantsBillingAward award);

    /**
     * This method calculates the Budget and cumulative amount for Award Account
     *
     * @param awardAccount
     * @param balanceTypeCode
     * @return
     */
    KualiDecimal getBudgetAndActualsForAwardAccount(ContractsAndGrantsBillingAwardAccount awardAccount,
            String balanceTypeCode);

    /**
     * Get award accounts's control accounts
     *
     * @param award
     * @return
     */
    List<Account> getContractControlAccounts(ContractsAndGrantsBillingAward award);

    /**
     * To retrieve processing chart code and org code from the billing chart code and org code
     *
     * @param coaCode
     * @param orgCode
     * @return list of processing codes
     */
    List<String> getProcessingFromBillingCodes(String coaCode, String orgCode);

    /**
     * Determine if the collectorPrincipalId can view the invoice, leverages role qualifiers
     * on the CGB Collector role to perform the check.
     *
     * @param invoice              The invoice to check if the collector can view.
     * @param collectorPrincipalId The principal id of the collector to check permissions for.
     * @return Returns true if the collector can view the invoice, false otherwise.
     */
    boolean canViewInvoice(ContractsGrantsInvoiceDocument invoice, String collectorPrincipalId);

    /**
     * This method sets the last billed date to Award and Award Account objects based on the status of the invoice.
     * If this is the final invoice, also sets Final Billed indicator on Award Account
     *
     * @param document ContractGrantsInvoiceDocument referencing the Award and Award Account objects to update
     */
    void updateLastBilledDate(ContractsGrantsInvoiceDocument document);

    /**
     * This method updates the Bills and Milestone objects billed Field.
     *
     * @param billed
     * @param invoiceMilestones
     * @param invoiceBills
     */
    void updateBillsAndMilestones(boolean billed, List<InvoiceMilestone> invoiceMilestones, List<InvoiceBill> invoiceBills);

    /**
     * This method generates the attached invoices for the invoice addresses in the Contracts & Grants Invoice Document.
     *
     * @param document
     */
    void generateInvoicesForInvoiceAddresses(ContractsGrantsInvoiceDocument document);

    /**
     * This method updates AwardAccounts
     *
     * @param accountDetails
     * @param proposalNumber
     */
    void updateUnfinalizationToAwardAccount(List<InvoiceAccountDetail> accountDetails, String proposalNumber);

    /**
     * Corrects the Contracts & Grants Invoice Document.
     *
     * @param document
     */
    void correctContractsGrantsInvoiceDocument(ContractsGrantsInvoiceDocument document);

    /**
     * Determines if a Contracts & Grants cost category contains a given object code
     *
     * @param category            the cost category which may contain an object code
     * @param chartOfAccountsCode the chart of the object code to check
     * @param objectCode          the object code to check
     * @return true if the cost category contains the given object code, false otherwise
     */
    boolean doesCostCategoryContainObjectCode(CostCategory category, String chartOfAccountsCode, String objectCode);

    /**
     * Calculate the lastBilledDate for the Award based on it's AwardAccounts
     *
     * @param award ContractsAndGrantsBillingAward to calculate lastBilledDate for
     * @return the lastBilledDate
     */
    java.sql.Date getLastBilledDate(ContractsAndGrantsBillingAward award);

    /**
     * This method checks the Contract Control account set for Award Account based on award's invoicing option.
     *
     * @param award
     * @return errorString
     */
    List<String> checkAwardContractControlAccounts(ContractsAndGrantsBillingAward award);

    /**
     * Determines if the given invoice template can be utilized by the given CGB Invoice Document based on
     * a comparison of the billing chart/org of the invoiceTemplate to the billing chart/org of the invoice doc.
     *
     * @param invoiceTemplate                the invoice template to check
     * @param contractsGrantsInvoiceDocument the invoice document to check against
     * @return true if the document can utilize the template, false otherwise
     */
    boolean isTemplateValidForContractsGrantsInvoiceDocument(InvoiceTemplate invoiceTemplate,
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Determines whether the given ContractsGrantsInvoiceDocument is "effective" or not: if it is disapproved, cancelled,
     * or error corrected then it is NOT effective, and in all other cases, it is effective
     *
     * @param documentNumber the invoice document to check
     * @return true if the document is "effective" given the rules above, false otherwise
     */
    boolean isInvoiceDocumentEffective(String documentNumber);

    /**
     * Update the billed indicator on a List of given Invoice Bills
     *
     * @param billed       the value for the billed indicator
     * @param invoiceBills the bills to update
     */
    void updateBillsBilledIndicator(boolean billed, List<InvoiceBill> invoiceBills);

    /**
     * Update the billed indicator on a List of given Milestones
     *
     * @param billed            the value for the billed indicator
     * @param invoiceMilestones the invoice milestones to update
     */
    void updateMilestonesBilledIndicator(boolean billed, List<InvoiceMilestone> invoiceMilestones);

    /**
     * This helper method returns a map of a list of invoices mapped by the proposal number of the invoice
     *
     * @param invoices The list of invoices for which filtering to be done by proposal number
     * @return Returns the map of invoices based on key of proposal number.
     */
    Map<String, List<ContractsGrantsInvoiceDocument>> getInvoicesByAward(Collection<ContractsGrantsInvoiceDocument> invoices);

    /**
     * Recalculates the totals - based on the invoice detail account object codes which have categories - for all
     * accounting lines on the given ContractsGrantsInvoiceDocument.
     *
     * @param contractsGrantsInvoiceDocument a CGB Invoice with accounting lines to recalculate
     */
    void recalculateSourceAccountingLineTotals(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Calculate amounts previously billed for prior MS or PDBS invoices in case the billing frequency of the award
     * changed from MS/PDBS to cost reimbursable before the current invoice was generated. These amounts are displayed
     * in the Invoice Details tab of the document.
     *
     * @param contractsGrantsInvoiceDocument a CBG invoice to calculate previously billed amounts for
     */
    void calculatePreviouslyBilledAmounts(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Calculate and return the total billed amount from any other invoices with the same award and billing period
     *
     * @param document invoice used to find other related invoices
     * @return calculated new total billed amount
     */
    KualiDecimal getOtherTotalBilledForAwardPeriod(ContractsGrantsInvoiceDocument document);

    /**
     * Determines if the given Contracts & Grants Invoice Document was (likely) created in batch (as opposed to the
     * lookup screen or the LOC)
     *
     * @param document the Contracts & Grants Invoice to test
     * @return true if the document was likely created in batch, false otherwise
     */
    boolean isDocumentBatchCreated(ContractsGrantsInvoiceDocument document);

    /**
     * Determines if the given Contracts & Grants Invoice Document passes routing validation.  Note: no error messages
     * are returned; this simply checks if any error messages are created or not for the document
     *
     * @param document the Contracts & Grants Invoice to check
     * @return true if the c&g invoice passes validation with no errors, false otherwise
     */
    boolean doesInvoicePassValidation(ContractsGrantsInvoiceDocument document);

    /**
     * Return list of Suspension Categories defined from configuration
     *
     * @return
     */
    List<SuspensionCategory> getSuspensionCategories();

    /**
     * Mark the selected address/email address as manually sent.
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice with addresses to be marked manually sent
     */
    void markManuallySent(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);

    /**
     * Queue invoice to be transmitted to selected email addresses.
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice to be queued for transmission
     */
    void queueInvoiceTransmissions(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument);
    
    /**
     * Set or Clear the Final Bill Indicator of the Contracts & Grants Invoice Document.  In addition, the final bill
     * indicator on the active Award Account from the proposal will be updated.
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice to be updated final bill
     */
    //CU customization: backport FINP-8642
    void updateFinalBillIndicator(ContractsGrantsInvoiceDocument document);


    /**
     * Add invoice transmission note
     *
     * @param contractsGrantsInvoiceDocument the Contracts & Grants Invoice to be queued for transmission
     * @param invoiceTransmissionMethod the invoice transmission used
     */
    void addInvoiceTransmissionNote(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, String invoiceTransmissionMethod);

}
