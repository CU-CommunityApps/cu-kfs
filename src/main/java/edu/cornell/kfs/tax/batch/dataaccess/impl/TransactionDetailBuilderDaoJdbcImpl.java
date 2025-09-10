package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.ProcessSummary;
import org.kuali.kfs.sys.KFSConstants.PaymentPayeeTypes;
import org.kuali.kfs.sys.KFSConstants.PaymentSourceConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.kim.CuKimConstants;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionSourceHandler;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.dto.DvSourceData.DvSourceDataField;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData.PdpSourceDataField;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData.PrncSourceDataField;
import edu.cornell.kfs.tax.batch.dto.SubQueryFields.DocumentTypeSubQueryField;
import edu.cornell.kfs.tax.batch.dto.SubQueryFields.DvSubQueryField;
import edu.cornell.kfs.tax.batch.dto.SubQueryFields.RouteHeaderSubQueryField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;
import edu.cornell.kfs.tax.batch.util.TaxQueryBuilder;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.QuerySort;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.SqlFunction;
import edu.cornell.kfs.tax.businessobject.DvDisbursementView;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

public class TransactionDetailBuilderDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements TransactionDetailBuilderDao {

    private static final Logger LOG = LogManager.getLogger();

    private TaxTableMetadataLookupService taxTableMetadataLookupService;
    private EncryptionService encryptionService;

