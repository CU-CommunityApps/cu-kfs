/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.datadictionary.EntityNotFoundException;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.businessobject.service.exception.ForbiddenException;
import org.kuali.kfs.sys.businessobject.service.exception.NotAllowedException;
import org.kuali.kfs.sys.util.DateRangeUtil;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BatchFileSearchService extends SearchService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectDictionaryService businessObjectDictionaryService;

    @Override
    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final MultiValueMap<String, String> fieldValues,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending
    ) {
        LOG.trace(
                "getSearchResults(...) - Enter : businessObjectClass={}, fieldValues={}, skip={}, limit={}, "
                + "sortField={}, sortAscending={}",
                businessObjectClass::getSimpleName,
                () -> fieldValues,
                () -> skip,
                () -> limit,
                () -> sortField,
                () -> sortAscending
        );

        final List<BatchFile> allFiles = getFiles(fieldValues);
        final Stream<BatchFile> stream = allFiles.stream();

        final BusinessObjectSorter boSorter = new BusinessObjectSorter();
        final List<BusinessObjectBase> sortedAndSliced =
                boSorter.sort(
                        businessObjectClass,
                        skip,
                        limit,
                        sortField,
                        sortAscending,
                        stream
                );

        final Pair<Collection<? extends BusinessObjectBase>, Integer> pair = Pair.of(sortedAndSliced, allFiles.size());
        LOG.trace(
                "getSearchResults(...) - Exit : businessObjectClass={}; fieldValues={}; skip={}; limit={}; "
                + "sortField={}; sortAscending={}; pair={}",
                businessObjectClass::getSimpleName,
                () -> fieldValues,
                () -> skip,
                () -> limit,
                () -> sortField,
                () -> sortAscending,
                () -> pair
        );
        return pair;
    }

    @Override
    public Object find(final Class<? extends BusinessObjectBase> businessObjectClass, final String id) {
        final BusinessObjectAdminService batchFileAdminService = businessObjectDictionaryService
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
        } catch (final AuthorizationException ae) {
            throw new ForbiddenException();
        } catch (final EntityNotFoundException e) {
            return null;
        }
    }

    private static List<BatchFile> getFiles(final MultiValueMap<String, String> fieldValues) {
        LOG.trace("getFiles(...) - Enter : fieldValues={}", fieldValues);

        final String fileNameWildcard = fieldValues.getFirst("fileName");

        final String lastModifiedDate = fieldValues.getFirst("lastModifiedDate");
        final DateRangeUtil lastModifiedDateRangeUtil = getLastModifierDateRangeUtil(lastModifiedDate);
        Instant startInstant = null;
        Instant endInstant = null;
        if (lastModifiedDateRangeUtil != null) {
            final Date lowerDate = lastModifiedDateRangeUtil.getLowerDate();
            if (lowerDate != null) {
                startInstant = lowerDate.toInstant();
            }
            final Date upperDate = lastModifiedDateRangeUtil.getUpperDate();
            if (upperDate != null) {
                endInstant = upperDate.toInstant();
            }
        }

        final List<Path> userSelectedPaths =
                Optional.ofNullable(fieldValues.get("path"))
                        .map(paths -> paths.stream().map(Path::of).collect(Collectors.toList()))
                        .orElseGet(List::of);
        final List<Path> directoriesToSearch = getDirectoriesToSearch(userSelectedPaths);

        final List<BatchFile> results = new ArrayList<>();
        for (final Path directory : directoriesToSearch) {
            results.addAll(searchDirectory(directory, fileNameWildcard, startInstant, endInstant));
        }

        LOG.trace(
                "getFiles(...) - Exit : fieldValues={}; results.size={}",
                () -> fieldValues,
                results::size
        );
        return results;
    }

    private static Collection<? extends BatchFile> searchDirectory(
            final Path directory,
            final String fileNameWildcard,
            final Instant startInstant,
            final Instant endInstant
    ) {
        LOG.trace(
                "searchDirectory(...) - Enter : directory={}; fileNameWildcard={}; startInstant={}; endInstant={}",
                directory::toString,
                () -> fileNameWildcard,
                () -> startInstant,
                () -> endInstant
        );

        try (Stream<Path> paths = Files.walk(directory)) {
            final List<BatchFile> batchFiles =
                    paths.filter(Files::isRegularFile)
                            .filter(file -> matchesCriteria(file, fileNameWildcard, startInstant, endInstant))
                            .map(Path::toFile)
                            .map(BatchFile::new)
                            .collect(Collectors.toList());
            LOG.trace("searchDirectory(...) - Exit : directory={}; fileNameWildcard={}; startInstant={}; "
                     + "endInstant={}; batchFiles.size={}",
                    directory::toString,
                    () -> fileNameWildcard,
                    () -> startInstant,
                    () -> endInstant,
                    batchFiles::size
            );
            return batchFiles;
        } catch (final IOException e) {
            LOG.atError()
                    .withThrowable(e)
                    .log("searchDirectory(...) - Error accessing directory : directory={}", directory);
            return List.of();
        }
    }

    private static boolean matchesCriteria(
            final Path file,
            final String fileNameWildcard,
            final Instant startInstant,
            final Instant endInstant
    ) {
        return nameMatchesWildcard(file, fileNameWildcard)
               && lastModificationInDesiredRange(file, startInstant, endInstant);
    }

    private static boolean nameMatchesWildcard(final Path file, final String fileNameWildcard) {
        LOG.trace(
                "nameMatchesWildcard(...) - Enter : file={}; fileNameWildcard={}",
                file::toString,
                () -> fileNameWildcard
        );

        if (StringUtils.isBlank(fileNameWildcard)) {
            return true;
        }

        final boolean match =
                FilenameUtils.wildcardMatch(file.getFileName().toString(), fileNameWildcard, IOCase.INSENSITIVE);
        LOG.trace(
                "nameMatchesWildcard(...) - Exit : file={}; fileNameWildcard={}; match={}",
                file::toString,
                () -> fileNameWildcard,
                () -> match
        );
        return match;
    }

    private static boolean lastModificationInDesiredRange(
            final Path file,
            final Instant startInstant,
            final Instant endInstant
    ) {
        LOG.trace(
                "lastModificationInDesiredRange(...) - Enter : file={}; startInstant={}; endInstant={}",
                file::toString,
                () -> startInstant,
                () -> endInstant
        );

        if (startInstant == null && endInstant == null) {
            return true;
        }

        try {
            final Instant lastModifiedInstant = Files.getLastModifiedTime(file).toInstant();

            final boolean inRange =
                    (startInstant == null || !startInstant.isAfter(lastModifiedInstant))
                    && (endInstant == null || !endInstant.isBefore(lastModifiedInstant));
            LOG.trace(
                    "lastModificationInDesiredRange(...) - Exit : file={}; startInstant={}; endInstant={}; inRange={}",
                    file::toString,
                    () -> startInstant,
                    () -> endInstant,
                    () -> inRange
            );
            return inRange;
        } catch (final IOException e) {
            LOG.atError().withThrowable(e).log(
                    "lastModificationInDesiredRange(...) - Error reading file attributes: file={}",
                    file
            );
            return false;
        }
    }

    private static DateRangeUtil getLastModifierDateRangeUtil(final String lastModifiedDatePattern) {
        LOG.trace("getLastModifierDateRangeUtil(...) - Enter : lastModifiedDatePattern={}", lastModifiedDatePattern);

        if (StringUtils.isBlank(lastModifiedDatePattern)) {
            return null;
        }

        final DateRangeUtil dateRange = new DateRangeUtil();
        dateRange.setDateStringWithLongValues(lastModifiedDatePattern);

        if (dateRange.isEmpty()) {
            throw new RuntimeException("Unable to perform search using last modified date " + lastModifiedDatePattern);
        }
        LOG.trace("getLastModifierDateRangeUtil(...) - Exit : lastModifiedDatePattern={}", lastModifiedDatePattern);
        return dateRange;
    }

    /*
     * Cornell customization: changed access level to protected on this method so
     * that it can be overridden in unit test
     * 
     */
    protected static List<Path> getDirectoriesToSearch(final List<Path> userSelectedPaths) {
        LOG.trace("getDirectoriesToSearch(...) - Enter : userSelectedPaths={}", userSelectedPaths);

        final List<Path> uniqueSelectedDirectories = getUniqueSelectedDirectories(userSelectedPaths);

        List<Path> directoriesToSearch = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userSelectedPaths)) {
            for (final Path uniqueSelectedDirectory : uniqueSelectedDirectories) {
                final Path directory = BatchFileUtils.resolvePathToAbsolutePath(uniqueSelectedDirectory);
                if (Files.exists(directory)) {
                    directoriesToSearch.add(directory);
                }
            }
        } else {
            directoriesToSearch = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        }

        LOG.trace(
                "getDirectoriesToSearch(...) - Exit : userSelectedPaths={}; directoriesToSearch.size={}",
                () -> userSelectedPaths,
                directoriesToSearch::size
        );
        return directoriesToSearch;
    }

    // Ignore redundant child paths
    private static List<Path> getUniqueSelectedDirectories(final List<Path> userSelectedDirectories) {
        LOG.trace("getUniqueSelectedDirectories(...) - Enter : userSelectedDirectories={}", userSelectedDirectories);

        final List<Path> uniqueSelectedDirectories = new LinkedList<>(userSelectedDirectories);

        for (final Path path1 : userSelectedDirectories) {
            for (final Path path2 : userSelectedDirectories) {
                if (!path1.equals(path2) && path2.startsWith(path1)) {
                    uniqueSelectedDirectories.remove(path2);
                }
            }
        }

        LOG.trace(
                "getUniqueSelectedDirectories(...) - Exit : userSelectedDirectories={}; uniqueSelectedDirectories={}",
                userSelectedDirectories,
                uniqueSelectedDirectories
        );
        return uniqueSelectedDirectories;
    }

    public void setBusinessObjectDictionaryService(
            final BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

}
