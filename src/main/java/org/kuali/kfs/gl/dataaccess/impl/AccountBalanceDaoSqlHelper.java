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
package org.kuali.kfs.gl.dataaccess.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.Constant;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.businessobject.AccountBalance;
import org.kuali.kfs.gl.dataaccess.GeneralLedgerSqlHelper;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides functionality to generate sql statements used when retrieving account balances.
 */
// Exposing for overriding in UConn
// CU customization: adjust code to be Oracle compliant
public class AccountBalanceDaoSqlHelper extends GeneralLedgerSqlHelper<AccountBalance> {
    private static final Logger LOG = LogManager.getLogger();
    private static final String ORGANIZATION_CODE_CRITERIA = "account.organizationCode";

    private static final Map<String, String> CONSOLIDATED_SORT_MAP =
            Map.ofEntries(Map.entry("universityFiscalYear", "UNIV_FISCAL_YR"),
                    Map.entry("chartOfAccountsCode", "FIN_COA_CD"),
                    Map.entry("accountNumber", "ACCOUNT_NBR"),
                    Map.entry("objectCode", "FIN_OBJECT_CD"),
                    Map.entry("currentBudgetLineBalanceAmount", "BUDGET"),
                    Map.entry("accountLineActualsBalanceAmount", "ACTUALS"),
                    Map.entry("accountLineEncumbranceBalanceAmount", "ENCUMBRANCE"),
                    Map.entry(GeneralLedgerConstants.DummyBusinessObject.GENERIC_AMOUNT, "VARIANCE")
            );

