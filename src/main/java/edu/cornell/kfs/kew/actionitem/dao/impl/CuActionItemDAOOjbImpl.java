package edu.cornell.kfs.kew.actionitem.dao.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionitem.dao.impl.ActionItemDAOOjbImpl;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.kew.CuKewPropertyConstants;
import edu.cornell.kfs.kew.actionitem.ActionItemExtension;
import edu.cornell.kfs.kew.actionitem.dao.CuActionItemDAO;

public class CuActionItemDAOOjbImpl extends ActionItemDAOOjbImpl implements CuActionItemDAO {

    @Override
    public void deleteActionItem(final ActionItem actionItem) {
        deleteExtensionForActionItem(actionItem);
        super.deleteActionItem(actionItem);
    }

    private void deleteExtensionForActionItem(final ActionItem actionItem) {
        if (ObjectUtils.isNull(actionItem)) {
            return;
        }
        final ActionItemExtension extension = findActionItemExtensionByActionItemId(actionItem.getId());
        if (ObjectUtils.isNotNull(extension)) {
            getPersistenceBrokerTemplate().delete(extension);
        }
    }

    @Override
    public void deleteByDocumentId(final String documentId) {
        deleteActionItemExtensionsByDocumentId(documentId);
        super.deleteByDocumentId(documentId);
    }

    private void deleteActionItemExtensionsByDocumentId(final String documentId) {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKewPropertyConstants.DOCUMENT_ID, documentId);
        deleteActionItemExtensionsForMatchingActionItems(criteria);
    }

    private void deleteActionItemExtensionsForMatchingActionItems(final Criteria actionItemCriteria) {
        final ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(ActionItem.class, actionItemCriteria);
        subQuery.setAttributes(new String[] {KRADPropertyConstants.ID});
        subQuery.setJdbcTypes(new int[] {java.sql.Types.VARCHAR});
        
        final Criteria extensionCriteria = new Criteria();
        extensionCriteria.addIn(CuKewPropertyConstants.ACTION_ITEM_ID, subQuery);
        getPersistenceBrokerTemplate().deleteByQuery(
                new QueryByCriteria(ActionItemExtension.class, extensionCriteria));
    }

    @Override
    public ActionItemExtension findActionItemExtensionByActionItemId(final String actionItemId) {
        final Criteria criteria = new Criteria();
        criteria.addEqualTo(CuKewPropertyConstants.ACTION_ITEM_ID, actionItemId);
        return (ActionItemExtension) getPersistenceBrokerTemplate().getObjectByQuery(
                new QueryByCriteria(ActionItemExtension.class, criteria));
    }

    @Override
    public void saveActionItemExtension(final ActionItemExtension extension) {
        getPersistenceBrokerTemplate().store(extension);
    }

}
