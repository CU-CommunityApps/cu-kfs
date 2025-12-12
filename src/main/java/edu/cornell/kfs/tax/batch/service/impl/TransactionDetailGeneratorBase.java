package edu.cornell.kfs.tax.batch.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.PaymentSourceConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public abstract class TransactionDetailGeneratorBase<T> {

    private static final Logger LOG = LogManager.getLogger();

    public static final int MAX_BATCH_INSERT_SIZE = 100;

    protected final TaxBatchConfig config;
    protected final TaxDtoRowMapper<T> rowMapper;
    protected final TransactionDetailBuilderHelperService helperService;
    protected final TaxStatistics statistics;
    protected final List<TransactionDetail> pendingBatchInserts;
    protected final RouteHeaderLite emptyRouteHeader;
    protected final String taxSourceType;
    protected int numTransactionDetailsInserted;

    protected TransactionDetailGeneratorBase(final TaxBatchConfig config, final TaxDtoRowMapper<T> rowMapper,
            final TransactionDetailBuilderHelperService helperService, final String taxSourceType,
            final TaxStatType... initialZeroValueStats) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(rowMapper, "rowMapper cannot be null");
        Objects.requireNonNull(helperService, "helperService cannot be null");
        Validate.notBlank(taxSourceType, "taxSourceType cannot be blank");
        Objects.requireNonNull(initialZeroValueStats, "initialZeroValueStats var-args cannot be null");
        this.config = config;
        this.rowMapper = rowMapper;
        this.helperService = helperService;
        this.taxSourceType = taxSourceType;
        this.statistics = new TaxStatistics(initialZeroValueStats);
        this.pendingBatchInserts = new ArrayList<>(MAX_BATCH_INSERT_SIZE);
        this.emptyRouteHeader = new RouteHeaderLite();
        this.numTransactionDetailsInserted = 0;
    }

    public TaxStatistics generateAndInsertTransactionDetails() throws SQLException {
        LOG.info("generateAndInsertTransactionDetails, Starting generation of {} transaction details", taxSourceType);
        while (rowMapper.moveToNextRow()) {
            final TransactionDetail detail = generateTransactionDetailFromCurrentSourceDataRow();
            pendingBatchInserts.add(detail);
            if (pendingBatchInserts.size() >= MAX_BATCH_INSERT_SIZE) {
                performBatchInserts();
            }
        }

        if (pendingBatchInserts.size() > 0) {
            performBatchInserts();
        }

        LOG.info("generateAndInsertTransactionDetails, Finished generation of {} {} transaction details",
                numTransactionDetailsInserted, taxSourceType);

        return statistics;
    }

    protected void performBatchInserts() {
        LOG.info("performBatchInserts, Preparing to insert next batch of {} {} transaction details",
                pendingBatchInserts.size(), taxSourceType);
        final List<String> documentIds = getDocumentIdsFromPendingBatchInserts();
        final List<RouteHeaderLite> routeHeaders = helperService.getBasicRouteHeaderData(documentIds);
        final Map<String, RouteHeaderLite> routeHeadersMap = routeHeaders.stream()
                .collect(Collectors.toUnmodifiableMap(RouteHeaderLite::getDocumentNumber, Function.identity()));

        final List<TransactionDetail> detailsToInsert = pendingBatchInserts.stream()
                .map(detail -> prepareTransactionDetailForInsertionIfPossible(detail, routeHeadersMap))
                .filter(ObjectUtils::isNotNull)
                .collect(Collectors.toUnmodifiableList());

        if (detailsToInsert.isEmpty()) {
            LOG.warn("performBatchInserts, None of the batch's {} {} transaction details are eligible for insertion",
                    pendingBatchInserts.size(), taxSourceType);
        } else if (detailsToInsert.size() < pendingBatchInserts.size()) {
            LOG.warn("performBatchInserts, Only {} of the batch's {} {} transaction details are eligible for insertion",
                    detailsToInsert.size(), pendingBatchInserts.size(), taxSourceType);
            helperService.insertTransactionDetails(detailsToInsert, config);
        }

        if (!detailsToInsert.isEmpty()) {
            helperService.insertTransactionDetails(detailsToInsert, config);
            numTransactionDetailsInserted += detailsToInsert.size();
            LOG.info("performBatchInserts, Successfully inserted batch of {} {} transaction details",
                    detailsToInsert.size(), taxSourceType);
        }

        pendingBatchInserts.clear();
    }

    protected List<String> getDocumentIdsFromPendingBatchInserts() {
        return pendingBatchInserts.stream()
                .map(TransactionDetail::getDocumentNumber)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    protected abstract TransactionDetail generateTransactionDetailFromCurrentSourceDataRow() throws SQLException;

    protected TransactionDetail prepareTransactionDetailForInsertionIfPossible(
            final TransactionDetail detail, final Map<String, RouteHeaderLite> routeHeaders) {
        final String documentNumber = StringUtils.defaultIfBlank(detail.getDocumentNumber(), KFSConstants.EMPTY_STRING);
        final RouteHeaderLite routeHeader = routeHeaders.get(documentNumber);
        return prepareTransactionDetailForInsertionIfPossible(detail, Optional.ofNullable(routeHeader));
    }

    protected abstract TransactionDetail prepareTransactionDetailForInsertionIfPossible(
            final TransactionDetail detail, final Optional<RouteHeaderLite> relatedRouteHeader);

    protected boolean isForeignDraftOrWireTransfer(final String paymentMethodCode) {
        return isForeignDraft(paymentMethodCode) || isWireTransfer(paymentMethodCode);
    }

    protected boolean isForeignDraft(final String paymentMethodCode) {
        return StringUtils.equals(paymentMethodCode, PaymentSourceConstants.PAYMENT_METHOD_DRAFT);
    }

    protected boolean isWireTransfer(final String paymentMethodCode) {
        return StringUtils.equals(paymentMethodCode, PaymentSourceConstants.PAYMENT_METHOD_WIRE);
    }

    protected boolean documentWasFinalizedDuringTaxReportingTimeframe(final RouteHeaderLite routeHeader) {
        final java.sql.Date finalizedDate = routeHeader.getFinalizedDateAsSqlDate();
        return StringUtils.equals(routeHeader.getDocRouteStatus(), KewApiConstants.ROUTE_HEADER_FINAL_CD)
                && finalizedDate != null
                && finalizedDate.compareTo(config.getStartDate()) >= 0
                && finalizedDate.compareTo(config.getEndDate()) <= 0;
    }

    protected void setInitialValueForTaxBoxField(final TransactionDetail detail) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new UnsupportedOperationException(
                        "This implementation currently does not support 1099 tax processing");
            case CUTaxConstants.TAX_TYPE_1042S:
                detail.setForm1042SBox(CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
                break;
            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    protected void checkForAccountAndOrganizationExistence(final TransactionDetail detail) {
        final Account account = helperService.getAccountByPrimaryId(detail.getChartCode(), detail.getAccountNumber());
        if (ObjectUtils.isNotNull(account)) {
            final Organization organization = helperService.getOrganizationByPrimaryId(
                    detail.getChartCode(), account.getOrganizationCode());
            if (ObjectUtils.isNull(organization)) {
                statistics.increment(TaxStatType.NUM_NO_ORG);
            }
        } else {
            statistics.increment(TaxStatType.NUM_NO_ACCOUNT);
        }
    }

    protected String getPrincipalNameIfPresentForPerson(final String principalId) {
        final Person person = helperService.getPerson(principalId);
        if (ObjectUtils.isNotNull(person) && StringUtils.isNotBlank(person.getName())) {
            return person.getPrincipalName();
        } else {
            statistics.increment(TaxStatType.NUM_NO_ENTITY_NAME);
            return null;
        }
    }

    protected void populateIncomeCodeFieldsOnTransaction(final TransactionDetail detail) {
        final String incomeClassCode = StringUtils.defaultString(detail.getIncomeClassCode());
        final String objectCode = StringUtils.defaultString(detail.getFinObjectCode());
        String incomeCode = getIrsIncomeCodeByKfsIncomeClassCode(incomeClassCode);
        String incomeCodeSubType = getIrsIncomeCodeSubTypeByKfsIncomeClassCode(incomeClassCode);

        if (StringUtils.isBlank(incomeCode)) {
            if (shouldWithholdFederalTaxFor1042S(objectCode)) {
                incomeCode = getNonReportableIncomeCode();
                statistics.increment(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES, taxSourceType);
            } else {
                statistics.increment(TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES);
                if (shouldWithholdStateIncomeTaxFor1042S(objectCode)) {
                    incomeCode = getNonReportableIncomeCode();
                    statistics.increment(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, taxSourceType);
                } else {
                    statistics.increment(TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES);
                    incomeCode = getExcludedIncomeCode();
                    statistics.increment(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES, taxSourceType);
                }
            }
        }

        if (StringUtils.isBlank(incomeCodeSubType)) {
            incomeCodeSubType = getExcludedIncomeCodeSubType();
            statistics.increment(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES, taxSourceType);
        }

        detail.setIncomeCode(incomeCode);
        detail.setIncomeCodeSubType(incomeCodeSubType);
    }

    protected String getIrsIncomeCodeByKfsIncomeClassCode(final String incomeClassCode) {
        final Map<String, String> incomeClassCodeToIrsIncomeCodeMappings = helperService.getSubParameters(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.INCOME_CLASS_CODE_TO_IRS_INCOME_CODE);
        return incomeClassCodeToIrsIncomeCodeMappings.get(incomeClassCode);
    }

    protected String getIrsIncomeCodeSubTypeByKfsIncomeClassCode(final String incomeClassCode) {
        final Map<String, String> incomeClassCodeToIrsIncomeCodeSubTypeMappings = helperService.getSubParameters(
                CUTaxConstants.TAX_1042S_PARM_DETAIL,
                Tax1042SParameterNames.INCOME_CLASS_CODE_TO_IRS_INCOME_CODE_SUB_TYPE);
        return incomeClassCodeToIrsIncomeCodeSubTypeMappings.get(incomeClassCode);
    }

    protected boolean shouldWithholdFederalTaxFor1042S(final String objectCode) {
        final Set<String> fedTaxWithheldObjectCodes = helperService.getParameterValues(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES);
        return fedTaxWithheldObjectCodes.contains(objectCode);
    }

    protected boolean shouldWithholdStateIncomeTaxFor1042S(final String objectCode) {
        final Set<String> stateTaxWithheldObjectCodes = helperService.getParameterValues(
                CUTaxConstants.TAX_1042S_PARM_DETAIL,
                Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES);
        return stateTaxWithheldObjectCodes.contains(objectCode);
    }

    protected String getNonReportableIncomeCode() {
        return helperService.getParameterValue(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.NON_REPORTABLE_INCOME_CODE);
    }

    protected String getExcludedIncomeCode() {
        return helperService.getParameterValue(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.EXCLUDED_INCOME_CODE);
    }

    protected String getExcludedIncomeCodeSubType() {
        return helperService.getParameterValue(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.EXCLUDED_INCOME_CODE_SUB_TYPE);
    }

    protected Integer getKualiIntegerAsPlainInteger(final KualiInteger value) {
        return value != null ? value.intValue() : null;
    }

    protected KualiDecimal defaultToZeroIfNull(final KualiDecimal value) {
        return value != null ? value : KualiDecimal.ZERO;
    }

}
