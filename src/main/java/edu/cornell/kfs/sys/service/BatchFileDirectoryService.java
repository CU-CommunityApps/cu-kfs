package edu.cornell.kfs.sys.service;

import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.util.KeyValue;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface BatchFileDirectoryService {

    @Cacheable(value= SystemOptions.CACHE_NAME, key="'buildStagingDirectories'")
    List<KeyValue> buildBatchFileStagingDirectories();

    @Cacheable(value= SystemOptions.CACHE_NAME, key="'buildStagingAndReportsDirectories'")
    List<KeyValue> buildBatchFileLookupDirectories();

}