    @Override
    public void deleteTransactionDetailsForConfiguredTaxTypeAndYear(final TaxBatchConfig config) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                TransactionDetailField.class);
        final TransactionDetailField taxBoxField = getTaxBoxFieldForCriteria(config);

        final CuSqlQuery query = new TaxQueryBuilder(metadata)
                .deleteFrom(TransactionDetail.class)
                .where(
                        Criteria.equal(TransactionDetailField.reportYear, Types.INTEGER, config.getReportYear()),
                        Criteria.isNotNull(taxBoxField)
                )
                .build();

        final int numDeletedRows = executeUpdate(query);
        LOG.debug("deleteTransactionDetailsForConfiguredTaxTypeAndYear, Deleted {} rows for tax type {} and year {}",
                numDeletedRows, config.getTaxType(), config.getReportYear());
    }

    private TransactionDetailField getTaxBoxFieldForCriteria(final TaxBatchConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new UnsupportedOperationException(
                        "This implementation currently does not support 1099 tax processing");
            case CUTaxConstants.TAX_TYPE_1042S:
                return TransactionDetailField.form1042SBox;
            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    @Override
    public TaxStatistics createDvTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<DvSourceData> dvSourceHandler,
            final TransactionDetailHandler secondPassHandler) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                DvSourceDataField.class);
        final CuSqlQuery query = createDvSourceDataQuery(config, metadata);
        return queryForResults(query, resultSet -> {
            try {
                final TaxDtoRowMapper<DvSourceData> rowMapper = new TaxDtoRowMapperImpl<>(
                        DvSourceData::new, encryptionService, metadata, resultSet);
                return dvSourceHandler.generateTransactionDetails(config, rowMapper);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            } catch (final SQLException | RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CuSqlQuery createDvSourceDataQuery(final TaxBatchConfig config, final TaxDtoDbMetadata metadata) {
        final TaxQueryBuilder dvDocumentSubQuery = createDvDocumentSubQuery(config, metadata);

        return new TaxQueryBuilder(metadata)
                .selectAllMappedFields()
                .from(DisbursementVoucherPayeeDetail.class)
                .join(DisbursementVoucherNonresidentTax.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.dvNraDocumentNumber))
                .join(SourceAccountingLine.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.accountingLineDocumentNumber))
                .join(dvDocumentSubQuery, DisbursementVoucherDocument.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.dvDocumentNumber))
                .join(VendorHeader.class,
                        Criteria.equal(DvSourceDataField.disbursementVoucherPayeeTypeCode, PaymentPayeeTypes.VENDOR),
                        Criteria.like(DvSourceDataField.disbVchrPayeeIdNumber, SqlFunction.CONCAT(
                                SqlFunction.TO_CHAR(DvSourceDataField.vendorHeaderGeneratedIdentifier), "-%"
                        ))
                )
                .leftJoin(DvDisbursementView.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.custPaymentDocNbr))
                .where(
                        Criteria.isNotNull(DvSourceDataField.accountingLineSequenceNumber),
                        Criteria.isNotNull(DvSourceDataField.financialDocumentLineTypeCode),
                        getTaxTypeSpecificDvQueryCriteria(config)
                )
                .orderBy(
                        QuerySort.ascending(DvSourceDataField.vendorTaxNumber),
                        QuerySort.ascending(DvSourceDataField.extractDate),
                        QuerySort.ascending(DvSourceDataField.dvDocumentNumber),
                        QuerySort.ascending(DvSourceDataField.accountingLineSequenceNumber)
                )
                .build();
    }

    private Criteria getTaxTypeSpecificDvQueryCriteria(final TaxBatchConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new UnsupportedOperationException(
                        "This implementation currently does not support 1099 tax processing");
            case CUTaxConstants.TAX_TYPE_1042S:
                return Criteria.equal(DvSourceDataField.vendorForeignIndicator, KRADConstants.YES_INDICATOR_VALUE);
            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    private TaxQueryBuilder createDvDocumentSubQuery(final TaxBatchConfig config,
            final TaxDtoDbMetadata parentQueryMetadata) {
        final TaxDtoDbMetadata dvMetadata1 = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                DvSubQueryField.class, parentQueryMetadata.getMaximumAliasSuffix() + 1);
        final TaxDtoDbMetadata dvMetadata2 = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                DvSubQueryField.class, dvMetadata1.getMaximumAliasSuffix() + 1);

        final TaxQueryBuilder documentsFilteredByFinalizedDateSubquery =
                createRouteHeaderSubqueryFilteredByFinalizedDate(
                        config, DisbursementVoucherConstants.DOCUMENT_TYPE_CODE, dvMetadata2);

        return new TaxQueryBuilder(dvMetadata1)
                .selectAllFieldsMappedTo(DisbursementVoucherDocument.class)
                .from(DisbursementVoucherDocument.class)
                .join(UniversityDate.class,
                        Criteria.equal(DvSubQueryField.paidDate, DvSubQueryField.universityDate))
                .where(Criteria.between(
                        DvSubQueryField.paidDate, Types.DATE, config.getStartDate(), config.getEndDate()))
                .unionAll(new TaxQueryBuilder(dvMetadata2)
                        .selectAllFieldsMappedTo(DisbursementVoucherDocument.class)
                        .from(DisbursementVoucherDocument.class)
                        .join(UniversityDate.class,
                                Criteria.equal(DvSubQueryField.universityDate, Types.DATE, config.getStartDate()))
                        .where(
                                Criteria.in(DvSubQueryField.documentDisbVchrPaymentMethodCode, List.of(
                                        PaymentSourceConstants.PAYMENT_METHOD_DRAFT,
                                        PaymentSourceConstants.PAYMENT_METHOD_WIRE
                                )),
                                Criteria.isNull(DvSubQueryField.paidDate),
                                Criteria.in(DvSubQueryField.dvDocumentNumber, documentsFilteredByFinalizedDateSubquery)
                        )
                );
    }

    private TaxQueryBuilder createRouteHeaderSubqueryFilteredByFinalizedDate(
            final TaxBatchConfig config, final String documentTypeName, final TaxDtoDbMetadata parentQueryMetadata) {
        final TaxDtoDbMetadata documentMetadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                RouteHeaderSubQueryField.class, parentQueryMetadata.getMaximumAliasSuffix() + 1);
        final TaxDtoDbMetadata docTypeMetadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                DocumentTypeSubQueryField.class, documentMetadata.getMaximumAliasSuffix() + 1);

        final TaxQueryBuilder documentTypeSubQuery = new TaxQueryBuilder(docTypeMetadata)
                .select(DocumentTypeSubQueryField.documentTypeId)
                .from(DocumentType.class)
                .where(Criteria.equal(DocumentTypeSubQueryField.name, documentTypeName));

        return new TaxQueryBuilder(documentMetadata)
                .select(RouteHeaderSubQueryField.documentId)
                .from(DocumentRouteHeaderValue.class)
                .where(
                        Criteria.in(RouteHeaderSubQueryField.documentTypeId, documentTypeSubQuery),
                        Criteria.between(RouteHeaderSubQueryField.finalizedDate, Types.TIMESTAMP,
                                toTimestamp(config.getStartDate(), LocalTime.of(0, 0, 0, 0)),
                                toTimestamp(config.getEndDate(), LocalTime.of(23, 59, 59, 999999999)))
                );
    }

    private Timestamp toTimestamp(final java.sql.Date date, final LocalTime localTime) {
        final LocalDate localDate = date.toLocalDate();
        final LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        return Timestamp.valueOf(localDateTime);
    }

    @Override
    public TaxStatistics createPdpTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PdpSourceData> pdpSourceHandler,
            final TransactionDetailHandler secondPassHandler) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                PdpSourceDataField.class);
        createPdpSourceDataQuery(config, metadata);
        return null;
    }

    private CuSqlQuery createPdpSourceDataQuery(final TaxBatchConfig config, final TaxDtoDbMetadata metadata) {
        return new TaxQueryBuilder(metadata)
                .selectAllMappedFields()
                .from(PaymentDetail.class)
                .join(PaymentGroup.class, Criteria.equal(
                        PdpSourceDataField.paymentDetailPaymentGroupId, PdpSourceDataField.paymentGroupId))
                .join(ProcessSummary.class, Criteria.equal(
                        PdpSourceDataField.paymentGroupProcessId, PdpSourceDataField.summaryProcessId))
                .join(CustomerProfile.class, Criteria.equal(
                        PdpSourceDataField.summaryCustomerId, PdpSourceDataField.customerId))
                .join(PaymentAccountDetail.class, Criteria.equal(
                        PdpSourceDataField.paymentDetailId, PdpSourceDataField.accountDetailPaymentDetailId))
                .join(VendorHeader.class,
                        Criteria.equal(PdpSourceDataField.payeeIdTypeCode, PaymentPayeeTypes.VENDOR),
                        Criteria.like(PdpSourceDataField.payeeId, SqlFunction.CONCAT(
                                SqlFunction.TO_CHAR(PdpSourceDataField.vendorHeaderGeneratedIdentifier), "-%"
                        ))
                )
                .leftJoin(CuPaymentRequestDocument.class, Criteria.equal(
                        PdpSourceDataField.custPaymentDocNbr, PdpSourceDataField.preqDocumentNumber))
                .leftJoin(DisbursementVoucherDocument.class, Criteria.equal(
                        PdpSourceDataField.custPaymentDocNbr, PdpSourceDataField.dvDocumentNumber))
                .leftJoin(DisbursementVoucherNonresidentTax.class, Criteria.equal(
                        PdpSourceDataField.dvDocumentNumber, PdpSourceDataField.dvNraDocumentNumber))
                .where(
                        Criteria.between(PdpSourceDataField.disbursementDate, Types.DATE,
                                config.getStartDate(), config.getEndDate()),
                        Criteria.notAnd(
                                Criteria.equal(PdpSourceDataField.customerCampusCode, CuKimConstants.CORNELL_IT_CAMPUS),
                                Criteria.equal(PdpSourceDataField.unitCode, CUTaxConstants.KUAL_UNIT),
                                Criteria.equal(PdpSourceDataField.subUnitCode,
                                        DisbursementVoucherConstants.DOCUMENT_TYPE_CODE)
                        ),
                        Criteria.between(PdpSourceDataField.disbursementNbr,
                                PdpSourceDataField.beginDisbursementNbr, PdpSourceDataField.endDisbursementNbr),
                        getTaxTypeSpecificPdpQueryCriteria(config)
                )
                .orderBy(
                        QuerySort.ascending(PdpSourceDataField.vendorTaxNumber),
                        QuerySort.ascending(PdpSourceDataField.summaryLastUpdatedTimestamp),
                        QuerySort.ascending(PdpSourceDataField.summaryId)
                )
                .build();
    }

    private Criteria getTaxTypeSpecificPdpQueryCriteria(final TaxBatchConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new UnsupportedOperationException(
                        "This implementation currently does not support 1099 tax processing");
            case CUTaxConstants.TAX_TYPE_1042S:
                return Criteria.equal(PdpSourceDataField.vendorForeignIndicator, KRADConstants.YES_INDICATOR_VALUE);
            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    @Override
    public TaxStatistics createPrncTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PrncSourceData> prncSourceHandler,
            final TransactionDetailHandler secondPassHandler) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                PrncSourceDataField.class);
        createPrncSourceDataQuery(config, metadata);
        return null;
    }

    private CuSqlQuery createPrncSourceDataQuery(final TaxBatchConfig config, final TaxDtoDbMetadata metadata) {
        return new TaxQueryBuilder(metadata)
                .build();
    }

}
