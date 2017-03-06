package edu.cornell.kfs.sys.businessobject.options;

import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.context.SpringContext;
import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CuBatchFileDirectoryPathValuesFinder extends KeyValuesBase {

    public List<KeyValue> getKeyValues() {
//        List<File> rootDirectories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
//        return SpringContext.getBean(BatchFileDirectoryService.class).buildDirectoryKeyValuesList(rootDirectories);
        return SpringContext.getBean(BatchFileDirectoryService.class).buildStagingAndReportsDirectories();
    }

}
