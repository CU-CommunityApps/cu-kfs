package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.coa.batch.businessobject.LegacyAccountAttachment;
import edu.cornell.kfs.coa.batch.dataaccess.CopyLegacyAccountAttachmentsDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CopyLegacyAccountAttachmentsDaoJdbc
        extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CopyLegacyAccountAttachmentsDao {

    private static final Logger LOG = LogManager.getLogger();

    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

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
        final LegacyAccountAttachment attachment = new LegacyAccountAttachment();
        attachment.setId(rs.getLong("COPYING_ACCT_ATTACH_ID"));
        attachment.setLegacyAccountCode(rs.getString("ORIGINAL_ACCOUNT_CODE"));
        attachment.setKfsChartCode(rs.getString("KFS_CHART_CODE"));
        attachment.setKfsAccountNumber(rs.getString("KFS_ACCOUNT_NBR"));
        attachment.setFileName(rs.getString("FILE_NAME"));
        attachment.setAddedBy(rs.getString("ADDED_BY"));
        attachment.setFileDescription(rs.getString("FILE_DESCRIPTION"));
        attachment.setFilePath(rs.getString("FILE_SYSTEM_FILE_NAME"));
        attachment.setRetryCount(rs.getInt("RETRY_COUNT"));
        attachment.setCopied(rs.getString("COPIED_IND"));
        attachment.setLatestErrorMessage(rs.getString("LATEST_ERR_MSG"));
        return attachment;
    }

    @Override
    public void markLegacyAccountAttachmentAsCopied(final LegacyAccountAttachment legacyAccountAttachment) {
        final Long id = legacyAccountAttachment.getId();
        final CuSqlQuery sqlQuery = new CuSqlChunk()
                .append("UPDATE KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" SET COPIED_IND = ").appendAsParameter(KRADConstants.YES_INDICATOR_VALUE)
                .append(" WHERE COPYING_ACCT_ATTACH_ID = ").appendAsParameter(Types.BIGINT, id)
                .toQuery();
        executeUpdate(sqlQuery);
    }

    @Override
    public void recordCopyingErrorForLegacyAccountAttachment(final LegacyAccountAttachment legacyAccountAttachment,
            final String errorMessage) {
        final Long id = legacyAccountAttachment.getId();
        final String errorMessageToStore = StringUtils.left(errorMessage, MAX_ERROR_MESSAGE_LENGTH);
        final CuSqlQuery sqlQuery = new CuSqlChunk()
                .append("UPDATE KFS.TEMP_ACCT_ATTACH_FOR_COPYING")
                .append(" SET RETRY_COUNT = RETRY_COUNT + 1,")
                .append(" LATEST_ERR_MSG = ").appendAsParameter(errorMessageToStore)
                .append(" WHERE COPYING_ACCT_ATTACH_ID = ").appendAsParameter(Types.BIGINT, id)
                .toQuery();
        executeUpdate(sqlQuery);
    }

}
