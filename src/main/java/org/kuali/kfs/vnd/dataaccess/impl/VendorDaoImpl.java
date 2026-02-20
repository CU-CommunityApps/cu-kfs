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
package org.kuali.kfs.vnd.dataaccess.impl;

import edu.cornell.kfs.vnd.businessobject.VendorWithTaxId;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.dataaccess.VendorDao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.GeneralSecurityException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Map.entry;

/**
 * Uses JDBC/JdbcTemplate for directly implemented methods; delegates to OJB-specific implementation for the remaining
 * methods.
 */
// CU customization: 
// * update SQL that is MySql specific to work on Oracle database.
// * update class to implement CU-specific DAO and invoke the related OJB DAO methods.
// * backport FINP-11585 from the 11/21/2024 release
public class VendorDaoImpl implements VendorDao, CuVendorDao {
    private static final Logger LOG = LogManager.getLogger();
    private static final int CAPACITY = 16;
    private static final String PARENT_SQL_SELECT =
            "SELECT DETAIL.VNDR_HDR_GNRTD_ID, DETAIL.VNDR_DTL_ASND_ID, DETAIL.OBJ_ID, "
            + "DETAIL.VER_NBR, VNDR_PARENT_IND, VNDR_NM, DETAIL.DOBJ_MAINT_CD_ACTV_IND, "
            + "VNDR_INACTV_REAS_CD, VNDR_DUNS_NBR, DETAIL.VNDR_PMT_TERM_CD, DETAIL.VNDR_SHP_TTL_CD, "
            + "DETAIL.VNDR_SHP_PMT_TERM_CD, VNDR_CNFM_IND, VNDR_PRPYMT_IND, VNDR_CCRD_IND, "
            + "VNDR_MIN_ORD_AMT, VNDR_URL_ADDR, VNDR_SOLD_TO_NM, VNDR_RMT_NM, VNDR_RSTRC_IND, "
            + "VNDR_RSTRC_REAS_TXT, VNDR_RSTRC_DT, VNDR_RSTRC_PRSN_ID, VNDR_SOLD_TO_GNRTD_ID, "
            + "VNDR_SOLD_TO_ASND_ID, VNDR_1ST_LST_NM_IND, COLLECT_TAX_IND, DFLT_PMT_MTHD_CD, "
            + "DETAIL.LAST_UPDT_TS, COUNTRY.POSTAL_CNTRY_NM, STATE.POSTAL_STATE_NM, "
            + "HEADER.OBJ_ID AS HEADER_OBJ_ID, ALL_ALIASES, ALL_COMMODITIES, "
            + "ALL_SUPPLIER_DIVERSITIES, HEADER.VNDR_TYP_CD ";

    private static final String PARENT_SQL_FROM = "FROM PUR_VNDR_DTL_T DETAIL ";

