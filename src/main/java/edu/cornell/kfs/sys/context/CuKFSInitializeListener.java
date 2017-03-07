package edu.cornell.kfs.sys.context;

import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import org.kuali.kfs.sys.context.KFSInitializeListener;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.CompletableFuture;

public class CuKFSInitializeListener extends KFSInitializeListener {

    /**
     * Overridden so we can call into the BatchFileDirectoryService and load the cache with the values
     * displayed in the select list on the Batch File Lookup Create Done Batch File Lookup. With AWS EFS,
     * the process of building the KeyValues lists with the directories is slow (can be up to 10 minutes)
     * so even though the values are cached, the first user to try to access the lookup on a particular node
     * after it has been recycled experiences quite a delay. This code is meant to eliminate that delay.
     *
     * It makes the call asynchronously so the startup of the application isn't slowed down.
     *
     * @param sce
     */
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);

        BatchFileDirectoryService batchFileDirectoryService = SpringContext.getBean(BatchFileDirectoryService.class);

        CompletableFuture.supplyAsync(batchFileDirectoryService::buildBatchFileStagingDirectories);
        CompletableFuture.supplyAsync(batchFileDirectoryService::buildBatchFileLookupDirectories);
    }

}
