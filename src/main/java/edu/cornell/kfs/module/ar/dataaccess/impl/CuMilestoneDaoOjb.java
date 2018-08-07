package edu.cornell.kfs.module.ar.dataaccess.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.Milestone;
import org.kuali.kfs.module.ar.dataaccess.MilestoneDao;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import java.util.Collection;
import java.util.Date;

public class CuMilestoneDaoOjb extends PlatformAwareDaoBaseOjb implements MilestoneDao {

    @Override
    public Collection<Milestone> getMilestonesForNotification(Date expectedCompletionLimitDate) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.ACTIVE, true);
        criteria.addEqualTo(ArPropertyConstants.BILLED, false);
        criteria.addIsNull(ArPropertyConstants.MilestoneFields.MILESTONE_ACTUAL_COMPLETION_DATE);

        //Avoid exception in base code by converting java.util.Date to java.sql.Date
        java.sql.Date expectedCompletionLimitSqlDate = new java.sql.Date(expectedCompletionLimitDate.getTime());
        criteria.addLessOrEqualThan(ArPropertyConstants.MilestoneFields.MILESTONE_EXPECTED_COMPLETION_DATE, expectedCompletionLimitSqlDate);

        QueryByCriteria queryByCriteria = new QueryByCriteria(Milestone.class, criteria);
        queryByCriteria.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);
        queryByCriteria.addOrderByAscending(ArPropertyConstants.MilestoneFields.MILESTONE_NUMBER);

        return getPersistenceBrokerTemplate().getCollectionByQuery(queryByCriteria);
    }

}