    private static final Map<String, String> UNCONSOLIDATED_SORT_MAP = Stream.concat(
            CONSOLIDATED_SORT_MAP.entrySet().stream(),
            Map.ofEntries(Map.entry("subAccountNumber", "SUB_ACCT_NBR"), Map.entry("subObjectCode", "FIN_SUB_OBJ_CD"))
                    .entrySet()
                    .stream()
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    // Exposing for accessing in UConn
    protected static final Map<String, String> CRITERIA_MAP = Map.ofEntries(
            // CU customization: replace MySql specific IFNULL with Oracle native function: NVL
            Map.entry("universityFiscalYear", "NVL(BALANCE.UNIV_FISCAL_YR, :currentUniversityFiscalYear)"),
            Map.entry("chartOfAccountsCode", "BALANCE.FIN_COA_CD"),
            Map.entry("accountNumber", "BALANCE.ACCOUNT_NBR"),
            Map.entry("subAccountNumber", "BALANCE.SUB_ACCT_NBR"),
            Map.entry("objectCode", "BALANCE.FIN_OBJECT_CD"),
            Map.entry("subObjectCode", "BALANCE.FIN_SUB_OBJ_CD"),
            Map.entry(ORGANIZATION_CODE_CRITERIA, "ACCOUNT.ORG_CD")
    );

    // Exposing for accessing in UConn
    protected final boolean isConsolidated;
    protected final String pendingEntryOption;
    protected final boolean excludeTransfers;
    protected final boolean excludeIndirectCost;
    protected final boolean excludeBalanceSheets;
    protected final boolean excludePriorYearBalances;
    protected final ParameterService parameterService;
    protected final SystemOptions systemOptions;

    // Exposing for overriding in UConn
    public AccountBalanceDaoSqlHelper(
            final Map<String, String> fieldValues,
            final boolean isConsolidated,
            final int currentUniversityFiscalYear,
            final ParameterService parameterService,
            final SystemOptions systemOptions
    ) {
        super(fieldValues);
        this.isConsolidated = isConsolidated;
        // Need to default here due to overlays using old UI do not pass this value if unchanged
        pendingEntryOption = fieldValues.getOrDefault(Constant.PENDING_ENTRY_OPTION, Constant.NO_PENDING_ENTRY);
        parameters.put("currentUniversityFiscalYear", currentUniversityFiscalYear);
        excludeTransfers = StringUtils.equals(
                fieldValues.get(GeneralLedgerConstants.DummyBusinessObject.TRANSFERS_OPTION),
                GeneralLedgerConstants.IncludeExcludeOptions.EXCLUDE
        );
        excludeIndirectCost = StringUtils.equals(
                fieldValues.get(GeneralLedgerConstants.DummyBusinessObject.INDIRECT_COST_OPTION),
                GeneralLedgerConstants.IncludeExcludeOptions.EXCLUDE
        );
        excludeBalanceSheets = StringUtils.equals(
                fieldValues.get(GeneralLedgerConstants.DummyBusinessObject.BALANCE_SHEET_OPTION),
                GeneralLedgerConstants.IncludeExcludeOptions.EXCLUDE
        );
        final String priorYearBalancesOption = fieldValues.get(GeneralLedgerConstants.DummyBusinessObject.PRIOR_YEAR_BALANCES_OPTION);
        excludePriorYearBalances = priorYearBalancesOption == null || StringUtils.equals(
                fieldValues.get(GeneralLedgerConstants.DummyBusinessObject.PRIOR_YEAR_BALANCES_OPTION),
                GeneralLedgerConstants.IncludeExcludeOptions.EXCLUDE
        );
        this.parameterService = parameterService;
        this.systemOptions = systemOptions;
    }

    // Exposing for overriding in UConn
    @Override
    protected AccountBalance mapResultSetToObject(final Supplier<AccountBalance> supplier, final ResultSet rs) throws SQLException {
        final AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccountNumber(rs.getString("ACCOUNT_NBR"));
        accountBalance.setChartOfAccountsCode(rs.getString("FIN_COA_CD"));
        accountBalance.setObjectCode(rs.getString("FIN_OBJECT_CD"));
        accountBalance.setUniversityFiscalYear(rs.getInt("UNIV_FISCAL_YR"));
        accountBalance.setAccountLineActualsBalanceAmount(new KualiDecimal(rs.getBigDecimal("ACTUALS")));
        accountBalance.setAccountLineEncumbranceBalanceAmount(new KualiDecimal(rs.getBigDecimal("ENCUMBRANCE")));
        accountBalance.setCurrentBudgetLineBalanceAmount(new KualiDecimal(rs.getBigDecimal("BUDGET")));
        accountBalance.getDummyBusinessObject().setGenericAmount(new KualiDecimal(rs.getBigDecimal("VARIANCE")));
        if (isConsolidated) {
            accountBalance.setSubAccountNumber(Constant.CONSOLIDATED_SUB_ACCOUNT_NUMBER);
            accountBalance.setSubObjectCode(Constant.CONSOLIDATED_SUB_OBJECT_CODE);
        } else {
            accountBalance.setSubAccountNumber(rs.getString("SUB_ACCT_NBR"));
            accountBalance.setSubObjectCode(rs.getString("FIN_SUB_OBJ_CD"));
        }

        return accountBalance;
    }

    // Exposing for override in Overlays
    protected String addSearchCriteria() {
        String searchCriteria = addSearchCriteria(CRITERIA_MAP);
        if (excludeTransfers) {
            final Collection<String> transferExclusions =
                    parameterService.getParameterValuesAsString(AccountBalance.class,
                            GeneralLedgerConstants.AccountBalanceParameters.TRANSFER_SUB_TYPES
                    );
            parameters.put("transferExclusions", transferExclusions);
            searchCriteria += GeneralSql.EXCLUDE_TRANSFER_WHERE;
        }
        if (excludeIndirectCost) {
            final Collection<String> indirectCostExclusions =
                    parameterService.getParameterValuesAsString(AccountBalance.class,
                            GeneralLedgerConstants.AccountBalanceParameters.INDIRECT_COST_OBJECT_LEVELS
                    );
            parameters.put("indirectCostExclusions", indirectCostExclusions);
            searchCriteria += GeneralSql.EXCLUDE_INDIRECT_COSTS_WHERE;
        }
        if (excludeBalanceSheets) {
            final Collection<String> balanceSheetExclusions =
                    parameterService.getParameterValuesAsString(AccountBalance.class,
                            GeneralLedgerConstants.AccountBalanceParameters.BALANCE_SHEET_CATEGORIES
                    );
            parameters.put("balanceSheetExclusions", balanceSheetExclusions);
            searchCriteria += GeneralSql.EXCLUDE_BALANCE_SHEETS_WHERE;
        }
        return searchCriteria;
    }

    // Exposing for accessing in Overlays
    @Override
    protected String addCondition(
            final Map<? super String, ? super Object> parameters,
            final String fieldValueKey,
            final String sqlField
    ) {
        if (!StringUtils.equals(fieldValueKey, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)) {
            return super.addCondition(parameters, fieldValueKey, sqlField);
        }
        String condition = "";
        final String fieldValue = fieldValues.get(fieldValueKey);
        if (StringUtils.isNotBlank(fieldValue)) {
            if (StringUtils.equals(fieldValueKey, KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)
                && !excludePriorYearBalances) {
                if (!StringUtils.isNumeric(fieldValue)) {
                    LOG.atError().log("addCondition(...): Non-numeric fiscal year: {}", fieldValue);
                    throw new RuntimeException("Non-numeric fiscal year: " + fieldValue);
                }
                condition = sqlField + " IN (:" + fieldValueKey + ") ";
                parameters.put(fieldValueKey, List.of(fieldValue, Integer.parseInt(fieldValue) - 1));
            } else {
                return super.addCondition(parameters, fieldValueKey, sqlField);
            }
        }
        return condition;
    }

    @Override
    protected String buildCountSql() {
        final String countSql;
        if (StringUtils.equals(Constant.NO_PENDING_ENTRY, pendingEntryOption)) {
            countSql = wrap(buildFinalSql());
        } else {
            countSql = wrap(buildPendingEntrySql());
        }
        // CU customization: AS BALANCE_COUNT has been replaced with BALANCE_COUNT for Oracle compatibility
        return "SELECT COUNT(*) FROM (" + countSql + ") BALANCE_COUNT";
    }

    @Override
    protected String buildSql() {
        if (!StringUtils.equals(Constant.NO_PENDING_ENTRY, pendingEntryOption)) {
            return addSortAndLimitSql(buildPendingEntrySql(),
                    isConsolidated ? CONSOLIDATED_SORT_MAP : UNCONSOLIDATED_SORT_MAP
            );
        }
        return addSortAndLimitSql(wrap(buildFinalSql()),
                isConsolidated ? CONSOLIDATED_SORT_MAP : UNCONSOLIDATED_SORT_MAP
        );
    }

    private String buildPendingEntrySql() {
        final String pendingBudgetSql = buildPendingBudgetSql();
        final String actualBalancesSql = buildActualBalancesSql();
        final String pendingActualsSql = buildPendingActualsSql();
        final String encumbranceBalancesSql = buildEncumbranceBalancesSql();
        final String pendingEncumbranceSql = buildPendingEncumbranceSql();
        final String finalSql = buildFinalSql();
        final String outerSelect =
                isConsolidated ? OuterWrapperSql.AGGREGATE_CONSOLIDATED_SELECT : OuterWrapperSql.AGGREGATE_UNCONSOLIDATED_SELECT;
        final String groupByClause =
                isConsolidated ? OuterWrapperSql.AGGREGATE_CONSOLIDATED_GROUP_BY : OuterWrapperSql.AGGREGATE_UNCONSOLIDATED_GROUP_BY;
        final String fromClause = PendingBalanceSql.UNION_ALIASES.stream()
                .map(alias -> (isConsolidated
                                       ? PendingBalanceSql.CONSOLIDATED_UNION_SELECT
                                       : PendingBalanceSql.UNCONSOLIDATED_UNION_SELECT) + " FROM " + alias)
                .collect(Collectors.joining(" UNION ALL "));

        // CU customization: AS BALANCES has been replaced with BALANCES for Oracle compatibility
        final String aggregateSql = outerSelect + " FROM (" + fromClause + ") BALANCES " + groupByClause;
        final String varianceSql = wrapWithVariance(aggregateSql);

        // CU customization:
        // All CTEs (Common Table Expressions - WITH clauses) are now at the top level. Oracle doesn't allow WITH clauses to be nested within other WITH clauses.
        return "WITH " + pendingBudgetSql + ", "
               + actualBalancesSql + ", "
               + pendingActualsSql + ", "
               + encumbranceBalancesSql + ", "
               + pendingEncumbranceSql + ", "
               + "FINAL_BALANCES AS (" + finalSql + ") "
               + varianceSql;
    }

    private String buildPendingBudgetSql() {
        String innerSelectClause = isConsolidated
                ? PendingBalanceSql.CONSOLIDATED_INNER_SELECT
                : PendingBalanceSql.UNCONSOLIDATED_INNER_SELECT;
        innerSelectClause += PendingBalanceSql.Budget.INNER_SELECT;
        // If include prior year balances, set the fiscal year to the search criteria value
        innerSelectClause = String.format(
                innerSelectClause,
                excludePriorYearBalances
                        ? PendingBalanceSql.UNIVERSITY_FISCAL_YEAR_DEFAULT
                        : fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)
        );
        final String innerFromClause = buildFromClause(generatePendingFromClauses());
        final String filterWhereClause = addSearchCriteria();
        String innerWhereClause = PendingBalanceSql.Budget.INNER_WHERE;
        innerWhereClause += pendingEntryOption.equals(Constant.ALL_PENDING_ENTRY)
                ? PendingBalanceSql.ALL_PENDING_WHERE
                : PendingBalanceSql.APPROVED_PENDING_WHERE;
        if (StringUtils.isNotBlank(filterWhereClause)) {
            innerWhereClause += " AND " + filterWhereClause;
        }
        return "PENDING_BUDGETS AS (" + innerSelectClause + innerFromClause + innerWhereClause + ")";
    }

