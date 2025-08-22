package edu.cornell.kfs.kew.actiontaken.dao.impl;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actiontaken.dao.impl.ActionTakenDAOOjbImpl;
import org.kuali.kfs.kew.actiontaken.dao.impl.ActionTakenDaoImpl;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cornell.kfs.kew.actiontaken.dao.CuActionTakenDAO;

public class CuActionTakenDaoImpl extends ActionTakenDaoImpl implements CuActionTakenDAO {

    private static final Logger LOG = LogManager.getLogger();

    private static final String LAST_MODIFIED_DATE_QUERY =
            "select STAT_MDFN_DT from KREW_DOC_HDR_T where DOC_HDR_ID=?";

    private final JdbcTemplate jdbcTemplate;

    public CuActionTakenDaoImpl(final ActionTakenDAOOjbImpl actionTakenDaoOjb, final JdbcTemplate jdbcTemplate) {
        super(actionTakenDaoOjb, jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Timestamp getLastModifiedDate(final String documentId) {
        try {
            final Timestamp lastModifiedDate = jdbcTemplate.queryForObject(
                    LAST_MODIFIED_DATE_QUERY, Timestamp.class, documentId);
            LOG.debug("getLastModifiedDate, Document {} has a Last Modified Date of {}",
                    documentId, lastModifiedDate);
            return lastModifiedDate;
        } catch (final DataAccessException e) {
            LOG.atError()
                    .withThrowable(e)
                    .log("getLastModifiedDate, Error determining Last Modified Date for document {}", documentId);
            throw new WorkflowRuntimeException("Error determining Last Modified Date.", e);
        }
    }

}
