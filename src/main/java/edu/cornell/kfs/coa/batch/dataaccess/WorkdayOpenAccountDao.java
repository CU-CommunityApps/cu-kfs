package edu.cornell.kfs.coa.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.coa.batch.businessobject.WorkdayOpenAccountDetail;

public interface WorkdayOpenAccountDao {
    List<WorkdayOpenAccountDetail> getWorkdayOpenAccountDetail();
}