    // CU customization:Builds the ACTUAL_BALANCES CTE (moved to top level)
    private String buildActualBalancesSql() {
        String innerSelectClause = isConsolidated
                ? PendingBalanceSql.CONSOLIDATED_INNER_SELECT
                : PendingBalanceSql.UNCONSOLIDATED_INNER_SELECT;
        innerSelectClause += PendingBalanceSql.Actuals.INNER_SELECT;
        // If include prior year balances, set the fiscal year to the search criteria value
        innerSelectClause = String.format(
                innerSelectClause,
                excludePriorYearBalances
                        ? PendingBalanceSql.UNIVERSITY_FISCAL_YEAR_DEFAULT
                        : fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)
        );
        final String innerFromClause = buildFromClause(generatePendingFromClauses());
        final String filterWhereClause = addSearchCriteria();
        String innerWhereClause = PendingBalanceSql.Actuals.INNER_WHERE;
        innerWhereClause += pendingEntryOption.equals(Constant.ALL_PENDING_ENTRY)
                ? PendingBalanceSql.ALL_PENDING_WHERE
                : PendingBalanceSql.APPROVED_PENDING_WHERE;
        if (StringUtils.isNotBlank(filterWhereClause)) {
            innerWhereClause += " AND " + filterWhereClause;
        }
        return "ACTUAL_BALANCES AS (" + innerSelectClause + innerFromClause + innerWhereClause + ")";
    }

    // CU customization: method has been modified and it now references ACTUAL_BALANCES instead of creating it
    private String buildPendingActualsSql() {
        String outerSelectClause = isConsolidated
                ? PendingBalanceSql.Actuals.CONSOLIDATED_OUTER_SELECT
                : PendingBalanceSql.Actuals.UNCONSOLIDATED_OUTER_SELECT;
        outerSelectClause += PendingBalanceSql.Actuals.OUTER_SELECT;

        final String outerSql =
                outerSelectClause + PendingBalanceSql.Actuals.OUTER_FROM + PendingBalanceSql.Actuals.OUTER_WHERE;
        return "PENDING_ACTUALS AS (" + outerSql + ")";
    }

    // CU customization: Builds the ENCUMBRANCE_BALANCES CTE (moved to top level)
    private String buildEncumbranceBalancesSql() {
        String innerSelectClause = isConsolidated
                ? PendingBalanceSql.CONSOLIDATED_INNER_SELECT
                : PendingBalanceSql.UNCONSOLIDATED_INNER_SELECT;
        innerSelectClause += PendingBalanceSql.Encumbrance.INNER_SELECT;
        // If include prior year balances, set the fiscal year to the search criteria value
        innerSelectClause = String.format(
                innerSelectClause,
                excludePriorYearBalances
                        ? PendingBalanceSql.UNIVERSITY_FISCAL_YEAR_DEFAULT
                        : fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)
        );
        final String innerFromClause = buildFromClause(generatePendingFromClauses());
        final String filterWhereClause = addSearchCriteria();
        String innerWhereClause = PendingBalanceSql.Encumbrance.INNER_WHERE;
        innerWhereClause += pendingEntryOption.equals(Constant.ALL_PENDING_ENTRY)
                ? PendingBalanceSql.ALL_PENDING_WHERE
                : PendingBalanceSql.APPROVED_PENDING_WHERE;
        if (StringUtils.isNotBlank(filterWhereClause)) {
            innerWhereClause += " AND " + filterWhereClause;
        }
        return "ENCUMBRANCE_BALANCES AS (" + innerSelectClause + innerFromClause + innerWhereClause + ")";
    }

    // CU customization: method has been modified and it now references ENCUMBRANCE_BALANCES instead of creating it
    private String buildPendingEncumbranceSql() {
        String outerSelectClause = isConsolidated
                ? PendingBalanceSql.Encumbrance.CONSOLIDATED_OUTER_SELECT
                : PendingBalanceSql.Encumbrance.UNCONSOLIDATED_OUTER_SELECT;
        outerSelectClause += PendingBalanceSql.Encumbrance.OUTER_SELECT;

        final String outerSql = outerSelectClause
                                + PendingBalanceSql.Encumbrance.OUTER_FROM
                                + PendingBalanceSql.Encumbrance.OUTER_WHERE;
        return "PENDING_ENCUMBRANCES AS (" + outerSql + ")";
    }

    private String wrap(final String unwrappedSql) {
        final String selectClause = isConsolidated
                ? OuterWrapperSql.AGGREGATE_CONSOLIDATED_SELECT
                : OuterWrapperSql.AGGREGATE_UNCONSOLIDATED_SELECT;
        final String groupByClause = isConsolidated
                ? OuterWrapperSql.AGGREGATE_CONSOLIDATED_GROUP_BY
                : OuterWrapperSql.AGGREGATE_UNCONSOLIDATED_GROUP_BY;
        // CU customization: AS BALANCES has been replaced with BALANCES for Oracle compatibility
        final String aggregateClause = selectClause + " FROM (" + unwrappedSql + ") BALANCES " + groupByClause;
        return wrapWithVariance(aggregateClause);
    }

    private String wrapWithVariance(final String aggregateSql) {
        final String varianceSelectClause = isConsolidated
                ? OuterWrapperSql.VARIANCE_CONSOLIDATED_SELECT
                : OuterWrapperSql.VARIANCE_UNCONSOLIDATED_SELECT;
        return varianceSelectClause + String.format(OuterWrapperSql.VARIANCE_FROM, aggregateSql)
               + OuterWrapperSql.VARIANCE_JOIN;
    }

    private String buildFinalSql() {
        String selectClause = isConsolidated
                ? FinalBalanceSql.CONSOLIDATED_INNER_SELECT
                : FinalBalanceSql.UNCONSOLIDATED_INNER_SELECT;
        final boolean beginningEncumbrancesLoaded = systemOptions.isFinancialBeginEncumbranceLoadInd();
        final String amountSelect;
        if (excludePriorYearBalances || !beginningEncumbrancesLoaded) {
            amountSelect = String.format(FinalBalanceSql.INNER_SELECT, FinalBalanceSql.DEFAULT_ENCUMBRANCE);
        } else {
            amountSelect = String.format(
                    FinalBalanceSql.INNER_SELECT,
                    String.format(
                            FinalBalanceSql.PRIOR_YEAR_ENCUMBRANCE,
                            Integer.parseInt(fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)) - 1
                    )
            );
        }
        selectClause += amountSelect;
        // If include prior year balances, set the fiscal year to the search criteria value
        selectClause = String.format(
                selectClause,
                excludePriorYearBalances
                        ? FinalBalanceSql.UNIVERSITY_FISCAL_YEAR_DEFAULT
                        : fieldValues.get(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR)
        );
        String whereClause = addSearchCriteria();
        if (StringUtils.isNotBlank(whereClause)) {
            whereClause = " WHERE " + whereClause;
        }
        final String fromClause = buildFromClause(generateFinalFromClauses());
        return selectClause + fromClause + whereClause;
    }

    // Exposing for override in Overlays
    protected Set<String> generateFinalFromClauses() {
        // Using LinkedHashSet to maintain order
        final Set<String> fromClauses = new LinkedHashSet<>();
        fromClauses.add(FinalBalanceSql.INNER_FROM);
        if (excludeBalanceSheets) {
            fromClauses.add(GeneralSql.JOIN_OBJECT_TYPE);
        }
        if (fieldValues.containsKey(ORGANIZATION_CODE_CRITERIA)) {
            fromClauses.add(GeneralSql.JOIN_ACCOUNT);
        }
        return fromClauses;
    }

    // Exposing for override in Overlays
    protected Set<String> generatePendingFromClauses() {
        // Using LinkedHashSet to maintain order
        final Set<String> innerFromClauses = new LinkedHashSet<>();
        innerFromClauses.add(PendingBalanceSql.INNER_FROM);
        if (StringUtils.equals(Constant.APPROVED_PENDING_ENTRY, pendingEntryOption)) {
            innerFromClauses.add(PendingBalanceSql.JOIN_DOC_HEADER);
        }
        if (excludeTransfers || excludeIndirectCost) {
            innerFromClauses.add(GeneralSql.JOIN_OBJECT_CODE);
        }
        if (excludeBalanceSheets) {
            innerFromClauses.add(GeneralSql.JOIN_OBJECT_CODE);
            innerFromClauses.add(GeneralSql.JOIN_OBJECT_TYPE);
        }
        if (fieldValues.containsKey(ORGANIZATION_CODE_CRITERIA)) {
            innerFromClauses.add(GeneralSql.JOIN_ACCOUNT);
        }
        return innerFromClauses;
    }

    private String buildFromClause(final Set<String> clauses) {
        return String.join(" ", clauses) + " ";
    }

    // Exposing for use in Overlays
    public static final class GeneralSql {
        public static final String EXCLUDE_TRANSFER_WHERE =
                "AND FIN_OBJ_SUB_TYP_CD NOT IN (:transferExclusions) ";
        public static final String EXCLUDE_INDIRECT_COSTS_WHERE =
                "AND FIN_OBJ_LEVEL_CD NOT IN (:indirectCostExclusions) ";
        public static final String EXCLUDE_BALANCE_SHEETS_WHERE =
                "AND ACCTG_CTGRY_CD NOT IN (:balanceSheetExclusions) ";
        public static final String JOIN_OBJECT_TYPE =
                "       INNER JOIN CA_OBJ_TYPE_T OBJECT_TYPE ON"
                + "         CODE.FIN_OBJ_TYP_CD = OBJECT_TYPE.FIN_OBJ_TYP_CD ";
        public static final String JOIN_ACCOUNT =
                "      INNER JOIN CA_ACCOUNT_T ACCOUNT ON"
                + "        BALANCE.FIN_COA_CD = ACCOUNT.FIN_COA_CD"
                + "    AND BALANCE.ACCOUNT_NBR = ACCOUNT.ACCOUNT_NBR ";
        // CU customization: replace MySql specific IFNULL with Oracle native function: NVL
        public static final String JOIN_OBJECT_CODE =
                "    INNER JOIN CA_OBJECT_CODE_T CODE ON"
                + "        NVL(BALANCE.UNIV_FISCAL_YR, :currentUniversityFiscalYear) = CODE.UNIV_FISCAL_YR"
                + "    AND BALANCE.FIN_COA_CD = CODE.FIN_COA_CD"
                + "    AND BALANCE.FIN_OBJECT_CD = CODE.FIN_OBJECT_CD ";

    }

    private static final class OuterWrapperSql {
        private static final String VARIANCE_CONSOLIDATED_SELECT =
                "SELECT AGGREGATED_BALANCES.UNIV_FISCAL_YR,"
                + "     AGGREGATED_BALANCES.FIN_COA_CD,"
                + "     ACCOUNT_NBR,"
                + "     AGGREGATED_BALANCES.FIN_OBJECT_CD,"
                + "     BUDGET,"
                + "     ACTUALS,"
                + "     ENCUMBRANCE,"
                + "     CASE"
                + "         WHEN OBJECT_CODE.FIN_OBJ_TYP_CD IN"
                + "             (OPTIONS.FOBJTP_EXPNXPND_CD, OPTIONS.FOBJTP_XPNDNEXP_CD, OPTIONS.FOBJTP_XPND_EXP_CD)"
                + "         THEN BUDGET - ACTUALS - ENCUMBRANCE"
                + "         ELSE ACTUALS - BUDGET"
                + "     END AS VARIANCE";
        private static final String VARIANCE_UNCONSOLIDATED_SELECT = VARIANCE_CONSOLIDATED_SELECT
                + ",    FIN_SUB_OBJ_CD,"
                + "     SUB_ACCT_NBR";
        // CU customization: AS AGGREGATED_BALANCES has been replaced with AGGREGATED_BALANCES for Oracle compatibility
        private static final String VARIANCE_FROM = " FROM (%s) AGGREGATED_BALANCES";
        private static final String VARIANCE_JOIN =
                "   JOIN FS_OPTION_T OPTIONS ON AGGREGATED_BALANCES.UNIV_FISCAL_YR = OPTIONS.UNIV_FISCAL_YR"
                + " JOIN CA_OBJECT_CODE_T OBJECT_CODE ON"
                + "          AGGREGATED_BALANCES.UNIV_FISCAL_YR = OBJECT_CODE.UNIV_FISCAL_YR"
                + "      AND AGGREGATED_BALANCES.FIN_COA_CD = OBJECT_CODE.FIN_COA_CD"
                + "      AND AGGREGATED_BALANCES.FIN_OBJECT_CD = OBJECT_CODE.FIN_OBJECT_CD";

        private static final String AGGREGATE_CONSOLIDATED_SELECT =
                "SELECT UNIV_FISCAL_YR,"
                + "     FIN_COA_CD,"
                + "     ACCOUNT_NBR,"
                + "     FIN_OBJECT_CD,"
                + "     SUM(BUDGET) BUDGET,"
                + "     SUM(ACTUALS) ACTUALS,"
                + "     SUM(ENCUMBRANCE) ENCUMBRANCE";
        private static final String AGGREGATE_UNCONSOLIDATED_SELECT =
                AGGREGATE_CONSOLIDATED_SELECT
                + ",    FIN_SUB_OBJ_CD,"
                + "     SUB_ACCT_NBR";
        private static final String AGGREGATE_CONSOLIDATED_GROUP_BY =
                "GROUP BY UNIV_FISCAL_YR,"
                + "       FIN_COA_CD,"
                + "       ACCOUNT_NBR,"
                + "       FIN_OBJECT_CD";
        private static final String AGGREGATE_UNCONSOLIDATED_GROUP_BY =
                AGGREGATE_CONSOLIDATED_GROUP_BY
                + ",      FIN_SUB_OBJ_CD,"
                + "       SUB_ACCT_NBR";
    }

    private static final class FinalBalanceSql {
        private static final String UNIVERSITY_FISCAL_YEAR_DEFAULT = "BALANCE.UNIV_FISCAL_YR";
        private static final String CONSOLIDATED_INNER_SELECT =
                "SELECT %s UNIV_FISCAL_YR,"
                + "     BALANCE.FIN_COA_CD,"
                + "     BALANCE.ACCOUNT_NBR,"
                + "     BALANCE.FIN_OBJECT_CD, ";
        private static final String UNCONSOLIDATED_INNER_SELECT =
                CONSOLIDATED_INNER_SELECT
                + "     FIN_SUB_OBJ_CD,"
                + "     SUB_ACCT_NBR,";
        // CU customization: AS ENCUMBRANCE has been replaced with ENCUMBRANCE for Oracle compatibility
        private static final String INNER_SELECT =
                "       CURR_BDLN_BAL_AMT BUDGET,"
                + "     ACLN_ACTLS_BAL_AMT ACTUALS,"
                + "     %s ENCUMBRANCE ";

        private static final String PRIOR_YEAR_ENCUMBRANCE =
                "CASE"
                + "    WHEN BALANCE.UNIV_FISCAL_YR = %s THEN 0"
                + "    ELSE ACLN_ENCUM_BAL_AMT "
                + "END";

        private static final String DEFAULT_ENCUMBRANCE = "ACLN_ENCUM_BAL_AMT";

        private static final String INNER_FROM =
                "FROM GL_ACCT_BALANCES_T BALANCE"
                + "    INNER JOIN CA_OBJECT_CODE_T CODE ON"
                + "        BALANCE.UNIV_FISCAL_YR = CODE.UNIV_FISCAL_YR"
                + "    AND BALANCE.FIN_COA_CD = CODE.FIN_COA_CD"
                + "    AND BALANCE.FIN_OBJECT_CD = CODE.FIN_OBJECT_CD ";
    }

    private static final class PendingBalanceSql {
        // CU customization: replace MySql specific IFNULL with Oracle native function: NVL
        private static final String UNIVERSITY_FISCAL_YEAR_DEFAULT =
                "NVL(BALANCE.UNIV_FISCAL_YR, :currentUniversityFiscalYear)";
        private static final String CONSOLIDATED_INNER_SELECT =
                "SELECT %s UNIV_FISCAL_YR,"
                + "     BALANCE.FIN_COA_CD,"
                + "     BALANCE.ACCOUNT_NBR,"
                + "     BALANCE.FIN_OBJECT_CD,"
                + "     BALANCE.FIN_OBJ_TYP_CD,";
        private static final String UNCONSOLIDATED_INNER_SELECT =
                CONSOLIDATED_INNER_SELECT
                + "     FIN_SUB_OBJ_CD,"
                + "     SUB_ACCT_NBR,";
        // CU customization: replace MySql specific IFNULL with Oracle native function: NVL
        private static final String INNER_FROM =
                "FROM GL_PENDING_ENTRY_T BALANCE "
                + "    INNER JOIN FS_OPTION_T OPTIONS ON "
                + "        NVL(BALANCE.UNIV_FISCAL_YR, :currentUniversityFiscalYear) = OPTIONS.UNIV_FISCAL_YR ";
        private static final String JOIN_DOC_HEADER =
                "    INNER JOIN FS_DOC_HEADER_T DOC ON "
                + "        BALANCE.FDOC_NBR = DOC.FDOC_NBR ";

        private static final String CONSOLIDATED_UNION_SELECT =
                "SELECT UNIV_FISCAL_YR,"
                + "     FIN_COA_CD,"
                + "     ACCOUNT_NBR,"
                + "     FIN_OBJECT_CD,"
                + "     BUDGET,"
                + "     ACTUALS,"
                + "     ENCUMBRANCE";
        private static final String UNCONSOLIDATED_UNION_SELECT =
                CONSOLIDATED_UNION_SELECT
                + ",    FIN_SUB_OBJ_CD,"
                + "     SUB_ACCT_NBR";
        private static final String ALL_PENDING_WHERE =
                "AND ("
                + "   FDOC_APPROVED_CD IS NULL "
                + "OR FDOC_APPROVED_CD <> '" + KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.PROCESSED + "'"
                + ") ";
        private static final String APPROVED_PENDING_WHERE =
                "AND FDOC_STATUS_CD = '" + KFSConstants.DocumentStatusCodes.APPROVED + "' "
                + "AND FDOC_APPROVED_CD <> '" + KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.PROCESSED + "'";

        private static final List<String> UNION_ALIASES =
                List.of("FINAL_BALANCES", "PENDING_BUDGETS", "PENDING_ACTUALS", "PENDING_ENCUMBRANCES");

        private static final class Budget {
            private static final String INNER_SELECT = "TRN_LDGR_ENTR_AMT BUDGET, 0 ACTUALS, 0 ENCUMBRANCE ";
            private static final String INNER_WHERE = "WHERE FIN_BALANCE_TYP_CD = OPTIONS.BDGT_CHK_BALTYP_CD ";
        }

        private static final class Actuals {
            private static final String INNER_SELECT =
                    "      0 BUDGET, "
                    + "    TRN_LDGR_ENTR_AMT ACTUALS, "
                    + "    0 ENCUMBRANCE,"
                    + "    BALANCE.FIN_BALANCE_TYP_CD,"
                    + "    TRN_DEBIT_CRDT_CD ";
            private static final String INNER_WHERE = "WHERE FIN_BALANCE_TYP_CD = OPTIONS.ACT_FIN_BAL_TYP_CD ";
            private static final String CONSOLIDATED_OUTER_SELECT =
                    "SELECT UNIV_FISCAL_YR,"
                    + "     FIN_COA_CD,"
                    + "     ACCOUNT_NBR,"
                    + "     FIN_OBJECT_CD,";
            private static final String UNCONSOLIDATED_OUTER_SELECT =
                    CONSOLIDATED_OUTER_SELECT
                    + "     FIN_SUB_OBJ_CD,"
                    + "     SUB_ACCT_NBR,";
            private static final String OUTER_SELECT =
                    "       BUDGET,"
                    + "     CASE"
                    + "         WHEN BALANCE_TYPE.FIN_OFFST_GNRTN_CD = 'Y'"
                    + "              AND ACTUAL_BALANCES.TRN_DEBIT_CRDT_CD != OBJECT_TYPE.FIN_OBJTYP_DBCR_CD "
                    + "         THEN ACTUALS * -1"
                    + "         ELSE ACTUALS"
                    + "     END AS ACTUALS,"
                    + "     ENCUMBRANCE ";
            private static final String OUTER_FROM =
                    "FROM ACTUAL_BALANCES"
                    + "     LEFT JOIN"
                    + "   CA_BALANCE_TYPE_T BALANCE_TYPE "
                    + "         ON ACTUAL_BALANCES.FIN_BALANCE_TYP_CD = BALANCE_TYPE.FIN_BALANCE_TYP_CD"
                    + "     LEFT JOIN"
                    + "   CA_OBJ_TYPE_T OBJECT_TYPE "
                    + "         ON ACTUAL_BALANCES.FIN_OBJ_TYP_CD = OBJECT_TYPE.FIN_OBJ_TYP_CD ";
            private static final String OUTER_WHERE =
                    "WHERE ("
                    + "         BALANCE_TYPE.FIN_OFFST_GNRTN_CD = 'N' "
                    + "     AND ACTUAL_BALANCES.TRN_DEBIT_CRDT_CD = ' '"
                    + "    )"
                    + " OR ACTUAL_BALANCES.TRN_DEBIT_CRDT_CD = OBJECT_TYPE.FIN_OBJTYP_DBCR_CD"
                    + " OR ("
                    + "         BALANCE_TYPE.FIN_OFFST_GNRTN_CD = 'Y' "
                    + "     AND ACTUAL_BALANCES.TRN_DEBIT_CRDT_CD != OBJECT_TYPE.FIN_OBJTYP_DBCR_CD"
                    + "    ) ";
        }

        private static final class Encumbrance {
            private static final String INNER_SELECT =
                    "      0 BUDGET, "
                    + "    0 ACTUALS, "
                    + "    TRN_LDGR_ENTR_AMT ENCUMBRANCE,"
                    + "    BALANCE.FIN_BALANCE_TYP_CD,"
                    + "    TRN_DEBIT_CRDT_CD ";
            private static final String INNER_WHERE =
                    "WHERE "
                    + " ("
                    + "     BALANCE.FIN_BALANCE_TYP_CD = OPTIONS.EXT_ENC_FBALTYP_CD"
                    + "  OR BALANCE.FIN_BALANCE_TYP_CD = OPTIONS.INT_ENC_FBALTYP_CD"
                    + "  OR BALANCE.FIN_BALANCE_TYP_CD = OPTIONS.PRE_ENC_FBALTYP_CD"
                    + "  OR BALANCE.FIN_BALANCE_TYP_CD = OPTIONS.CSTSHR_ENCUM_FIN_BAL_TYP_CD"
                    + " )";
            private static final String CONSOLIDATED_OUTER_SELECT =
                    "SELECT UNIV_FISCAL_YR,"
                    + "     FIN_COA_CD,"
                    + "     ACCOUNT_NBR,"
                    + "     FIN_OBJECT_CD,";
            private static final String UNCONSOLIDATED_OUTER_SELECT =
                    CONSOLIDATED_OUTER_SELECT
                    + "     FIN_SUB_OBJ_CD,"
                    + "     SUB_ACCT_NBR,";
            private static final String OUTER_SELECT =
                    "       BUDGET,"
                    + "     ACTUALS,"
                    + "     CASE"
                    + "         WHEN BALANCE_TYPE.FIN_OFFST_GNRTN_CD = 'Y' "
                    + "              AND ENCUMBRANCE_BALANCES.TRN_DEBIT_CRDT_CD != OBJECT_TYPE.FIN_OBJTYP_DBCR_CD"
                    + "         THEN ENCUMBRANCE * -1"
                    + "         ELSE ENCUMBRANCE"
                    + "     END AS ENCUMBRANCE ";
            private static final String OUTER_FROM =
                    "FROM ENCUMBRANCE_BALANCES"
                    + "     LEFT JOIN"
                    + "   CA_BALANCE_TYPE_T BALANCE_TYPE"
                    + "         ON ENCUMBRANCE_BALANCES.FIN_BALANCE_TYP_CD = BALANCE_TYPE.FIN_BALANCE_TYP_CD"
                    + "     LEFT JOIN"
                    + "   CA_OBJ_TYPE_T OBJECT_TYPE"
                    + "         ON ENCUMBRANCE_BALANCES.FIN_OBJ_TYP_CD = OBJECT_TYPE.FIN_OBJ_TYP_CD ";
            private static final String OUTER_WHERE =
                    "WHERE ("
                    + "         BALANCE_TYPE.FIN_OFFST_GNRTN_CD = 'N'"
                    + "     AND ENCUMBRANCE_BALANCES.TRN_DEBIT_CRDT_CD = ' '"
                    + "    )"
                    + " OR ENCUMBRANCE_BALANCES.TRN_DEBIT_CRDT_CD = OBJECT_TYPE.FIN_OBJTYP_DBCR_CD"
                    + " OR ("
                    + "         BALANCE_TYPE.FIN_OFFST_GNRTN_CD = 'Y'"
                    + "     AND ENCUMBRANCE_BALANCES.TRN_DEBIT_CRDT_CD != OBJECT_TYPE.FIN_OBJTYP_DBCR_CD"
                    + "    ) ";
        }
    }
}
