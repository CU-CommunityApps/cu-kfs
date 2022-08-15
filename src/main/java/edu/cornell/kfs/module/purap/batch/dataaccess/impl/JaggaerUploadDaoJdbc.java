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
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

import org.springframework.jdbc.core.RowMapper;

public class JaggaerUploadDaoJdbc extends PlatformAwareDaoBaseJdbc implements JaggaerUploadDao {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String VNDR_HDR_GNRTD_ID = "VNDR_HDR_GNRTD_ID";
    private static final String VNDR_DTL_ASND_ID = "VNDR_DTL_ASND_ID";
    private static final String VNDR_NM = "VNDR_NM";
    private static final String VNDR_CORP_CTZN_CNTRY_CD = "VNDR_CORP_CTZN_CNTRY_CD";
    private static final String VNDR_OWNR_CD = "VNDR_OWNR_CD";
    private static final String VNDR_URL_ADDR = "VNDR_URL_ADDR";
    
    private static final String VNDR_ADDR_GNRTD_ID = "VNDR_ADDR_GNRTD_ID";
    private static final String VNDR_ADDR_TYP_CD = "VNDR_ADDR_TYP_CD";
    private static final String VNDR_DFLT_ADDR_IND = "VNDR_DFLT_ADDR_IND";
    private static final String VNDR_CNTRY_CD = "VNDR_CNTRY_CD";
    private static final String VNDR_LN1_ADDR = "VNDR_LN1_ADDR";
    private static final String VNDR_LN2_ADDR = "VNDR_LN2_ADDR";
    private static final String VNDR_CTY_NM = "VNDR_CTY_NM";
    private static final String VNDR_ST_CD = "VNDR_ST_CD";
    private static final String VNDR_ADDR_INTL_PROV_NM = "VNDR_ADDR_INTL_PROV_NM";
    private static final String VNDR_ZIP_CD = "VNDR_ZIP_CD";

