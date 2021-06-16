/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.kew.routeheader.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.OptimisticLockException;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.core.framework.persistence.platform.DatabasePlatform;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.ActionRequestStatus;
import org.kuali.kfs.kew.api.exception.LockingException;
import org.kuali.kfs.kew.docsearch.SearchableAttributeValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValueContent;
import org.kuali.kfs.kew.routeheader.dao.DocumentRouteHeaderDAO;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.dao.CannotAcquireLockException;
import org.springmodules.orm.ojb.OjbFactoryUtils;
import org.springmodules.orm.ojb.PersistenceBrokerCallback;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class DocumentRouteHeaderDAOOjbImpl extends PersistenceBrokerDaoSupport implements DocumentRouteHeaderDAO {

    private static final Logger LOG = LogManager.getLogger();

    public void saveRouteHeader(DocumentRouteHeaderValue routeHeader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("About to Save the route Header: " + routeHeader.getDocumentId() + " / version=" +
                    routeHeader.getVersionNumber());
            DocumentRouteHeaderValue currHeader = findRouteHeader(routeHeader.getDocumentId());
            if (currHeader != null) {
                LOG.debug("Current Header Version: " + currHeader.getVersionNumber());
            } else {
                LOG.debug("Current Header: null");
            }
            LOG.debug(ExceptionUtils.getStackTrace(new Throwable()));
        }
        try {
            getPersistenceBrokerTemplate().store(routeHeader);
            /*
             * FINP-7381 changes from KualiCo patch release 2021-02-26 applied to
             * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
             */
            getPersistenceBrokerTemplate().store(getDocumentContentToStore(routeHeader));
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof OptimisticLockException) {
                LOG.error("Optimistic Locking Exception saving document header or content. Offending object: " +
                        ((OptimisticLockException) ex.getCause()).getSourceObject() + "; DocumentId = " +
                        routeHeader.getDocumentId() + " ;  Version Number = " + routeHeader.getVersionNumber());
            }
            LOG.error("Unable to save document header or content. Route Header: " + routeHeader, ex);
            throw ex;
        }
    }
    
    /*
     * FINP-7381 changes from KualiCo patch release 2021-02-26 applied to
     * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
     * 
     * New private method.
     */
    private DocumentRouteHeaderValueContent getDocumentContentToStore(DocumentRouteHeaderValue routeHeader) {
        // if there is an existing doc content object, we want to use that and just update the string doc content
        // value to avoid OptimisticLockExceptions when doc content is updated via doc operations
        DocumentRouteHeaderValueContent docContentToStore = KEWServiceLocator.getRouteHeaderService()
                .getContent(routeHeader.getDocumentId());
        if (ObjectUtils.isNotNull(docContentToStore)) {
            docContentToStore.setDocumentContent(routeHeader.getDocContent());
            getPersistenceBrokerTemplate().store(docContentToStore);
        } else {
            docContentToStore = routeHeader.getDocumentContent();
            docContentToStore.setDocumentId(routeHeader.getDocumentId());
        }
        return docContentToStore;
    }

    public DocumentRouteHeaderValueContent getContent(String documentId) {
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        return (DocumentRouteHeaderValueContent) this.getPersistenceBrokerTemplate()
                .getObjectByQuery(new QueryByCriteria(
                        DocumentRouteHeaderValueContent.class, crit));
    }

    public void clearRouteHeaderSearchValues(String documentId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        QueryByCriteria query = new QueryByCriteria(SearchableAttributeValue.class, criteria);
        query.addOrderByAscending("searchableAttributeValueId");
        Collection<SearchableAttributeValue> results =
                this.getPersistenceBrokerTemplate().getCollectionByQuery(query);
        if (!results.isEmpty()) {
            for (SearchableAttributeValue srchAttrVal : results) {
                this.getPersistenceBrokerTemplate().delete(srchAttrVal);
            }
        }
    }

    public Collection<SearchableAttributeValue> findSearchableAttributeValues(String documentId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        QueryByCriteria query = new QueryByCriteria(SearchableAttributeValue.class, criteria);
        query.addOrderByAscending("searchableAttributeValueId");
        return this.getPersistenceBrokerTemplate().getCollectionByQuery(query);
    }

    public void lockRouteHeader(final String documentId, final boolean wait) {
        /*
         * String sql = (wait ? LOCK_SQL_WAIT : LOCK_SQL_NOWAIT); try { getJdbcTemplate().update(sql, new Object[] { documentId }); } catch (CannotAcquireLockException e) { throw new LockingException("Could not aquire lock on document, documentId=" + documentId, e); }
         */
        this.getPersistenceBrokerTemplate().execute(new PersistenceBrokerCallback() {
            public Object doInPersistenceBroker(PersistenceBroker broker) {
                PreparedStatement statement = null;
                try {
                    Connection connection = broker.serviceConnectionManager().getConnection();
                    String sql = getPlatform().getLockRouteHeaderQuerySQL(documentId, wait);
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, documentId);
                    statement.execute();
                    return null;
                } catch (SQLException | LookupException | CannotAcquireLockException e) {
                    throw new LockingException("Could not aquire lock on document, documentId=" + documentId, e);
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            // should we be logging something?
                        }
                    }
                }
            }
        });

    }

    public DocumentRouteHeaderValue findRouteHeader(String documentId) {
        return findRouteHeader(documentId, false);
    }

    public DocumentRouteHeaderValue findRouteHeader(String documentId, boolean clearCache) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        if (clearCache) {
            this.getPersistenceBrokerTemplate().clearCache();
        }
        return (DocumentRouteHeaderValue) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(
                DocumentRouteHeaderValue.class, criteria));
    }

    public Collection<DocumentRouteHeaderValue> findRouteHeaders(Collection<String> documentIds) {
        return findRouteHeaders(documentIds, false);
    }

    public Collection<DocumentRouteHeaderValue> findRouteHeaders(Collection<String> documentIds, boolean clearCache) {
        if (documentIds == null || documentIds.isEmpty()) {
            return null;
        }
        Criteria crit = new Criteria();
        crit.addIn("documentId", documentIds);
        if (clearCache) {
            this.getPersistenceBrokerTemplate().clearCache();
        }
        return (Collection<DocumentRouteHeaderValue>) this.getPersistenceBrokerTemplate()
                .getCollectionByQuery(new QueryByCriteria(DocumentRouteHeaderValue.class, crit));
    }

    public void deleteRouteHeader(DocumentRouteHeaderValue routeHeader) {
        this.getPersistenceBrokerTemplate().delete(routeHeader);
    }

    public String getNextDocumentId() {
        return (String) this.getPersistenceBrokerTemplate().execute(new PersistenceBrokerCallback() {
            public Object doInPersistenceBroker(PersistenceBroker broker) {
                return getPlatform().getNextValSQL("KREW_DOC_HDR_S", broker).toString();
            }
        });
    }

    protected DatabasePlatform getPlatform() {
        return (DatabasePlatform) GlobalResourceLoader.getService(KFSConstants.DB_PLATFORM);
    }

    public Collection<String> findPendingByResponsibilityIds(Set<String> responsibilityIds) {
        Collection<String> documentIds = new ArrayList<>();
        if (responsibilityIds.isEmpty()) {
            return documentIds;
        }
        PersistenceBroker broker = null;
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            broker = getPersistenceBroker(false);
            conn = broker.serviceConnectionManager().getConnection();
            String respIds = "('";
            int index = 0;
            for (String responsibilityId : responsibilityIds) {
                respIds += responsibilityId + (index == responsibilityIds.size() - 1 ? "" : "','");
                index++;
            }
            respIds += "')";
            String query = "SELECT DISTINCT(doc_hdr_id) FROM KREW_ACTN_RQST_T " +
                    "WHERE (STAT_CD='" +
                    ActionRequestStatus.INITIALIZED.getCode() +
                    "' OR STAT_CD='" +
                    ActionRequestStatus.ACTIVATED.getCode() +
                    "') AND RSP_ID IN " + respIds;
            LOG.debug("Query to find pending documents for requeue: " + query);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                documentIds.add(rs.getString(1));
            }
        } catch (SQLException sqle) {
            LOG.error("SQLException: " + sqle.getMessage(), sqle);
            throw new WorkflowRuntimeException(sqle);
        } catch (LookupException le) {
            LOG.error("LookupException: " + le.getMessage(), le);
            throw new WorkflowRuntimeException(le);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOG.warn("Could not close result set.");
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.warn("Could not close statement.");
                }
            }
            try {
                if (broker != null) {
                    OjbFactoryUtils.releasePersistenceBroker(broker, this.getPersistenceBrokerTemplate().getPbKey());
                }
            } catch (Exception e) {
                LOG.error("Failed closing connection: " + e.getMessage(), e);
            }
        }
        return documentIds;
    }

    public boolean hasSearchableAttributeValue(
            String documentId, String searchableAttributeKey, String searchableAttributeValue) {
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        crit.addEqualTo("searchableAttributeKey", searchableAttributeKey);
        Collection results = getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(
                SearchableAttributeValue.class, crit));
        if (!results.isEmpty()) {
            for (Iterator iterator = results.iterator(); iterator.hasNext(); ) {
                SearchableAttributeValue attribute = (SearchableAttributeValue) iterator.next();
                if (StringUtils.equals(attribute.getSearchableAttributeDisplayValue(), searchableAttributeValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getDocumentStatus(String documentId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
        query.setAttributes(new String[]{"docRouteStatus"});
        String status = null;
        Iterator iter = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);
        while (iter.hasNext()) {
            Object[] row = (Object[]) iter.next();
            status = (String) row[0];
        }
        return status;
    }

    public String getAppDocId(String documentId) {
        Criteria crit = new Criteria();
        crit.addEqualTo("documentId", documentId);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, crit);
        query.setAttributes(new String[]{"appDocId"});
        String appDocId = null;
        Iterator iter = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);
        while (iter.hasNext()) {
            Object[] row = (Object[]) iter.next();
            appDocId = (String) row[0];
        }
        return appDocId;
    }

    public String getAppDocStatus(String documentId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("documentId", documentId);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
        query.setAttributes(new String[]{"appDocStatus"});
        String appDocStatus = null;
        Iterator iter = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);
        while (iter.hasNext()) {
            Object[] row = (Object[]) iter.next();
            appDocStatus = (String) row[0];
        }
        return appDocStatus;
    }

    public void save(SearchableAttributeValue searchableAttributeValue) {
        getPersistenceBrokerTemplate().store(searchableAttributeValue);
    }

    public Collection findByDocTypeAndAppId(String documentTypeName, String appId) {
        Collection documentIds = new ArrayList<>();

        PersistenceBroker broker = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            broker = getPersistenceBroker(false);
            conn = broker.serviceConnectionManager().getConnection();

            String query =
                    "SELECT DISTINCT " +
                            "    (docHdr.doc_hdr_id) " +
                            "FROM " +
                            "    KREW_DOC_HDR_T docHdr, " +
                            "    KREW_DOC_TYP_T docTyp " +
                            "WHERE " +
                            "    docHdr.APP_DOC_ID     = ? " +
                            "    AND docHdr.DOC_TYP_ID = docTyp.DOC_TYP_ID " +
                            "    AND docTyp.DOC_TYP_NM = ?";

            LOG.debug("Query to find documents by app id: " + query);

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, appId);
            stmt.setString(2, documentTypeName);
            rs = stmt.executeQuery();

            while (rs.next()) {
                documentIds.add(new String(rs.getString(1)));
            }
            rs.close();
        } catch (SQLException sqle) {
            LOG.error("SQLException: " + sqle.getMessage(), sqle);
            throw new WorkflowRuntimeException(sqle);
        } catch (LookupException le) {
            LOG.error("LookupException: " + le.getMessage(), le);
            throw new WorkflowRuntimeException(le);
        } finally {
            try {
                if (broker != null) {
                    OjbFactoryUtils.releasePersistenceBroker(broker, this.getPersistenceBrokerTemplate().getPbKey());
                }
            } catch (Exception e) {
                LOG.error("Failed closing connection: " + e.getMessage(), e);
            }
        }
        return documentIds;
    }
}
