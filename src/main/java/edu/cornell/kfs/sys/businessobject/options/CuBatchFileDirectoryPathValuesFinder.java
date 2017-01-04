/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2016 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.sys.businessobject.options;

import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.context.SpringContext;
import edu.cornell.kfs.sys.service.BatchFileDirectoryService;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import java.io.File;
import java.util.List;

public class CuBatchFileDirectoryPathValuesFinder extends KeyValuesBase {

    public List<KeyValue> getKeyValues() {
        List<File> rootDirectories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        return SpringContext.getBean(BatchFileDirectoryService.class).buildDirectoryKeyValuesList(rootDirectories);
    }

}