    private static final String PARENT_SQL_JOIN =
            "JOIN PUR_VNDR_HDR_T HEADER ON DETAIL.VNDR_HDR_GNRTD_ID = HEADER.VNDR_HDR_GNRTD_ID "
            + "LEFT JOIN SH_CNTRY_T COUNTRY ON HEADER.VNDR_CORP_CTZN_CNTRY_CD = " + "COUNTRY.POSTAL_CNTRY_CD "
            + "JOIN PUR_VNDR_TYP_T VENDOR_TYPE ON HEADER.VNDR_TYP_CD = VENDOR_TYPE.VNDR_TYP_CD "
            + "JOIN PUR_VNDR_ADDR_T ADDRESS ON VENDOR_TYPE.VNDR_ADDR_TYP_REQ_CD = "
            + "ADDRESS.VNDR_ADDR_TYP_CD AND HEADER.VNDR_HDR_GNRTD_ID = ADDRESS.VNDR_HDR_GNRTD_ID "
            + "AND DETAIL.VNDR_DTL_ASND_ID = ADDRESS.VNDR_DTL_ASND_ID AND " + "ADDRESS.VNDR_DFLT_ADDR_IND = 'Y' "
            + "LEFT JOIN SH_ST_T STATE ON ADDRESS.VNDR_ST_CD = STATE.POSTAL_STATE_CD AND ADDRESS.VNDR_CNTRY_CD = "
            + "STATE.POSTAL_CNTRY_CD "
            + "LEFT JOIN (SELECT LISTAGG(DISTINCT VNDR_ALIAS_NM, ', ') WITHIN GROUP (ORDER BY VNDR_ALIAS_NM ASC) AS ALL_ALIASES, "
            + "VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID FROM PUR_VNDR_ALIAS_T "
            + "WHERE DOBJ_MAINT_CD_ACTV_IND = 'Y' GROUP BY VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID) "
            + "ALIASES ON DETAIL.VNDR_HDR_GNRTD_ID = ALIASES.VNDR_HDR_GNRTD_ID AND "
            + "DETAIL.VNDR_DTL_ASND_ID = ALIASES.VNDR_DTL_ASND_ID "
            + "LEFT JOIN (SELECT LISTAGG(DISTINCT PUR_COMM_CD, ', ') WITHIN GROUP (ORDER BY PUR_COMM_CD ASC) AS ALL_COMMODITIES, "
            + "VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID FROM PUR_VNDR_COMM_T "
            + "WHERE DOBJ_MAINT_CD_ACTV_IND = 'Y' GROUP BY VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID) "
            + "COMMODITIES ON DETAIL.VNDR_HDR_GNRTD_ID = COMMODITIES.VNDR_HDR_GNRTD_ID AND "
            + "DETAIL.VNDR_DTL_ASND_ID = COMMODITIES.VNDR_DTL_ASND_ID "
            + "LEFT JOIN (SELECT LISTAGG(DISTINCT VNDR_SUPP_DVRST_DESC, ', ') WITHIN GROUP (ORDER BY VNDR_SUPP_DVRST_DESC ASC) AS ALL_SUPPLIER_DIVERSITIES, "
            + "LISTAGG(DISTINCT SUPP.VNDR_SUPP_DVRST_CD, ', ') WITHIN GROUP (ORDER BY SUPP.VNDR_SUPP_DVRST_CD ASC) AS ALL_SUPPLIER_DIVERSITY_CODES, "
            + "VNDR_HDR_GNRTD_ID "
            + "FROM PUR_SUPP_DVRST_T SUPP JOIN PUR_VNDR_SUPP_DVRST_T VENDOR_SUPP ON "
            + "SUPP.VNDR_SUPP_DVRST_CD = VENDOR_SUPP.VNDR_SUPP_DVRST_CD AND "
            + "VENDOR_SUPP.DOBJ_MAINT_CD_ACTV_IND = 'Y' WHERE SUPP.DOBJ_MAINT_CD_ACTV_IND = 'Y' "
            + "GROUP BY VNDR_HDR_GNRTD_ID) DIVERSITIES ON HEADER.VNDR_HDR_GNRTD_ID = "
            + "DIVERSITIES.VNDR_HDR_GNRTD_ID ";

    private static final String CHILD_SQL_SELECT =
            "SELECT CHILD.VNDR_HDR_GNRTD_ID, CHILD.VNDR_DTL_ASND_ID, CHILD.OBJ_ID, CHILD.VER_NBR, "
            + "CHILD.VNDR_PARENT_IND, PARENT.VNDR_NM || ' > ' || CHILD.VNDR_NM, "
            + "CHILD.DOBJ_MAINT_CD_ACTV_IND, CHILD.VNDR_INACTV_REAS_CD, CHILD.VNDR_DUNS_NBR, "
            + "CHILD.VNDR_PMT_TERM_CD, CHILD.VNDR_SHP_TTL_CD, CHILD.VNDR_SHP_PMT_TERM_CD, "
            + "CHILD.VNDR_CNFM_IND, CHILD.VNDR_PRPYMT_IND, CHILD.VNDR_CCRD_IND, "
            + "CHILD.VNDR_MIN_ORD_AMT, CHILD.VNDR_URL_ADDR, CHILD.VNDR_SOLD_TO_NM, "
            + "CHILD.VNDR_RMT_NM, CHILD.VNDR_RSTRC_IND, CHILD.VNDR_RSTRC_REAS_TXT, "
            + "CHILD.VNDR_RSTRC_DT, CHILD.VNDR_RSTRC_PRSN_ID, CHILD.VNDR_SOLD_TO_GNRTD_ID, "
            + "CHILD.VNDR_SOLD_TO_ASND_ID, CHILD.VNDR_1ST_LST_NM_IND, CHILD.COLLECT_TAX_IND, "
            + "CHILD.DFLT_PMT_MTHD_CD, CHILD.LAST_UPDT_TS, COUNTRY.POSTAL_CNTRY_NM, "
            + "STATE.POSTAL_STATE_NM, HEADER.OBJ_ID AS HEADER_OBJ_ID, ALL_ALIASES, ALL_COMMODITIES, "
            + "ALL_SUPPLIER_DIVERSITIES, HEADER.VNDR_TYP_CD ";

    private static final String CHILD_SQL_FROM = "FROM PUR_VNDR_DTL_T CHILD ";

