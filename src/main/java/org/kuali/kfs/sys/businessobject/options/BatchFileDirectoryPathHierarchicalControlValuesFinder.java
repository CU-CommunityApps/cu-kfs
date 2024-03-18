/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.sys.businessobject.options;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.keyvalues.HierarchicalControlValuesFinder;
import org.kuali.kfs.krad.keyvalues.HierarchicalData;
import org.kuali.kfs.sys.batch.service.BatchFileDirectoryService;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.List;

public class BatchFileDirectoryPathHierarchicalControlValuesFinder implements HierarchicalControlValuesFinder {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<HierarchicalData> getHierarchicalControlValues() {
        LOG.info("Create batch directories values finder");
        return SpringContext.getBean(BatchFileDirectoryService.class).buildBatchFileLookupDirectoriesHierarchy();
    }
}
