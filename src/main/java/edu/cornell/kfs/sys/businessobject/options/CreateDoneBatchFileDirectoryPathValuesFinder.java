package edu.cornell.kfs.sys.businessobject.options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.businessobject.options.BatchFileDirectoryPathValuesFinder;
import org.kuali.rice.core.api.util.KeyValue;

import edu.cornell.kfs.sys.batch.CuBatchFileUtils;

public class CreateDoneBatchFileDirectoryPathValuesFinder extends BatchFileDirectoryPathValuesFinder {
	
	@Override
	public List<KeyValue> getKeyValues() {
		List<File> rootDirectories = CuBatchFileUtils.retrieveBatchFileStagingRootDirectories();
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        for (File rootDirectory: rootDirectories) {
            SubDirectoryWalker walker = new SubDirectoryWalker(keyValues);
            try {
                walker.addKeyValues(rootDirectory);
            }
            catch (IOException e) {
                throw new RuntimeException("IOException caught.", e);
            }
        }
        
        return keyValues;
	}

}
