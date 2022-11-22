package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;
import edu.cornell.kfs.sys.batch.service.impl.TableLookupCriteriaPurgeServiceImpl;

public class PayeeACHAccountExtractDetailPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl
        implements TableLookupCriteriaPurgeService {

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addEqualTo(CUPdpPropertyConstants.PayeeACHAccountExtractDetail.STATUS, CUPdpConstants.PayeeAchAccountExtractStatuses.PROCESSED);
        lookupCriteria.addLessOrEqualThan(CUPdpPropertyConstants.PayeeACHAccountExtractDetail.LAST_UPDATED_TIMESTAMP, dateForPurge);
        return lookupCriteria;
    }

}
