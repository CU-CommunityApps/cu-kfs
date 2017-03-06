package edu.cornell.kfs.sys.context;

import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import org.kuali.kfs.sys.context.KFSInitializeListener;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.ServletContextEvent;

public class CuKFSInitializeListener extends KFSInitializeListener {

    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        SpringContext.getBean(BatchFileDirectoryService.class).init();
    }

}
