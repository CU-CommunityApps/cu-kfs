package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.util.Iterator;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.util.OjbCollectionAware;
import org.kuali.rice.kns.util.TransactionalServiceUtils;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionFlagsDao;

public class BudgetConstructionFlagsDaoOjb extends PlatformAwareDaoBaseOjb implements BudgetConstructionFlagsDao,
        OjbCollectionAware {

    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionFlagsDao#getFlagForPosition(java.lang.String)
     */
    public String getFlagForPosition(String positionNumber) {
        String flag = KFSConstants.EMPTY_STRING;;

        Criteria criteria = new Criteria();

        criteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.POSITION_NBR, positionNumber);
        criteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.CSF_FUNDING_STATUS_CD, CUBCConstants.StatusFlag.NEW.getFlagValue());

        Criteria orCriteria = new Criteria();
        orCriteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.POSITION_NBR, positionNumber);
        orCriteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.CSF_FUNDING_STATUS_CD, CUBCConstants.StatusFlag.CHANGED.getFlagValue());

        criteria.addOrCriteria(orCriteria);

        ReportQueryByCriteria reportByCriteria = QueryFactory.newReportQuery(
                        BudgetConstructionCalculatedSalaryFoundationTracker.class,
                        criteria);

        String[] attributes = new String[]{CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.CSF_FUNDING_STATUS_CD};
        reportByCriteria.setAttributes(attributes);

        Iterator<?> iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportByCriteria);
        if (iterator.hasNext()) {
            TransactionalServiceUtils.exhaustIterator(iterator);
            flag = CUBCConstants.SOME_CSF_CHANGE_FLAG;
        }

        return flag;
    }

    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionFlagsDao#getFlagForIncumbent(java.lang.String)
     */
    public String getFlagForIncumbent(String emplid) {
        String flag = KFSConstants.EMPTY_STRING;

        Criteria criteria = new Criteria();

        criteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.EMPLID, emplid);
        criteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.CSF_FUNDING_STATUS_CD, CUBCConstants.StatusFlag.NEW.getFlagValue());

        Criteria orCriteria = new Criteria();
        orCriteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.EMPLID, emplid);
        orCriteria.addEqualTo(CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.CSF_FUNDING_STATUS_CD, CUBCConstants.StatusFlag.CHANGED.getFlagValue());

        criteria.addOrCriteria(orCriteria);

        ReportQueryByCriteria reportByCriteria = new ReportQueryByCriteria(
                BudgetConstructionCalculatedSalaryFoundationTracker.class, new String[]{CUBCPropertyConstants.BudgetConstructionCalculatedSalaryFoundationTrackerProperties.CSF_FUNDING_STATUS_CD},
                criteria);

        Iterator<?> iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportByCriteria);

        if (iterator.hasNext()) {
            TransactionalServiceUtils.exhaustIterator(iterator);
            flag = CUBCConstants.SOME_CSF_CHANGE_FLAG;;
        }

        return flag;
    }

}
