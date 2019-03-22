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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.rest.application.SysApiApplication;
import org.kuali.kfs.sys.util.DateRangeUtil;
import org.kuali.rice.kim.api.identity.Person;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchFileLookupSearchServiceImpl extends LookupSearchService {

    @Override
    public List<BusinessObjectBase> getSearchResults(Class<? extends BusinessObjectBase> businessObjectClass,
            MultivaluedMap<String, String> fieldValues) {
        return getFiles(fieldValues)
                .parallelStream()
                .map(batchFile -> (BusinessObjectBase) batchFile)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private List<BatchFile> getFiles(MultivaluedMap<String, String> fieldValues) {
        List<BatchFile> results = new ArrayList<>();

        IOFileFilter filter = FileFilterUtils.fileFileFilter();

        String fileNamePattern = fieldValues.getFirst("fileName");
        IOFileFilter fileNameBasedFilter = getFileNameBasedFilter(fileNamePattern);
        if (fileNameBasedFilter != null) {
            filter = FileFilterUtils.and(filter, fileNameBasedFilter);
        }

        String lastModifiedDate = fieldValues.getFirst("lastModifiedDate");
        IOFileFilter lastModifiedDateBasedFilter = getLastModifiedDateBasedFilter(lastModifiedDate);
        if (lastModifiedDateBasedFilter != null) {
            filter = FileFilterUtils.and(filter, lastModifiedDateBasedFilter);
        }

        List<String> pathPatterns = fieldValues.get("path");
        List<File> directories = getDirectoriesToSearch(pathPatterns);
        BatchFileLookupSearchServiceImpl.BatchFileFinder finder = new BatchFileLookupSearchServiceImpl.BatchFileFinder(
                results, filter);
        finder.find(directories);
        return results;
    }

    private IOFileFilter getFileNameBasedFilter(String fileNamePattern) {
        if (StringUtils.isNotBlank(fileNamePattern)) {
            return new WildcardFileFilter(fileNamePattern, IOCase.INSENSITIVE);
        }
        return null;
    }

    private IOFileFilter getLastModifiedDateBasedFilter(String lastModifiedDatePattern) {
        if (StringUtils.isBlank(lastModifiedDatePattern)) {
            return null;
        }

        DateRangeUtil dateRange = new DateRangeUtil();
        dateRange.setDateStringWithLongValues(lastModifiedDatePattern);
        if (!dateRange.isEmpty()) {
            return new BatchFileLookupSearchServiceImpl.LastModifiedDateFileFilter(dateRange.getLowerDate(),
                    dateRange.getUpperDate());
        } else {
            throw new RuntimeException("Unable to perform search using last modified date " + lastModifiedDatePattern);
        }
    }

	/*
	 * Cornell customization: changed access level to protected on this method so
	 * that it can be overridden in unit test
	 * 
	 */
    protected List<File> getDirectoriesToSearch(List<String> selectedPaths) {
        List<String> searchPaths = getPathsToSearch(selectedPaths);

        List<File> directories = new ArrayList<>();
        if (selectedPaths != null) {
            for (String searchPath : searchPaths) {
                File directory = new File(BatchFileUtils.resolvePathToAbsolutePath(searchPath));
                if (directory.exists()) {
                    directories.add(directory);
                }
            }
        } else {
            directories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        }

        return directories;
    }

    private List<String> getPathsToSearch(List<String> selectedPaths) {
        if (CollectionUtils.isEmpty(selectedPaths)) {
            return selectedPaths;
        }

        // Ignore redundant child paths
        List<String> searchPaths = new ArrayList<>();
        selectedPaths.stream().sorted(Comparator.comparingInt(String::length)).forEach(selectedPath -> {
            String[] searchPathsStringArray = searchPaths.toArray(new String[searchPaths.size()]);
            if (!StringUtils.startsWithAny(selectedPath, searchPathsStringArray)) {
                searchPaths.add(selectedPath);
            }
        });

        return searchPaths;
    }

    protected BatchFileAdminAuthorizationService getBatchFileAdminAuthorizationService() {
        return SpringContext.getBean(BatchFileAdminAuthorizationService.class);
    }

    protected class BatchFileFinder extends DirectoryWalker {
        private List<BatchFile> results;

        BatchFileFinder(List<BatchFile> results, IOFileFilter fileFilter) {
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

    protected class LastModifiedDateFileFilter extends AbstractFileFilter {
        private Date fromDate;
        private Date toDate;

        LastModifiedDateFileFilter(Date fromDate, Date toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        @Override
        public boolean accept(File file) {
            Date lastModifiedDate = new Date(file.lastModified());
            if (fromDate != null && fromDate.after(lastModifiedDate)) {
                return false;
            }
            if (toDate != null && toDate.before(lastModifiedDate)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public List<Map<String, Object>> getActionLinks(BusinessObjectBase businessObject, Person user) {
        BatchFile batchFile = (BatchFile) businessObject;
        List<Map<String, Object>> actionLinks = new LinkedList<>();

        if (canDownloadFile(batchFile, user)) {
            Map<String, Object> downloadLink = new LinkedHashMap<>();
            downloadLink.put("label", "Download");
            downloadLink.put("url", getDownloadURL(batchFile));
            downloadLink.put("method", "GET");
            actionLinks.add(downloadLink);
        }

        if (canDeleteFile(batchFile, user)) {
            Map<String, Object> deleteLink = new LinkedHashMap<>();
            deleteLink.put("label", "Delete");
            deleteLink.put("url", getResourceURL(batchFile));
            deleteLink.put("method", "DELETE");
            actionLinks.add(deleteLink);
        }

        return actionLinks;
    }

    private boolean canDownloadFile(BatchFile batchFile, Person user) {
        return getBatchFileAdminAuthorizationService().canDownload(batchFile, user);
    }

    private boolean canDeleteFile(BatchFile batchFile, Person user) {
        return getBatchFileAdminAuthorizationService().canDelete(batchFile, user);
    }

    private String getResourceURL(BatchFile batchFile) {
        return SysApiApplication.SYS_ROOT + "/" + SysApiApplication.BUSINESS_OBJECT_RESOURCE + "/BatchFile/" +
                batchFile.getId();
    }

    private String getDownloadURL(BatchFile batchFile) {
        return this.getResourceURL(batchFile) + ".bin";
    }
}
