package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.PreparedStatementCreatorForUpdatableResultSets;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxColumns.NoteColumn;
import edu.cornell.kfs.tax.batch.TaxColumns.TransactionDetailColumn;
import edu.cornell.kfs.tax.batch.TaxColumns.VendorAddressColumn;
import edu.cornell.kfs.tax.batch.TaxColumns.VendorDetailColumn;
import edu.cornell.kfs.tax.batch.TaxOutputConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailExtractor;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.businessobject.NoteLite;
import edu.cornell.kfs.tax.businessobject.VendorAddressLite;
import edu.cornell.kfs.tax.businessobject.VendorDetailLite;
import edu.cornell.kfs.vnd.CUVendorConstants.CUAddressTypes;

public class TransactionDetailProcessorDaoJdbcImpl extends PlatformAwareDaoBaseJdbc
        implements TransactionDetailProcessorDao {

    private static final Logger LOG = LogManager.getLogger();

    private EncryptionService encryptionService;

    @Override
    public TaxStatistics processTransactionDetails(final TaxOutputConfig config,
            final FailableBiFunction<TaxOutputConfig, TransactionDetailExtractor, TaxStatistics, Exception> handler)
                    throws SQLException {
        final CuSqlQuery query = createTransactionDetailQuery(config);
        final PreparedStatementCreator statementCreator = new PreparedStatementCreatorForUpdatableResultSets(
                query.getQueryString());
        final PreparedStatementSetter statementSetter = new ArgumentTypePreparedStatementSetter(
                query.getParameterValuesArray(), query.getParameterTypesArray());

        return getJdbcTemplate().query(statementCreator, statementSetter, resultSet -> {
            try {
                final TransactionDetailExtractor extractor = new TransactionDetailExtractorImpl(
                        resultSet, encryptionService);
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

    private CuSqlQuery createTransactionDetailQuery(final TaxOutputConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099 :
                throw new RuntimeException("This implementation currently does not support 1099 processing");

            case CUTaxConstants.TAX_TYPE_1042S :
                return createTransactionDetailQueryFor1042S(config);

            default :
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    private CuSqlQuery createTransactionDetailQueryFor1042S(final TaxOutputConfig config) {
        return new CuSqlChunk()
                .append("SELECT ").append(getCommaSeparatedListOfColumnSelectors(TransactionDetailColumn.class))
                .append(" FROM KFS.TX_TRANSACTION_DETAIL_T")
                .append(" WHERE REPORT_YEAR = ").appendAsParameter(Types.INTEGER, config.getReportYear())
                .append(" AND FORM_1042S_BOX = ").appendAsParameter(CUTaxConstants.NEEDS_UPDATING_BOX_KEY)
                .append(" ORDER BY VENDOR_TAX_NBR, INCOME_CODE, INCOME_CODE_SUB_TYPE")
                .toQuery();
    }

    private String getCommaSeparatedListOfColumnSelectors(final Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(this::getColumnExpressionForSelect)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
    }

    private String getColumnExpressionForSelect(final Enum<?> column) {
        final String selectorExpression = column.toString();
        return StringUtils.equals(selectorExpression, column.name())
                ? selectorExpression
                : StringUtils.join(selectorExpression, KFSConstants.BLANK_SPACE, CUKFSConstants.DOUBLE_QUOTE,
                        column.name(), CUKFSConstants.DOUBLE_QUOTE);
    }



    @Override
    public VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId)
            throws SQLException {
        final CuSqlQuery query = createVendorQuery(vendorHeaderId, vendorDetailId);
        final PreparedStatementSetter statementSetter = new ArgumentTypePreparedStatementSetter(
                query.getParameterValuesArray(), query.getParameterTypesArray());
        final ResultSetExtractor<VendorQueryResults> extractor =
                resultSet -> extractVendorQueryResults(resultSet, vendorHeaderId, vendorDetailId);
        return getJdbcTemplate().query(query.getQueryString(), statementSetter, extractor);
    }

    private CuSqlQuery createVendorQuery(final Integer vendorHeaderId, final Integer vendorDetailId) {
        return new CuSqlChunk()
                .append("SELECT ").append(getCommaSeparatedListOfColumnSelectors(VendorDetailColumn.class))
                .append(" FROM KFS.PUR_VNDR_DTL_T DTL")
                .append(" JOIN KFS.PUR_VNDR_HDR_T HDR ON DTL.VNDR_HDR_GNRTD_ID = HDR.VNDR_HDR_GNRTD_ID")
                .append(" WHERE DTL.VNDR_HDR_GNRTD_ID = ").appendAsParameter(Types.INTEGER, vendorHeaderId)
                .append(" AND (")
                        .append("DTL.VNDR_DTL_ASND_ID = ").appendAsParameter(Types.INTEGER, vendorDetailId)
                        .append(" OR DTL.VNDR_PARENT_IND = ").appendAsParameter(KRADConstants.YES_INDICATOR_VALUE)
                .append(")")
                .append(" ORDER BY DTL.VNDR_PARENT_IND ASC NULLS FIRST")
                .toQuery();
    }

    private VendorQueryResults extractVendorQueryResults(final ResultSet resultSet, final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException {
        final VendorDetailLiteExtractorImpl vendorExtractor = new VendorDetailLiteExtractorImpl(
                resultSet, encryptionService);
        if (!vendorExtractor.moveToNextRow()) {
            return new VendorQueryResults(null, null);
        }

        final VendorDetailLite vendorDetail = vendorExtractor.getCurrentRow();
        final VendorDetailLite parentDetail;
        if (!vendorExtractor.moveToNextRow()) {
            if (vendorDetailId.equals(vendorDetail.getVendorDetailAssignedIdentifier())) {
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
                        vendorHeaderId, vendorDetailId);
            }

            if (!parentDetail.isVendorParentIndicator()) {
                LOG.warn("extractVendorDetailResults, Vendor {}-{} was expected to be the parent of vendor {}-{} "
                        + "but it wasn't. It will be ignored.",
                        parentDetail.getVendorHeaderGeneratedIdentifier(),
                        parentDetail.getVendorDetailAssignedIdentifier(), vendorHeaderId, vendorDetailId);
                return new VendorQueryResults(vendorDetail, null);
            } else {
                return new VendorQueryResults(vendorDetail, parentDetail);
            }
        }
    }



    @Override
    public VendorAddressLite getHighestPriorityUSVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException {
        final CuSqlChunk countryCodeCondition = new CuSqlChunk()
                .append(" AND (")
                        .append("VNDR_CNTRY_CD IS NULL")
                        .append(" OR VNDR_CNTRY_CD = ").appendAsParameter(KFSConstants.COUNTRY_CODE_UNITED_STATES)
                .append(")");
        return getHighestPriorityVendorAddress(vendorHeaderId, vendorDetailId, countryCodeCondition);
    }

    @Override
    public VendorAddressLite getHighestPriorityForeignVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException {
        final CuSqlChunk countryCodeCondition = new CuSqlChunk()
                .append(" AND VNDR_CNTRY_CD IS NOT NULL")
                .append(" AND VNDR_CNTRY_CD <> ").appendAsParameter(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        return getHighestPriorityVendorAddress(vendorHeaderId, vendorDetailId, countryCodeCondition);
    }

    private VendorAddressLite getHighestPriorityVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId, final CuSqlChunk countryCodeCondition) throws SQLException {
        final CuSqlQuery query = createVendorAddressQuery(vendorHeaderId, vendorDetailId, countryCodeCondition);
        final PreparedStatementSetter statementSetter = new ArgumentTypePreparedStatementSetter(
                query.getParameterValuesArray(), query.getParameterTypesArray());
        return getJdbcTemplate().query(query.getQueryString(), statementSetter, this::extractVendorAddressResults);
    }

    private CuSqlQuery createVendorAddressQuery(final Integer vendorHeaderId, final Integer vendorDetailId,
            final CuSqlChunk countryCodeCondition) {
        return new CuSqlChunk()
                .append("SELECT ").append(getCommaSeparatedListOfColumnSelectors(VendorAddressColumn.class))
                .append(" FROM KFS.PUR_VNDR_ADDR_T")
                .append(" WHERE VNDR_HDR_GNRTD_ID = ").appendAsParameter(Types.INTEGER, vendorHeaderId)
                .append(" AND VNDR_HDR_GNRTD_ID = ").appendAsParameter(Types.INTEGER, vendorDetailId)
                .append(countryCodeCondition)
                .append(" AND ").append(CuSqlChunk.asSqlInCondition("VNDR_ADDR_TYP_CD",
                        List.of(CUAddressTypes.TAX, AddressTypes.REMIT, AddressTypes.PURCHASE_ORDER)))
                .append(" AND DOBJ_MAINT_CD_ACTV_IND = ").appendAsParameter(KRADConstants.YES_INDICATOR_VALUE)
                .append(" ORDER BY VNDR_ADDR_TYP_CD DESC, VNDR_ADDR_GNRTD_ID DESC")
                .toQuery();
    }

    private VendorAddressLite extractVendorAddressResults(final ResultSet resultSet) throws SQLException {
        final VendorAddressLiteExtractorImpl addressExtractor = new VendorAddressLiteExtractorImpl(
                resultSet, encryptionService);
        if (addressExtractor.moveToNextRow()) {
            return addressExtractor.getCurrentRow();
        } else {
            return null;
        }
    }



    @Override
    public List<NoteLite> getNotesByDocumentNumber(final String documentNumber) throws SQLException {
        final CuSqlQuery query = createNoteQuery(documentNumber);
        final PreparedStatementSetter statementSetter = new ArgumentTypePreparedStatementSetter(
                query.getParameterValuesArray(), query.getParameterTypesArray());
        return getJdbcTemplate().query(query.getQueryString(), statementSetter, this::extractNoteResults);
    }

    private CuSqlQuery createNoteQuery(final String documentNumber) {
        return new CuSqlChunk()
                .append("SELECT ").append(getCommaSeparatedListOfColumnSelectors(NoteColumn.class))
                .append(" FROM KFS.KRNS_NTE_T")
                .append(" WHERE RMT_OBJ_ID = (SELECT OBJ_ID FROM KFS.FS_DOC_HEADER_T WHERE FDOC_NBR = ")
                        .appendAsParameter(documentNumber).append(")")
                .toQuery();
    }

    private List<NoteLite> extractNoteResults(final ResultSet resultSet) throws SQLException {
        final NoteLiteExtractorImpl noteExtractor = new NoteLiteExtractorImpl(resultSet, encryptionService);
        Stream.Builder<NoteLite> notes = Stream.builder();
        while (noteExtractor.moveToNextRow()) {
            notes.add(noteExtractor.getCurrentRow());
        }
        return notes.build().collect(Collectors.toUnmodifiableList());
    }



    public void setEncryptionService(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

}
