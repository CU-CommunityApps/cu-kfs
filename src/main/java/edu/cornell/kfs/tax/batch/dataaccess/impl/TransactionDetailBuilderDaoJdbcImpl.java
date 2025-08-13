package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
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
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
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
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
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
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.FieldUpdate;
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
    public void insertTransactionDetailsWithoutVendorInfo(
            final List<TransactionDetail> transactionDetails, final TaxBatchConfig config) {
        final CuSqlQuery insertQuery = createQueryForBatchInsertingTransactionDetails(config);
        int[] insertCounts = executeBatchUpdate(insertQuery, transactionDetails);

        for (int i = 0; i < insertCounts.length; i++) {
            if (insertCounts[i] != 1) {
                LOG.warn("insertTransactionDetailsWithoutVendorInfo, Batch insert for Transaction Detail "
                        + "at batch index {} should have inserted 1 row, but it actually inserted {} rows instead!",
                        i, insertCounts[i]);
            }
        }
        LOG.debug("insertTransactionDetailsWithoutVendorInfo, Inserted {} transaction details", transactionDetails::size);
    }

    private CuSqlQuery createQueryForBatchInsertingTransactionDetails(final TaxBatchConfig config) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                TransactionDetailField.class);
        final FieldUpdate taxBoxFieldUpdate = getTaxBoxFieldUpdateForInsert(config);

        final FieldUpdate[] fieldUpdates = {
                FieldUpdate.ofNextSequenceValue(TransactionDetailField.transactionDetailId, CUTaxBatchConstants.TRANSACTION_DETAIL_ID_SEQUENCE),
                FieldUpdate.of(TransactionDetailField.reportYear, Types.INTEGER, TransactionDetail::getReportYear),
                FieldUpdate.of(TransactionDetailField.documentNumber, TransactionDetail::getDocumentNumber),
                FieldUpdate.of(TransactionDetailField.documentType, TransactionDetail::getDocumentType),
                FieldUpdate.of(TransactionDetailField.financialDocumentLineNumber, Types.INTEGER, TransactionDetail::getFinancialDocumentLineNumber),
                FieldUpdate.of(TransactionDetailField.finObjectCode, TransactionDetail::getFinObjectCode),
                FieldUpdate.of(TransactionDetailField.netPaymentAmount, Types.DECIMAL, TransactionDetail::getNetPaymentAmount),
                FieldUpdate.of(TransactionDetailField.documentTitle, TransactionDetail::getDocumentTitle),
                FieldUpdate.of(TransactionDetailField.vendorTaxNumber, TransactionDetail::getVendorTaxNumber),
                FieldUpdate.of(TransactionDetailField.incomeCode, TransactionDetail::getIncomeCode),
                FieldUpdate.of(TransactionDetailField.incomeCodeSubType, TransactionDetail::getIncomeCodeSubType),
                FieldUpdate.of(TransactionDetailField.dvCheckStubText, TransactionDetail::getDvCheckStubText),
                FieldUpdate.of(TransactionDetailField.payeeId, TransactionDetail::getPayeeId),
                FieldUpdate.ofCharBoolean(TransactionDetailField.nraPaymentIndicator, TransactionDetail::getNraPaymentIndicator),
                FieldUpdate.of(TransactionDetailField.paymentDate, Types.DATE, TransactionDetail::getPaymentDate),
                FieldUpdate.of(TransactionDetailField.paymentPayeeName, TransactionDetail::getPaymentPayeeName),
                FieldUpdate.of(TransactionDetailField.incomeClassCode, TransactionDetail::getIncomeClassCode),
                FieldUpdate.ofCharBoolean(TransactionDetailField.incomeTaxTreatyExemptIndicator, TransactionDetail::getIncomeTaxTreatyExemptIndicator),
                FieldUpdate.ofCharBoolean(TransactionDetailField.foreignSourceIncomeIndicator, TransactionDetail::getForeignSourceIncomeIndicator),
                FieldUpdate.of(TransactionDetailField.federalIncomeTaxPercent, Types.DECIMAL, TransactionDetail::getFederalIncomeTaxPercent),
                FieldUpdate.of(TransactionDetailField.paymentDescription, TransactionDetail::getPaymentDescription),
                FieldUpdate.of(TransactionDetailField.paymentLine1Address, TransactionDetail::getPaymentLine1Address),
                FieldUpdate.of(TransactionDetailField.paymentCountryName, TransactionDetail::getPaymentCountryName),
                FieldUpdate.of(TransactionDetailField.chartCode, TransactionDetail::getChartCode),
                FieldUpdate.of(TransactionDetailField.accountNumber, TransactionDetail::getAccountNumber),
                FieldUpdate.of(TransactionDetailField.initiatorNetId, TransactionDetail::getInitiatorNetId),
                taxBoxFieldUpdate,
                FieldUpdate.of(TransactionDetailField.paymentReasonCode, TransactionDetail::getPaymentReasonCode),
                FieldUpdate.of(TransactionDetailField.disbursementNbr, Types.BIGINT, TransactionDetail::getDisbursementNbr),
                FieldUpdate.of(TransactionDetailField.paymentStatusCode, TransactionDetail::getPaymentStatusCode),
                FieldUpdate.of(TransactionDetailField.disbursementTypeCode, TransactionDetail::getDisbursementTypeCode),
                FieldUpdate.of(TransactionDetailField.ledgerDocumentTypeCode, TransactionDetail::getLedgerDocumentTypeCode)
        };

        return new TaxQueryBuilder(metadata)
                .insertValuesInto(TransactionDetail.class, fieldUpdates)
                .build();
    }

    private FieldUpdate getTaxBoxFieldUpdateForInsert(final TaxBatchConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new UnsupportedOperationException(
                        "This implementation currently does not support 1099 tax processing");
            case CUTaxConstants.TAX_TYPE_1042S:
                return FieldUpdate.of(TransactionDetailField.form1042SBox, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    @Override
    public TaxStatistics createDvTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<DvSourceData> dvSourceHandler) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                DvSourceDataField.class);
        final CuSqlQuery query = createDvSourceDataQuery(config, metadata);

        return queryAndProcessTaxSourceData(query, resultSet -> {
            final TaxDtoRowMapper<DvSourceData> rowMapper = new TaxDtoRowMapperImpl<>(
                    DvSourceData::new, encryptionService, metadata, resultSet);
            return dvSourceHandler.generateTransactionDetails(config, rowMapper);
        });
    }

    private TaxStatistics queryAndProcessTaxSourceData(final CuSqlQuery query,
            FailableFunction<ResultSet, TaxStatistics, Exception> queryResultsProcessor) {
        return queryForResults(query, resultSet -> {
            try {
                return queryResultsProcessor.apply(resultSet);
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
                        getTaxTypeSpecificQueryCriteria(config, DvSourceDataField.vendorForeignIndicator)
                )
                .orderBy(
                        QuerySort.ascending(DvSourceDataField.vendorTaxNumber),
                        QuerySort.ascending(DvSourceDataField.extractDate),
                        QuerySort.ascending(DvSourceDataField.dvDocumentNumber),
                        QuerySort.ascending(DvSourceDataField.accountingLineSequenceNumber)
                )
                .build();
    }

    private Criteria getTaxTypeSpecificQueryCriteria(final TaxBatchConfig config,
            final TaxDtoFieldEnum vendorForeignIndicatorField) {
        final String vendorForeignIndicatorValue =
                StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S)
                        ? KRADConstants.YES_INDICATOR_VALUE : KRADConstants.NO_INDICATOR_VALUE;

        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                throw new UnsupportedOperationException(
                        "This implementation currently does not support 1099 tax processing");
            case CUTaxConstants.TAX_TYPE_1042S:
                return Criteria.equal(vendorForeignIndicatorField, vendorForeignIndicatorValue);
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
            final TransactionSourceHandler<PdpSourceData> pdpSourceHandler) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                PdpSourceDataField.class);
        final CuSqlQuery query = createPdpSourceDataQuery(config, metadata);

        return queryAndProcessTaxSourceData(query, resultSet -> {
            final TaxDtoRowMapper<PdpSourceData> rowMapper = new TaxDtoRowMapperImpl<>(
                    PdpSourceData::new, encryptionService, metadata, resultSet);
            return pdpSourceHandler.generateTransactionDetails(config, rowMapper);
        });
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
                        getTaxTypeSpecificQueryCriteria(config, PdpSourceDataField.vendorForeignIndicator)
                )
                .orderBy(
                        QuerySort.ascending(PdpSourceDataField.vendorTaxNumber),
                        QuerySort.ascending(PdpSourceDataField.summaryLastUpdatedTimestamp),
                        QuerySort.ascending(PdpSourceDataField.summaryId)
                )
                .build();
    }

    @Override
    public TaxStatistics createPrncTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PrncSourceData> prncSourceHandler) {
        final TaxDtoDbMetadata metadata = taxTableMetadataLookupService.getDatabaseMappingMetadataForDto(
                PrncSourceDataField.class);
        final CuSqlQuery query = createPrncSourceDataQuery(config, metadata);

        return queryAndProcessTaxSourceData(query, resultSet -> {
            final TaxDtoRowMapper<PrncSourceData> rowMapper = new TaxDtoRowMapperImpl<>(
                    PrncSourceData::new, encryptionService, metadata, resultSet);
            return prncSourceHandler.generateTransactionDetails(config, rowMapper);
        });
    }

    private CuSqlQuery createPrncSourceDataQuery(final TaxBatchConfig config, final TaxDtoDbMetadata metadata) {
        final TaxQueryBuilder documentsFilteredByFinalizedDateSubquery =
                createRouteHeaderSubqueryFilteredByFinalizedDate(
                        config, CuPaymentRequestDocument.DOCUMENT_TYPE_NON_CHECK, metadata);

        return new TaxQueryBuilder(metadata)
                .selectAllMappedFields()
                .from(PaymentRequestItem.class)
                .join(CuPaymentRequestDocument.class, Criteria.equal(
                        PrncSourceDataField.purapDocumentIdentifier, PrncSourceDataField.preqPurapDocumentIdentifier))
                .join(PaymentRequestAccount.class, Criteria.equal(
                        PrncSourceDataField.itemIdentifier, PrncSourceDataField.accountItemIdentifier))
                .join(VendorHeader.class, Criteria.equal(
                        PrncSourceDataField.preqVendorHeaderGeneratedIdentifier,
                        PrncSourceDataField.vendorHeaderGeneratedIdentifier))
                .join(UniversityDate.class, Criteria.equal(
                        PrncSourceDataField.universityDate, Types.DATE, config.getStartDate()))
                .where(
                        Criteria.in(PrncSourceDataField.paymentMethodCode, List.of(
                                PaymentSourceConstants.PAYMENT_METHOD_DRAFT,
                                PaymentSourceConstants.PAYMENT_METHOD_WIRE
                        )),
                        Criteria.isNotNull(PrncSourceDataField.accountIdentifier),
                        Criteria.in(PrncSourceDataField.preqDocumentNumber, documentsFilteredByFinalizedDateSubquery),
                        getTaxTypeSpecificQueryCriteria(config, PrncSourceDataField.vendorForeignIndicator)
                )
                .orderBy(
                        QuerySort.ascending(PrncSourceDataField.vendorTaxNumber),
                        QuerySort.ascending(PrncSourceDataField.preqDocumentNumber),
                        QuerySort.ascending(PrncSourceDataField.accountItemIdentifier),
                        QuerySort.ascending(PrncSourceDataField.accountIdentifier)
                )
                .build();
    }

}
