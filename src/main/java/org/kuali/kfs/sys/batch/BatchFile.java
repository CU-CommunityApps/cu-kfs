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
package org.kuali.kfs.sys.batch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class BatchFile extends TransientBusinessObjectBase {
    private static final Logger LOG = LogManager.getLogger();
    public static final String CACHE_NAME = "BatchFile";

    private File file;
    
	/*
	 * Cornell customization: add default constructor so that the old batch file
	 * lookup will work on the Create Disencumbrance page. This should be removed
	 * once the new batch file lookup will suppport return from lookup.
	 */
	public BatchFile() {

	}

    /**
     * @param id id containing path information about the {@link File} this instance should encapsulate.
     * @throws FileNotFoundException if path indicated by {@code id} does not correspond to an existing file or the
     *         path does not correspond to a valid batch file location.
     */
    public BatchFile(String id) throws FileNotFoundException {
        String fullPath = decodeId(id);
        try {
            // DO NOT remove the resolvePath here. This check ensures that the path is relative to the batch file root
            // directories. It's a safety mechanism to prevent access to files outside the scope of batch file.
            file = new File(BatchFileUtils.resolvePathToAbsolutePath(fullPath));
        } catch (UnsupportedOperationException uoe) {
            String msg = "Unable to locate a batch file corresponding to id: " + id;
            LOG.error(msg + ". Resolved path for this id: " + fullPath + ". This may be indicative of a location " +
                    "outside permitted batch file locations.");
            FileNotFoundException fnfe = new FileNotFoundException(msg);
            fnfe.initCause(uoe);
            throw fnfe;
        }
        if (!file.exists()) {
            String msg = "Unable to locate a batch file corresponding to id: " + id;
            LOG.error(msg + ". Resolved path for this id: " + fullPath);
            throw new FileNotFoundException(msg);
        }
    }

    public BatchFile(File file) {
        this.file = file;
    }

    public String getPath() {
        return BatchFileUtils.pathRelativeToRootDirectory(file.getAbsoluteFile().getParentFile().getAbsolutePath());
    }

    public String getFileName() {
        return file.getName();
    }

    public Date getLastModifiedDate() {
        return new Date(file.lastModified());
    }

    public long getFileSize() {
        return file.length();
    }

    // purposely not creating a getter method, to prevent the file object from being unintentionally accessed via form
    // parameters
    public File retrieveFile() {
        return file;
    }

    /**
     * @return String containing synthetic id to be associated with this Batch File instance.
     */
    public String getId() {
        String encoded;
        String fullPath = getPath() + File.separator + getFileName();
        encoded = Base64.getEncoder().encodeToString(fullPath.getBytes(StandardCharsets.UTF_8));
        return encoded;
    }

    private String decodeId(String id) {
        byte[] pathAsBytes = Base64.getDecoder().decode(id);
        return new String(pathAsBytes, StandardCharsets.UTF_8);
    }

}
