package edu.cornell.kfs.module.ld.businessobject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.UrlFactory;

public class LaborLedgerBatchFileLookupableHelperServiceImpl extends BatchFileLookupableHelperServiceImpl {

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        List<BatchFile> results = new ArrayList<BatchFile>();

        IOFileFilter filter = FileFilterUtils.fileFileFilter();

        IOFileFilter pathBasedFilter = getPathBasedFileFilter();
        if (pathBasedFilter != null) {
            filter = FileFilterUtils.andFileFilter(filter, pathBasedFilter);
        }

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
        List<File> rootDirectories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        finder.find(rootDirectories);

        return results;
    }

    protected IOFileFilter getPathBasedFileFilter() {
        List<File> selectedFiles = getSelectedDirectories();
        if (selectedFiles.isEmpty()) {
            return null;
        }
        IOFileFilter fileFilter = null;
        for (File selectedFile : selectedFiles) {
            IOFileFilter subFilter = new SubDirectoryFileFilter(selectedFile);
            if (fileFilter == null) {
                fileFilter = subFilter;
            } else {
                fileFilter = FileFilterUtils.orFileFilter(fileFilter, subFilter);
            }
        }
        return fileFilter;
    }

    protected List<File> getSelectedDirectories() {
        List<File> directories = new ArrayList<File>();

        String path = KFSConstants.EMPTY_STRING;

        path = "staging" + System.getProperty("file.separator") + "ld" + System.getProperty("file.separator") + "enterpriseFeed";

        File directory = new File(BatchFileUtils.resolvePathToAbsolutePath(path));
        if (!directory.exists()) {
            throw new RuntimeException("Non existent directory " + BatchFileUtils.resolvePathToAbsolutePath("staging" + System.getProperty("file.separator") + "ld" + System.getProperty("file.separator") + "enterpriseFeed"));
        }
        directories.add(directory);

        return directories;
    }

    /**
     * @see org.kuali.kfs.sys.businessobject.lookup.BatchFileLookupableHelperServiceImpl#getCustomActionUrls(org.kuali.rice.kns.bo.BusinessObject,
     *      java.util.List)
     */
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        List<HtmlData> links = new ArrayList<HtmlData>();

        BatchFile batchFile = (BatchFile) businessObject;

        if (canCreateDisencumbrance(batchFile)) {
            links.add(getCreateDisencumbranceUrl(batchFile));
        }
        if (canDownloadFile(batchFile)) {
            links.add(getDownloadUrl(batchFile));
        }
        if (canDeleteFile(batchFile)) {
            links.add(getDeleteUrl(batchFile));
        }

        return links;
    }

    protected HtmlData getDownloadUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KNSConstants.DISPATCH_REQUEST_PARAMETER, "download");
        String href = UrlFactory.parameterizeUrl("../laborLedgerBatchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "download", "Download");
    }

    protected HtmlData getDeleteUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KNSConstants.DISPATCH_REQUEST_PARAMETER, "delete");
        String href = UrlFactory.parameterizeUrl("../laborLedgerBatchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "delete", "Delete");
    }

    protected HtmlData getCreateDisencumbranceUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KNSConstants.DISPATCH_REQUEST_PARAMETER, "disencumber");
        String href = UrlFactory.parameterizeUrl("../laborLedgerBatchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "disencumber", "Create Disencumbrance");
    }

    /**
     * @param batchFile
     * @return
     */
    protected boolean canCreateDisencumbrance(BatchFile batchFile) {
        if (batchFile.getFileName().endsWith(".data") && canDeleteFile(batchFile)) {
            return true;
        } else
            return false;
    }
}
