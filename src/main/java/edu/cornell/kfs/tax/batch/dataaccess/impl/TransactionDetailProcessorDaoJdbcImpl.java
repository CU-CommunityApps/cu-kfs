package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite.DocumentHeaderField;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite.VendorAddressField;
import edu.cornell.kfs.tax.batch.dto.VendorContactLite;
import edu.cornell.kfs.tax.batch.dto.VendorContactLite.VendorContactField;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.util.TaxQueryBuilder;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.FieldUpdate;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;
import edu.cornell.kfs.vnd.CUVendorConstants.CUAddressTypes;
import edu.cornell.kfs.vnd.CUVendorConstants.VendorContactTypeCodes;

public class TransactionDetailProcessorDaoJdbcImpl extends TransactionDetailDaoJdbcBase
        implements TransactionDetailProcessorDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public TaxStatistics processTransactionDetails(final TaxBatchConfig config,
            final TransactionDetailHandler handler) {
        final TaxDtoDbMetadata transactionDetailMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(TransactionDetailField.class);
        final CuSqlQuery query = createTransactionDetailQuery(config, transactionDetailMetadata);

        return queryForResults(query, resultSet -> {
            try {
                final TaxDtoRowMapper<TransactionDetail> rowMapper = new TaxDtoRowMapperImpl<>(
                        TransactionDetail::new, encryptionService, transactionDetailMetadata, resultSet);
                return handler.performProcessing(config, rowMapper);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final SQLException | RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CuSqlQuery createTransactionDetailQuery(
            final TaxBatchConfig config, final TaxDtoDbMetadata transactionDetailMetadata) {
        final TransactionDetailField taxBoxField = determineTaxBoxFieldToExamine(config);
        final Criteria taxBoxCriteria;

        if (config.getMode() == TaxBatchConfig.Mode.CREATE_TAX_FILES) {
            taxBoxCriteria = Criteria.equal(taxBoxField, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
        } else {
            taxBoxCriteria = Criteria.notEqual(taxBoxField, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
        }

        return new TaxQueryBuilder(transactionDetailMetadata)
                .selectAllMappedFields()
                .from(TransactionDetail.class)
                .where(
                        Criteria.equal(TransactionDetailField.reportYear, Types.INTEGER, config.getReportYear()),
                        taxBoxCriteria)
                .orderBy(
                        QuerySort.ascending(TransactionDetailField.vendorTaxNumber),
                        QuerySort.ascending(TransactionDetailField.incomeCode),
                        QuerySort.ascending(TransactionDetailField.incomeCodeSubType))
                .build();
    }

    private TransactionDetailField determineTaxBoxFieldToExamine(final TaxBatchConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new RuntimeException("This implementation currently does not support 1099 processing");

            case CUTaxConstants.TAX_TYPE_1042S:
                return TransactionDetailField.form1042SBox;

            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    @Override
    public void updateVendorInfoAndTaxBoxesOnTransactionDetails(List<TransactionDetail> transactionDetails,
            TaxBatchConfig config) {
        if (!StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S)) {
            throw new RuntimeException("This implementation currently does not support 1099 processing");
        }
        final CuSqlQuery query = createQueryForBatchUpdatesToTransactionDetails();
        final int[] updateCounts = executeBatchUpdate(query, transactionDetails);

        for (int i = 0; i < updateCounts.length; i++) {
            if (updateCounts[i] != 1) {
                final TransactionDetail transactionDetail = transactionDetails.get(i);
                LOG.warn("updateVendorInfoAndTaxBoxesOnTransactionDetails, Batch update for Transaction Detail {} "
                        + "at batch index {} should have updated 1 row, but it actually updated {} rows instead!",
                        transactionDetail.getTransactionDetailId(), i, updateCounts[i]);
            }
        }
    }

    private CuSqlQuery createQueryForBatchUpdatesToTransactionDetails() {
        final TaxDtoDbMetadata transactionDetailMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(TransactionDetailField.class);

        final FieldUpdate[] fieldUpdates = {
                FieldUpdate.of(TransactionDetailField.vendorName, TransactionDetail::getVendorName),
                FieldUpdate.of(TransactionDetailField.parentVendorName, TransactionDetail::getParentVendorName),
                FieldUpdate.of(TransactionDetailField.vendorEmailAddress, TransactionDetail::getVendorEmailAddress),
                FieldUpdate.of(TransactionDetailField.vendorChapter4StatusCode, TransactionDetail::getVendorChapter4StatusCode),
                FieldUpdate.of(TransactionDetailField.vendorGIIN, TransactionDetail::getVendorGIIN),
                FieldUpdate.of(TransactionDetailField.vendorLine1Address, TransactionDetail::getVendorLine1Address),
                FieldUpdate.of(TransactionDetailField.vendorLine2Address, TransactionDetail::getVendorLine2Address),
                FieldUpdate.of(TransactionDetailField.vendorCityName, TransactionDetail::getVendorCityName),
                FieldUpdate.of(TransactionDetailField.vendorStateCode, TransactionDetail::getVendorStateCode),
                FieldUpdate.of(TransactionDetailField.vendorZipCode, TransactionDetail::getVendorZipCode),
                FieldUpdate.of(TransactionDetailField.vendorForeignLine1Address, TransactionDetail::getVendorForeignLine1Address),
                FieldUpdate.of(TransactionDetailField.vendorForeignLine2Address, TransactionDetail::getVendorForeignLine2Address),
                FieldUpdate.of(TransactionDetailField.vendorForeignCityName, TransactionDetail::getVendorForeignCityName),
                FieldUpdate.of(TransactionDetailField.vendorForeignZipCode, TransactionDetail::getVendorForeignZipCode),
                FieldUpdate.of(TransactionDetailField.vendorForeignProvinceName, TransactionDetail::getVendorForeignProvinceName),
                FieldUpdate.of(TransactionDetailField.vendorForeignCountryCode, TransactionDetail::getVendorForeignCountryCode),
                FieldUpdate.of(TransactionDetailField.form1042SBox, TransactionDetail::getForm1042SBox),
                FieldUpdate.of(TransactionDetailField.form1042SOverriddenBox, TransactionDetail::getForm1042SOverriddenBox)
        };

        return new TaxQueryBuilder(transactionDetailMetadata)
                .update(TransactionDetail.class)
                .set(fieldUpdates)
                .where(Criteria.equal(
                        TransactionDetailField.transactionDetailId, TransactionDetail::getTransactionDetailId))
                .build();
    }

    @Override
    public VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId) {
        final Pair<Integer, Integer> vendorId = Pair.of(vendorHeaderId, vendorDetailId);
        final TaxDtoDbMetadata vendorMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorField.class);
        final CuSqlQuery query = createVendorQuery(vendorId, vendorMetadata);

        return queryForResults(query, resultSet -> readVendorQueryResults(resultSet, vendorId, vendorMetadata));
    }

    private CuSqlQuery createVendorQuery(final Pair<Integer, Integer> vendorId,
            final TaxDtoDbMetadata vendorMetadata) {
        return new TaxQueryBuilder(vendorMetadata)
                .selectAllMappedFields()
                .from(VendorDetail.class)
                .join(VendorHeader.class,
                        Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier,
                                VendorField.vendorHeaderGeneratedIdentifier))
                .where(
                        Criteria.equal(VendorField.vendorDetailVendorHeaderGeneratedIdentifier, Types.INTEGER,
                                vendorId.getLeft()),
                        Criteria.or(
                                Criteria.equal(VendorField.vendorDetailAssignedIdentifier, Types.INTEGER,
                                        vendorId.getRight()),
                                Criteria.equal(VendorField.vendorParentIndicator, KRADConstants.YES_INDICATOR_VALUE)
                        )
                )
                .orderBy(QuerySort.ascendingNullsFirst(VendorField.vendorParentIndicator))
                .build();
    }

    private VendorQueryResults readVendorQueryResults(final ResultSet resultSet,
            final Pair<Integer, Integer> vendorId,
            final TaxDtoDbMetadata vendorMetadata) throws SQLException {
        final TaxDtoRowMapper<VendorDetailLite> vendorMapper = new TaxDtoRowMapperImpl<>(
                VendorDetailLite::new, encryptionService, vendorMetadata, resultSet);
        if (!vendorMapper.moveToNextRow()) {
            return new VendorQueryResults(null, null);
        }

        final VendorDetailLite vendorDetail = vendorMapper.readCurrentRow();
        if (!vendorMapper.moveToNextRow()) {
            if (vendorId.getRight().equals(vendorDetail.getVendorDetailAssignedIdentifier())) {
                return new VendorQueryResults(vendorDetail, null);
            } else {
                return new VendorQueryResults(null, vendorDetail);
            }
        } else {
            final VendorDetailLite parentDetail = vendorMapper.readCurrentRow();
            if (vendorMapper.moveToNextRow()) {
                LOG.warn("extractVendorDetailResults, More than 2 vendors were found by a query that should have "
                        + "only fetched vendor {}-{} and/or its parent. The remaining results will be ignored.",
                        vendorId.getLeft(), vendorId.getRight());
            }

            if (!parentDetail.isVendorParentIndicator()) {
                LOG.warn("extractVendorDetailResults, Vendor {}-{} was expected to be the parent of vendor {}-{} "
                        + "but it wasn't. It will be ignored.",
                        parentDetail.getVendorHeaderGeneratedIdentifier(),
                        parentDetail.getVendorDetailAssignedIdentifier(), vendorId.getLeft(), vendorId.getRight());
                return new VendorQueryResults(vendorDetail, null);
            } else {
                return new VendorQueryResults(vendorDetail, parentDetail);
            }
        }
    }



    @Override
    public List<VendorAddressLite> getPrioritizedUSVendorAddresses(final Integer vendorHeaderId,
            final Integer vendorDetailId) {
        return getPrioritizedVendorAddresses(Pair.of(vendorHeaderId, vendorDetailId), true);
    }

    @Override
    public List<VendorAddressLite> getPrioritizedForeignVendorAddresses(final Integer vendorHeaderId,
            final Integer vendorDetailId) {
        return getPrioritizedVendorAddresses(Pair.of(vendorHeaderId, vendorDetailId), false);
    }

    private List<VendorAddressLite> getPrioritizedVendorAddresses(final Pair<Integer, Integer> vendorId,
            final boolean searchForUSAddresses) {
        final TaxDtoDbMetadata addressMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorAddressField.class);
        final CuSqlQuery query = createVendorAddressQuery(vendorId, searchForUSAddresses, addressMetadata);

        return queryForResults(query, resultSet -> readFullResults(resultSet, addressMetadata, VendorAddressLite::new));
    }

    private CuSqlQuery createVendorAddressQuery(final Pair<Integer, Integer> vendorId,
            final boolean searchForUSAddresses,
            final TaxDtoDbMetadata addressMetadata) {

        final Criteria countryCriteria;
        if (searchForUSAddresses) {
            countryCriteria = Criteria.or(
                    Criteria.isNull(VendorAddressField.vendorCountryCode),
                    Criteria.equal(VendorAddressField.vendorCountryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES));
        } else {
            countryCriteria = Criteria.and(
                    Criteria.isNotNull(VendorAddressField.vendorCountryCode),
                    Criteria.notEqual(VendorAddressField.vendorCountryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES));
        }

        return new TaxQueryBuilder(addressMetadata)
                .selectAllMappedFields()
                .from(VendorAddress.class)
                .where(
                        Criteria.equal(VendorAddressField.vendorHeaderGeneratedIdentifier, Types.INTEGER,
                                vendorId.getLeft()),
                        Criteria.equal(VendorAddressField.vendorDetailAssignedIdentifier, Types.INTEGER,
                                vendorId.getRight()),
                        countryCriteria,
                        Criteria.in(VendorAddressField.vendorAddressTypeCode,
                                List.of(CUAddressTypes.TAX, AddressTypes.REMIT, AddressTypes.PURCHASE_ORDER)),
                        Criteria.equal(VendorAddressField.active, KRADConstants.YES_INDICATOR_VALUE))
                .orderBy(
                        QuerySort.descending(VendorAddressField.vendorAddressTypeCode),
                        QuerySort.descending(VendorAddressField.vendorAddressGeneratedIdentifier))
                .build();
    }

    @Override
    public List<VendorContactLite> getPrioritizedVendorContactsWithEmails(final Integer vendorHeaderId,
            final Integer vendorDetailId) {
        final TaxDtoDbMetadata contactMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorContactField.class);
        final CuSqlQuery query = createVendorContactQuery(vendorHeaderId, vendorDetailId, contactMetadata);
        return queryForResults(query, resultSet -> readFullResults(resultSet, contactMetadata, VendorContactLite::new));
    }

    private CuSqlQuery createVendorContactQuery(final Integer vendorHeaderId, final Integer vendorDetailId,
            final TaxDtoDbMetadata contactMetadata) {
        return new TaxQueryBuilder(contactMetadata)
                .selectAllMappedFields()
                .from(VendorContact.class)
                .where(
                        Criteria.equal(VendorContactField.vendorHeaderGeneratedIdentifier, Types.INTEGER,
                                vendorHeaderId),
                        Criteria.equal(VendorContactField.vendorDetailAssignedIdentifier, Types.INTEGER,
                                vendorDetailId),
                        Criteria.in(VendorContactField.vendorContactTypeCode, List.of(
                                VendorContactTypeCodes.ACCOUNTS_RECEIVABLE,
                                VendorContactTypeCodes.VENDOR_INFORMATION_FORM)),
                        Criteria.isNotNull(VendorContactField.vendorContactEmailAddress),
                        Criteria.equal(VendorContactField.active, KRADConstants.YES_INDICATOR_VALUE))
                .orderBy(
                        QuerySort.ascending(VendorContactField.vendorContactTypeCode),
                        QuerySort.descending(VendorContactField.vendorContactGeneratedIdentifier))
                .build();
    }

    @Override
    public List<NoteLite> getNotesByDocumentNumber(final String documentNumber) {
        final TaxDtoDbMetadata noteMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(NoteField.class);
        final TaxDtoDbMetadata docHeaderMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(DocumentHeaderField.class);
        final CuSqlQuery query = createNoteQuery(documentNumber, noteMetadata, docHeaderMetadata);
        return queryForResults(query, resultSet -> readFullResults(resultSet, noteMetadata, NoteLite::new));
    }

    private CuSqlQuery createNoteQuery(final String documentNumber, final TaxDtoDbMetadata noteMetadata,
            final TaxDtoDbMetadata docHeaderMetadata) {

        final TaxQueryBuilder objectIdSubQuery = new TaxQueryBuilder(docHeaderMetadata)
                .select(DocumentHeaderField.objectId)
                .from(DocumentHeader.class)
                .where(Criteria.equal(DocumentHeaderField.documentNumber, documentNumber));

        return new TaxQueryBuilder(noteMetadata)
                .selectAllMappedFields()
                .from(Note.class)
                .where(Criteria.equal(NoteField.remoteObjectIdentifier, objectIdSubQuery))
                .build();
    }

}
