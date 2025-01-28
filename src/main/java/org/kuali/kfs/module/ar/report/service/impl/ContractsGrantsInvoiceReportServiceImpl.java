/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.ar.report.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.integration.cg.ContractAndGrantsProposal;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAgency;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArParameterConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceLookupResult;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceLookupResultAward;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsLetterOfCreditReviewDetail;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.ContractsGrantsLetterOfCreditReviewDocument;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsBillingAwardVerificationService;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.module.ar.report.service.ContractsGrantsInvoiceReportService;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.PdfFormFillerUtil;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.ChartOrgHolderImpl;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.report.ReportInfo;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.ReportGenerationService;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * CU Customization: Backported the FINP-9927 changes into this file.
 * This overlay can be removed when we upgrade to the 2023-10-11 financials patch.
 * 
 * This class implements the methods for report generation services for Contracts & Grants.
 */
public class ContractsGrantsInvoiceReportServiceImpl implements ContractsGrantsInvoiceReportService {

    private static final Logger LOG = LogManager.getLogger();
    private static final MonthDay END_OF_Q1 = MonthDay.of(Month.MARCH, 31);
    private static final MonthDay END_OF_Q2 = MonthDay.of(Month.JUNE, 30);
    private static final MonthDay END_OF_Q3 = MonthDay.of(Month.SEPTEMBER, 30);
    private static final MonthDay END_OF_Q4 = MonthDay.of(Month.DECEMBER, 31);

    protected DateTimeService dateTimeService;
    protected DataDictionaryService dataDictionaryService;
    protected PersonService personService;
    protected BusinessObjectService businessObjectService;
    protected ParameterService parameterService;
    protected ConfigurationService configService;
    protected KualiModuleService kualiModuleService;
    protected DocumentService documentService;
    protected NoteService noteService;
    protected ReportInfo reportInfo;
    protected ReportGenerationService reportGenerationService;
    protected ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService;
    protected ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;
    protected OptionsService optionsService;
    private ContractsGrantsBillingAwardVerificationService contractsGrantsBillingAwardVerificationService;
    private FinancialSystemUserService financialSystemUserService;

