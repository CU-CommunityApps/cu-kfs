package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.service.CuBatchFileDirectoryService;
import edu.cornell.kfs.sys.util.SubDirectoryWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.batch.service.impl.BatchFileDirectoryServiceImpl;
import org.kuali.rice.core.api.util.KeyValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CuBatchFileDirectoryServiceImpl extends BatchFileDirectoryServiceImpl implements CuBatchFileDirectoryService {

	private static final Logger LOG = LogManager.getLogger(CuBatchFileDirectoryServiceImpl.class);

    @Override
    public List<KeyValue> buildBatchFileStagingDirectories() {
        LOG.debug("Starting BatchFileDirectoryServiceImpl.buildBatchFileStagingDirectories().");
        List<KeyValue> directoryKeyValueList = buildDirectoryKeyValuesList(CuBatchFileUtils.retrieveBatchFileStagingRootDirectories());
        LOG.debug("Finished BatchFileDirectoryServiceImpl.buildBatchFileStagingDirectories().");

        return directoryKeyValueList;
    }

    @Override
    public List<KeyValue> buildBatchFileLookupDirectories() {
        LOG.debug("Starting BatchFileDirectoryServiceImpl.buildBatchFileLookupDirectories().");
        List<KeyValue> directoryKeyValueList = buildDirectoryKeyValuesList(BatchFileUtils.retrieveBatchFileLookupRootDirectories());
        LOG.debug("Finished BatchFileDirectoryServiceImpl.buildBatchFileLookupDirectories().");

        return directoryKeyValueList;
    }

    private List<KeyValue> buildDirectoryKeyValuesList(List<File> rootDirectories) {
        List<KeyValue> keyValues = new ArrayList<>();

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
