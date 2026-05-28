package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiSupplierOrderFromDao;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiSupplierOrderFromDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiSupplierOrderFromDao {

    private static final Logger LOG = LogManager.getLogger();

    private static final Pattern SUPPLIER_ADDRESS_ID_PATTERN = Pattern.compile("^SUPP\\d+_(\\d+)_\\d+$");
    private static final int ADDRESS_BATCH_SIZE = 200;

    @Override
    public void clearExistingListOfKfsVendorAddressLinks() {
        LOG.info("clearExistingListOfKfsVendorAddressLinks was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_EXTR_SUPP_ORD_FRM_VNDR_ADDR_LNK_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfKfsVendorAddressLinks finished truncating table.");
    }

    @Override
    public void clearExistingListOfSupplierAddressLinks() {
        LOG.info("clearExistingListOfSupplierAddressLinks was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_EXTR_SUPP_ORD_FRM_SUPP_ADDR_LNK_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfSupplierAddressLinks finished truncating table.");
    }

    @Override
    public void clearExistingListOfExtractablePurchaseOrderAddressIds() {
        LOG.info("clearExistingListOfExtractablePurchaseOrderAddressIds was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_SUPP_ORD_FRM_ADDR_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractablePurchaseOrderAddressIds finished truncating table.");
    }

    @Override
    public void updateSupplierOrderFromExtractQuerySettings(final String supplierJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE KFS.CU_CEMI_SUPP_ORD_FRM_QUERY_SETTINGS_T ")
                .append("SET SUPP_EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDate)
                .toQuery();

        final int numRowsUpdated = executeUpdate(query);
        if (numRowsUpdated != 1) {
            LOG.error("updateSupplierOrderFromExtractQuerySettings, Query should have updated 1 row, "
                    + "but it updated {} instead", numRowsUpdated);
            throw new RuntimeException("Failed to update Remit To Supplier query settings");
        }
    }

    @Override
    public void queryAndStoreListOfKfsVendorAddressLinks(final Supplier<Stream<VendorAddress>> addressQueryRunner) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS CU_CEMI_EXTR_SUPP_ORD_FRM_VNDR_ADDR_LNK_T (")
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

        queryAndStoreListOfAddressLinks(VendorAddress.class, query, addressQueryRunner);
    }

    private String getConcatenatedKfsVendorAddressData(final VendorAddress vendorAddress) {
        return CemiUtils.generateConcatenatedKey(
                vendorAddress.getVendorLine1Address(), vendorAddress.getVendorLine2Address(),
                vendorAddress.getVendorCityName(), vendorAddress.getVendorStateCode(),
                vendorAddress.getVendorZipCode(), vendorAddress.getVendorCountryCode());
    }

    private <T> void queryAndStoreListOfAddressLinks(final Class<T> addressClass, final CuSqlQuery query,
            final Supplier<Stream<T>> addressQueryRunner) {
        try (
            final Stream<T> addresses = addressQueryRunner.get();
        ) {
            int count = 0;
            final Iterator<T> addressesIterator = addresses.iterator();
            final List<T> cachedAddresses = new ArrayList<>(ADDRESS_BATCH_SIZE);

            for (final T address : IteratorUtils.asIterable(addressesIterator)) {
                count++;
                cachedAddresses.add(address);
                if (cachedAddresses.size() >= ADDRESS_BATCH_SIZE) {
                    storeListOfAddressLinks(addressClass, query, cachedAddresses);
                    cachedAddresses.clear();
                }
            }

            if (cachedAddresses.size() > 0) {
                storeListOfAddressLinks(addressClass, query, cachedAddresses);
            }
            LOG.info("queryAndStoreListOfAddressLinks, Finished writing {} {} links",
                    count, addressClass.getSimpleName());
        }
    }

    private <T> void storeListOfAddressLinks(final Class<T> addressClass, final CuSqlQuery query,
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
    public void queryAndStoreListOfSupplierAddressLinks(
            final Supplier<Stream<CemiSupplierAddressBo>> addressQueryRunner) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS CU_CEMI_EXTR_SUPP_ORD_FRM_SUPP_ADDR_LNK_T (")
                .append("SUPP_ADDR_ID, VNDR_ADDR_GNRTD_ID, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, CONCAT_ADDR")
                .append(") ")
                .append("SELECT ")
                .appendAsParameter(Types.VARCHAR, CemiSupplierAddressBo::getAddressId)
                .append(", ")
                .appendAsParameter(Types.INTEGER, this::extractKfsVendorId)
                .append(", VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, ")
                .appendAsParameter(Types.VARCHAR, this::getConcatenatedSupplierAddressData)
                .append(" FROM KFS.KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T ")
                .append("WHERE WKDY_SPLR_ID = ")
                        .appendAsParameter(Types.VARCHAR, CemiSupplierAddressBo::getSupplierId)
                .append(" AND EXTR_FILE_RUNDATE = ")
                        .appendAsParameter(Types.VARCHAR, CemiSupplierAddressBo::getJobRunDate)
                .toQuery();

        queryAndStoreListOfAddressLinks(CemiSupplierAddressBo.class, query, addressQueryRunner);
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
    public void queryAndStoreAddressIdsForSupplierOrderFromExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.KFS.CU_CEMI_SUPP_ORD_FRM_ADDR_T (")
                .append("SUPP_EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID, VNDR_ADDR_GNRTD_ID")
                .append(") ")
                .append("SELECT EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID, VNDR_ADDR_GNRTD_ID ")
                .append("FROM KFS.CU_CEMI_SUPP_ORD_FRM_PO_ADDRESSES_FINAL_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreAddressIdsForSupplierOrderFromExtract, Found {} potential addresses to extract",
                numRowsInserted);
    }    

    @Override
    public boolean determineIfSupplierIsUsedForPunchouts(final String supplierId, final String supplierJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT COUNT(1) FROM KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T EXT ")
                .append("JOIN KFS.CU_CEMI_EXTR_SUPP_ORD_FRM_B2B_VNDR_T B2B ")
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
