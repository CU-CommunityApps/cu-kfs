/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.businessobject.lookup;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.lookup.AbstractLookupableHelperServiceImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.bo.BusinessObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * Cornell Customization: Add old batch file lookup to support lookups on Create Disencumbrance page. This should be removed once the new batch file lookup will support return from lookup.
 */
public class BatchFileLookupableHelperServiceImpl extends AbstractLookupableHelperServiceImpl {
    protected DateTimeService dateTimeService;
    protected BatchFileAdminAuthorizationService batchFileAdminAuthorizationService;

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
        List<File> selectedFiles = getSelectedDirectories(getSelectedPaths());
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

    protected IOFileFilter getFileNameBasedFilter(String fileNamePattern) {
        if (StringUtils.isNotBlank(fileNamePattern)) {
            return new WildcardFileFilter(fileNamePattern, IOCase.INSENSITIVE);
        }
        return null;
    }

    protected IOFileFilter getLastModifiedDateBasedFilter(String lastModifiedDatePattern) {
        if (StringUtils.isBlank(lastModifiedDatePattern)) {
            return null;
        }
        try {
            if (lastModifiedDatePattern.startsWith("<=")) {
                String dateString = StringUtils.removeStart(lastModifiedDatePattern, "<=");
                Date toDate = dateTimeService.convertToDate(dateString);
                return new LastModifiedDateFileFilter(null, toDate);
            }
            if (lastModifiedDatePattern.startsWith(">=")) {
                String dateString = StringUtils.removeStart(lastModifiedDatePattern, ">=");
                Date fromDate = dateTimeService.convertToDate(dateString);
                return new LastModifiedDateFileFilter(fromDate, null);
            }
            if (lastModifiedDatePattern.contains("..")) {
                String[] dates = StringUtils.splitByWholeSeparator(lastModifiedDatePattern, "..", 2);
                Date fromDate = dateTimeService.convertToDate(dates[0]);
                Date toDate = dateTimeService.convertToDate(dates[1]);
                return new LastModifiedDateFileFilter(fromDate, toDate);
            }
        } catch (ParseException e) {
            throw new RuntimeException("Can't parse date", e);
        }
        throw new RuntimeException("Unable to perform search using last modified date " + lastModifiedDatePattern);
    }

    protected List<File> getSelectedDirectories(String[] selectedPaths) {
        List<File> directories = new ArrayList<File>();
        if (selectedPaths != null) {
            for (String selectedPath : selectedPaths) {
                File directory = new File(BatchFileUtils.resolvePathToAbsolutePath(selectedPath));
                if (!directory.exists()) {
                    throw new RuntimeException("Non existent directory " + BatchFileUtils.resolvePathToAbsolutePath(selectedPath));
                }
                directories.add(directory);
            }
        }
        return directories;
    }

    /**
     * KRAD Conversion: gets rows after customizing the field values
     * <p>
     * No use of data dictionary
     */
    protected String[] getSelectedPaths() {
        List<Row> rows = getRows();
        if (rows == null) {
            return null;
        }
        for (Row row : rows) {
            for (Field field : row.getFields()) {
                if ("path".equals(field.getPropertyName()) && Field.MULTISELECT.equals(field.getFieldType())) {
                    String[] values = field.getPropertyValues();
                    return values;
                }
            }
        }
        return null;
    }

    protected class SubDirectoryFileFilter extends AbstractFileFilter {
        private File superDirectory;

        public SubDirectoryFileFilter(File superDirectory) {
            this.superDirectory = superDirectory.getAbsoluteFile();
        }

        @Override
        public boolean accept(File file) {
            file = file.getAbsoluteFile();
            file = file.getParentFile();
            while (file != null) {
                if (file.equals(superDirectory)) {
                    return true;
                }
                file = file.getParentFile();
            }
            return false;
        }
    }

    protected class LastModifiedDateFileFilter extends AbstractFileFilter {
        private Date fromDate;
        private Date toDate;

        public LastModifiedDateFileFilter(Date fromDate, Date toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        @Override
        public boolean accept(File file) {
            Date lastModifiedDate = KfsDateUtils.clearTimeFields(new Date(file.lastModified()));

            if (fromDate != null && fromDate.after(lastModifiedDate)) {
                return false;
            }
            if (toDate != null && toDate.before(lastModifiedDate)) {
                return false;
            }
            return true;
        }
    }

    protected class BatchFileFinder extends DirectoryWalker {
        private List<BatchFile> results;

        public BatchFileFinder(List<BatchFile> results, IOFileFilter fileFilter) {
            super(null, fileFilter, -1);
            this.results = results;
        }

        public void find(Collection<File> rootDirectories) {
            try {
                for (File rootDirectory : rootDirectories) {
                    walk(rootDirectory, null);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error performing lookup", e);
            }
        }

        /**
         * @see org.apache.commons.io.DirectoryWalker#handleFile(java.io.File, int, java.util.Collection)
         */
        @Override
        protected void handleFile(File file, int depth, Collection results) throws IOException {
            super.handleFile(file, depth, results);
            BatchFile batchFile = new BatchFile(file);
            this.results.add(batchFile);
        }
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        List<HtmlData> links = new ArrayList<HtmlData>();

        BatchFile batchFile = (BatchFile) businessObject;
        if (canDownloadFile(batchFile)) {
            links.add(getDownloadUrl(batchFile));
        }
        if (canDeleteFile(batchFile)) {
            links.add(getDeleteUrl(batchFile));
        }
        return links;
    }

    protected boolean canDownloadFile(BatchFile batchFile) {
        return batchFileAdminAuthorizationService.canDownload(batchFile, GlobalVariables.getUserSession().getPerson());
    }

    protected boolean canDeleteFile(BatchFile batchFile) {
        return batchFileAdminAuthorizationService.canDelete(batchFile, GlobalVariables.getUserSession().getPerson());
    }

    protected HtmlData getDownloadUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "download");
        String href = UrlFactory.parameterizeUrl("../batchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "download", "Download");
    }

    protected HtmlData getDeleteUrl(BatchFile batchFile) {
        Properties parameters = new Properties();
        parameters.put("filePath", BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "delete");
        String href = UrlFactory.parameterizeUrl("../batchFileAdmin.do", parameters);
        return new AnchorHtmlData(href, "delete", "Delete");
    }

    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#validateSearchParameters(java.util.Map)
     */
    @Override
    public void validateSearchParameters(Map fieldValues) {
        super.validateSearchParameters(fieldValues);

        String[] selectedPaths = getSelectedPaths();
        if (selectedPaths != null) {
            for (String selectedPath : selectedPaths) {
                String resolvedPath = BatchFileUtils.resolvePathToAbsolutePath(selectedPath);
                if (!BatchFileUtils.isDirectoryAccessible(resolvedPath)) {
                    throw new RuntimeException("Can't access path " + selectedPath);
                }
            }
        }
    }

    /**
     * Sets the batchFileAdminAuthorizationService attribute value.
     *
     * @param batchFileAdminAuthorizationService The batchFileAdminAuthorizationService to set.
     */
    public void setBatchFileAdminAuthorizationService(BatchFileAdminAuthorizationService batchFileAdminAuthorizationService) {
        this.batchFileAdminAuthorizationService = batchFileAdminAuthorizationService;
    }
}
