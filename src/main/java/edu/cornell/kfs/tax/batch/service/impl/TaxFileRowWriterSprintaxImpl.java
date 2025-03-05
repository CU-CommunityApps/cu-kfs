package edu.cornell.kfs.tax.batch.service.impl;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.BiFunction;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dto.SprintaxRowData;
import edu.cornell.kfs.tax.batch.dto.SprintaxRowData.SprintaxField;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.util.TaxUtils;

public class TaxFileRowWriterSprintaxImpl extends TaxFileRowWriterCsvBase<SprintaxRowData> {

    private final DecimalFormat amountFormat;
    private final DecimalFormat percentFormat;
    private final boolean maskSensitiveData;

    public TaxFileRowWriterSprintaxImpl(final TaxOutputDefinitionV2 taxOutputDefinition,
            final WrappedCsvWriter wrappedCsvWriter, final boolean maskSensitiveData) {
        super(taxOutputDefinition, SprintaxField.class, wrappedCsvWriter);
        this.amountFormat = TaxUtils.buildDefaultAmountFormatForFileOutput();
        this.percentFormat = TaxUtils.buildDefaultPercentFormatForSprintaxFileOutput();
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    protected Map<TaxDtoFieldEnum, BiFunction<String, Object, String>> buildFormattersMap() {
        return Map.ofEntries(
                Map.entry(SprintaxField.formattedSSNValue, this::maskTaxIdIfNecessary),
                Map.entry(SprintaxField.formattedITINValue, this::maskTaxIdIfNecessary),
                Map.entry(SprintaxField.payment_grossAmount, this::formatAmount),
                Map.entry(SprintaxField.payment_chapter3TaxRate, this::formatPercent),
                Map.entry(SprintaxField.payment_federalTaxWithheldAmount, this::formatNegatedAmount),
                Map.entry(SprintaxField.payment_stateIncomeTaxWithheldAmount, this::formatNegatedAmount)
        );
    }

    private String maskTaxIdIfNecessary(final String sectionName, final Object value) {
        return maskSensitiveData ? CUTaxConstants.MASKED_VALUE_11_CHARS : performDefaultFormatting(sectionName, value);
    }

    private String formatAmount(final String sectionName, final Object value) {
        return value != null ? amountFormat.format((KualiDecimal) value) : KFSConstants.EMPTY_STRING;
    }

    private String formatNegatedAmount(final String sectionName, final Object value) {
        return value != null ? amountFormat.format(((KualiDecimal) value).negated()) : KFSConstants.EMPTY_STRING;
    }

    private String formatPercent(final String sectionName, final Object value) {
        return value != null ? percentFormat.format((KualiDecimal) value) : KFSConstants.EMPTY_STRING;
    }

}
