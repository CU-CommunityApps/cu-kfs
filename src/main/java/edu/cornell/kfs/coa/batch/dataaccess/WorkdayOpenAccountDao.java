package edu.cornell.kfs.coa.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.coa.batch.businessobject.WorkdayOpenAccountDetailDTO;

public interface WorkdayOpenAccountDao {
    List<WorkdayOpenAccountDetailDTO> getWorkdayOpenAccountDetails();
}
