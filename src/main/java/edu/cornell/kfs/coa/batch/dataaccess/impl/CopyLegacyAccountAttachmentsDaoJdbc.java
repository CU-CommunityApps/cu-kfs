package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.coa.batch.CuCoaBatchConstants.LegacyAccountAttachmentProperty;
import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.dataaccess.CopyLegacyAccountAttachmentsDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CopyLegacyAccountAttachmentsDaoJdbc
        extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CopyLegacyAccountAttachmentsDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<LegacyAccountAttachment> getLegacyAccountAttachmentsToCopy(
            final int fetchSize, final int maxRetryCount) {
        final CuSqlQuery sqlQuery = buildLegacyAccountAttachmentQuery(fetchSize, maxRetryCount);
        final List<LegacyAccountAttachment> results = queryForValues(sqlQuery, this::mapLegacyAccountAttachment);
        LOG.info("getLegacyAccountAttachmentsToCopy, Fetched {} attachments for copying", results.size());
        return results;
    }

    private CuSqlQuery buildLegacyAccountAttachmentQuery(final int fetchSize, final int maxRetryCount) {
        return new CuSqlChunk()
                .append("SELECT * FROM KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" WHERE COPIED_IND = ").appendAsParameter(KRADConstants.NO_INDICATOR_VALUE)
                .append(" AND RETRY_COUNT < ").appendAsParameter(Types.INTEGER, maxRetryCount)
                .append(" ORDER BY COPYING_ACCT_ATTACH_ID")
                .append(" FETCH FIRST ").appendAsParameter(Types.INTEGER, fetchSize).append(" ROWS ONLY")
                .toQuery();
    }

    private LegacyAccountAttachment mapLegacyAccountAttachment(final ResultSet rs, final int rowNum)
            throws SQLException {
        final LegacyAccountAttachment legacyAccountAttachment = new LegacyAccountAttachment();
        for (final LegacyAccountAttachmentProperty dtoProperty : LegacyAccountAttachmentProperty.values()) {
            final String columnValue = rs.getString(dtoProperty.getColumnName());
            dtoProperty.getPropertySetter().accept(legacyAccountAttachment, columnValue);
        }
        return legacyAccountAttachment;
    }

    @Override
    public void markLegacyAccountAttachmentsAsCopied(final List<LegacyAccountAttachment> legacyAccountAttachments) {
        final CuSqlQuery sqlQuery = buildQueryForMarkingAccountAttachmentsAsCopied(legacyAccountAttachments);
        final int numUpdatedRows = executeUpdate(sqlQuery);
        LOG.info("markLegacyAccountAttachmentsAsCopied, {} attachments were marked as copied", numUpdatedRows);
        if (numUpdatedRows != legacyAccountAttachments.size()) {
            LOG.warn("markLegacyAccountAttachmentsAsCopied, {} attachments were meant to be marked as copied, "
                    + "but only {} were updated in the database", legacyAccountAttachments.size(), numUpdatedRows);
        }
    }

    private CuSqlQuery buildQueryForMarkingAccountAttachmentsAsCopied(
            final List<LegacyAccountAttachment> legacyAccountAttachments) {
        final List<String> ids = legacyAccountAttachments.stream()
                .map(LegacyAccountAttachment::getId)
                .collect(Collectors.toUnmodifiableList());
        return new CuSqlChunk()
                .append("UPDATE KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" SET COPIED_IND = ").appendAsParameter(KRADConstants.YES_INDICATOR_VALUE)
                .append(" WHERE ").append(CuSqlChunk.asSqlInCondition("COPYING_ACCT_ATTACH_ID", ids))
                .toQuery();
    }

    @Override
    public void incrementRetryCountsOnLegacyAccountAttachments(
            final List<LegacyAccountAttachment> legacyAccountAttachments) {
        final CuSqlQuery sqlQuery = buildQueryForIncrementingRetryCounts(legacyAccountAttachments);
        final int numUpdatedRows = executeUpdate(sqlQuery);
        LOG.info("incrementRetryCountsOnLegacyAccountAttachments, {} attachments had their retry counts incremented",
                numUpdatedRows);
        if (numUpdatedRows != legacyAccountAttachments.size()) {
            LOG.warn("incrementRetryCountsOnLegacyAccountAttachments, {} attachments were meant to have their "
                    + "retry counts incremented, but only {} were updated in the database",
                    legacyAccountAttachments.size(), numUpdatedRows);
        }
    }

    private CuSqlQuery buildQueryForIncrementingRetryCounts(
            final List<LegacyAccountAttachment> legacyAccountAttachments) {
        final List<String> ids = legacyAccountAttachments.stream()
                .map(LegacyAccountAttachment::getId)
                .collect(Collectors.toUnmodifiableList());
        return new CuSqlChunk()
                .append("UPDATE KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" SET RETRY_COUNT = RETRY_COUNT + 1")
                .append(" WHERE ").append(CuSqlChunk.asSqlInCondition("COPYING_ACCT_ATTACH_ID", ids))
                .toQuery();
    }

}