    private static final String CHILD_SQL_JOIN =
            "JOIN PUR_VNDR_DTL_T PARENT ON PARENT.VNDR_HDR_GNRTD_ID = CHILD.VNDR_HDR_GNRTD_ID AND "
            + "PARENT.VNDR_PARENT_IND = 'Y' "
            + "JOIN PUR_VNDR_HDR_T HEADER ON CHILD.VNDR_HDR_GNRTD_ID = HEADER.VNDR_HDR_GNRTD_ID "
            + "LEFT JOIN SH_CNTRY_T COUNTRY ON HEADER.VNDR_CORP_CTZN_CNTRY_CD = COUNTRY.POSTAL_CNTRY_CD "
            + "JOIN PUR_VNDR_TYP_T VENDOR_TYPE ON HEADER.VNDR_TYP_CD = VENDOR_TYPE.VNDR_TYP_CD "
            + "JOIN PUR_VNDR_ADDR_T ADDRESS ON VENDOR_TYPE.VNDR_ADDR_TYP_REQ_CD = "
            + "ADDRESS.VNDR_ADDR_TYP_CD AND HEADER.VNDR_HDR_GNRTD_ID = ADDRESS.VNDR_HDR_GNRTD_ID AND "
            + "CHILD.VNDR_DTL_ASND_ID = ADDRESS.VNDR_DTL_ASND_ID AND ADDRESS.VNDR_DFLT_ADDR_IND = 'Y' "
            + "LEFT JOIN SH_ST_T STATE ON ADDRESS.VNDR_ST_CD = STATE.POSTAL_STATE_CD AND ADDRESS.VNDR_CNTRY_CD = "
            + "STATE.POSTAL_CNTRY_CD "
            + "LEFT JOIN (SELECT LISTAGG(DISTINCT VNDR_ALIAS_NM, ', ') WITHIN GROUP (ORDER BY VNDR_ALIAS_NM) "
            + "AS ALL_ALIASES, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID FROM PUR_VNDR_ALIAS_T "
            + "WHERE DOBJ_MAINT_CD_ACTV_IND = 'Y' GROUP BY VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID) "
            + "ALIASES ON CHILD.VNDR_HDR_GNRTD_ID = ALIASES.VNDR_HDR_GNRTD_ID AND "
            + "CHILD.VNDR_DTL_ASND_ID = ALIASES.VNDR_DTL_ASND_ID "
            + "LEFT JOIN (SELECT LISTAGG(DISTINCT PUR_COMM_CD, ', ') WITHIN GROUP (ORDER BY PUR_COMM_CD) "
            + "AS ALL_COMMODITIES, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID FROM PUR_VNDR_COMM_T "
            + "WHERE DOBJ_MAINT_CD_ACTV_IND = 'Y' GROUP BY VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID) "
            + "COMMODITIES ON CHILD.VNDR_HDR_GNRTD_ID = COMMODITIES.VNDR_HDR_GNRTD_ID AND "
            + "CHILD.VNDR_DTL_ASND_ID = COMMODITIES.VNDR_DTL_ASND_ID "
            + "LEFT JOIN (SELECT LISTAGG(DISTINCT VNDR_SUPP_DVRST_DESC, ', ') WITHIN GROUP (ORDER BY "
            + "VNDR_SUPP_DVRST_DESC) AS ALL_SUPPLIER_DIVERSITIES, "
            + "LISTAGG(DISTINCT SUPP.VNDR_SUPP_DVRST_CD, ', ') WITHIN GROUP (ORDER BY SUPP.VNDR_SUPP_DVRST_CD) "
            + "AS ALL_SUPPLIER_DIVERSITY_CODES, VNDR_HDR_GNRTD_ID "
            + "FROM PUR_SUPP_DVRST_T SUPP JOIN PUR_VNDR_SUPP_DVRST_T VENDOR_SUPP ON "
            + "SUPP.VNDR_SUPP_DVRST_CD = VENDOR_SUPP.VNDR_SUPP_DVRST_CD AND "
            + "VENDOR_SUPP.DOBJ_MAINT_CD_ACTV_IND = 'Y' WHERE SUPP.DOBJ_MAINT_CD_ACTV_IND = 'Y' "
            + "GROUP BY VNDR_HDR_GNRTD_ID) DIVERSITIES ON CHILD.VNDR_HDR_GNRTD_ID = "
            + "DIVERSITIES.VNDR_HDR_GNRTD_ID ";

