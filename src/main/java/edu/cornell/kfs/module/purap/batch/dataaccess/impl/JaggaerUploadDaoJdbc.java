package edu.cornell.kfs.module.purap.batch.dataaccess.impl;

import java.sql.Date;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerAddressType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerLegalStructure;
import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractAddressUploadDto;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractPartyUploadDto;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.vnd.CUVendorConstants.FIELD_NAMES;

public class JaggaerUploadDaoJdbc extends CuSqlQueryPlatformAwareDaoBaseJdbc implements JaggaerUploadDao {
    private static final Logger LOG = LogManager.getLogger();
    
    protected ISOFIPSConversionService isoFipsConversionService;
    
    @Override
    public List<JaggaerContractPartyUploadDto> findJaggaerContractParty(JaggaerContractUploadProcessingMode processingMode, Date processingDate) {
        try {
            RowMapper<JaggaerContractPartyUploadDto> rowMapper = (resultSet, rowNumber) -> {
                if (rowNumber % 100 == 0) {
                    LOG.info("findJaggaerContractParty, processing row number " + rowNumber);
                }
                JaggaerContractPartyUploadDto dto = new JaggaerContractPartyUploadDto();
                dto.setRowType(JaggaerContractPartyUploadRowType.PARTY);
                dto.setOverrideDupError(CUPurapConstants.FALSE_STRING);
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
            CuSqlQuery sqlQuery = buildVendorSql(processingMode, processingDate);
            logSQL(sqlQuery);
            return queryForValues(sqlQuery, rowMapper, false);
        } catch (Exception e) {
            LOG.error("findJaggaerContractParty, had an error finding jaggaer contract parties: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<JaggaerContractAddressUploadDto> findJaggaerContractAddress(JaggaerContractUploadProcessingMode processingMode, Date processingDate) {
        try {
            RowMapper<JaggaerContractAddressUploadDto> rowMapper = (resultSet, rowNumber) -> {
                if (rowNumber % 100 == 0) {
                    LOG.info("findJaggaerContractAddress, processing row number " + rowNumber);
                }
                JaggaerContractAddressUploadDto dto = new JaggaerContractAddressUploadDto();
                dto.setRowType(JaggaerContractPartyUploadRowType.ADDRESS);
                dto.setAddressID(resultSet.getString(FIELD_NAMES.VNDR_ADDR_GNRTD_ID));
                dto.setSciQuestID(StringUtils.EMPTY);
                dto.setAddressType(JaggaerAddressType.findJaggaerAddressTypeFromKfsAddressTypeCode(resultSet.getString(FIELD_NAMES.VNDR_ADDR_TYP_CD)));
                if (StringUtils.equals(resultSet.getString(FIELD_NAMES.VNDR_DFLT_ADDR_IND), KRADConstants.YES_INDICATOR_VALUE)) {
                    dto.setPrimaryType(dto.getAddressType().jaggaerAddressType);
                } else {
                    dto.setPrimaryType(StringUtils.EMPTY);
                }
                dto.setActive(CUPurapConstants.TRUE_STRING);
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
            CuSqlQuery sqlQuery = buildVendorAddressSql(processingMode, processingDate);
            logSQL(sqlQuery);
            return queryForValues(sqlQuery, rowMapper, false);
        } catch (Exception e) {
            LOG.error("findJaggaerContractAddress, had an error finding jaggaer contract addresses: ", e);
            throw new RuntimeException(e);
        }
    }
    
    private CuSqlQuery buildVendorSql(JaggaerContractUploadProcessingMode processingMode, Date processingDate) {
        CuSqlChunk chunk = CuSqlChunk.of("SELECT VH.VNDR_HDR_GNRTD_ID, VD.VNDR_DTL_ASND_ID, VD.VNDR_NM, VH.VNDR_CORP_CTZN_CNTRY_CD, VH.VNDR_OWNR_CD, VD.VNDR_URL_ADDR ",
                buildFromClause(false),
                buildJoinClauseWithPOVendor(false),
                buildRestrictActiveRows(false),
                buildTimeFrameRestrictionClause(processingMode, processingDate),
                buildOrderByClause());
        return chunk.toQuery();
    }
    
    private CuSqlQuery buildVendorAddressSql(JaggaerContractUploadProcessingMode processingMode, Date processingDate) {
        Collection<String> addressTypes = Arrays.asList(CUPurapConstants.JaggaerAddressType.REMIT.kfsAddressTypeCode, 
                CUPurapConstants.JaggaerAddressType.FULFILLMENT.kfsAddressTypeCode);
        
        CuSqlChunk chunk = CuSqlChunk.of("SELECT VA.VNDR_ADDR_GNRTD_ID, VA.VNDR_ADDR_TYP_CD, VA.VNDR_DFLT_ADDR_IND, VA.VNDR_CNTRY_CD, VA.VNDR_LN1_ADDR, ",
                "VA.VNDR_LN2_ADDR, VA.VNDR_CTY_NM, VA.VNDR_ST_CD, VA.VNDR_ADDR_INTL_PROV_NM, VA.VNDR_ZIP_CD, VA.VNDR_HDR_GNRTD_ID, VA.VNDR_DTL_ASND_ID ",
                buildFromClause(true),
                buildJoinClauseWithPOVendor(true),
                buildRestrictActiveRows(true),
                buildTimeFrameRestrictionClause(processingMode, processingDate),
                "AND VA.VNDR_ADDR_TYP_CD in (",
                CuSqlChunk.forStringParameters(addressTypes),
                ") ",
                buildOrderByClause());
        return chunk.toQuery();
    }
    
    private CuSqlChunk buildFromClause(boolean includeVendorAddress) {
        CuSqlChunk chunk = CuSqlChunk.of("FROM KFS.PUR_VNDR_HDR_T VH, KFS.PUR_VNDR_DTL_T VD");
        if (includeVendorAddress) {
            chunk.append(", KFS.PUR_VNDR_ADDR_T VA");
        }
        chunk.append(StringUtils.SPACE);
        return chunk;
    }
    
    private CuSqlChunk buildJoinClauseWithPOVendor(boolean includeVendorAddress) {
        CuSqlChunk chunk = CuSqlChunk.of("WHERE VH.VNDR_HDR_GNRTD_ID = VD.VNDR_HDR_GNRTD_ID ");
        if (includeVendorAddress) {
            chunk.append("AND VA.VNDR_HDR_GNRTD_ID = VD.VNDR_HDR_GNRTD_ID ",
                    "AND VA.VNDR_DTL_ASND_ID = VD.VNDR_DTL_ASND_ID ");
        }
        chunk.append("AND VH.VNDR_TYP_CD = ", 
                CuSqlChunk.forParameter(CUPurapConstants.JaggaerAddressType.FULFILLMENT.kfsAddressTypeCode),
                StringUtils.SPACE);
        return chunk;
    }
    
    private CuSqlChunk buildRestrictActiveRows(boolean includeVendorAddress) {
        CuSqlChunk chunk = CuSqlChunk.of("AND VD.DOBJ_MAINT_CD_ACTV_IND = ", 
                CuSqlChunk.forParameter(KFSConstants.ACTIVE_INDICATOR));
        if (includeVendorAddress) {
            chunk.append(" AND VA.DOBJ_MAINT_CD_ACTV_IND = ", 
                    CuSqlChunk.forParameter(KFSConstants.ACTIVE_INDICATOR));
        }
        chunk.append(StringUtils.SPACE);
        return chunk;
    }
    
    private CuSqlChunk buildTimeFrameRestrictionClause(JaggaerContractUploadProcessingMode processingMode, Date processingDate) {
        if (processingMode == JaggaerContractUploadProcessingMode.PO) {
            CuSqlChunk chunk = CuSqlChunk.of("AND VH.VNDR_HDR_GNRTD_ID IN (",
                    buildPurchaseOrderLimitSubQuery(processingDate),
                    ") ");
            return chunk;
        } else {
            CuSqlChunk chunk = CuSqlChunk.of("AND VD.LAST_UPDT_TS > ",
                    CuSqlChunk.forParameter(Types.DATE, processingDate),
                    StringUtils.SPACE);
            return chunk;
        }
    }
    
    private CuSqlChunk buildPurchaseOrderLimitSubQuery(Date processingDate) {
        CuSqlChunk chunk = CuSqlChunk.of("SELECT VNDR_HDR_GNRTD_ID FROM (",
                "SELECT VNDR_HDR_GNRTD_ID, COUNT(1) ",
                "FROM KFS.PUR_PO_T ",
                "WHERE PO_LST_TRNS_DT > ",
                CuSqlChunk.forParameter(Types.DATE, processingDate),
                " GROUP BY VNDR_HDR_GNRTD_ID HAVING COUNT(1) > 1)");
        return chunk;
    }
    
    private CuSqlChunk buildOrderByClause() {
        CuSqlChunk chunk = CuSqlChunk.of("ORDER BY VD.VNDR_HDR_GNRTD_ID, VD.VNDR_DTL_ASND_ID");
        return chunk;
    }
    
    private String buildVendorNumber(String vendorHeaderId, String vendorDetailId) {
        return vendorHeaderId + KFSConstants.DASH + vendorDetailId;
    }
    
    private String convertToISOCountry(String fipsCountryCode) {
        if (StringUtils.isBlank(fipsCountryCode)) {
            LOG.debug("convertToISOCountry, empty FIPS country found returning US as default");
            fipsCountryCode = KFSConstants.COUNTRY_CODE_UNITED_STATES;
        }
        
        String isoCountry = StringUtils.EMPTY;
        
        try {
            isoCountry = isoFipsConversionService.convertFIPSCountryCodeToActiveISOCountryCode(fipsCountryCode);
        } catch (RuntimeException runtimeException) {
            LOG.error("convertToISOCountry, returning empty string, unable to get ISO country for FIPS country " + fipsCountryCode, runtimeException);
        }
        
        return isoCountry;
    }

    public void setIsoFipsConversionService(ISOFIPSConversionService isoFipsConversionService) {
        this.isoFipsConversionService = isoFipsConversionService;
    }

}
