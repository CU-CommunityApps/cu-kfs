package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.module.ld.CuLaborPropertyConstants;
import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;

public class PositionDataWorkdayLookupCriteriaPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl implements TableLookupCriteriaPurgeService {

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addLessOrEqualThan(CuLaborPropertyConstants.PositionDataWorkday.INACTIVATION_DATE, dateForPurge);
        return lookupCriteria;
    }

}
 