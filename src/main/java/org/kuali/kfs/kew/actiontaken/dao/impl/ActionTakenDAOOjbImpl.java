/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kew.actiontaken.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.actiontaken.dao.ActionTakenDAO;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.springmodules.orm.ojb.PersistenceBrokerCallback;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * ====
 *  * CU Customization: Added handling of the custom "getLastModifiedDate" action list preference.
 * ====
 */

/**
 * OJB implementation of the {@link ActionTakenDAO}.
 */
public class ActionTakenDAOOjbImpl extends PersistenceBrokerDaoSupport implements ActionTakenDAO {

    private static final Logger LOG = LogManager.getLogger();
    private static final String LAST_ACTION_TAKEN_DATE_QUERY =
            "select max(ACTN_DT) from KREW_ACTN_TKN_T where DOC_HDR_ID=? and ACTN_CD=?";

    @Override
    public void deleteActionTaken(final ActionTaken actionTaken) {
        LOG.debug("deleting ActionTaken {}", actionTaken::getActionTakenId);
        getPersistenceBrokerTemplate().delete(actionTaken);
    }

    @Override
    public ActionTaken findByActionTakenId(final String actionTakenId) {
        LOG.debug("finding Action Taken by actionTakenId {}", actionTakenId);
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("actionTakenId", actionTakenId);
        criteria.addEqualTo("currentIndicator", Boolean.TRUE);
        return (ActionTaken) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(
                ActionTaken.class, criteria));
    }

    @Override
    public Collection<ActionTaken> findByDocumentId(final String documentId) {
        LOG.debug("finding Action Takens by documentId {}", documentId);
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        criteria.addEqualTo("currentIndicator", Boolean.TRUE);

        final QueryByCriteria qByCrit = new QueryByCriteria(ActionTaken.class, criteria);

        qByCrit.addOrderByAscending("actionDate");

        return (Collection<ActionTaken>) getPersistenceBrokerTemplate().getCollectionByQuery(qByCrit);
    }

    @Override
    public List<ActionTaken> findByDocumentIdWorkflowId(final String documentId, final String principalId) {
        LOG.debug("finding Action Takens by documentId {} and principalId{}", documentId, principalId);
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        criteria.addEqualTo("principalId", principalId);
        criteria.addEqualTo("currentIndicator", Boolean.TRUE);
        return (List<ActionTaken>) getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(
                ActionTaken.class, criteria));
    }

    @Override
    public List findByDocumentIdIgnoreCurrentInd(final String documentId) {
        LOG.debug("finding ActionsTaken ignoring currentInd by documentId:{}", documentId);
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        final QueryByCriteria qByCrit = new QueryByCriteria(ActionTaken.class, criteria);

        qByCrit.addOrderByAscending("actionDate");
        return (List) getPersistenceBrokerTemplate().getCollectionByQuery(qByCrit);
    }

    @Override
    public void saveActionTaken(final ActionTaken actionTaken) {
        LOG.debug("saving ActionTaken");
        checkNull(actionTaken.getDocumentId(), "Document ID");
        checkNull(actionTaken.getActionTaken(), "action taken code");
        checkNull(actionTaken.getDocVersion(), "doc version");
        checkNull(actionTaken.getPerson(), "user principalId");

        if (actionTaken.getActionDate() == null) {
            actionTaken.setActionDate(new Timestamp(System.currentTimeMillis()));
        }
        if (actionTaken.getCurrentIndicator() == null) {
            actionTaken.setCurrentIndicator(Boolean.TRUE);
        }
        LOG.debug(
                "saving ActionTaken: routeHeader {}, actionTaken {}, principalId {}",
                actionTaken::getDocumentId,
                actionTaken::getActionTaken,
                actionTaken::getPrincipalId
        );
        getPersistenceBrokerTemplate().store(actionTaken);
    }

    //TODO perhaps runtime isn't the best here, maybe a dao runtime exception
    private void checkNull(final Object value, final String valueName) throws RuntimeException {
        if (value == null) {
            throw new RuntimeException("Null value for " + valueName);
        }
    }

    @Override
    public boolean hasUserTakenAction(final String principalId, final String documentId) {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        criteria.addEqualTo("principalId", principalId);
        criteria.addEqualTo("currentIndicator", Boolean.TRUE);
        final int count = getPersistenceBrokerTemplate().getCount(new QueryByCriteria(ActionTaken.class, criteria));
        return count > 0;
    }

    @Override
    public Timestamp getLastActionTakenDate(final String documentId, final WorkflowAction workflowAction) {
        return (Timestamp) getPersistenceBrokerTemplate().execute(new PersistenceBrokerCallback() {
            @Override
            public Object doInPersistenceBroker(final PersistenceBroker broker) {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    final Connection connection = broker.serviceConnectionManager().getConnection();
                    statement = connection.prepareStatement(LAST_ACTION_TAKEN_DATE_QUERY);
                    statement.setString(1, documentId);
                    statement.setString(2, workflowAction.getCode());
                    resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        return null;
                    } else {
                        return resultSet.getTimestamp(1);
                    }
                } catch (final Exception e) {
                    throw new WorkflowRuntimeException("Error determining Last Action Taken Date.", e);
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (final SQLException e) {
                            // should we be logging something?
                        }
                    }
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (final SQLException e) {
                            // should we be logging something?
                        }
                    }
                }
            }
        });
    }

    private static final String LAST_MODIFIED_DATE_QUERY =
            "select STAT_MDFN_DT from KREW_DOC_HDR_T where DOC_HDR_ID=?";

    @Override
    public Timestamp getLastModifiedDate(String documentId) {
        return (Timestamp) getPersistenceBrokerTemplate().execute(new PersistenceBrokerCallback() {
            public Object doInPersistenceBroker(PersistenceBroker broker) {
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                try {
                    Connection connection = broker.serviceConnectionManager().getConnection();
                    statement = connection.prepareStatement(LAST_MODIFIED_DATE_QUERY);
                    statement.setString(1, documentId);
                    resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        return null;
                    } else {
                        return resultSet.getTimestamp(1);
                    }
                } catch (Exception e) {
                    throw new WorkflowRuntimeException("Error determining Last Modified Date.", e);
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            // should we be logging something?
                        }
                    }
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            // should we be logging something?
                        }
                    }
                }
            }
        });
    }
}
