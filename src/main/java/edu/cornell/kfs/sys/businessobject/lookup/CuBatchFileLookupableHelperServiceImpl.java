package edu.cornell.kfs.sys.businessobject.lookup;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;

public class CuBatchFileLookupableHelperServiceImpl extends BatchFileLookupableHelperServiceImpl {

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        List<BatchFile> results = new ArrayList<BatchFile>();

        IOFileFilter filter = FileFilterUtils.fileFileFilter();

        String fileNamePattern = fieldValues.get("fileName");
        IOFileFilter fileNameBasedFilter = getFileNameBasedFilter(fileNamePattern);
        if (fileNameBasedFilter != null) {
            filter = FileFilterUtils.andFileFilter(filter, fileNameBasedFilter);
        }

        String lastModifiedDate = fieldValues.get("lastModifiedDate");
        IOFileFilter lastModifiedDateBasedFilter = getLastModifiedDateBasedFilter(lastModifiedDate);
        if (lastModifiedDateBasedFilter != null) {
            filter = FileFilterUtils.andFileFilter(filter, lastModifiedDateBasedFilter);
        }

        BatchFileFinder finder = new BatchFileFinder(results, filter);
        List<File> selectedDirectories = getSelectedDirectories(getSelectedPaths());
        if (selectedDirectories.isEmpty()) {
            List<Path> rootDirectories = retrieveRootDirectories();
            finder.find(rootDirectories);
        } else {
            finder.find(selectedDirectories);
        }

        return results;
    }

    protected List<Path> retrieveRootDirectories() {
        return BatchFileUtils.retrieveBatchFileLookupRootDirectories();
    }

}