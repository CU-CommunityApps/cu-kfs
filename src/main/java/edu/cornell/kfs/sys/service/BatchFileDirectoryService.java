package edu.cornell.kfs.sys.service;

import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.util.KeyValue;
import org.springframework.cache.annotation.Cacheable;

import java.io.File;
import java.util.List;

public interface BatchFileDirectoryService {

    @Cacheable(value= SystemOptions.CACHE_NAME, key="'buildDirectoryKeyValuesList' + #p0")
    List<KeyValue> buildDirectoryKeyValuesList(List<File> rootDirectories);

}
