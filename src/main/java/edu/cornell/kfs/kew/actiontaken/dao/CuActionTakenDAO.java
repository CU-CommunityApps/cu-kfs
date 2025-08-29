package edu.cornell.kfs.kew.actiontaken.dao;

import java.sql.Timestamp;

import org.kuali.kfs.kew.actiontaken.dao.ActionTakenDAO;

public interface CuActionTakenDAO extends ActionTakenDAO {

    Timestamp getLastModifiedDate(final String documentId);

}