    @Override
    public List<JaggaerContractPartyUploadDto> findJaggaerContractParty() {
        try {
            RowMapper<JaggaerContractPartyUploadDto> rowMapper = (resultSet, rowNumber) -> {
                JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
                dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
                dto.setOverrideDupError(false);
                dto.setERPNumber(buildVendorNumber(resultSet.getString(VNDR_HDR_GNRTD_ID), resultSet.getString(VNDR_DTL_ASND_ID)));
                dto.setSciQuestID(StringUtils.EMPTY);
                dto.setContractPartyName(resultSet.getString(VNDR_NM));
                dto.setDoingBusinessAs(StringUtils.EMPTY);
                dto.setOtherNames(StringUtils.EMPTY);
                dto.setCountryOfOrigin(convertToISOCountry(resultSet.getString(VNDR_CORP_CTZN_CNTRY_CD)));
                dto.setActive(StringUtils.EMPTY);
                dto.setContractPartyType(JaggaerContractPartyType.SUPPLIER);
                dto.setPprimary(StringUtils.EMPTY);
                dto.setLegalStructure(JaggaerLegalStructure.findJaggaerLegalStructureByKFSOwnershipCode(resultSet.getString(VNDR_OWNR_CD)));
                dto.setTaxIDType(StringUtils.EMPTY);
                dto.setTaxIdentificationNumber(StringUtils.EMPTY);
                dto.setVATRegistrationNumber(StringUtils.EMPTY);
                dto.setWebsiteURL(resultSet.getString(VNDR_URL_ADDR));
                return dto;
            };
            return this.getJdbcTemplate().query(buildVendorSql(), rowMapper);
        } catch (Exception e) {
            LOG.error("findJaggaerContractParty, had an error finding jaggaer contract partiess: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<JaggaerContractAddressUploadDto> findJaggaerContractAddress() {
        try {
            RowMapper<JaggaerContractAddressUploadDto> rowMapper = (resultSet, rowNumber) -> {
                JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
                dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
                dto.setAddressID(resultSet.getString(VNDR_ADDR_GNRTD_ID));
                dto.setSciQuestID(StringUtils.EMPTY);
                dto.setAddressType(JaggaerAddressType.findJaggaerAddressTypeFromKfsAddressTypeCode(resultSet.getString(VNDR_ADDR_TYP_CD)));
                dto.setPrimaryType(resultSet.getString(VNDR_DFLT_ADDR_IND));
                dto.setActive(StringUtils.EMPTY);
                dto.setCountry(convertToISOCountry(resultSet.getString(VNDR_CNTRY_CD)));
                dto.setStreetLine1(resultSet.getString(VNDR_LN1_ADDR));
                dto.setStreetLine2(resultSet.getString(VNDR_LN2_ADDR));
                dto.setStreetLine3(StringUtils.EMPTY);
                dto.setCity(resultSet.getString(VNDR_CTY_NM));
                dto.setState(resultSet.getString(VNDR_ST_CD));
                dto.setPostalCode(resultSet.getString(VNDR_ZIP_CD));
                dto.setPhone(StringUtils.EMPTY);
                dto.setTollFreeNumber(StringUtils.EMPTY);
                dto.setFax(StringUtils.EMPTY);
                dto.setNotes(StringUtils.EMPTY);
                
                
                dto.setERPNumber(buildVendorNumber(resultSet.getString(VNDR_HDR_GNRTD_ID), resultSet.getString(VNDR_DTL_ASND_ID)));
                
                
                return dto;
            };
            return this.getJdbcTemplate().query(buildVendorAddressSql(), rowMapper);
        } catch (Exception e) {
            LOG.error("findJaggaerContractAddress, had an error finding jaggaer contract addresses: ", e);
            throw new RuntimeException(e);
        }
    }
    
    private String buildVendorSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT VH.VNDR_HDR_GNRTD_ID, VD.VNDR_DTL_ASND_ID, VD.VNDR_NM, VH.VNDR_CORP_CTZN_CNTRY_CD, VH.VNDR_OWNR_CD, VD.VNDR_URL_ADDR ");
        sb.append("FROM KFS.PUR_VNDR_HDR_T VH, KFS.PUR_VNDR_DTL_T VD ");
        sb.append("WHERE VH.VNDR_HDR_GNRTD_ID = VD.VNDR_HDR_GNRTD_ID ");
        sb.append("AND VH.VNDR_TYP_CD = 'PO' ");
        sb.append("AND VD.DOBJ_MAINT_CD_ACTV_IND = 'Y' ");
        sb.append("AND VH.VNDR_HDR_GNRTD_ID IN (").append(buildPurchasOrderLimitSubQuery()).append(")");
        String sql = sb.toString();
        LOG.info("buildVendorSql, SQL: " + sql);
        return sql;
    }
    
    private String buildVendorAddressSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT VNDR_ADDR_GNRTD_ID, VNDR_ADDR_TYP_CD, VNDR_DFLT_ADDR_IND, VNDR_CNTRY_CD, VNDR_LN1_ADDR, ");
        sb.append("VNDR_LN2_ADDR, VNDR_CTY_NM, VNDR_ST_CD, VNDR_ADDR_INTL_PROV_NM, VNDR_ZIP_CD, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID ");
        sb.append("FROM KFS.PUR_VNDR_ADDR_T ");
        sb.append("WHERE DOBJ_MAINT_CD_ACTV_IND = 'Y' ");
        sb.append("AND VNDR_HDR_GNRTD_ID IN (").append(buildPurchasOrderLimitSubQuery()).append(")");
        String sql = sb.toString();
        LOG.info("buildVendorAddressSql, SQL: " + sql);
        return sql;
    }
    
    private String buildPurchasOrderLimitSubQuery() {
        StringBuilder sb = new StringBuilder("SELECT VNDR_HDR_GNRTD_ID FROM (");
        sb.append("SELECT VNDR_HDR_GNRTD_ID, COUNT(1) ");
        sb.append("FROM KFS.PUR_PO_T ");
        sb.append("WHERE PO_LST_TRNS_DT > TO_DATE('").append(findActicePOFromDate()).append("', 'YYYY-MM-DD') ");
        sb.append("GROUP BY VNDR_HDR_GNRTD_ID ");
        sb.append("HAVING COUNT(1) > 1)");
        return sb.toString();
    }
    
    private String findActicePOFromDate() {
        /**
         * @todo this should come froma  parameter
         */
        //return "2018-07-01";
        return "2022-07-01";
    }
    
    private String buildVendorNumber(String vendorHeaderId, String venderDetailId) {
        return vendorHeaderId + KFSConstants.DASH + venderDetailId;
    }
    
    private String convertToISOCountry(String fIPSCountry) {
        if (StringUtils.isBlank(fIPSCountry)) {
            LOG.debug("convertToISOCountry, empty FIPS country found returning US as default");
            fIPSCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.fipsCountryCode;
        }
        /*
         * @TODO use generic mapping framework
         */
        return fIPSCountry;
                
    }

}