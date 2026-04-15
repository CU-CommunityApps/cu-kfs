package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiOrderFromSupplierDao;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiOrderFromSupplierDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiOrderFromSupplierDao {

    private static final Logger LOG = LogManager.getLogger();

    private static final Pattern SUPPLIER_ADDRESS_ID_PATTERN = Pattern.compile("^SUPP\\d+_(\\d+)_\\d+$");
    private static final int ADDRESS_BATCH_SIZE = 200;

    @Override
    public void clearExistingListOfKfsVendorAddressLinks() {
        LOG.info("clearExistingListOfKfsVendorAddressLinks was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_VNDR_ADDR_LNK_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfKfsVendorAddressLinks finished truncating table.");
    }

    @Override
    public void clearExistingListOfSupplierAddressLinks() {
        LOG.info("clearExistingListOfSupplierAddressLinks was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_SUPP_ADDR_LNK_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfSupplierAddressLinks finished truncating table.");
    }

    @Override
    public void clearExistingListOfExtractablePurchaseOrderAddressIds() {
        LOG.info("clearExistingListOfExtractablePurchaseOrderAddressIds was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_ADDR_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractablePurchaseOrderAddressIds finished truncating table.");
    }

    @Override
    public void updateOrderFromSupplierExtractQuerySettings(final String supplierJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE KFS.CU_CEMI_ORD_FRM_SUPP_QUERY_SETTINGS_T ")
                .append("SET SUPP_EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDate)
                .toQuery();

        final int numRowsUpdated = executeUpdate(query);
        if (numRowsUpdated != 1) {
            LOG.error("updateOrderFromSupplierExtractQuerySettings, Query should have updated 1 row, "
                    + "but it updated {} instead", numRowsUpdated);
            throw new RuntimeException("Failed to update Remit To Supplier query settings");
        }
    }

    @Override
    public void storeAsListOfKfsVendorAddressLinks(final Iterator<VendorAddress> addressIterator) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_VNDR_ADDR_LNK_T (")
                .append("VNDR_ADDR_GNRTD_ID, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, CONCAT_ADDR")
                .append(") VALUES (")
                .appendAsParameter(Types.INTEGER, VendorAddress::getVendorAddressGeneratedIdentifier)
                .append(CUKFSConstants.COMMA_AND_SPACE)
                .appendAsParameter(Types.INTEGER, VendorAddress::getVendorHeaderGeneratedIdentifier)
                .append(CUKFSConstants.COMMA_AND_SPACE)
                .appendAsParameter(Types.INTEGER, VendorAddress::getVendorDetailAssignedIdentifier)
                .append(CUKFSConstants.COMMA_AND_SPACE)
                .appendAsParameter(Types.VARCHAR, this::getConcatenatedKfsVendorAddressData)
                .append(")")
                .toQuery();

        storeAsListOfAddressLinks(VendorAddress.class, query, addressIterator);
    }

    private String getConcatenatedKfsVendorAddressData(final VendorAddress vendorAddress) {
        return CemiUtils.generateConcatenatedKey(
                vendorAddress.getVendorLine1Address(), vendorAddress.getVendorLine2Address(),
                vendorAddress.getVendorCityName(), vendorAddress.getVendorStateCode(),
                vendorAddress.getVendorZipCode(), vendorAddress.getVendorCountryCode());
    }

    private <T> void storeAsListOfAddressLinks(final Class<T> addressClass, final CuSqlQuery query,
            final Iterator<T> addressIterator) {
        int count = 0;
        final List<T> batchedAddresses = new ArrayList<>(ADDRESS_BATCH_SIZE);

        for (final T address : IteratorUtils.asIterable(addressIterator)) {
            count++;
            batchedAddresses.add(address);
            if (batchedAddresses.size() >= ADDRESS_BATCH_SIZE) {
                storeSubListOfAddressLinks(addressClass, query, batchedAddresses);
                batchedAddresses.clear();
            }
        }

        if (batchedAddresses.size() > 0) {
            storeSubListOfAddressLinks(addressClass, query, batchedAddresses);
        }
        LOG.info("queryAndStoreListOfAddressLinks, Finished writing {} {} links",
                count, addressClass.getSimpleName());
    }

    private <T> void storeSubListOfAddressLinks(final Class<T> addressClass, final CuSqlQuery query,
            final List<T> addresses) {
        final int[] resultCounts = executeBatchUpdate(query, addresses);
        int index = 0;
        for (final int resultCount : resultCounts) {
            if (resultCount != 1) {
                LOG.warn("storeListOfAddressLinks, In batch update of {} {} links, the update at index {} "
                        + "should have inserted exactly 1 row, but it actually inserted {} rows instead",
                        addresses.size(), addressClass.getSimpleName(), index, resultCount);
            }
            index++;
        }
    }

    @Override
    public void storeAsListOfSupplierAddressLinks(final Iterator<CemiSupplierAddressBo> addressIterator) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_SUPP_ADDR_LNK_T (")
                .append("SUPP_ADDR_ID, VNDR_ADDR_GNRTD_ID, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, CONCAT_ADDR")
                .append(") ")
                .append("SELECT ")
                .appendAsParameter(Types.VARCHAR, CemiSupplierAddressBo::getAddressId)
                .append(", ")
                .appendAsParameter(Types.INTEGER, this::extractKfsVendorId)
                .append(", ")
                .append("VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, ")
                .appendAsParameter(Types.VARCHAR, this::getConcatenatedSupplierAddressData)
                .append(" FROM KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T ")
                .append("WHERE WKDY_SPLR_ID = ")
                        .appendAsParameter(Types.VARCHAR, CemiSupplierAddressBo::getSupplierId)
                .append(" AND EXTR_FILE_RUNDATE = ")
                        .appendAsParameter(Types.VARCHAR, CemiSupplierAddressBo::getJobRunDate)
                .toQuery();

        storeAsListOfAddressLinks(CemiSupplierAddressBo.class, query, addressIterator);
    }

    private Integer extractKfsVendorId(final CemiSupplierAddressBo supplierAddress) {
        final String supplierAddressId = supplierAddress.getAddressId();
        final Matcher addressIdMatcher = SUPPLIER_ADDRESS_ID_PATTERN.matcher(supplierAddressId);
        String kfsAddressId = null;
        if (addressIdMatcher.matches()) {
            kfsAddressId = addressIdMatcher.group(1);
        }
        Validate.validState(StringUtils.isNumeric(kfsAddressId),
                "Supplier Address did not properly embed a numeric KFS Vendor Address in its ID: %s", supplierAddressId);
        return Integer.valueOf(kfsAddressId);
    }

    private String getConcatenatedSupplierAddressData(final CemiSupplierAddressBo supplierAddress) {
        return CemiUtils.generateConcatenatedKey(
                    supplierAddress.getAddressLine1(), supplierAddress.getAddressLine2(),
                    supplierAddress.getCity(), supplierAddress.getState(),
                    supplierAddress.getZipCode(), supplierAddress.getCountryForAddress());
    }

    @Override
    public void queryAndStoreAddressIdsForOrderFromSupplierExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_ADDR_T (")
                .append("SUPP_EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID")
                .append(") ")
                .append("SELECT DISTINCT EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID ")
                .append("FROM KFS.CU_CEMI_ORD_FRM_SUPP_PO_ADDRESSES_FINAL_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreAddressIdsForOrderFromSupplierExtract, Found {} potential addresses to extract",
                numRowsInserted);
    }

    @Override
    public boolean determineIfSupplierIsUsedForPunchouts(final String supplierId, final String supplierJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT COUNT(1) FROM KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T EXT ")
                .append("JOIN KFS.CU_CEMI_EXTR_ORD_FRM_SUPP_B2B_VNDR_T B2B ")
                        .append("ON EXT.VNDR_HDR_GNRTD_ID = B2B.VNDR_HDR_GNRTD_ID ")
                        .append("AND EXT.VNDR_DTL_ASND_ID = B2B.VNDR_DTL_ASND_ID ")
                .append("WHERE EXT.WKDY_SPLR_ID = ").appendAsParameter(supplierId)
                .append(" AND EXT.EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDate)
                .toQuery();

        final int punchoutSupplierCount = queryForResults(query, resultSet -> {
            Validate.validState(resultSet.next(), "Punchout supplier count query didn't return any count results");
            return resultSet.getInt(1);
        });
        return punchoutSupplierCount > 0;
    }

}
