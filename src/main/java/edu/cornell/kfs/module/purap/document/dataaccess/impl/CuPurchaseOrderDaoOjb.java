package edu.cornell.kfs.module.purap.document.dataaccess.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.AutoClosePurchaseOrderView;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.dataaccess.impl.PurchaseOrderDaoOjb;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KewApiConstants;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.businessobject.CuAutoClosePurchaseOrderView;

public class CuPurchaseOrderDaoOjb extends PurchaseOrderDaoOjb {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<AutoClosePurchaseOrderView> getAllOpenPurchaseOrders(List<String> excludedVendorChoiceCodes) {
        LOG.info("getAllOpenPurchaseOrders() started");
        Criteria criteria = new Criteria();
        criteria.addIsNull(PurapPropertyConstants.RECURRING_PAYMENT_TYPE_CODE);
        criteria.addEqualTo(PurapPropertyConstants.TOTAL_ENCUMBRANCE, new KualiDecimal(0));
        criteria.addEqualTo(PurapPropertyConstants.PURCHASE_ORDER_CURRENT_INDICATOR, true);
        criteria.addEqualTo(KewApiConstants.APP_DOC_STATUS_DETAIL, PurchaseOrderStatuses.APPDOC_OPEN);
        for (String excludeCode : excludedVendorChoiceCodes) {
            criteria.addNotEqualTo(PurapPropertyConstants.VENDOR_CHOICE_CODE, excludeCode);
        }
        QueryByCriteria qbc = new QueryByCriteria(CuAutoClosePurchaseOrderView.class, criteria);
        LOG.info("getAllOpenPurchaseOrders() Query criteria is {}", criteria);
        
        // KFSUPGRADE-363
        limitResultSize(qbc);
        List<AutoClosePurchaseOrderView> l = (List<AutoClosePurchaseOrderView>) getPersistenceBrokerTemplate().getCollectionByQuery(qbc);
        LOG.info("getAllOpenPurchaseOrders() ended.");

        return l;
    }    
    
    /**
     * Limit the size of the result set from the given query operation
     * KFSUPGRADE-363
     * @param query the given query operation
     */
    private void limitResultSize(Query query) {
        int startingIndex = 1;

        // get the result limit number from configuration
        String limitConfig = SpringContext.getBean(ParameterService.class).getParameterValueAsString(PurchaseOrderDocument.class, CUPurapParameterConstants.AUTO_CLOSE_PO_RESULTS_LIMIT);
        
        Integer limit = Integer.MAX_VALUE;
        if (limitConfig != null) {
            limit = Integer.valueOf(limitConfig);
        }

        int endingIndex = limit.intValue();

        LOG.debug("getAllOpenPurchaseOrders() limiting Query results size to " + endingIndex);

        query.setStartAtIndex(startingIndex);
        query.setEndAtIndex(endingIndex);
    }
  

}