    private static final Map<String, String> SORT_MAP = Map.ofEntries(
            entry("vendorName", "VNDR_NM"),
            entry("vendorAliasesForLookup", "ALL_ALIASES"),
            entry("vendorNumber", "VNDR_HDR_GNRTD_ID || '-' || VNDR_DTL_ASND_ID"),
            entry("activeIndicator", "DOBJ_MAINT_CD_ACTV_IND"),
            entry("vendorHeader.vendorTypeCode", "VNDR_TYP_CD"),
            entry("vendorStateForLookup", "POSTAL_STATE_NM"),
            entry("vendorHeader.vendorCountry.name", "POSTAL_CNTRY_NM"),
            entry("vendorCommoditiesForLookup", "ALL_COMMODITIES"),
            entry("vendorSupplierDiversitiesForLookup", "ALL_SUPPLIER_DIVERSITIES")
    );

    private static final String SQL_WILDCARDS = "%_";

    private static final String VENDOR_COMMODITY_CODE =
            VendorPropertyConstants.VENDOR_COMMODITIES_CODE + "." + VendorPropertyConstants.PURCHASING_COMMODITY_CODE;
    private static final String VENDOR_CONTRACT_NUMBER =
            VendorPropertyConstants.VENDOR_CONTRACT + "." + PurapPropertyConstants.VENDOR_CONTRACT_ID;
    private static final String VENDOR_STATE_CODE =
            VendorPropertyConstants.VENDOR_ADDRESS + "." + VendorPropertyConstants.VENDOR_ADDRESS_STATE;
    private static final String VENDOR_SUPPLIER_DIVERSITIES_CODE =
            "vendorHeader." + VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES + ".vendorSupplierDiversityCode";

    private final EncryptionService encryptionService;
    private final JdbcTemplate jdbcTemplate;
    private final VendorDaoOjb vendorDaoOjb;

    public VendorDaoImpl(
            final EncryptionService encryptionService,
            final JdbcTemplate jdbcTemplate,
            final VendorDaoOjb vendorDaoOjb
    ) {
        Validate.isTrue(encryptionService != null, "encryptionService must be provided");
        this.encryptionService = encryptionService;
        Validate.isTrue(jdbcTemplate != null, "jdbcTemplate must be provided");
        this.jdbcTemplate = jdbcTemplate;
        Validate.isTrue(vendorDaoOjb != null, "vendorDaoOjb must be provided");
        this.vendorDaoOjb = vendorDaoOjb;
    }

    @Override
    public VendorContract getVendorB2BContract(
            final VendorDetail vendorDetail,
            final String campus,
            final Date currentSqlDate
    ) {
        return vendorDaoOjb.getVendorB2BContract(vendorDetail, campus, currentSqlDate);
    }

    @Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getVendorDetails(
            final Map<String, String> searchProps,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending
    ) {
        final List<String> sqlParameters = new ArrayList<>();
        final String baseSql = getVendorsSqlWithoutSortOrLimit(searchProps, sqlParameters);
        final String sortedAndLimitedSql = addSortAndLimitSql(baseSql, skip, limit, sortField, sortAscending);
        final List<VendorDetail> vendorDetails = getVendorDetailsInternal(sortedAndLimitedSql, sqlParameters);
        final int recordCount = getVendorDetailsCount(baseSql, sqlParameters);
        vendorDetails.forEach(PersistableBusinessObjectBase::refresh);
        vendorDetails.forEach(PersistableBusinessObjectBase::refreshNonUpdateableReferences);
        return Pair.of(vendorDetails, recordCount);
    }

    private String getVendorsSqlWithoutSortOrLimit(
            final Map<String, String> searchProps,
            final List<? super String> sqlParameters
    ) {
        final StringBuilder parentSqlWhere = new StringBuilder(CAPACITY).append("WHERE VNDR_PARENT_IND = 'Y' ");
        final StringBuilder childSqlWhere = new StringBuilder(CAPACITY).append("WHERE CHILD.VNDR_PARENT_IND = 'N' ");

        addSearchCriteria(parentSqlWhere, childSqlWhere, searchProps, sqlParameters);

        final StringBuilder fullSql = new StringBuilder(CAPACITY).append(PARENT_SQL_SELECT)
                .append(PARENT_SQL_FROM)
                .append(PARENT_SQL_JOIN)
                .append(parentSqlWhere)
                .append("UNION ")
                .append(CHILD_SQL_SELECT)
                .append(CHILD_SQL_FROM)
                .append(CHILD_SQL_JOIN)
                .append(childSqlWhere);
        return fullSql.toString();
    }

