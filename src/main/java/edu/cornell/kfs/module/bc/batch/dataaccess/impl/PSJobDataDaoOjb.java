package edu.cornell.kfs.module.bc.batch.dataaccess.impl;

import java.util.Collection;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.krad.util.OjbCollectionAware;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.batch.dataaccess.PSJobDataDao;
import edu.cornell.kfs.module.bc.businessobject.PSJobData;

public class PSJobDataDaoOjb extends PlatformAwareDaoBaseOjb implements PSJobDataDao,
        OjbCollectionAware {

    /**
     * It returns all the entries from the CU_PS_JOB_DATA table that have the employee
     * type equal to 'Z'.
     * 
     * @see edu.cornell.kfs.module.bc.batch.dataaccess.PSJobDataDao#getExistingExecutives()
     */
    public Collection<PSJobData> getExistingExecutives() {
        Criteria criteria = new Criteria();

        criteria.addEqualTo(CUBCPropertyConstants.PSJobDataProperties.EMPL_TYP,
                CUBCConstants.EmployeeType.EXECUTIVES);

        QueryByCriteria query = QueryFactory.newQuery(PSJobData.class, criteria);

        Collection<PSJobData> executives = getPersistenceBrokerTemplate().getCollectionByQuery(query);

        return executives;
    }

    /**
     * Gets all the entries in the CU_PS_JOB_DATA table where the position number is in
     * the given list.
     * 
     * @see edu.cornell.kfs.module.bc.batch.dataaccess.PSJobDataDao#getPSJobDataEntriesByPositionNumbers(java.util.List)
     */
    public Collection<PSJobData> getPSJobDataEntriesByPositionNumbers(List<String> positionNumbers) {
        Criteria criteria = new Criteria();

        criteria.addIn(CUBCPropertyConstants.PSJobDataProperties.POSITION_NBR,
                positionNumbers);

        QueryByCriteria query = QueryFactory.newQuery(PSJobData.class, criteria);

        Collection<PSJobData> executives = getPersistenceBrokerTemplate().getCollectionByQuery(query);

        return executives;
    }

}
