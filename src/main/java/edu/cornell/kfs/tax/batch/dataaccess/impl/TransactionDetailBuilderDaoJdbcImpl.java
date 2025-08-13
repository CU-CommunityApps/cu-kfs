package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;

import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionSourceHandler;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.dto.DvSourceData.DvSourceDataField;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.batch.service.TaxTableMetadataLookupService;
import edu.cornell.kfs.tax.batch.util.TaxQueryBuilder;
import edu.cornell.kfs.tax.batch.util.TaxQueryUtils.Criteria;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

public class TransactionDetailBuilderDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements TransactionDetailBuilderDao {

    private static final Logger LOG = LogManager.getLogger();

    private TaxTableMetadataLookupService taxTableMetadataLookupService;

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
        createDvSourceDataQuery(config, metadata);
        return null;
    }

    private CuSqlQuery createDvSourceDataQuery(final TaxBatchConfig config, final TaxDtoDbMetadata metadata) {
        return new TaxQueryBuilder(metadata)
                .selectAllMappedFields()
                .from(DisbursementVoucherPayeeDetail.class)
                .join(DisbursementVoucherNonresidentTax.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.dvNraDocumentNumber))
                .join(SourceAccountingLine.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.accountingLineDocumentNumber))
                .join(DisbursementVoucherDocument.class, Criteria.equal(
                        DvSourceDataField.payeeDetailDocumentNumber, DvSourceDataField.dvDocumentNumber))
                .build();
    }

    @Override
    public TaxStatistics createPdpTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PdpSourceData> pdpSourceHandler,
            final TransactionDetailHandler secondPassHandler) {
        return null;
    }

    @Override
    public TaxStatistics createPrncTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PrncSourceData> prncSourceHandler,
            final TransactionDetailHandler secondPassHandler) {
        return null;
    }

}
