package edu.cornell.kfs.kew.actionitem.dao.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionitem.dao.impl.ActionItemDAOOjbImpl;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.kew.CuKewPropertyConstants;
import edu.cornell.kfs.kew.actionitem.ActionItemExtension;
import edu.cornell.kfs.kew.actionitem.dao.CuActionItemDAO;

public class CuActionItemDAOOjbImpl extends ActionItemDAOOjbImpl implements CuActionItemDAO {

    private void deleteActionItemExtensionsByActionRequestId(Long actionRequestId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKewPropertyConstants.ACTION_REQUEST_ID, actionRequestId);
        deleteActionItemExtensionsForMatchingActionItems(criteria);
    }

    @Override
    public void deleteActionItem(ActionItem actionItem) {
        deleteExtensionForActionItem(actionItem);
        super.deleteActionItem(actionItem);
    }

    private void deleteExtensionForActionItem(ActionItem actionItem) {
        if (ObjectUtils.isNull(actionItem)) {
            return;
        }
        ActionItemExtension extension = findActionItemExtensionByActionItemId(actionItem.getId());
        if (ObjectUtils.isNotNull(extension)) {
            getPersistenceBrokerTemplate().delete(extension);
        }
    }

    private void deleteActionItemExtensionsByDocumentIdWorkflowUserId(String documentId, String workflowUserId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKewPropertyConstants.DOCUMENT_ID, documentId);
        criteria.addEqualTo(KFSPropertyConstants.PRINCIPAL_ID, workflowUserId);
        deleteActionItemExtensionsForMatchingActionItems(criteria);
    }

    @Override
    public void deleteByDocumentId(String documentId) {
        deleteActionItemExtensionsByDocumentId(documentId);
        super.deleteByDocumentId(documentId);
    }

    private void deleteActionItemExtensionsByDocumentId(String documentId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKewPropertyConstants.DOCUMENT_ID, documentId);
        deleteActionItemExtensionsForMatchingActionItems(criteria);
    }

    private void deleteActionItemExtensionsForMatchingActionItems(Criteria actionItemCriteria) {
        ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(ActionItem.class, actionItemCriteria);
        subQuery.setAttributes(new String[] {KRADPropertyConstants.ID});
        subQuery.setJdbcTypes(new int[] {java.sql.Types.VARCHAR});
        
        Criteria extensionCriteria = new Criteria();
        extensionCriteria.addIn(CuKewPropertyConstants.ACTION_ITEM_ID, subQuery);
        getPersistenceBrokerTemplate().deleteByQuery(
                new QueryByCriteria(ActionItemExtension.class, extensionCriteria));
    }

    @Override
    public ActionItemExtension findActionItemExtensionByActionItemId(String actionItemId) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKewPropertyConstants.ACTION_ITEM_ID, actionItemId);
        return (ActionItemExtension) getPersistenceBrokerTemplate().getObjectByQuery(
                new QueryByCriteria(ActionItemExtension.class, criteria));
    }

    @Override
    public void saveActionItemExtension(ActionItemExtension extension) {
        getPersistenceBrokerTemplate().store(extension);
    }

}
