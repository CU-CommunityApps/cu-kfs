package edu.cornell.kfs.tax.batch.service.impl;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

public class TaxFileRowWriterTransactionPrinterImpl extends TaxFileRowWriterCsvBase<TransactionDetail> {

    private final Pattern whitespacePattern;
    private final boolean maskSensitiveData;

    public TaxFileRowWriterTransactionPrinterImpl(final TaxOutputDefinitionV2 taxOutputDefinition,
            final WrappedCsvWriter wrappedCsvWriter, final boolean maskSensitiveData) {
        super(taxOutputDefinition, TransactionDetailField.class, wrappedCsvWriter);
        this.whitespacePattern = Pattern.compile("\\p{Space}");
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    protected Map<TaxDtoFieldEnum, BiFunction<String, Object, String>> buildFormattersMap() {
        return Map.ofEntries(
                Map.entry(TransactionDetailField.vendorTaxNumber, this::maskTaxNumberIfNecessary),
                Map.entry(TransactionDetailField.vendorGIIN, this::maskGIINIfNecessary),
                Map.entry(TransactionDetailField.dvCheckStubText, this::formatDvCheckStubText),
                Map.entry(TransactionDetailField.netPaymentAmount, this::formatKualiDecimal),
                Map.entry(TransactionDetailField.federalIncomeTaxPercent, this::formatKualiDecimal),
                Map.entry(TransactionDetailField.vendorForeignIndicator, this::formatBoolean),
                Map.entry(TransactionDetailField.nraPaymentIndicator, this::formatBoolean),
                Map.entry(TransactionDetailField.incomeTaxTreatyExemptIndicator, this::formatBoolean),
                Map.entry(TransactionDetailField.foreignSourceIncomeIndicator, this::formatBoolean)
        );
    }

    @Override
    protected String performDefaultFormatting(final String sectionName, final Object value) {
        final String stringValue = super.performDefaultFormatting(sectionName, value);
        return stringValue.replace('\t', ' ');
    }

    private String maskTaxNumberIfNecessary(final String sectionName, final Object value) {
        return maskSensitiveData ? CUTaxConstants.MASKED_VALUE_9_CHARS : performDefaultFormatting(sectionName, value);
    }

    private String maskGIINIfNecessary(final String sectionName, final Object value) {
        return maskSensitiveData ? CUTaxConstants.MASKED_VALUE_19_CHARS : performDefaultFormatting(sectionName, value);
    }

    private String formatDvCheckStubText(final String sectionName, final Object value) {
        return value != null
                ? whitespacePattern.matcher(value.toString()).replaceAll(KFSConstants.BLANK_SPACE)
                : KFSConstants.EMPTY_STRING;
    }

    private String formatKualiDecimal(final String sectionName, final Object value) {
        return value != null
                ? ((KualiDecimal) value).bigDecimalValue().toPlainString()
                : KFSConstants.EMPTY_STRING;
    }

    private String formatBoolean(final String sectionName, final Object value) {
        if (value == null) {
            return KFSConstants.EMPTY_STRING;
        }
        return ((Boolean) value).booleanValue() ? KRADConstants.YES_INDICATOR_VALUE : KRADConstants.NO_INDICATOR_VALUE;
    }

}
