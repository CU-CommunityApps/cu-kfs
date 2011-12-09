package edu.cornell.kfs.module.bc.batch.dataaccess.impl;

import java.sql.Date;
import java.text.ParseException;
import java.util.Iterator;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.module.ld.businessobject.PositionData;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.util.OjbCollectionAware;
import org.kuali.rice.kns.util.TransactionalServiceUtils;

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

        ReportQueryByCriteria reportByCriteria = QueryFactory.newReportQuery(
                        PositionData.class,
                        criteria);

        String[] attributes = new String[]{"positionNumber", "max(effectiveDate)", "budgetedPosition"};
        String[] groupBy = new String[]{"positionNumber", "budgetedPosition"};
        reportByCriteria.setAttributes(attributes);
        reportByCriteria.addGroupBy(groupBy);

        Iterator<Object[]> iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportByCriteria);
        if (iterator.hasNext()) {
            Object[] results = iterator.next();
            if (results != null && results.length > 1 && "Y".equalsIgnoreCase(results[2].toString())) {
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
