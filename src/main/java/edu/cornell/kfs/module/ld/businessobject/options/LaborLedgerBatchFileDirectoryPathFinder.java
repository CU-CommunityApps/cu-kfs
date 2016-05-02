package edu.cornell.kfs.module.ld.businessobject.options;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.valuefinder.ValueFinder;

import edu.cornell.kfs.sys.CUKFSConstants;

public class LaborLedgerBatchFileDirectoryPathFinder implements ValueFinder {

    /**
     * @see org.kuali.rice.kns.lookup.valueFinder.ValueFinder#getValue()
     */
    public String getValue() {
        // KualiConfigurationService kualiConfigurationService = SpringContext.getBean(KualiConfigurationService.class);
        // String configProperty =
        // kualiConfigurationService.getPropertyString(KFSConstants.BATCH_FILE_LOOKUP_ROOT_DIRECTORIES);
        // String values[] = configProperty.split(";");
        String path = KFSConstants.EMPTY_STRING;

        path = "staging" + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + "ld" + System.getProperty(CUKFSConstants.FILE_SEPARATOR) + "enterpriseFeed";

        return path;
    }
}