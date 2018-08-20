package edu.cornell.kfs.sys.businessobject.options;

import org.kuali.kfs.sys.context.SpringContext;
import edu.cornell.kfs.sys.service.CuBatchFileDirectoryService;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;

import java.util.List;

public class CuBatchFileDirectoryPathValuesFinder extends KeyValuesBase {

    public List<KeyValue> getKeyValues() {
        return SpringContext.getBean(CuBatchFileDirectoryService.class).buildBatchFileLookupDirectories();
    }

}
