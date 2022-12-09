/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.sys.businessobject.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.datadictionary.EntityNotFoundException;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.businessobject.service.impl.BusinessObjectSorter;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.util.DateRangeUtil;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class BatchFileSearchService extends SearchService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectDictionaryService businessObjectDictionaryService;

    @Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            Class<? extends BusinessObjectBase> businessObjectClass,
            MultivaluedMap<String, String> fieldValues, int skip, int limit, String sortField, boolean sortAscending) {

        List<BatchFile> allFiles = getFiles(fieldValues);
        Stream<BatchFile> stream = allFiles.parallelStream();

        BusinessObjectSorter boSorter = new BusinessObjectSorter();
        List<BusinessObjectBase> sortedAndSliced = boSorter.sort(businessObjectClass, skip, limit, sortField,
                sortAscending, stream);

        return Pair.of(sortedAndSliced, allFiles.size());
    }

    @Override
    public Object find(Class<? extends BusinessObjectBase> businessObjectClass, String id) {
        BusinessObjectAdminService batchFileAdminService = businessObjectDictionaryService
                .getBusinessObjectAdminService(businessObjectClass);

        if (!batchFileAdminService.allowsDownload(null, null)) {
            LOG.debug(
                    "Requested BO does not support download : bo={}; batchFileAdminServiceName={}",
                    businessObjectClass::getSimpleName,
                    () -> batchFileAdminService.getClass().getSimpleName()
            );
            throw new NotAllowedException("The requested business object does not support GETs with the supplied " +
                    "media type.");
        }

        try {
            return batchFileAdminService.download(id);
        } catch (AuthorizationException ae) {
            throw new ForbiddenException();
        } catch (EntityNotFoundException e) {
            return null;
        }
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
        BatchFileSearchService.BatchFileFinder finder = new BatchFileSearchService.BatchFileFinder(
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
            return new BatchFileSearchService.LastModifiedDateFileFilter(dateRange.getLowerDate(),
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

    public void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
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
            return toDate == null || !toDate.before(lastModifiedDate);
        }
    }
}
