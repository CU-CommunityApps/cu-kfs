package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import edu.cornell.kfs.sys.util.SubDirectoryWalker;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.rice.core.api.util.KeyValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BatchFileDirectoryServiceImpl implements BatchFileDirectoryService {

    @Override
    public void init() {
        CompletableFuture.supplyAsync(this::buildStagingDirectories);
        CompletableFuture.supplyAsync(this::buildStagingAndReportsDirectories);
    }

    @Override
    public List<KeyValue> buildStagingDirectories() {
        List<File> rootDirectories = CuBatchFileUtils.retrieveBatchFileStagingRootDirectories();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return buildDirectoryKeyValuesList(rootDirectories);
    }

    @Override
    public List<KeyValue> buildStagingAndReportsDirectories() {
        List<File> rootDirectories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return buildDirectoryKeyValuesList(rootDirectories);
    }

    @Override
    public List<KeyValue> buildDirectoryKeyValuesList(List<File> rootDirectories) {
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
