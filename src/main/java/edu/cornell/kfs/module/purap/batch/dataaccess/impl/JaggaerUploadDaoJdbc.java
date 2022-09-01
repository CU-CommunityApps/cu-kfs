package edu.cornell.kfs.module.purap.batch.dataaccess.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.vnd.CUVendorConstants.FIELD_NAMES;

import org.springframework.jdbc.core.RowMapper;

public class JaggaerUploadDaoJdbc extends PlatformAwareDaoBaseJdbc implements JaggaerUploadDao {
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public List<JaggaerContractPartyUploadDto> findJaggaerContractParty(JaggaerContractUploadProcessingMode processingMode, String processingDate) {
        try {
            RowMapper<JaggaerContractPartyUploadDto> rowMapper = (resultSet, rowNumber) -> {
                JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
                dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
                dto.setOverrideDupError(false);
                dto.setERPNumber(buildVendorNumber(resultSet.getString(FIELD_NAMES.VNDR_HDR_GNRTD_ID), resultSet.getString(FIELD_NAMES.VNDR_DTL_ASND_ID)));
                dto.setSciQuestID(StringUtils.EMPTY);
                dto.setContractPartyName(resultSet.getString(FIELD_NAMES.VNDR_NM));
                dto.setDoingBusinessAs(StringUtils.EMPTY);
                dto.setOtherNames(StringUtils.EMPTY);
                dto.setCountryOfOrigin(convertToISOCountry(resultSet.getString(FIELD_NAMES.VNDR_CORP_CTZN_CNTRY_CD)));
                dto.setActive(StringUtils.EMPTY);
                dto.setContractPartyType(JaggaerContractPartyType.SUPPLIER);
                dto.setPrimary(StringUtils.EMPTY);
                dto.setLegalStructure(JaggaerLegalStructure.findJaggaerLegalStructureByKFSOwnershipCode(resultSet.getString(FIELD_NAMES.VNDR_OWNR_CD)));
                dto.setTaxIDType(StringUtils.EMPTY);
                dto.setTaxIdentificationNumber(StringUtils.EMPTY);
                dto.setVATRegistrationNumber(StringUtils.EMPTY);
                dto.setWebsiteURL(resultSet.getString(FIELD_NAMES.VNDR_URL_ADDR));
                return dto;
            };
            return this.getJdbcTemplate().query(buildVendorSql(processingMode, processingDate), rowMapper);
        } catch (Exception e) {
            LOG.error("findJaggaerContractParty, had an error finding jaggaer contract parties: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<JaggaerContractAddressUploadDto> findJaggaerContractAddress(JaggaerContractUploadProcessingMode processingMode, String processingDate) {
        try {
            RowMapper<JaggaerContractAddressUploadDto> rowMapper = (resultSet, rowNumber) -> {
                JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
                dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
                dto.setAddressID(resultSet.getString(FIELD_NAMES.VNDR_ADDR_GNRTD_ID));
                dto.setSciQuestID(StringUtils.EMPTY);
                dto.setAddressType(JaggaerAddressType.findJaggaerAddressTypeFromKfsAddressTypeCode(resultSet.getString(FIELD_NAMES.VNDR_ADDR_TYP_CD)));
                if (StringUtils.equals(resultSet.getString(FIELD_NAMES.VNDR_DFLT_ADDR_IND), "Y")) {
                    dto.setPrimaryType(dto.getAddressTypeName());
                } else {
                    dto.setPrimaryType(StringUtils.EMPTY);
                }
                dto.setActive(StringUtils.EMPTY);
                dto.setCountry(convertToISOCountry(resultSet.getString(FIELD_NAMES.VNDR_ADDRESS_CNTRY_CD)));
                dto.setStreetLine1(resultSet.getString(FIELD_NAMES.VNDR_LN1_ADDR));
                dto.setStreetLine2(resultSet.getString(FIELD_NAMES.VNDR_LN2_ADDR));
                dto.setStreetLine3(StringUtils.EMPTY);
                dto.setCity(resultSet.getString(FIELD_NAMES.VNDR_CTY_NM));
                
                String state = resultSet.getString(FIELD_NAMES.VNDR_ST_CD);
                if (StringUtils.isBlank(state)) {
                    state = resultSet.getString(FIELD_NAMES.VNDR_ADDR_INTL_PROV_NM);
                }
                dto.setState(state);
                
                dto.setPostalCode(resultSet.getString(FIELD_NAMES.VNDR_ZIP_CD));
                dto.setPhone(StringUtils.EMPTY);
                dto.setTollFreeNumber(StringUtils.EMPTY);
                dto.setFax(StringUtils.EMPTY);
                dto.setNotes(StringUtils.EMPTY);
                dto.setERPNumber(buildVendorNumber(resultSet.getString(FIELD_NAMES.VNDR_HDR_GNRTD_ID), resultSet.getString(FIELD_NAMES.VNDR_DTL_ASND_ID)));
                
                return dto;
            };
            return this.getJdbcTemplate().query(buildVendorAddressSql(processingMode, processingDate), rowMapper);
        } catch (Exception e) {
            LOG.error("findJaggaerContractAddress, had an error finding jaggaer contract addresses: ", e);
            throw new RuntimeException(e);
        }
    }
    
    private String buildVendorSql(JaggaerContractUploadProcessingMode processingMode, String processingDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT VH.VNDR_HDR_GNRTD_ID, VD.VNDR_DTL_ASND_ID, VD.VNDR_NM, VH.VNDR_CORP_CTZN_CNTRY_CD, VH.VNDR_OWNR_CD, VD.VNDR_URL_ADDR ");
        sb.append(buildFromClause(false));
        sb.append(buildJoinClauseWithPOVendors(false));
        sb.append(buildRestrictActiveRows(false));
        sb.append(buildTimeFrameRestrictionClause(processingMode, processingDate));
        sb.append(buildOrderByClause());
        
        String sql = sb.toString();
        LOG.info("buildVendorSql, SQL: " + sql);
        return sql;
    }
    
    private String buildVendorAddressSql(JaggaerContractUploadProcessingMode processingMode, String processingDate) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT VA.VNDR_ADDR_GNRTD_ID, VA.VNDR_ADDR_TYP_CD, VA.VNDR_DFLT_ADDR_IND, VA.VNDR_CNTRY_CD, VA.VNDR_LN1_ADDR, ");
        sb.append("VA.VNDR_LN2_ADDR, VA.VNDR_CTY_NM, VA.VNDR_ST_CD, VA.VNDR_ADDR_INTL_PROV_NM, VA.VNDR_ZIP_CD, VA.VNDR_HDR_GNRTD_ID, VA.VNDR_DTL_ASND_ID ");
        sb.append(buildFromClause(true));
        sb.append(buildJoinClauseWithPOVendors(true));
        sb.append(buildRestrictActiveRows(true));
        sb.append(buildTimeFrameRestrictionClause(processingMode, processingDate));
        sb.append("AND VA.VNDR_ADDR_TYP_CD in ('RM', 'PO') ");
        sb.append(buildOrderByClause());
        
        String sql = sb.toString();
        LOG.info("buildVendorAddressSql, SQL: " + sql);
        return sql;
    }
    
    private String buildFromClause(boolean includeVendorAddress) {
        StringBuilder sb = new StringBuilder("FROM KFS.PUR_VNDR_HDR_T VH, KFS.PUR_VNDR_DTL_T VD");
        if (includeVendorAddress) {
            sb.append(", KFS.PUR_VNDR_ADDR_T VA");
        }
        sb.append(StringUtils.SPACE);
        return sb.toString();
    }
    
    private String buildJoinClauseWithPOVendors(boolean includeVendorAddress) {
        StringBuilder sb = new StringBuilder("WHERE VH.VNDR_HDR_GNRTD_ID = VD.VNDR_HDR_GNRTD_ID ");
        if (includeVendorAddress) {
            sb.append("AND VA.VNDR_HDR_GNRTD_ID = VD.VNDR_HDR_GNRTD_ID ");
            sb.append("AND VA.VNDR_DTL_ASND_ID = VD.VNDR_DTL_ASND_ID ");
        }
        sb.append("AND VH.VNDR_TYP_CD = 'PO' ");
        return sb.toString();
    }
    
    private String buildRestrictActiveRows(boolean includeVendorAddress) {
        StringBuilder sb = new StringBuilder("AND VD.DOBJ_MAINT_CD_ACTV_IND = 'Y' ");
        if (includeVendorAddress) {
            sb.append("AND VA.DOBJ_MAINT_CD_ACTV_IND = 'Y' ");
        }
        return sb.toString();
    }
    
    private String buildTimeFrameRestrictionClause(JaggaerContractUploadProcessingMode processingMode, String processingDate) {
        StringBuilder sb = new StringBuilder();
        if (processingMode == JaggaerContractUploadProcessingMode.PO) {
            sb.append("AND VH.VNDR_HDR_GNRTD_ID IN (").append(buildPurchaseOrderLimitSubQuery(processingDate)).append(") ");
        } else {
            sb.append("AND VD.LAST_UPDT_TS > TO_DATE('").append(processingDate).append("', 'YYYY-MM-DD') ");
        }
        return sb.toString();
    }
    
    private String buildPurchaseOrderLimitSubQuery(String processingDate) {
        StringBuilder sb = new StringBuilder("SELECT VNDR_HDR_GNRTD_ID FROM (");
        sb.append("SELECT VNDR_HDR_GNRTD_ID, COUNT(1) ");
        sb.append("FROM KFS.PUR_PO_T ");
        sb.append("WHERE PO_LST_TRNS_DT > TO_DATE('").append(processingDate).append("', 'YYYY-MM-DD') ");
        sb.append("GROUP BY VNDR_HDR_GNRTD_ID ");
        sb.append("HAVING COUNT(1) > 1)");
        return sb.toString();
    }
    
    private String buildOrderByClause() {
        return "ORDER BY VD.VNDR_HDR_GNRTD_ID, VD.VNDR_DTL_ASND_ID";
    }
    
    private String buildVendorNumber(String vendorHeaderId, String vendorDetailId) {
        return vendorHeaderId + KFSConstants.DASH + vendorDetailId;
    }
    
    private String convertToISOCountry(String fIPSCountry) {
        if (StringUtils.isBlank(fIPSCountry)) {
            LOG.debug("convertToISOCountry, empty FIPS country found returning US as default");
            fIPSCountry = "US";
        }
        /*
         * @TODO use generic mapping framework
         */
        return fIPSCountry;
    }

}