    private void addSearchCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final Map<String, String> searchProps,
            final List<? super String> sqlParameters
    ) {
        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_NAME)) {
            addVendorNameCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_NAME)
            );
        }

        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_TAX_NUMBER)) {
            addUSTaxNumberCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_TAX_NUMBER)
            );
        }

        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_FOREIGN_TAX_ID)) {
            addForeignTaxIdCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_FOREIGN_TAX_ID)
            );
        }

        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_NUMBER)) {
            addVendorNumberCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_NUMBER)
            );
        }

        if (searchProps.containsKey("active")) {
            addActiveCriteria(parentSqlWhere, childSqlWhere, sqlParameters, searchProps.get("active"));
        }

        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_TYPE_CODE)) {
            addVendorTypeCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_TYPE_CODE)
            );
        }

        if (searchProps.containsKey(VENDOR_STATE_CODE)) {
            addStateCriteria(parentSqlWhere, childSqlWhere, sqlParameters, searchProps.get(VENDOR_STATE_CODE));
        }

        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_CORP_CITIZEN_CODE)) {
            addCorpCitizenCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_CORP_CITIZEN_CODE)
            );
        }

        if (searchProps.containsKey(VENDOR_COMMODITY_CODE)) {
            addCommodityCodeCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VENDOR_COMMODITY_CODE)
            );
        }

        if (searchProps.containsKey(VENDOR_SUPPLIER_DIVERSITIES_CODE)) {
            addSupplierDiversityCodeCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VENDOR_SUPPLIER_DIVERSITIES_CODE)
            );
        }

        if (searchProps.containsKey(VENDOR_CONTRACT_NUMBER)) {
            addContractNumberCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VENDOR_CONTRACT_NUMBER)
            );
        }

        if (searchProps.containsKey(VendorPropertyConstants.VENDOR_DEBARRED_INDICATOR)) {
            addDebarredCriteria(parentSqlWhere,
                    childSqlWhere,
                    sqlParameters,
                    searchProps.get(VendorPropertyConstants.VENDOR_DEBARRED_INDICATOR)
            );
        }
    }

    private void addVendorNameCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";
        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                .append("NAME_DETAIL WHERE UPPER(NAME_DETAIL.VNDR_NM) ")
                .append(comparator)
                .append(" ? UNION SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_ALIAS_T NAME_ALIAS WHERE ")
                .append("NAME_ALIAS.VNDR_ALIAS_NM ")
                .append(comparator)
                .append(" ? AND NAME_ALIAS.DOBJ_MAINT_CD_ACTV_IND = 'Y')");

        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                .append("NAME_DETAIL WHERE UPPER(NAME_DETAIL.VNDR_NM) ")
                .append(comparator)
                .append(" ? UNION SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_ALIAS_T NAME_ALIAS WHERE ")
                .append("NAME_ALIAS.VNDR_ALIAS_NM ")
                .append(comparator)
                .append(" ? AND NAME_ALIAS.DOBJ_MAINT_CD_ACTV_IND = 'Y')");
        sqlParameters.addAll(List.of(wildcardAdjusted, wildcardAdjusted));
    }

    private String parseWildcard(final String value) {
        return value != null ? StringUtils.replace(StringUtils.replace(value.toUpperCase(Locale.ROOT).trim(), "*", "%"),
                "?",
                "_"
        ) : "";
    }

    // Making protected for CSU to override
    protected void addUSTaxNumberCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        parentSqlWhere.append("AND HEADER.VNDR_US_TAX_NBR = ? ");
        childSqlWhere.append("AND HEADER.VNDR_US_TAX_NBR = ? ");
        try {
            sqlParameters.add(encryptionService.encrypt(value));
        } catch (final GeneralSecurityException e) {
            LOG.atError().withThrowable(e).log("addUSTaxNumberCriteria: Failed to encrypt search criteria");
        }
    }

    // Making protected for CSU to override
    protected void addForeignTaxIdCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        parentSqlWhere.append("AND HEADER.VNDR_FOREIGN_TAX_ID = ? ");
        childSqlWhere.append("AND HEADER.VNDR_FOREIGN_TAX_ID = ? ");
        try {
            sqlParameters.add(encryptionService.encrypt(value));
        } catch (final GeneralSecurityException e) {
            LOG.atError().withThrowable(e).log("addForeignTaxIdCriteria: Failed to encrypt search criteria");
        }
    }

    private void addVendorNumberCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";

        if (value.contains("-")) {
            parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                    .append("NUMBER_DETAIL WHERE NUMBER_DETAIL.VNDR_HDR_GNRTD_ID || '-' || ")
                    .append("NUMBER_DETAIL.VNDR_DTL_ASND_ID ")
                    .append(comparator)
                    .append(" ? )");

            childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                    .append("NUMBER_DETAIL WHERE NUMBER_DETAIL.VNDR_HDR_GNRTD_ID || '-' || ")
                    .append("NUMBER_DETAIL.VNDR_DTL_ASND_ID ")
                    .append(comparator)
                    .append(" ? )");
            sqlParameters.add(wildcardAdjusted);
        } else {
            parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                    .append("NUMBER_DETAIL WHERE NUMBER_DETAIL.VNDR_HDR_GNRTD_ID ")
                    .append(comparator)
                    .append(" ? )");

            childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                    .append("NUMBER_DETAIL WHERE NUMBER_DETAIL.VNDR_HDR_GNRTD_ID ")
                    .append(comparator)
                    .append(" ? )");
            sqlParameters.add(wildcardAdjusted);
        }
    }

    private static void addActiveCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                .append("ACTIVE_DETAIL WHERE ACTIVE_DETAIL.DOBJ_MAINT_CD_ACTV_IND = ?) ");
        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_DTL_T ")
                .append("ACTIVE_DETAIL WHERE ACTIVE_DETAIL.DOBJ_MAINT_CD_ACTV_IND = ?) ");
        sqlParameters.add(value);
    }

    private static void addVendorTypeCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        parentSqlWhere.append("AND HEADER.VNDR_TYP_CD = ? ");
        childSqlWhere.append("AND HEADER.VNDR_TYP_CD = ? ");
        sqlParameters.add(value);
    }

    private void addStateCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";

        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_ADDR_T ")
                .append("ADDRESS_INNER WHERE UPPER(ADDRESS_INNER.VNDR_ST_CD) ")
                .append(comparator)
                .append(" ? )");

        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_ADDR_T ")
                .append("ADDRESS_INNER WHERE UPPER(ADDRESS_INNER.VNDR_ST_CD) ")
                .append(comparator)
                .append(" ? )");
        sqlParameters.add(wildcardAdjusted);
    }

    private void addCorpCitizenCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";

        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_HDR_T ")
                .append("COUNTRY_HEADER WHERE UPPER(COUNTRY_HEADER.VNDR_CORP_CTZN_CNTRY_CD) ")
                .append(comparator)
                .append(" ? )");

        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_HDR_T ")
                .append("COUNTRY_HEADER WHERE UPPER(COUNTRY_HEADER.VNDR_CORP_CTZN_CNTRY_CD) ")
                .append(comparator)
                .append(" ? )");
        sqlParameters.add(wildcardAdjusted);
    }

    private void addCommodityCodeCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";

        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_COMM_T ")
                .append("COMMODITY_DETAIL WHERE UPPER(COMMODITY_DETAIL.PUR_COMM_CD) ")
                .append(comparator)
                .append(" ? AND COMMODITY_DETAIL.DOBJ_MAINT_CD_ACTV_IND = 'Y') ");

        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_COMM_T ")
                .append("COMMODITY_DETAIL WHERE UPPER(COMMODITY_DETAIL.PUR_COMM_CD) ")
                .append(comparator)
                .append(" ? AND COMMODITY_DETAIL.DOBJ_MAINT_CD_ACTV_IND = 'Y') ");
        sqlParameters.add(wildcardAdjusted);
    }

    private void addSupplierDiversityCodeCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";

        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_SUPP_DVRST_T ")
                .append("DIVERSITY_DETAIL WHERE UPPER(DIVERSITY_DETAIL.VNDR_SUPP_DVRST_CD) ")
                .append(comparator)
                .append(" ? AND DIVERSITY_DETAIL.DOBJ_MAINT_CD_ACTV_IND = 'Y') ");

        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_SUPP_DVRST_T ")
                .append("DIVERSITY_DETAIL WHERE UPPER(DIVERSITY_DETAIL.VNDR_SUPP_DVRST_CD) ")
                .append(comparator)
                .append(" ? AND DIVERSITY_DETAIL.DOBJ_MAINT_CD_ACTV_IND = 'Y') ");
        sqlParameters.add(wildcardAdjusted);
    }

    private void addContractNumberCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        final String wildcardAdjusted = parseWildcard(value);
        final String comparator = StringUtils.containsAny(wildcardAdjusted, SQL_WILDCARDS) ? "LIKE" : "=";

        parentSqlWhere.append("AND DETAIL.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_CONTR_T ")
                .append("CONTRACT_DETAIL WHERE UPPER(CONTRACT_DETAIL.VNDR_CONTR_GNRTD_ID) ")
                .append(comparator)
                .append(" ? AND CONTRACT_DETAIL.DOBJ_MAINT_CD_ACTV_IND = 'Y') ");

        childSqlWhere.append("AND CHILD.VNDR_HDR_GNRTD_ID IN (SELECT VNDR_HDR_GNRTD_ID FROM PUR_VNDR_CONTR_T ")
                .append("CONTRACT_DETAIL WHERE UPPER(CONTRACT_DETAIL.VNDR_CONTR_GNRTD_ID) ")
                .append(comparator)
                .append(" ? AND CONTRACT_DETAIL.DOBJ_MAINT_CD_ACTV_IND = 'Y') ");
        sqlParameters.add(wildcardAdjusted);
    }

    private static void addDebarredCriteria(
            final StringBuilder parentSqlWhere,
            final StringBuilder childSqlWhere,
            final List<? super String> sqlParameters,
            final String value
    ) {
        parentSqlWhere.append("AND HEADER.VNDR_DEBRD_IND = ? ");
        childSqlWhere.append("AND HEADER.VNDR_DEBRD_IND = ? ");
        sqlParameters.add(Objects.equals(value, "Yes") ? "Y" : "N");
    }

    private String addSortAndLimitSql(
            final String baseSql,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending
    ) {
        final StringBuilder orderedAndLimited = new StringBuilder(CAPACITY).append(baseSql);

        if (sortField != null && SORT_MAP.containsKey(sortField)) {
            orderedAndLimited.append("ORDER BY ")
                    .append(SORT_MAP.get(sortField))
                    .append(sortAscending ? " ASC " : " DESC ");
        }

        // CU Customization: Backport FINP-11585
        orderedAndLimited.append("OFFSET ").append(skip).append(" ROWS FETCH NEXT ").append(limit).append(" ROWS ONLY ");
        return orderedAndLimited.toString();
    }

    private List<VendorDetail> getVendorDetailsInternal(
            final String sortedAndLimitedSql,
            final List<String> parameters
    ) {
        LOG.trace(
                "getVendorDetailsInternal(...) - Enter - sortedAndLimitedSql={}; parameters={}",
                sortedAndLimitedSql,
                parameters
        );

        // The primary SELECT and the UNION SELECT are identical, so we need to double the parameters
        final List<String> allParameters = new ArrayList<>(parameters);
        allParameters.addAll(parameters);

        try {
            final List<VendorDetail> vendorDetails = jdbcTemplate.query(
                    sortedAndLimitedSql,
                    VendorDaoImpl::mapResultSetToVendorDetail,
                    allParameters.toArray()
            );
            LOG.trace("getVendorDetailsInternal(...) - Exit - vendorDetails={}", vendorDetails);
            return vendorDetails;
        } catch (final DataAccessException e) {
            LOG.atError()
                    .withThrowable(e)
                    .log("getVendorDetailsInternal(...) - Threre was a problem : sortedAndLimitedSql={}; "
                         + "parameters={}",
                            sortedAndLimitedSql,
                            parameters);
            // Throw runtime exception to prevent API from reporting no results, but no issues
            throw new RuntimeException(
                    "getVendorDetailsInternal(...): An error occurred when executing statement");
        }
    }

    private static VendorDetail mapResultSetToVendorDetail(
            final ResultSet rs,
            final int rowNum
    ) throws SQLException {
        final VendorHeader header = new VendorHeader();
        header.setVendorHeaderGeneratedIdentifier(rs.getInt("VNDR_HDR_GNRTD_ID"));
        header.setObjectId(rs.getString("HEADER_OBJ_ID"));

        final VendorDetail detail = new VendorDetail();
        detail.setVendorHeaderGeneratedIdentifier(rs.getInt("VNDR_HDR_GNRTD_ID"));
        detail.setVendorDetailAssignedIdentifier(rs.getInt("VNDR_DTL_ASND_ID"));
        detail.setObjectId(rs.getString("OBJ_ID"));
        detail.setVersionNumber(rs.getLong("VER_NBR"));
        detail.setVendorParentIndicator(Objects.equals(rs.getString("VNDR_PARENT_IND"), "Y"));
        detail.setVendorName(rs.getString("VNDR_NM"));
        detail.setActiveIndicator(Objects.equals(rs.getString("DOBJ_MAINT_CD_ACTV_IND"), "Y"));
        detail.setVendorInactiveReasonCode(rs.getString("VNDR_INACTV_REAS_CD"));
        detail.setVendorDunsNumber(rs.getString("VNDR_DUNS_NBR"));
        detail.setVendorPaymentTermsCode(rs.getString("VNDR_PMT_TERM_CD"));
        detail.setVendorShippingTitleCode(rs.getString("VNDR_SHP_TTL_CD"));
        detail.setVendorShippingPaymentTermsCode(rs.getString("VNDR_SHP_PMT_TERM_CD"));
        detail.setVendorConfirmationIndicator(Objects.equals(rs.getString("VNDR_CNFM_IND"), "Y"));
        detail.setVendorPrepaymentIndicator(Objects.equals(rs.getString("VNDR_PRPYMT_IND"), "Y"));
        detail.setVendorCreditCardIndicator(Objects.equals(rs.getString("VNDR_CCRD_IND"), "Y"));
        detail.setVendorMinimumOrderAmount(new KualiDecimal(rs.getDouble("VNDR_MIN_ORD_AMT")));
        detail.setVendorUrlAddress(rs.getString("VNDR_URL_ADDR"));
        detail.setVendorSoldToName(rs.getString("VNDR_SOLD_TO_NM"));
        detail.setVendorRemitName(rs.getString("VNDR_RMT_NM"));
        detail.setVendorRestrictedIndicator(Objects.equals(rs.getString("VNDR_RSTRC_IND"), "Y"));
        detail.setVendorRestrictedReasonText(rs.getString("VNDR_RSTRC_REAS_TXT"));
        detail.setVendorRestrictedDate(rs.getDate("VNDR_RSTRC_DT"));
        detail.setVendorRestrictedPersonIdentifier(rs.getString("VNDR_RSTRC_PRSN_ID"));
        detail.setVendorSoldToGeneratedIdentifier(rs.getInt("VNDR_SOLD_TO_GNRTD_ID"));
        detail.setVendorSoldToAssignedIdentifier(rs.getInt("VNDR_SOLD_TO_ASND_ID"));
        detail.setVendorFirstLastNameIndicator(Objects.equals(rs.getString("VNDR_1ST_LST_NM_IND"), "Y"));
        detail.setTaxableIndicator(Objects.equals(rs.getString("COLLECT_TAX_IND"), "Y"));
        detail.setDefaultPaymentMethodCode(rs.getString("DFLT_PMT_MTHD_CD"));
        detail.setLastUpdatedTimestamp(new Timestamp(rs.getDate("LAST_UPDT_TS").getTime()));
        detail.setVendorStateForLookup(rs.getString("POSTAL_STATE_NM"));
        detail.setVendorAliasesForLookup(rs.getString("ALL_ALIASES"));
        detail.setVendorCommoditiesForLookup(rs.getString("ALL_COMMODITIES"));
        detail.setVendorSupplierDiversitiesForLookup(rs.getString("ALL_SUPPLIER_DIVERSITIES"));
        detail.setVendorHeader(header);

        return detail;
    }

    private int getVendorDetailsCount(
            final String sql,
            final List<String> parameters
    ) {
        LOG.trace("getVendorDetailsCount(...) - Enter - sql={}; parameters={}", sql, parameters);
        final String countSql = "SELECT COUNT(*) FROM (" + sql + ") DUMMY";

        // The primary SELECT and the UNION SELECT are identical, so we need to double the parameters
        final List<String> allParameters = new ArrayList<>(parameters);
        allParameters.addAll(parameters);

        try {
            final Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, allParameters.toArray());
            if (count == null) {
                throw new RuntimeException("Failed to retrieve count from database.");
            }
            LOG.trace("getVendorDetailsCount(...) - Exit - count={}", count);
            return count;
        } catch (final DataAccessException e) {
            LOG.atError()
                    .withThrowable(e)
                    .log("getVendorDetailsCount(...) - There was a problem : sql={}; parameters={}", sql,
                    parameters);
            // Throw runtime exception to prevent API from reporting no results, but no issues
            throw new RuntimeException("getVendorDetailsCount(...): An error occurred when executing statement");
        }
    }

    @Override
    public List<BusinessObject> getSearchResults(final Map<String, String> fieldValues) {
        return ((CuVendorDao) vendorDaoOjb).getSearchResults(fieldValues);
    }

    @Override
    public Stream<VendorWithTaxId> getPotentialEmployeeVendorsAsCloseableStream() {
        return ((CuVendorDao) vendorDaoOjb).getPotentialEmployeeVendorsAsCloseableStream();
    }

    @Override
    public Stream<VendorDetail> getVendorsForCemiSupplierExtractAsCloseableStream() {
        return ((CuVendorDao) vendorDaoOjb).getVendorsForCemiSupplierExtractAsCloseableStream();
    }

}
