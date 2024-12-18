package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.util.Map;

import edu.cornell.kfs.tax.batch.SprintaxPaymentFileField;
import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayment1042S;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;

public class TaxFileRowWriterSprintaxPaymentsFileImpl extends TaxFileRowWriterSprintaxBase {

    public TaxFileRowWriterSprintaxPaymentsFileImpl(final String outputFileName, final String taxFileType,
            final TaxOutputDefinition taxOutputDefinition) throws IOException {
        super(outputFileName, taxFileType, taxOutputDefinition);
    }

    @Override
    public void writeDataRow(final SprintaxInfo1042S taxFileRow, final String sectionName) throws IOException {
        final SprintaxPayment1042S payment = taxFileRow.getCurrentPayment();
        final Map<String, String> dataValues = Map.ofEntries(
                buildEntry(SprintaxPaymentFileField.payeeId, taxFileRow.getPayeeId()),
                buildEntry(SprintaxPaymentFileField.uniqueFormId, payment.getUniqueFormId()),
                buildEntry(SprintaxPaymentFileField.incomeCode, payment.getIncomeCodeForOutput()),
                buildEntryForAmount(SprintaxPaymentFileField.grossAmount, payment.getGrossAmount()),
                buildEntry(SprintaxPaymentFileField.chapter3ExemptionCode, payment.getChapter3ExemptionCode()),
                buildEntryForPercent(SprintaxPaymentFileField.chapter3TaxRate, payment.getChapter3TaxRate()),
                buildEntry(SprintaxPaymentFileField.chapter4ExemptionCode, taxFileRow.getChapter4ExemptionCode()),
                buildNegatedEntryForAmount(SprintaxPaymentFileField.fedTaxWithheldAmount,
                        payment.getFederalTaxWithheldAmount()),
                buildEntry(SprintaxPaymentFileField.chapter3StatusCode, taxFileRow.getChapter3StatusCode()),
                buildEntry(SprintaxPaymentFileField.chapter4StatusCode, taxFileRow.getChapter4StatusCode()),
                buildEntry(SprintaxPaymentFileField.vendorGIIN, taxFileRow.getVendorGIIN()),
                buildEntry(SprintaxPaymentFileField.payerEIN, taxFileRow.getPayerEIN()),
                buildNegatedEntryForAmount(SprintaxPaymentFileField.stateIncomeTaxWithheldAmount,
                        payment.getStateIncomeTaxWithheldAmount())
        );
        writeCsvDataRow(dataValues, sectionName);
    }

}
