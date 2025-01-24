package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import edu.cornell.kfs.tax.batch.dataaccess.TaxDataExtractor;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailExtractor;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;
import edu.cornell.kfs.tax.batch.util.TaxQueryBuilder;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.DocumentHeaderProps;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.NoteProps;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.SortOrder;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.TransactionDetailProps;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.VendorAddressProps;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.VendorDetailProps;
import edu.cornell.kfs.tax.batch.util.TaxQueryConstants.VendorHeaderProps;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.vnd.CUVendorConstants.CUAddressTypes;

public class TransactionDetailProcessorDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements TransactionDetailProcessorDao {

    private static final Logger LOG = LogManager.getLogger();

    private TaxTableMetadataLookupService taxTableMetadataLookupService;

    @Override
    public <U> TaxStatistics processTransactionDetails(final TaxBatchConfig config, final Class<U> updaterDtoType,
            final FailableBiFunction<TaxBatchConfig, TransactionDetailExtractor<U>, TaxStatistics, Exception> handler)
                    throws SQLException {
        final TaxDtoMappingDefinition<TransactionDetail> detailReaderDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(TransactionDetail.class);
        final TaxDtoMappingDefinition<U> detailWriterDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(updaterDtoType);
        final CuSqlQuery query = createTransactionDetailQuery(config, detailReaderDefinition);

        return queryForUpdatableResults(query, resultSet -> {
            try {
                final TransactionDetailExtractor<U> extractor = new TransactionDetailExtractorImpl<>(
                        detailReaderDefinition, detailWriterDefinition, resultSet);
                return handler.apply(config, extractor);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final SQLException | RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CuSqlQuery createTransactionDetailQuery(final TaxBatchConfig config,
            final TaxDtoMappingDefinition<TransactionDetail> detailReaderDefinition) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099 :
                throw new RuntimeException("This implementation currently does not support 1099 processing");

            case CUTaxConstants.TAX_TYPE_1042S :
                return createTransactionDetailQueryFor1042S(config, detailReaderDefinition);

            default :
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    private CuSqlQuery createTransactionDetailQueryFor1042S(final TaxBatchConfig config,
            final TaxDtoMappingDefinition<TransactionDetail> detailReaderDefinition) {
        return new TaxQueryBuilder(detailReaderDefinition)
                .selectAllMappedFields()
                .from(TransactionDetail.class)
                .where(
                        Criteria.equal(TransactionDetailProps.REPORT_YEAR, Types.INTEGER, config.getReportYear()),
                        Criteria.equal(TransactionDetailProps.FORM_1042S_BOX, CUTaxConstants.NEEDS_UPDATING_BOX_KEY))
                .orderBy(
                        Pair.of(TransactionDetailProps.VENDOR_TAX_NUMBER, SortOrder.ASC),
                        Pair.of(TransactionDetailProps.INCOME_CODE, SortOrder.ASC),
                        Pair.of(TransactionDetailProps.INCOME_CODE_SUB_TYPE, SortOrder.ASC))
                .build();
    }



    @Override
    public VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId)
            throws SQLException {
        final Pair<Integer, Integer> vendorId = Pair.of(vendorHeaderId, vendorDetailId);
        final TaxDtoMappingDefinition<VendorDetailLite> vendorDetailDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorDetailLite.class);
        final CuSqlQuery query = createVendorQuery(vendorId, vendorDetailDefinition);

        return queryForResults(query,
                resultSet -> extractVendorQueryResults(resultSet, vendorId, vendorDetailDefinition));
    }

    private CuSqlQuery createVendorQuery(final Pair<Integer, Integer> vendorId,
            final TaxDtoMappingDefinition<VendorDetailLite> vendorDetailDefinition) {
        return new TaxQueryBuilder(vendorDetailDefinition)
                .selectAllMappedFields()
                .from(VendorDetail.class)
                .join(VendorHeader.class,
                        Criteria.equalToProperty(VendorDetailProps.VENDOR_HEADER_GENERATED_ID,
                                VendorHeaderProps.VENDOR_HEADER_GENERATED_ID))
                .where(
                        Criteria.equal(VendorDetailProps.VENDOR_HEADER_GENERATED_ID, Types.INTEGER,
                                vendorId.getLeft()),
                        Criteria.or(
                                Criteria.equal(VendorDetailProps.VENDOR_DETAIL_ASSIGNED_ID, Types.INTEGER,
                                        vendorId.getRight()),
                                Criteria.equal(VendorDetailProps.VENDOR_PARENT_INDICATOR,
                                        KRADConstants.YES_INDICATOR_VALUE)
                        )
                )
                .orderBy(
                        Pair.of(VendorDetailProps.VENDOR_PARENT_INDICATOR, SortOrder.ASC_NULLS_FIRST))
                .build();
    }

    private VendorQueryResults extractVendorQueryResults(final ResultSet resultSet,
            final Pair<Integer, Integer> vendorId,
            final TaxDtoMappingDefinition<VendorDetailLite> vendorDetailDefinition) throws SQLException {
        final TaxDataExtractor<VendorDetailLite> vendorExtractor = new TaxDataExtractorImpl<>(
                vendorDetailDefinition, resultSet);
        if (!vendorExtractor.moveToNextRow()) {
            return new VendorQueryResults(null, null);
        }

        final VendorDetailLite vendorDetail = vendorExtractor.getCurrentRow();
        final VendorDetailLite parentDetail;
        if (!vendorExtractor.moveToNextRow()) {
            if (vendorId.getRight().equals(vendorDetail.getVendorDetailAssignedIdentifier())) {
                parentDetail = null;
                return new VendorQueryResults(vendorDetail, null);
            } else {
                parentDetail = vendorDetail;
                return new VendorQueryResults(null, parentDetail);
            }
        } else {
            parentDetail = vendorExtractor.getCurrentRow();
            if (vendorExtractor.moveToNextRow()) {
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
        final TaxDtoMappingDefinition<VendorAddressLite> vendorAddressDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(VendorAddressLite.class);
        final CuSqlQuery query = createVendorAddressQuery(vendorId, searchForUSAddresses, vendorAddressDefinition);

        return queryForResults(query,
                resultSet -> extractVendorAddressResults(resultSet, vendorAddressDefinition));
    }

    private CuSqlQuery createVendorAddressQuery(final Pair<Integer, Integer> vendorId,
            final boolean searchForUSAddresses,
            final TaxDtoMappingDefinition<VendorAddressLite> vendorAddressDefinition) {

        final Criteria countryCriteria;
        if (searchForUSAddresses) {
            countryCriteria = Criteria.or(
                    Criteria.isNull(VendorAddressProps.VENDOR_ADDRESS_COUNTRY),
                    Criteria.equal(VendorAddressProps.VENDOR_ADDRESS_COUNTRY,
                            KFSConstants.COUNTRY_CODE_UNITED_STATES));
        } else {
            countryCriteria = Criteria.and(
                    Criteria.isNotNull(VendorAddressProps.VENDOR_ADDRESS_COUNTRY),
                    Criteria.notEqual(VendorAddressProps.VENDOR_ADDRESS_COUNTRY,
                            KFSConstants.COUNTRY_CODE_UNITED_STATES));
        }

        return new TaxQueryBuilder(vendorAddressDefinition)
                .selectAllMappedFields()
                .from(VendorAddress.class)
                .where(
                        Criteria.equal(VendorAddressProps.VENDOR_HEADER_GENERATED_ID, Types.INTEGER,
                                vendorId.getLeft()),
                        Criteria.equal(VendorAddressProps.VENDOR_DETAIL_ASSIGNED_ID, Types.INTEGER,
                                vendorId.getRight()),
                        countryCriteria,
                        Criteria.in(VendorAddressProps.VENDOR_ADDRESS_TYPE_CODE,
                                List.of(CUAddressTypes.TAX, AddressTypes.REMIT, AddressTypes.PURCHASE_ORDER)),
                        Criteria.equal(VendorAddressProps.ACTIVE, KRADConstants.YES_INDICATOR_VALUE))
                .orderBy(
                        Pair.of(VendorAddressProps.VENDOR_ADDRESS_TYPE_CODE, SortOrder.DESC),
                        Pair.of(VendorAddressProps.VENDOR_ADDRESS_GENERATED_ID, SortOrder.DESC))
                .build();
    }

    private VendorAddressLite extractVendorAddressResults(final ResultSet resultSet,
            final TaxDtoMappingDefinition<VendorAddressLite> vendorAddressDefinition) throws SQLException {
        final TaxDataExtractor<VendorAddressLite> addressExtractor = new TaxDataExtractorImpl<>(
                vendorAddressDefinition, resultSet);
        if (addressExtractor.moveToNextRow()) {
            return addressExtractor.getCurrentRow();
        } else {
            return null;
        }
    }



    @Override
    public List<NoteLite> getNotesByDocumentNumber(final String documentNumber) throws SQLException {
        final TaxDtoMappingDefinition<NoteLite> noteDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(NoteLite.class);
        final TaxDtoMappingDefinition<DocumentHeaderLite> documentHeaderDefinition = taxTableMetadataLookupService
                .getDatabaseMappingMetadataForDto(DocumentHeaderLite.class);
        final CuSqlQuery query = createNoteQuery(documentNumber, noteDefinition, documentHeaderDefinition);
        return queryForResults(query,
                resultSet -> extractNoteResults(resultSet, noteDefinition));
    }

    private CuSqlQuery createNoteQuery(final String documentNumber,
            final TaxDtoMappingDefinition<NoteLite> noteDefinition,
            final TaxDtoMappingDefinition<DocumentHeaderLite> documentHeaderDefinition) {

        final TaxQueryBuilder objectIdSubQuery = new TaxQueryBuilder(documentHeaderDefinition)
                .select(DocumentHeaderProps.OBJECT_ID)
                .from(DocumentHeader.class)
                .where(Criteria.equal(DocumentHeaderProps.DOCUMENT_NUMBER, documentNumber));

        return new TaxQueryBuilder(noteDefinition)
                .selectAllMappedFields()
                .from(Note.class)
                .where(Criteria.equal(NoteProps.REMOTE_OBJECT_ID, objectIdSubQuery))
                .build();
    }

    private List<NoteLite> extractNoteResults(final ResultSet resultSet,
            final TaxDtoMappingDefinition<NoteLite> noteDefinition) throws SQLException {
        final TaxDataExtractor<NoteLite> noteExtractor = new TaxDataExtractorImpl<>(
                noteDefinition, resultSet);
        Stream.Builder<NoteLite> notes = Stream.builder();
        while (noteExtractor.moveToNextRow()) {
            notes.add(noteExtractor.getCurrentRow());
        }
        return notes.build().collect(Collectors.toUnmodifiableList());
    }



    public void setTaxTableMetadataLookupService(final TaxTableMetadataLookupService taxTableMetadataLookupService) {
        this.taxTableMetadataLookupService = taxTableMetadataLookupService;
    }

}