    @Override
    public byte[] generateLOCReviewAsPdf(final ContractsGrantsLetterOfCreditReviewDocument document) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generateLOCReviewInPdf(baos, document);
        return baos.toByteArray();
    }

    /**
     * this method generated the actual pdf for the Contracts & Grants LOC Review Document.
     *
     * @param os
     * @param locDocument
     */
    protected void generateLOCReviewInPdf(final OutputStream os, ContractsGrantsLetterOfCreditReviewDocument locDocument) {
        try {
            final Document document =
                    new Document(new Rectangle(ArConstants.LOCReviewPdf.LENGTH, ArConstants.LOCReviewPdf.WIDTH));
            PdfWriter.getInstance(document, os);
            document.open();

            final Paragraph header = new Paragraph();
            final Paragraph text = new Paragraph();
            final Paragraph title = new Paragraph();

            // Lets write the header
            header.add(new Paragraph(configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_TITLE),
                    ArConstants.PdfReportFonts.LOC_REVIEW_TITLE_FONT
            ));
            if (StringUtils.isNotEmpty(locDocument.getLetterOfCreditFundGroupCode())) {
                header.add(new Paragraph(
                        configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_HEADER_FUND_GROUP_CODE)
                        + locDocument.getLetterOfCreditFundGroupCode(),
                        ArConstants.PdfReportFonts.LOC_REVIEW_TITLE_FONT
                ));
            }
            if (StringUtils.isNotEmpty(locDocument.getLetterOfCreditFundCode())) {
                header.add(new Paragraph(configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_HEADER_FUND_CODE)
                        + locDocument.getLetterOfCreditFundCode(),
                        ArConstants.PdfReportFonts.LOC_REVIEW_TITLE_FONT
                ));
            }
            header.add(new Paragraph(KFSConstants.BLANK_SPACE));
            header.setAlignment(Element.ALIGN_CENTER);
            title.add(new Paragraph(configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_HEADER_DOCUMENT_NUMBER)
                    + locDocument.getDocumentNumber(),
                    ArConstants.PdfReportFonts.LOC_REVIEW_HEADER_FONT
            ));
            final Person person = getPersonService().getPerson(locDocument.getDocumentHeader().getInitiatorPrincipalId());
            // writing the Document details
            title.add(new Paragraph(
                    configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_HEADER_APP_DOC_STATUS)
                    + locDocument.getDocumentHeader().getApplicationDocumentStatus(),
                    ArConstants.PdfReportFonts.LOC_REVIEW_HEADER_FONT
            ));
            title.add(new Paragraph(configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_HEADER_DOCUMENT_INITIATOR)
                    + person.getName(),
                    ArConstants.PdfReportFonts.LOC_REVIEW_HEADER_FONT
            ));
            title.add(new Paragraph(
                    configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_HEADER_DOCUMENT_CREATE_DATE)
                    + getDateTimeService().toDateString(locDocument.getDocumentHeader().getWorkflowCreateDate()),
                    ArConstants.PdfReportFonts.LOC_REVIEW_HEADER_FONT
            ));

            title.add(new Paragraph(KFSConstants.BLANK_SPACE));
            title.setAlignment(Element.ALIGN_RIGHT);

            text.add(new Paragraph(configService.getPropertyValueAsString(ArKeyConstants.LOC_REVIEW_PDF_SUBHEADER_AWARDS),
                    ArConstants.PdfReportFonts.LOC_REVIEW_SMALL_BOLD
            ));
            text.add(new Paragraph(KFSConstants.BLANK_SPACE));

            document.add(header);
            document.add(title);
            document.add(text);
            final PdfPTable table = new PdfPTable(11);
            table.setTotalWidth(ArConstants.LOCReviewPdf.RESULTS_TABLE_WIDTH);
            // fix the absolute width of the table
            table.setLockedWidth(true);

            // relative col widths in proportions - 1/11
            final float[] widths = new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f};
            table.setWidths(widths);
            table.setHorizontalAlignment(0);
            addAwardHeaders(table);
            if (CollectionUtils.isNotEmpty(locDocument.getHeaderReviewDetails()) && CollectionUtils.isNotEmpty(
                    locDocument.getAccountReviewDetails())) {
                for (final ContractsGrantsLetterOfCreditReviewDetail item : locDocument.getHeaderReviewDetails()) {
                    table.addCell(item.getProposalNumber());
                    table.addCell(item.getAwardDocumentNumber());
                    table.addCell(item.getAgencyNumber());
                    table.addCell(item.getCustomerNumber());
                    table.addCell(getDateTimeService().toDateString(item.getAwardBeginningDate()));
                    table.addCell(getDateTimeService().toDateString(item.getAwardEndingDate()));
                    table.addCell(contractsGrantsBillingUtilityService.formatForCurrency(item.getAwardBudgetAmount()));
                    table.addCell(contractsGrantsBillingUtilityService.formatForCurrency(item.getLetterOfCreditAmount()));
                    table.addCell(contractsGrantsBillingUtilityService.formatForCurrency(item.getClaimOnCashBalance()));
                    table.addCell(contractsGrantsBillingUtilityService.formatForCurrency(item.getAmountToDraw()));
                    table.addCell(contractsGrantsBillingUtilityService.formatForCurrency(item.getAmountAvailableToDraw()));

                    final PdfPCell cell = new PdfPCell();
                    cell.setPadding(ArConstants.LOCReviewPdf.RESULTS_TABLE_CELL_PADDING);
                    cell.setColspan(ArConstants.LOCReviewPdf.RESULTS_TABLE_COLSPAN);
                    final PdfPTable newTable = new PdfPTable(ArConstants.LOCReviewPdf.INNER_TABLE_COLUMNS);
                    newTable.setTotalWidth(ArConstants.LOCReviewPdf.INNER_TABLE_WIDTH);
                    // fix the absolute width of the newTable
                    newTable.setLockedWidth(true);

                    // relative col widths in proportions - 1/8
                    final float[] newWidths = new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f};
                    newTable.setWidths(newWidths);
                    newTable.setHorizontalAlignment(0);
                    addAccountsHeaders(newTable);
                    for (final ContractsGrantsLetterOfCreditReviewDetail newItem : locDocument.getAccountReviewDetails()) {
                        if (item.getProposalNumber().equals(newItem.getProposalNumber())) {
                            newTable.addCell(newItem.getAccountDescription());
                            newTable.addCell(newItem.getChartOfAccountsCode());
                            newTable.addCell(newItem.getAccountNumber());
                            String accountExpirationDate = KFSConstants.EMPTY_STRING;
                            if (ObjectUtils.isNotNull(newItem.getAccountExpirationDate())) {
                                accountExpirationDate =
                                        getDateTimeService().toDateString(newItem.getAccountExpirationDate());
                            }
                            newTable.addCell(accountExpirationDate);
                            newTable.addCell(contractsGrantsBillingUtilityService.formatForCurrency(newItem.getAwardBudgetAmount()));
                            newTable.addCell(contractsGrantsBillingUtilityService.formatForCurrency(newItem.getClaimOnCashBalance()));
                            newTable.addCell(contractsGrantsBillingUtilityService.formatForCurrency(newItem.getAmountToDraw()));
                            newTable.addCell(contractsGrantsBillingUtilityService.formatForCurrency(newItem.getFundsNotDrawn()));
                        }
                    }
                    cell.addElement(newTable);
                    table.addCell(cell);

                }
                document.add(table);
            }
            document.close();
        } catch (final DocumentException e) {
            LOG.error("problem during ContractsGrantsInvoiceReportServiceImpl.generateInvoiceInPdf()", e);
        }
    }

    /**
     * This method is used to set the headers for the CG LOC review Document.
     *
     * @param table
     */
    protected void addAccountsHeaders(final PdfPTable table) {
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.ACCOUNT_DESCRIPTION
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.ACCOUNT_NUMBER
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.AWARD_BUDGET_AMOUNT
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.CLAIM_ON_CASH_BALANCE
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.AMOUNT_TO_DRAW
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.FUNDS_NOT_DRAWN
        ));
    }

    /**
     * This method is used to set the headers for the CG LOC review Document.
     *
     * @param table
     */
    protected void addAwardHeaders(final PdfPTable table) {
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.PROPOSAL_NUMBER
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.AWARD_DOCUMENT_NUMBER
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.AGENCY_NUMBER
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.CUSTOMER_NUMBER
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.AWARD_BEGINNING_DATE
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                KFSPropertyConstants.AWARD_ENDING_DATE
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.AWARD_BUDGET_AMOUNT
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.LETTER_OF_CREDIT_AMOUNT
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.CLAIM_ON_CASH_BALANCE
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.AMOUNT_TO_DRAW
        ));
        table.addCell(getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                ArPropertyConstants.FUNDS_NOT_DRAWN
        ));
    }

    @Override
    public File generateFederalFinancialForm(
            final ContractsAndGrantsBillingAward award,
            final String period,
            final String year,
            final String formType,
            final ContractsAndGrantsBillingAgency agency
    ) {
        final Map<String, String> replacementList = new HashMap<>();
        final Date runDate = new Date(new java.util.Date().getTime());
        final String reportFileName = getReportInfo().getReportFileName();
        final String reportDirectory = getReportInfo().getReportsDirectory();
        try {
            if (formType.equals(ArConstants.FEDERAL_FORM_425) && ObjectUtils.isNotNull(award)) {
                final String fullReportFileName = reportGenerationService.buildFullFileName(runDate,
                        reportDirectory,
                        reportFileName,
                        ArConstants.FEDERAL_FUND_425_REPORT_ABBREVIATION
                ) + KFSConstants.ReportGeneration.PDF_FILE_EXTENSION;
                final File file = new File(fullReportFileName);
                final FileOutputStream fos = new FileOutputStream(file);
                stampPdfFormValues425(award, period, year, fos, replacementList);
                return file;
            } else if (formType.equals(ArConstants.FEDERAL_FORM_425A) && ObjectUtils.isNotNull(agency)) {
                final String fullReportFileName = reportGenerationService.buildFullFileName(runDate,
                        reportDirectory,
                        reportFileName,
                        ArConstants.FEDERAL_FUND_425A_REPORT_ABBREVIATION
                ) + KFSConstants.ReportGeneration.PDF_FILE_EXTENSION;
                final File file = new File(fullReportFileName);
                final FileOutputStream fos = new FileOutputStream(file);
                stampPdfFormValues425A(agency, period, year, fos, replacementList);
                return file;
            }
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException("Cannot find pdf to stamp for federal financial form", ex);
        }
        return null;
    }

    /**
     * @param award
     * @return
     */
    protected KualiDecimal getCashReceipts(final ContractsAndGrantsBillingAward award) {
        KualiDecimal cashReceipt = KualiDecimal.ZERO;
        final Map<String, String> fieldValues = new HashMap<>();
        if (ObjectUtils.isNotNull(award) && ObjectUtils.isNotNull(award.getProposalNumber())) {
            fieldValues.put(ArPropertyConstants.ContractsGrantsInvoiceDocumentFields.PROPOSAL_NUMBER,
                    award.getProposalNumber()
            );
        }
        final List<ContractsGrantsInvoiceDocument> list =
                (List<ContractsGrantsInvoiceDocument>) contractsGrantsInvoiceDocumentService.retrieveAllCGInvoicesByCriteria(
                        fieldValues);
        if (!CollectionUtils.isEmpty(list)) {
            for (final ContractsGrantsInvoiceDocument invoice : list) {
                final Map<String, String> primaryKeys = new HashMap<>();
                primaryKeys.put(ArPropertyConstants.CustomerInvoiceDocumentFields.FINANCIAL_DOCUMENT_REF_INVOICE_NUMBER,
                        invoice.getDocumentNumber()
                );
                final List<InvoicePaidApplied> ipas = (List<InvoicePaidApplied>) businessObjectService.findMatching(
                        InvoicePaidApplied.class,
                        primaryKeys
                );
                if (ObjectUtils.isNotNull(ipas)) {
                    for (final InvoicePaidApplied ipa : ipas) {
                        cashReceipt = cashReceipt.add(ipa.getInvoiceItemAppliedAmount());
                    }
                }
            }
        }
        return cashReceipt;
    }

    /**
     * This method is used to populate the replacement list to replace values from pdf template to actual values for
     * Federal Form 425.
     *
     * @param award
     * @param reportingPeriod
     * @param year
     */
    protected void populateListByAward(
            final ContractsAndGrantsBillingAward award,
            final String reportingPeriod,
            final String year,
            final Map<String, String> replacementList
    ) {
        KualiDecimal cashDisbursement = KualiDecimal.ZERO;
        final SystemOptions systemOption = optionsService.getCurrentYearOptions();

        for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
            int index = 0;
            KualiDecimal baseSum = KualiDecimal.ZERO;
            KualiDecimal amountSum = KualiDecimal.ZERO;
            cashDisbursement =
                    cashDisbursement.add(contractsGrantsInvoiceDocumentService.getBudgetAndActualsForAwardAccount(
                            awardAccount,
                            systemOption.getActualFinancialBalanceTypeCd()
                    ));
            if (ObjectUtils.isNotNull(awardAccount.getAccount().getFinancialIcrSeriesIdentifier())
                && ObjectUtils.isNotNull(awardAccount.getAccount().getAcctIndirectCostRcvyTypeCd())) {
                index++;
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_TYPE + "_" + index,
                        awardAccount.getAccount().getAcctIndirectCostRcvyTypeCd()
                );
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_RATE + "_" + index,
                        awardAccount.getAccount().getFinancialIcrSeriesIdentifier()
                );
                if (ObjectUtils.isNotNull(awardAccount.getAccount().getAccountEffectiveDate())) {
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_PERIOD_FROM + "_" + index,
                            getDateTimeService().toDateString(awardAccount.getAccount().getAccountEffectiveDate())
                    );
                }
                if (ObjectUtils.isNotNull(awardAccount.getAccount().getAccountExpirationDate())) {
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_PERIOD_TO + "_" + index,
                            getDateTimeService().toDateString(awardAccount.getAccount().getAccountExpirationDate())
                    );
                }
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_BASE + "_" + index,
                        contractsGrantsBillingUtilityService.formatForCurrency(award.getAwardTotalAmount())
                );
                final Map<String, Object> key = new HashMap<>();
                key.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
                key.put(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER,
                        awardAccount.getAccount().getFinancialIcrSeriesIdentifier()
                );
                key.put(KFSPropertyConstants.ACTIVE, true);
                key.put(KFSPropertyConstants.TRANSACTION_DEBIT_INDICATOR, KFSConstants.GL_DEBIT_CODE);
                final List<IndirectCostRecoveryRateDetail> icrDetail =
                        (List<IndirectCostRecoveryRateDetail>) businessObjectService.findMatchingOrderBy(
                                IndirectCostRecoveryRateDetail.class,
                                key,
                                KFSPropertyConstants.AWARD_INDR_COST_RCVY_ENTRY_NBR,
                                false
                        );
                if (CollectionUtils.isNotEmpty(icrDetail)) {
                    final KualiDecimal rate = new KualiDecimal(icrDetail.get(0).getAwardIndrCostRcvyRatePct());
                    if (ObjectUtils.isNotNull(rate)) {
                        final KualiDecimal ONE_HUNDRED = new KualiDecimal(100);
                        final KualiDecimal indirectExpenseAmount = award.getAwardTotalAmount().multiply(rate).divide(ONE_HUNDRED);
                        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                                ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_AMOUNT + "_" + index,
                                contractsGrantsBillingUtilityService.formatForCurrency(indirectExpenseAmount)
                        );
                        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                                ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_FEDERAL + "_" + index,
                                contractsGrantsBillingUtilityService.formatForCurrency(indirectExpenseAmount)
                        );
                        amountSum = amountSum.add(indirectExpenseAmount);
                    }
                }
                baseSum = baseSum.add(award.getAwardTotalAmount());
            }
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_BASE_SUM,
                    contractsGrantsBillingUtilityService.formatForCurrency(baseSum)
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_AMOUNT_SUM,
                    contractsGrantsBillingUtilityService.formatForCurrency(amountSum)
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.INDIRECT_EXPENSE_FEDERAL_SUM,
                    contractsGrantsBillingUtilityService.formatForCurrency(amountSum)
            );
        }

        final SystemInformation sysInfo = retrieveSystemInformationForAward(award, year);
        if (ObjectUtils.isNotNull(sysInfo)) {
            final String address = concatenateAddressFromSystemInformation(sysInfo);
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.RECIPIENT_ORGANIZATION,
                    address
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ZWEI,
                    sysInfo.getUniversityFederalEmployerIdentificationNumber());

            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.STREET1, sysInfo.getOrganizationRemitToLine1StreetAddress());
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.STREET2, sysInfo.getOrganizationRemitToLine2StreetAddress());
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.CITY, sysInfo.getOrganizationRemitToCityName());
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.STATE, sysInfo.getOrganizationRemitToStateCode());
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.ZIP, sysInfo.getOrganizationRemitToZipCode());
            if (ObjectUtils.isNotNull(sysInfo.getOrganizationRemitToCounty())) {
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.COUNTY, sysInfo.getOrganizationRemitToCounty().getName());
            }
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList, ArPropertyConstants.FederalFormReportFields.UEI, sysInfo.getUniqueEntityId());
        }

        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_AGENCY,
                award.getAgency().getFullName()
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_GRANT_NUMBER,
                award.getAwardDocumentNumber()
        );
        if (CollectionUtils.isNotEmpty(award.getActiveAwardAccounts())) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.RECIPIENT_ACCOUNT_NUMBER,
                    award.getActiveAwardAccounts().get(0).getAccountNumber()
            );
        }
        if (ObjectUtils.isNotNull(award.getAwardBeginningDate())) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.GRANT_PERIOD_FROM,
                    getDateTimeService().toDateString(award.getAwardBeginningDate())
            );
        }
        if (ObjectUtils.isNotNull(award.getAwardClosingDate())) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.GRANT_PERIOD_TO,
                    getDateTimeService().toDateString(award.getAwardClosingDate())
            );
        }
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.CASH_RECEIPTS,
                contractsGrantsBillingUtilityService.formatForCurrency(getCashReceipts(award))
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.TOTAL_FEDERAL_FUNDS_AUTHORIZED,
                contractsGrantsBillingUtilityService.formatForCurrency(award.getAwardTotalAmount())
        );

        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.REPORTING_PERIOD_END_DATE,
                getReportingPeriodEndDate(reportingPeriod, year)
        );
        if (ObjectUtils.isNotNull(cashDisbursement)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.CASH_DISBURSEMENTS,
                    contractsGrantsBillingUtilityService.formatForCurrency(cashDisbursement)
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.CASH_ON_HAND,
                    contractsGrantsBillingUtilityService.formatForCurrency(getCashReceipts(award).subtract(
                            cashDisbursement))
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.FEDERAL_SHARE_OF_EXPENDITURES,
                    contractsGrantsBillingUtilityService.formatForCurrency(cashDisbursement)
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.TOTAL_FEDERAL_SHARE,
                    contractsGrantsBillingUtilityService.formatForCurrency(cashDisbursement)
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.UNOBLIGATED_BALANCE_OF_FEDERAL_FUNDS,
                    contractsGrantsBillingUtilityService.formatForCurrency(award.getAwardTotalAmount()
                            .subtract(cashDisbursement))
            );
        }
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_SHARE_OF_UNLIQUIDATED_OBLIGATION,
                contractsGrantsBillingUtilityService.formatForCurrency(KualiDecimal.ZERO)
        );

        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.TOTAL_FEDERAL_INCOME_EARNED,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.INCOME_EXPENDED_DEDUCATION_ALTERNATIVE,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.INCOME_EXPENDED_ADDITION_ALTERNATIVE,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.UNEXPECTED_PROGRAM_INCOME,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.NAME,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.TELEPHONE,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.EMAIL_ADDRESS,
                KFSConstants.EMPTY_STRING
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.DATE_REPORT_SUBMITTED,
                getDateTimeService().toDateString(getDateTimeService().getCurrentDate())
        );
        if (ArConstants.QUARTER1.equals(reportingPeriod) || ArConstants.QUARTER2.equals(reportingPeriod)
            || ArConstants.QUARTER3.equals(reportingPeriod) || ArConstants.QUARTER4.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.QUARTERLY,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.SEMI_ANNUAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.SEMI_ANNUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.ANNUAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ANNUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.FINAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.FINAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        final String accountingBasis = parameterService.getParameterValueAsString(ArConstants.AR_NAMESPACE_CODE,
                ArParameterConstants.Components.FEDERAL_FINANCIAL_REPORT,
                ArParameterConstants.ACCOUNTING_BASIS
        );
        if (ArConstants.BASIS_OF_ACCOUNTING_CASH.equals(accountingBasis)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.CASH,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.BASIS_OF_ACCOUNTING_ACCRUAL.equals(accountingBasis)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ACCRUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
    }

    /**
     * Concatenates the address from an AR System Information object into a single String
     *
     * @param sysInfo the System Information business object to concatenate the address of
     * @return the concatenated address
     */
    protected static String concatenateAddressFromSystemInformation(final SystemInformation sysInfo) {
        String address = sysInfo.getOrganizationRemitToAddressName();
        if (StringUtils.isNotBlank(sysInfo.getOrganizationRemitToLine1StreetAddress())) {
            address += ", " + sysInfo.getOrganizationRemitToLine1StreetAddress();
        }
        if (StringUtils.isNotBlank(sysInfo.getOrganizationRemitToLine2StreetAddress())) {
            address += ", " + sysInfo.getOrganizationRemitToLine2StreetAddress();
        }
        if (StringUtils.isNotBlank(sysInfo.getOrganizationRemitToCityName())) {
            address += ", " + sysInfo.getOrganizationRemitToCityName();
        }
        if (StringUtils.isNotBlank(sysInfo.getOrganizationRemitToStateCode())) {
            address += " " + sysInfo.getOrganizationRemitToStateCode();
        }
        if (StringUtils.isNotBlank(sysInfo.getOrganizationRemitToZipCode())) {
            address += "-" + sysInfo.getOrganizationRemitToZipCode();
        }
        return address;
    }

    /**
     * Retrieves an AR System Information object for an award
     *
     * @param award the award to retrieve an associated System Information for
     * @param year  the year of the System Information object to retrieve
     * @return the System Information object, or null if nothing is found
     */
    protected SystemInformation retrieveSystemInformationForAward(final ContractsAndGrantsBillingAward award, final String year) {
        ChartOrgHolder chartOrgHolder = financialSystemUserService.getPrimaryOrganization(
                award.getAwardPrimaryFundManager().getFundManager().getPrincipalId(),
                KFSConstants.OptionalModuleNamespaces.ACCOUNTS_RECEIVABLE);

        final List<String> processingCodes = getContractsGrantsInvoiceDocumentService().getProcessingFromBillingCodes(
                chartOrgHolder.getChartOfAccountsCode(), chartOrgHolder.getOrganizationCode());
        if (!CollectionUtils.isEmpty(processingCodes) && processingCodes.size() > 1) {
            chartOrgHolder = new ChartOrgHolderImpl(processingCodes.get(0), processingCodes.get(1));
        }

        final Map<String, String> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        primaryKeys.put(KFSPropertyConstants.PROCESSING_CHART_OF_ACCT_CD,
                chartOrgHolder.getChartOfAccountsCode()
        );
        primaryKeys.put(KFSPropertyConstants.PROCESSING_ORGANIZATION_CODE,
                chartOrgHolder.getOrganizationCode()
        );
        return businessObjectService.findByPrimaryKey(SystemInformation.class, primaryKeys);
    }

    /**
     * This method is used to populate the replacement list to replace values from pdf template to actual values for
     * Federal Form 425A.
     *
     * @param awards
     * @param reportingPeriod
     * @param year
     * @param agency
     * @return total amount
     */
    protected List<KualiDecimal> populateListByAgency(
            final List<ContractsAndGrantsBillingAward> awards,
            final String reportingPeriod,
            final String year,
            final ContractsAndGrantsBillingAgency agency
    ) {
        final Map<String, String> replacementList = new HashMap<>();
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.REPORTING_PERIOD_END_DATE,
                getReportingPeriodEndDate(reportingPeriod, year)
        );
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.FEDERAL_AGENCY,
                agency.getFullName()
        );
        final SystemOptions systemOption = optionsService.getCurrentYearOptions();

        final Map<String, String> primaryKeys = new HashMap<>();
        primaryKeys.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
        if (CollectionUtils.isNotEmpty(awards)) {
            primaryKeys.put(KFSPropertyConstants.PROCESSING_CHART_OF_ACCT_CD,
                    awards.get(0).getPrimaryAwardOrganization().getChartOfAccountsCode()
            );
            primaryKeys.put(KFSPropertyConstants.PROCESSING_ORGANIZATION_CODE,
                    awards.get(0).getPrimaryAwardOrganization().getOrganizationCode()
            );
        }

        final SystemInformation sysInfo = businessObjectService.findByPrimaryKey(SystemInformation.class, primaryKeys);

        if (ObjectUtils.isNotNull(sysInfo)) {
            final String address = concatenateAddressFromSystemInformation(sysInfo);

            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.RECIPIENT_ORGANIZATION,
                    address
            );
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ZWEI,
                    sysInfo.getUniversityFederalEmployerIdentificationNumber()
            );
        }

        if (ArConstants.QUARTER1.equals(reportingPeriod) || ArConstants.QUARTER2.equals(reportingPeriod)
            || ArConstants.QUARTER3.equals(reportingPeriod) || ArConstants.QUARTER4.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.QUARTERLY,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.SEMI_ANNUAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.SEMI_ANNUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.ANNUAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ANNUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.FINAL.equals(reportingPeriod)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.FINAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        final String accountingBasis = parameterService.getParameterValueAsString(ArConstants.AR_NAMESPACE_CODE,
                ArParameterConstants.Components.FEDERAL_FINANCIAL_REPORT,
                ArParameterConstants.ACCOUNTING_BASIS
        );
        if (ArConstants.BASIS_OF_ACCOUNTING_CASH.equals(accountingBasis)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.CASH,
                    KFSConstants.OptionLabels.YES
            );
        }
        if (ArConstants.BASIS_OF_ACCOUNTING_ACCRUAL.equals(accountingBasis)) {
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.ACCRUAL,
                    KFSConstants.OptionLabels.YES
            );
        }
        contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                ArPropertyConstants.FederalFormReportFields.DATE_REPORT_SUBMITTED,
                getDateTimeService().toDateString(new Date(new java.util.Date().getTime()))
        );
        KualiDecimal totalCashControl = KualiDecimal.ZERO;
        KualiDecimal totalCashDisbursement = KualiDecimal.ZERO;
        for (int i = 0; i < 30; i++) {
            if (i < awards.size()) {
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.FEDERAL_GRANT_NUMBER + " " + (i + 1),
                        awards.get(i).getAwardDocumentNumber()
                );
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.RECIPIENT_ACCOUNT_NUMBER + " " + (i + 1),
                        awards.get(i).getActiveAwardAccounts().get(0).getAccountNumber()
                );
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.FEDERAL_CASH_DISBURSEMENT + " " + (i + 1),
                        contractsGrantsBillingUtilityService.formatForCurrency(getCashReceipts(awards.get(i)))
                );
                totalCashControl = totalCashControl.add(getCashReceipts(awards.get(i)));

                for (final ContractsAndGrantsBillingAwardAccount awardAccount : awards.get(i).getActiveAwardAccounts()) {
                    totalCashDisbursement =
                            totalCashDisbursement.add(contractsGrantsInvoiceDocumentService.getBudgetAndActualsForAwardAccount(
                                    awardAccount,
                                    systemOption.getActualFinancialBalanceTypeCd()
                            ));
                }
            }
        }
        final ArrayList<KualiDecimal> list = new ArrayList<>();
        list.add(totalCashControl);
        list.add(totalCashDisbursement);
        return list;
    }

    /**
     * @param reportingPeriod
     * @param year
     * @return the last day of the given reporting period.
     */
    protected String getReportingPeriodEndDate(final String reportingPeriod, final String year) {
        final int yearAsInt = Integer.parseInt(year);
        final LocalDate endDate;
        if (ArConstants.QUARTER1.equals(reportingPeriod)) {
            endDate = END_OF_Q1.atYear(yearAsInt);
        } else if (ArConstants.QUARTER2.equals(reportingPeriod) || ArConstants.SEMI_ANNUAL.equals(reportingPeriod)) {
            endDate = END_OF_Q2.atYear(yearAsInt);
        } else if (ArConstants.QUARTER3.equals(reportingPeriod)) {
            endDate = END_OF_Q3.atYear(yearAsInt);
        } else {
            endDate = END_OF_Q4.atYear(yearAsInt);
        }
        return getDateTimeService().toDateString(dateTimeService.getUtilDate(endDate));
    }

    /**
     * Use iText {@link PdfStamper} to stamp information into field values on a PDF Form Template.
     *
     * @param award           The award the values will be pulled from.
     * @param reportingPeriod
     * @param year
     * @param returnStream    The output stream the federal form will be written to.
     */
    protected void stampPdfFormValues425(
            final ContractsAndGrantsBillingAward award,
            final String reportingPeriod,
            final String year,
            final OutputStream returnStream,
            final Map<String, String> replacementList
    ) {
        try {
            populateListByAward(award, reportingPeriod, year, replacementList);
            final ClassPathResource template = new ClassPathResource(ArConstants.FF_425_TEMPLATE_NM);
            final byte[] pdfBytes = PdfFormFillerUtil.populateTemplate(template.getInputStream(), replacementList);
            returnStream.write(pdfBytes);
        } catch (final IOException ex) {
            throw new RuntimeException("Troubles stamping the old 425!", ex);
        }
    }

    /**
     * Use iText {@link PdfStamper} to stamp information into field values on a PDF Form Template.
     *
     * @param agency          The award the values will be pulled from.
     * @param reportingPeriod
     * @param year
     * @param returnStream    The output stream the federal form will be written to.
     */
    protected void stampPdfFormValues425A(
            final ContractsAndGrantsBillingAgency agency,
            final String reportingPeriod,
            final String year,
            final OutputStream returnStream,
            final Map<String, String> replacementList
    ) {
        try {
            final ClassPathResource federal425ATemplate = new ClassPathResource(ArConstants.FF_425A_TEMPLATE_NM);
            final ClassPathResource federal425Template = new ClassPathResource(ArConstants.FF_425_TEMPLATE_NM);

            final Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put(KFSPropertyConstants.AGENCY_NUMBER, agency.getAgencyNumber());
            fieldValues.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);
            final List<ContractsAndGrantsBillingAward> awards =
                    kualiModuleService.getResponsibleModuleService(ContractsAndGrantsBillingAward.class)
                            .getExternalizableBusinessObjectsList(ContractsAndGrantsBillingAward.class, fieldValues);
            Integer pageNumber = 1;
            final Integer totalPages;
            totalPages = awards.size() / ArConstants.Federal425APdf.NUMBER_OF_SUMMARIES_PER_PAGE + 1;
            final PdfCopyFields copy = new PdfCopyFields(returnStream);

            // generate replacement list for FF425
            populateListByAgency(awards, reportingPeriod, year, agency);
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.TOTAL_PAGES,
                    Objects.toString(totalPages + 1)
            );
            KualiDecimal sumCashControl = KualiDecimal.ZERO;
            KualiDecimal sumCumExp = KualiDecimal.ZERO;
            while (pageNumber <= totalPages) {
                final List<ContractsAndGrantsBillingAward> awardsList = new ArrayList<>();
                for (int i = (pageNumber - 1) * ArConstants.Federal425APdf.NUMBER_OF_SUMMARIES_PER_PAGE;
                        i < pageNumber * ArConstants.Federal425APdf.NUMBER_OF_SUMMARIES_PER_PAGE; i++) {
                    if (i < awards.size()) {
                        awardsList.add(awards.get(i));
                    }
                }
                // generate replacement list for FF425
                final List<KualiDecimal> list = populateListByAgency(awardsList, reportingPeriod, year, agency);
                if (CollectionUtils.isNotEmpty(list)) {
                    sumCashControl = sumCashControl.add(list.get(0));
                    if (list.size() > 1) {
                        sumCumExp = sumCumExp.add(list.get(1));
                    }
                }

                // populate form with document values
                contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                        ArPropertyConstants.FederalFormReportFields.PAGE_NUMBER,
                        Objects.toString(pageNumber + 1)
                );
                if (pageNumber == totalPages) {
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.TOTAL,
                            contractsGrantsBillingUtilityService.formatForCurrency(sumCashControl)
                    );
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.CASH_RECEIPTS,
                            contractsGrantsBillingUtilityService.formatForCurrency(sumCashControl)
                    );
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.CASH_DISBURSEMENTS,
                            contractsGrantsBillingUtilityService.formatForCurrency(sumCumExp)
                    );
                    contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                            ArPropertyConstants.FederalFormReportFields.CASH_ON_HAND,
                            contractsGrantsBillingUtilityService.formatForCurrency(sumCashControl.subtract(sumCumExp))
                    );
                }
                copy.addDocument(new PdfReader(renameFieldsIn(federal425ATemplate.getInputStream(), replacementList)));
                pageNumber++;
            }
            contractsGrantsBillingUtilityService.putValueOrEmptyString(replacementList,
                    ArPropertyConstants.FederalFormReportFields.PAGE_NUMBER,
                    "1"
            );

            // add the FF425 form.
            copy.addDocument(new PdfReader(renameFieldsIn(federal425Template.getInputStream(), replacementList)));
            copy.close();
        } catch (DocumentException | IOException ex) {
            throw new RuntimeException("Tried to stamp the 425A, but couldn't do it.  Just...just couldn't do it.", ex);
        }
    }

    /**
     * @param template the path to the original form
     * @param list     the replacement list
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    protected static byte[] renameFieldsIn(final InputStream template, final Map<String, String> list)
            throws IOException, DocumentException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PdfStamper stamper = new PdfStamper(new PdfReader(template), baos);
        final AcroFields fields = stamper.getAcroFields();
        for (final String field : list.keySet()) {
            fields.setField(field, list.get(field));
        }
        stamper.close();
        return baos.toByteArray();
    }

    @Override
    public byte[] combineInvoicePdfs(final Collection<ContractsGrantsInvoiceDocument> list)
            throws DocumentException, IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generateCombinedPdfForInvoices(list, baos);
        return baos.toByteArray();
    }

    @Override
    public byte[] combineInvoicePdfEnvelopes(final Collection<ContractsGrantsInvoiceDocument> list)
            throws DocumentException, IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generateCombinedPdfForEnvelopes(list, baos);
        return baos.toByteArray();
    }

    /**
     * Generates the pdf file for printing the invoices.
     *
     * @param list
     * @param outputStream
     * @throws DocumentException
     * @throws IOException
     */
    protected void generateCombinedPdfForInvoices(
            final Collection<ContractsGrantsInvoiceDocument> list, final OutputStream outputStream
    ) throws DocumentException, IOException {
        final PdfCopyFields copy = new PdfCopyFields(outputStream);
        boolean pageAdded = false;
        for (final ContractsGrantsInvoiceDocument invoice : list) {
            // add a document
            final List<InvoiceAddressDetail> invoiceAddressDetails = invoice.getInvoiceAddressDetails();

            for (final InvoiceAddressDetail invoiceAddressDetail : invoiceAddressDetails) {
                if (ArConstants.InvoiceTransmissionMethod.MAIL.equals(invoiceAddressDetail.getInvoiceTransmissionMethodCode())) {
                    final Note note = noteService.getNoteByNoteId(invoice.getInvoiceGeneralDetail().getInvoiceNoteId());
                    Integer numberOfCopiesToPrint =
                            invoiceAddressDetail.getCustomerAddress().getCustomerCopiesToPrint();
                    if (ObjectUtils.isNull(numberOfCopiesToPrint)) {
                        numberOfCopiesToPrint = 1;
                    }

                    if (ObjectUtils.isNotNull(note)) {
                        for (int i = 0; i < numberOfCopiesToPrint; i++) {
                            if (!pageAdded) {
                                copy.open();
                            }
                            pageAdded = true;
                            try (InputStream attachmentContents = note.getAttachment().getAttachmentContents()) {
                                copy.addDocument(new PdfReader(attachmentContents));
                            }
                        }
                    }
                    invoiceAddressDetail.markSent();
                    contractsGrantsInvoiceDocumentService.addInvoiceTransmissionNote(invoice,
                            invoiceAddressDetail.getInvoiceTransmissionMethodCode()
                    );
                }
            }
            documentService.updateDocument(invoice);
        }
        if (pageAdded) {
            copy.close();
        }
    }

    /**
     * Generates the pdf file for printing the envelopes.
     *
     * @param list
     * @param outputStream
     * @throws DocumentException
     * @throws IOException
     */
    protected static void generateCombinedPdfForEnvelopes(
            final Collection<ContractsGrantsInvoiceDocument> list, final OutputStream outputStream
    ) throws DocumentException, IOException {
        final Document document = new Document(new Rectangle(ArConstants.InvoiceEnvelopePdf.LENGTH,
                ArConstants.InvoiceEnvelopePdf.WIDTH
        ));
        PdfWriter.getInstance(document, outputStream);
        boolean pageAdded = false;

        for (final ContractsGrantsInvoiceDocument invoice : list) {
            // add a document
            for (final InvoiceAddressDetail invoiceAddressDetail : invoice.getInvoiceAddressDetails()) {
                if (ArConstants.InvoiceTransmissionMethod.MAIL.equals(invoiceAddressDetail.getInvoiceTransmissionMethodCode())) {
                    final CustomerAddress address = invoiceAddressDetail.getCustomerAddress();

                    Integer numberOfEnvelopesToPrint = address.getCustomerEnvelopesToPrintQuantity();
                    if (ObjectUtils.isNull(numberOfEnvelopesToPrint)) {
                        numberOfEnvelopesToPrint = 1;
                    }
                    for (int i = 0; i < numberOfEnvelopesToPrint; i++) {
                        // if a page has not already been added then open the document.
                        if (!pageAdded) {
                            document.open();
                        }
                        pageAdded = true;
                        document.newPage();
                        // adding the sent From address
                        final Organization org = invoice.getInvoiceGeneralDetail()
                                .getAward()
                                .getPrimaryAwardOrganization()
                                .getOrganization();
                        final Paragraph sentBy = generateAddressParagraph(org.getOrganizationName(),
                                org.getOrganizationLine1Address(),
                                org.getOrganizationLine2Address(),
                                org.getOrganizationCityName(),
                                org.getOrganizationStateCode(),
                                org.getOrganizationZipCode(),
                                ArConstants.PdfReportFonts.ENVELOPE_SMALL_FONT
                        );
                        sentBy.setIndentationLeft(ArConstants.InvoiceEnvelopePdf.INDENTATION_LEFT);
                        sentBy.setAlignment(Element.ALIGN_LEFT);

                        // adding the send To address
                        final Paragraph sendTo = generateAddressParagraph(address.getCustomerAddressName(),
                                address.getCustomerLine1StreetAddress(),
                                address.getCustomerLine2StreetAddress(),
                                address.getCustomerCityName(),
                                address.getCustomerStateCode(),
                                address.getCustomerZipCode(),
                                ArConstants.PdfReportFonts.ENVELOPE_TITLE_FONT
                        );
                        sendTo.setAlignment(Element.ALIGN_CENTER);
                        sendTo.add(new Paragraph(KFSConstants.BLANK_SPACE));

                        document.add(sentBy);
                        document.add(sendTo);
                    }
                }
            }
        }
        if (pageAdded) {
            document.close();
        }
    }

    /**
     * Generates a PDF paragraph for a given Address
     *
     * @param name         the name that this envelope is being sent to
     * @param line1Address the first line of the address
     * @param line2Address the second line of the address
     * @param cityName     the name of the city to send this to
     * @param stateCode    the code of the state or presumably province to send this to
     * @param postalCode   the postal code/zip code to send the envelope to
     * @param font         the font to write in
     * @return a PDF Paragraph for the address
     */
    protected static Paragraph generateAddressParagraph(
            final String name,
            final String line1Address,
            final String line2Address,
            final String cityName,
            final String stateCode,
            final String postalCode,
            final Font font
    ) {
        final Paragraph addressParagraph = new Paragraph();
        addressParagraph.add(new Paragraph(name, font));
        if (StringUtils.isNotBlank(line1Address)) {
            addressParagraph.add(new Paragraph(line1Address, font));
        }
        if (StringUtils.isNotBlank(line2Address)) {
            addressParagraph.add(new Paragraph(line2Address, font));
        }
        String string = "";
        if (StringUtils.isNotBlank(cityName)) {
            string += cityName;
        }
        if (StringUtils.isNotBlank(stateCode)) {
            string += ", " + stateCode;
        }
        if (StringUtils.isNotBlank(postalCode)) {
            string += "-" + postalCode;
        }
        if (StringUtils.isNotBlank(string)) {
            addressParagraph.add(new Paragraph(string, font));
        }
        return addressParagraph;
    }

    @Override
    public byte[] convertLetterOfCreditReviewToCSV(final ContractsGrantsLetterOfCreditReviewDocument LOCDocument) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
        ) {
            csvWriter.writeNext(new String[]{getDataDictionaryService().getAttributeLabel(
                    ContractsGrantsLetterOfCreditReviewDetail.class,
                    KFSPropertyConstants.PROPOSAL_NUMBER
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    KFSPropertyConstants.AWARD_DOCUMENT_NUMBER
            ), getDataDictionaryService().getAttributeLabel(ContractAndGrantsProposal.class,
                    ArPropertyConstants.ContractsAndGrantsBillingAwardFields.GRANT_NUMBER
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    KFSPropertyConstants.ACCOUNT_DESCRIPTION
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    KFSPropertyConstants.ACCOUNT_NUMBER
            ), getDataDictionaryService().getAttributeLabel(Account.class, KFSPropertyConstants.ACCOUNT_NAME),
                    getDataDictionaryService().getAttributeLabel(Account.class,
                            KFSPropertyConstants.ACCOUNT_FISCAL_OFFICER_USER + "." + KIMPropertyConstants.Person.NAME
                    ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    KFSPropertyConstants.ACCOUNT_EXPIRATION_DATE
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    ArPropertyConstants.AWARD_BUDGET_AMOUNT
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    ArPropertyConstants.CLAIM_ON_CASH_BALANCE
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    ArPropertyConstants.AMOUNT_TO_DRAW
            ), getDataDictionaryService().getAttributeLabel(ContractsGrantsLetterOfCreditReviewDetail.class,
                    ArPropertyConstants.FUNDS_NOT_DRAWN
            )});

            if (CollectionUtils.isNotEmpty(LOCDocument.getHeaderReviewDetails()) && CollectionUtils.isNotEmpty(
                    LOCDocument.getAccountReviewDetails())) {
                for (final ContractsGrantsLetterOfCreditReviewDetail item : LOCDocument.getHeaderReviewDetails()) {
                    final String proposalNumber = Objects.toString(item.getProposalNumber());
                    final String awardDocumentNumber = Objects.toString(item.getAwardDocumentNumber());

                    for (final ContractsGrantsLetterOfCreditReviewDetail newItem : LOCDocument.getAccountReviewDetails()) {
                        final String accountExpirationDate = ObjectUtils.isNull(newItem.getAccountExpirationDate())
                                ? KFSConstants.EMPTY_STRING
                                : getDateTimeService().toDateString(newItem.getAccountExpirationDate());
                        if (Objects.equals(item.getProposalNumber(), newItem.getProposalNumber())) {
                            newItem.refreshReferenceObject("account");
                            newItem.refreshReferenceObject("award");

                            csvWriter.writeNext(new String[]{proposalNumber, awardDocumentNumber,
                                    newItem.getAward().getProposal().getGrantNumber(), newItem.getAccountDescription(),
                                    newItem.getChartOfAccountsCode(), newItem.getAccountNumber(),
                                    newItem.getAccount().getAccountName(),
                                    newItem.getAccount().getAccountFiscalOfficerUser().getName(), accountExpirationDate,
                                    contractsGrantsBillingUtilityService.formatForCurrency(newItem.getAwardBudgetAmount()),
                                    contractsGrantsBillingUtilityService.formatForCurrency(newItem.getClaimOnCashBalance()),
                                    contractsGrantsBillingUtilityService.formatForCurrency(newItem.getAmountToDraw()),
                                    contractsGrantsBillingUtilityService.formatForCurrency(newItem.getFundsNotDrawn())});
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(
                    "problem during ContractsGrantsInvoiceReportServiceImpl.generateCSVToExport()",
                    e
            );
        }
        return baos.toByteArray();
    }

    @Override
    public Collection<ContractsGrantsInvoiceLookupResult> getPopulatedContractsGrantsInvoiceLookupResults(
            final Collection<ContractsAndGrantsBillingAward> awards
    ) {
        final Collection<ContractsGrantsInvoiceLookupResult> populatedContractsGrantsInvoiceLookupResults = new ArrayList<>();

        if (awards.size() == 0) {
            return populatedContractsGrantsInvoiceLookupResults;
        }

        final Map<String, List<ContractsAndGrantsBillingAward>> awardsByAgency = getAwardByAgency(awards);
        for (final List<ContractsAndGrantsBillingAward> contractsAndGrantsBillingAwards : awardsByAgency.values()) {
            if (CollectionUtils.isNotEmpty(contractsAndGrantsBillingAwards)) {
                final ContractsGrantsInvoiceLookupResult contractsGrantsInvoiceLookupResult =
                        new ContractsGrantsInvoiceLookupResult();

                final ContractsAndGrantsBillingAgency agency = contractsAndGrantsBillingAwards.get(0).getAgency();
                populateAgencyFields(contractsGrantsInvoiceLookupResult, agency);

                contractsGrantsInvoiceLookupResult.setAwards(contractsAndGrantsBillingAwards);

                final Collection<ContractsGrantsInvoiceLookupResultAward> contractsGrantsInvoiceLookupResultAwards =
                        new ArrayList<>();

                for (final ContractsAndGrantsBillingAward award : contractsAndGrantsBillingAwards) {
                    for (final ContractsAndGrantsBillingAwardAccount awardAccount : award.getActiveAwardAccounts()) {
                        if (contractsGrantsBillingAwardVerificationService.isAwardAccountValidToInvoiceBasedOnSchedule(
                                awardAccount)) {
                            contractsGrantsInvoiceLookupResultAwards.add(new ContractsGrantsInvoiceLookupResultAward(award,
                                    awardAccount
                            ));
                        }
                    }
                }

                contractsGrantsInvoiceLookupResult.setLookupResultAwards(contractsGrantsInvoiceLookupResultAwards);
                populatedContractsGrantsInvoiceLookupResults.add(contractsGrantsInvoiceLookupResult);
            }
        }

        return populatedContractsGrantsInvoiceLookupResults;
    }

    private static void populateAgencyFields(
            final ContractsGrantsInvoiceLookupResult contractsGrantsInvoiceLookupResult,
            final ContractsAndGrantsBillingAgency agency
    ) {
        if (ObjectUtils.isNotNull(agency)) {
            contractsGrantsInvoiceLookupResult.setAgencyNumber(agency.getAgencyNumber());
            contractsGrantsInvoiceLookupResult.setAgencyReportingName(agency.getReportingName());
            contractsGrantsInvoiceLookupResult.setAgencyFullName(agency.getFullName());
            contractsGrantsInvoiceLookupResult.setCustomerNumber(agency.getCustomerNumber());
        }
    }

    @Override
    public Collection<ContractsGrantsInvoiceLookupResult> getPopulatedContractsGrantsInvoiceLookupResultsFromAwardAccounts(
            final Collection<ContractsAndGrantsBillingAwardAccount> awardAccounts
    ) {
        final Collection<ContractsGrantsInvoiceLookupResult> populatedContractsGrantsInvoiceLookupResults = new ArrayList<>();

        if (awardAccounts.size() == 0) {
            return populatedContractsGrantsInvoiceLookupResults;
        }

        final ContractsGrantsInvoiceLookupResult contractsGrantsInvoiceLookupResult =
                new ContractsGrantsInvoiceLookupResult();

        final Set<ContractsAndGrantsBillingAward> contractsAndGrantsBillingAwards = new HashSet<>();
        final Collection<ContractsGrantsInvoiceLookupResultAward> contractsGrantsInvoiceLookupResultAwards =
                new ArrayList<>();

        for (final ContractsAndGrantsBillingAwardAccount awardAccount : awardAccounts) {
            final ContractsAndGrantsBillingAward award = awardAccount.getAward();
            if (StringUtils.isBlank(contractsGrantsInvoiceLookupResult.getAgencyNumber())) {
                populateAgencyFields(contractsGrantsInvoiceLookupResult, award.getAgency());
            }

            final ContractsGrantsInvoiceLookupResultAward contractsGrantsInvoiceLookupResultAward =
                    new ContractsGrantsInvoiceLookupResultAward(award, awardAccount);

            contractsGrantsInvoiceLookupResultAwards.add(contractsGrantsInvoiceLookupResultAward);
            contractsAndGrantsBillingAwards.add(award);
        }

        contractsGrantsInvoiceLookupResult.setLookupResultAwards(contractsGrantsInvoiceLookupResultAwards);
        contractsGrantsInvoiceLookupResult.setAwards(contractsAndGrantsBillingAwards);
        populatedContractsGrantsInvoiceLookupResults.add(contractsGrantsInvoiceLookupResult);

        return populatedContractsGrantsInvoiceLookupResults;
    }

    /**
     * @param awards
     * @return a Map of the given CGB Awards, keyed by the agency number
     */
    protected static Map<String, List<ContractsAndGrantsBillingAward>> getAwardByAgency(
            final Collection<ContractsAndGrantsBillingAward> awards
    ) {
        // use a map to sort awards by agency
        final Map<String, List<ContractsAndGrantsBillingAward>> awardsByAgency = new HashMap<>();
        for (final ContractsAndGrantsBillingAward award : awards) {
            // To display awards only if their Billing frequency is not LOC Billing
            if (StringUtils.isNotBlank(award.getBillingFrequencyCode())
                && !ArConstants.BillingFrequencyValues.isLetterOfCredit(award)) {
                final String agencyNumber = award.getAgencyNumber();
                if (awardsByAgency.containsKey(agencyNumber)) {
                    awardsByAgency.get(agencyNumber).add(award);
                } else {
                    final List<ContractsAndGrantsBillingAward> awardsByAgencyNumber = new ArrayList<>();
                    awardsByAgencyNumber.add(award);
                    awardsByAgency.put(agencyNumber, awardsByAgencyNumber);
                }
            }
        }

        return awardsByAgency;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setConfigService(final ConfigurationService configService) {
        this.configService = configService;
    }

    public void setKualiModuleService(final KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public NoteService getNoteService() {
        return noteService;
    }

    public void setNoteService(final NoteService noteService) {
        this.noteService = noteService;
    }

    public ReportInfo getReportInfo() {
        return reportInfo;
    }

    public void setReportInfo(final ReportInfo reportInfo) {
        this.reportInfo = reportInfo;
    }

    public ReportGenerationService getReportGenerationService() {
        return reportGenerationService;
    }

    public void setReportGenerationService(final ReportGenerationService reportGenerationService) {
        this.reportGenerationService = reportGenerationService;
    }

    public ContractsGrantsInvoiceDocumentService getContractsGrantsInvoiceDocumentService() {
        return contractsGrantsInvoiceDocumentService;
    }

    public void setContractsGrantsInvoiceDocumentService(
            final ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService
    ) {
        this.contractsGrantsInvoiceDocumentService = contractsGrantsInvoiceDocumentService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public ContractsGrantsBillingUtilityService getContractsGrantsBillingUtilityService() {
        return contractsGrantsBillingUtilityService;
    }

    public void setContractsGrantsBillingUtilityService(
            final ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService
    ) {
        this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
    }

    public OptionsService getOptionsService() {
        return optionsService;
    }

    public void setOptionsService(final OptionsService optionsService) {
        this.optionsService = optionsService;
    }

    public void setContractsGrantsBillingAwardVerificationService(
            final ContractsGrantsBillingAwardVerificationService contractsGrantsBillingAwardVerificationService
    ) {
        this.contractsGrantsBillingAwardVerificationService = contractsGrantsBillingAwardVerificationService;
    }

    public FinancialSystemUserService getFinancialSystemUserService() {
        return financialSystemUserService;
    }

    public void setFinancialSystemUserService(final FinancialSystemUserService financialSystemUserService) {
        this.financialSystemUserService = financialSystemUserService;
    }

}
