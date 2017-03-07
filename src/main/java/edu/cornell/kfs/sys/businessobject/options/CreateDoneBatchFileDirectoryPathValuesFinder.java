package edu.cornell.kfs.sys.businessobject.options;

import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.KeyValue;

import java.util.List;

public class CreateDoneBatchFileDirectoryPathValuesFinder extends CuBatchFileDirectoryPathValuesFinder {
	
	@Override
	public List<KeyValue> getKeyValues() {
		return SpringContext.getBean(BatchFileDirectoryService.class).buildBatchFileStagingDirectories();
	}

}
