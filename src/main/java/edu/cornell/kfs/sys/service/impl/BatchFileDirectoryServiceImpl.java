package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import edu.cornell.kfs.sys.util.SubDirectoryWalker;
import org.kuali.rice.core.api.util.KeyValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchFileDirectoryServiceImpl implements BatchFileDirectoryService {

    @Override
    public List<KeyValue> buildDirectoryKeyValuesList(List<File> rootDirectories) {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        for (File rootDirectory : rootDirectories) {
            SubDirectoryWalker walker = new SubDirectoryWalker(keyValues);
            try {
                walker.addKeyValues(rootDirectory);
            } catch (IOException e) {
                throw new RuntimeException("IOException caught.", e);
            }
        }

        return keyValues;

    }
}
