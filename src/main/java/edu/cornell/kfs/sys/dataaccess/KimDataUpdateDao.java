package edu.cornell.kfs.sys.dataaccess;

import java.util.List;

import edu.cornell.kfs.sys.dataaccess.dto.KimFeedPersonDTO;

public interface KimDataUpdateDao {

    void checkDataSourceStatus();

    void processDataUpdate(KimFeedPersonDTO personDTO);

    void markPersonRecordsAsDisabled(List<String> principalIds);

}
