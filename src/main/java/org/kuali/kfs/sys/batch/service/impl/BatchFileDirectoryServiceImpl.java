/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.sys.batch.service.impl;

import org.kuali.kfs.krad.keyvalues.HierarchicalData;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.batch.service.BatchFileDirectoryService;
import org.springframework.cache.annotation.Cacheable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/* Cornell Customization: backport redis*/
public class BatchFileDirectoryServiceImpl implements BatchFileDirectoryService {

    @Override
    @Cacheable(cacheNames = BatchFile.CACHE_NAME, key = "'getHierarchicalControlValues'")
    public List<HierarchicalData> buildBatchFileLookupDirectoriesHierarchy() {
        List<HierarchicalData> hierarchicalData = new LinkedList<>();
        List<File> rootDirectories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        rootDirectories.sort(Comparator.comparing(File::getName));
        for (File rootDirectory : rootDirectories) {
            if (rootDirectory.isDirectory()) {
                String directoryName = rootDirectory.getName();
                HierarchicalData dataForThisDirTree = new HierarchicalData(directoryName, directoryName);

                // TODO it needs a terminal stream operator to work but I don't know what kind needs to go there
                // hence the count
                children(rootDirectory.toPath(), dataForThisDirTree).count();
                // couldn't figure out how to not have fake root (children would get null first time); this was an easy
                // work around that shouldn't cost much
                hierarchicalData.add(dataForThisDirTree.getChildren().get(0));
            }
        }
        return hierarchicalData;
    }

    private Stream<Path> children(Path path, HierarchicalData hierarchicalData) {
        if (Files.isDirectory(path)) {
            try {
                String fileName = path.getFileName().toString();
                String relativePath = BatchFileUtils.pathRelativeToRootDirectory(path.toString());
                HierarchicalData subTreeData = new HierarchicalData(fileName, relativePath);
                hierarchicalData.addChild(subTreeData);
                return Files.list(path).sorted().flatMap(filePath -> children(filePath, subTreeData));
            } catch (Exception e) {
                return Stream.empty();
            }
        } else {
            return Stream.of(path);
        }
    }

}
