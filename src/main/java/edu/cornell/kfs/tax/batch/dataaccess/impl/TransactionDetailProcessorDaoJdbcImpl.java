package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailRowMapperFactory;
import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite.DocumentHeaderField;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite.VendorAddressField;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;
import edu.cornell.kfs.tax.batch.util.TaxQueryBuilder;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;
import edu.cornell.kfs.vnd.CUVendorConstants.CUAddressTypes;

public class TransactionDetailProcessorDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements TransactionDetailProcessorDao {

    private static final Logger LOG = LogManager.getLogger();

    private TaxTableMetadataLookupService taxTableMetadataLookupService;
    private EncryptionService encryptionService;

    @Override
    public <U> TaxStatistics processTransactionDetails(final TaxBatchConfig config,
            final TransactionDetailRowMapperFactory<U> rowMapperFactory,
            final TransactionDetailHandler<U> handler) throws SQLException {
        final TaxDtoDbMetadata transactionDetailMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(TransactionDetailField.class);
        final CuSqlQuery query = createTransactionDetailQuery(config, transactionDetailMetadata);

        return queryForUpdatableResults(query, resultSet -> {
            try {
                final TransactionDetailRowMapper<U> rowMapper = rowMapperFactory.apply(
                        encryptionService, transactionDetailMetadata, resultSet);
                return handler.apply(config, rowMapper);
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
    public VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId)
            throws SQLException {
        final Pair<Integer, Integer> vendorId = Pair.of(vendorHeaderId, vendorDetailId);
        final TaxDtoDbMetadata vendorMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorField.class);
        final CuSqlQuery query = createVendorQuery(vendorId, vendorMetadata);

        return queryForResults(query,
                resultSet -> extractVendorQueryResults(resultSet, vendorId, vendorMetadata));
    }

    private CuSqlQuery createVendorQuery(final Pair<Integer, Integer> vendorId,
            final TaxDtoDbMetadata vendorMetadata) {
        return new TaxQueryBuilder(vendorMetadata)
                .selectAllMappedFields()
                .from(VendorDetail.class)
                .join(VendorHeader.class,
                        Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier_forDetail,
                                VendorField.vendorHeaderGeneratedIdentifier_forHeader))
                .where(
                        Criteria.equal(VendorField.vendorHeaderGeneratedIdentifier_forDetail, Types.INTEGER,
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

    private VendorQueryResults extractVendorQueryResults(final ResultSet resultSet,
            final Pair<Integer, Integer> vendorId,
            final TaxDtoDbMetadata vendorMetadata) throws SQLException {
        final VendorDetailLiteMapper vendorMapper = new VendorDetailLiteMapper(
                encryptionService, vendorMetadata, resultSet);
        if (!vendorMapper.moveToNextRow()) {
            return new VendorQueryResults(null, null);
        }

        final VendorDetailLite vendorDetail = vendorMapper.readCurrentRow();
        final VendorDetailLite parentDetail;
        if (!vendorMapper.moveToNextRow()) {
            if (vendorId.getRight().equals(vendorDetail.getVendorDetailAssignedIdentifier())) {
                parentDetail = null;
                return new VendorQueryResults(vendorDetail, null);
            } else {
                parentDetail = vendorDetail;
                return new VendorQueryResults(null, parentDetail);
            }
        } else {
            parentDetail = vendorMapper.readCurrentRow();
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
    public VendorAddressLite getHighestPriorityUSVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException {
        return getHighestPriorityVendorAddress(Pair.of(vendorHeaderId, vendorDetailId), true);
    }

    @Override
    public VendorAddressLite getHighestPriorityForeignVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException {
        return getHighestPriorityVendorAddress(Pair.of(vendorHeaderId, vendorDetailId), false);
    }

    private VendorAddressLite getHighestPriorityVendorAddress(final Pair<Integer, Integer> vendorId,
            final boolean searchForUSAddresses) throws SQLException {
        final TaxDtoDbMetadata addressMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorAddressField.class);
        final CuSqlQuery query = createVendorAddressQuery(vendorId, searchForUSAddresses, addressMetadata);

        return queryForResults(query,
                resultSet -> extractVendorAddressResults(resultSet, addressMetadata));
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

    private VendorAddressLite extractVendorAddressResults(final ResultSet resultSet,
            final TaxDtoDbMetadata addressMetadata) throws SQLException {
        final VendorAddressLiteMapper addressMapper = new VendorAddressLiteMapper(
                encryptionService, addressMetadata, resultSet);
        if (addressMapper.moveToNextRow()) {
            return addressMapper.readCurrentRow();
        } else {
            return null;
        }
    }



    @Override
    public List<NoteLite> getNotesByDocumentNumber(final String documentNumber) throws SQLException {
        final TaxDtoDbMetadata noteMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(NoteField.class);
        final TaxDtoDbMetadata docHeaderMetadata = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(DocumentHeaderField.class);
        final CuSqlQuery query = createNoteQuery(documentNumber, noteMetadata, docHeaderMetadata);
        return queryForResults(query,
                resultSet -> extractNoteResults(resultSet, noteMetadata));
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

    private List<NoteLite> extractNoteResults(final ResultSet resultSet,
            final TaxDtoDbMetadata noteMetadata) throws SQLException {
        final NoteLiteMapper noteMapper = new NoteLiteMapper(encryptionService, noteMetadata, resultSet);
        Stream.Builder<NoteLite> notes = Stream.builder();
        while (noteMapper.moveToNextRow()) {
            notes.add(noteMapper.readCurrentRow());
        }
        return notes.build().collect(Collectors.toUnmodifiableList());
    }



    public void setTaxTableMetadataLookupService(final TaxTableMetadataLookupService taxTableMetadataLookupService) {
        this.taxTableMetadataLookupService = taxTableMetadataLookupService;
    }

    public void setEncryptionService(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

}
