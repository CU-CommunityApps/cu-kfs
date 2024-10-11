package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.dataaccess.CopyLegacyAccountAttachmentsDao;
import edu.cornell.kfs.sys.dataaccess.DtoPropertyHandler;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CopyLegacyAccountAttachmentsDaoJdbc
        extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CopyLegacyAccountAttachmentsDao {

    private static final Logger LOG = LogManager.getLogger();

    private final List<DtoPropertyHandler<LegacyAccountAttachment, ?>> dtoProperties = List.of(
            DtoPropertyHandler.forLong("COPYING_ACCT_ATTACH_ID", LegacyAccountAttachment::setId),
            DtoPropertyHandler.forString("ORIGINAL_ACCOUNT_CODE", LegacyAccountAttachment::setLegacyAccountCode),
            DtoPropertyHandler.forString("KFS_CHART_CODE", LegacyAccountAttachment::setKfsChartCode),
            DtoPropertyHandler.forString("KFS_ACCOUNT_NBR", LegacyAccountAttachment::setKfsAccountNumber),
            DtoPropertyHandler.forString("FILE_NAME", LegacyAccountAttachment::setFileName),
            DtoPropertyHandler.forString("ADDED_BY", LegacyAccountAttachment::setAddedBy),
            DtoPropertyHandler.forString("FILE_DESCRIPTION", LegacyAccountAttachment::setFileDescription),
            DtoPropertyHandler.forString("FILE_SYSTEM_FILE_NAME", LegacyAccountAttachment::setFilePath),
            DtoPropertyHandler.forInteger("RETRY_COUNT", LegacyAccountAttachment::setRetryCount),
            DtoPropertyHandler.forString("COPIED_IND", LegacyAccountAttachment::setCopied)
    );

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
                .append(" AND FILE_SYSTEM_FILE_NAME IS NOT NULL ")
                .append(" ORDER BY COPYING_ACCT_ATTACH_ID")
                .append(" FETCH FIRST ").appendAsParameter(Types.INTEGER, fetchSize).append(" ROWS ONLY")
                .toQuery();
    }

    private LegacyAccountAttachment mapLegacyAccountAttachment(final ResultSet rs, final int rowNum)
            throws SQLException {
        final LegacyAccountAttachment legacyAccountAttachment = new LegacyAccountAttachment();
        for (final DtoPropertyHandler<LegacyAccountAttachment, ?> dtoPropertyEntry : dtoProperties) {
            dtoPropertyEntry.setProperty(legacyAccountAttachment, rs);
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
        final List<Long> ids = legacyAccountAttachments.stream()
                .map(LegacyAccountAttachment::getId)
                .collect(Collectors.toUnmodifiableList());
        return new CuSqlChunk()
                .append("UPDATE KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" SET COPIED_IND = ").appendAsParameter(KRADConstants.YES_INDICATOR_VALUE)
                .append(" WHERE ").append(CuSqlChunk.asSqlInCondition("COPYING_ACCT_ATTACH_ID", Types.BIGINT, ids))
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
        final List<Long> ids = legacyAccountAttachments.stream()
                .map(LegacyAccountAttachment::getId)
                .collect(Collectors.toUnmodifiableList());
        return new CuSqlChunk()
                .append("UPDATE KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" SET RETRY_COUNT = RETRY_COUNT + 1")
                .append(" WHERE ").append(CuSqlChunk.asSqlInCondition("COPYING_ACCT_ATTACH_ID", Types.BIGINT, ids))
                .toQuery();
    }

}
