package edu.cornell.kfs.sys.service;

import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.util.KeyValue;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

public interface BatchFileDirectoryService {

    void init();

    @Cacheable(value= SystemOptions.CACHE_NAME, key="'buildStagingDirectories'")
    List<KeyValue> buildStagingDirectories();

    @Cacheable(value= SystemOptions.CACHE_NAME, key="'buildStagingAndReportsDirectories'")
    List<KeyValue> buildStagingAndReportsDirectories();

    List<KeyValue> buildDirectoryKeyValuesList(List<File> rootDirectories);

}
