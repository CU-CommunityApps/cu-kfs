package edu.cornell.kfs.module.bc.batch.dataaccess.impl;

import java.util.Iterator;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.ld.businessobject.PositionData;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.util.TransactionalServiceUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.krad.util.OjbCollectionAware;

import edu.cornell.kfs.module.bc.batch.dataaccess.PSPositionDataDao;

public class PSPositionDataDaoOjb extends PlatformAwareDaoBaseOjb implements PSPositionDataDao,
        OjbCollectionAware {

    protected DateTimeService dateTimeService;

    /**
     * 
     * @see edu.cornell.kfs.module.bc.batch.dataaccess.PSPositionDataDao#isPositionBudgeted(java.lang.String)
     */
    public boolean isPositionBudgeted(String positionNumber) {
        boolean budgeted = false;

        Criteria criteria = new Criteria();

        criteria.addEqualTo(KFSPropertyConstants.POSITION_NUMBER, positionNumber);
        criteria.addOrderByDescending("effectiveDate");

        QueryByCriteria query = QueryFactory.newQuery(PositionData.class, criteria);

        Iterator iterator = getPersistenceBrokerTemplate().getIteratorByQuery(query);

        if (iterator.hasNext()) {
            PositionData positionData = (PositionData) iterator.next();
            if (positionData != null & "Y".equalsIgnoreCase(positionData.getBudgetedPosition())) {
                budgeted = true;
                TransactionalServiceUtils.exhaustIterator(iterator);
            }
        }

        return budgeted;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
