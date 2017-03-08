package edu.cornell.kfs.sys.service.impl;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;

/**
 * Custom subclass of FileSystemFileStorageServiceImpl that has been configured
 * to allow for proper handling of batch files. This has been accomplished
 * by preventing an extra prefix or path separator from being prepended
 * to the file/directory paths.
 * 
 * NOTE: It would be ideal to just override the superclass's getFullPathName() method.
 * However, that method is private, so the current workaround is to forcibly use
 * an empty prefix and an empty separator. As a result, the emptyDirectory() method
 * has been reimplemented to use File.separator directly.
 */
public class BatchFileSystemFileStorageServiceImpl extends FileSystemFileStorageServiceImpl {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BatchFileSystemFileStorageServiceImpl.class);

    public BatchFileSystemFileStorageServiceImpl() {
        super();
        super.setPathPrefix(StringUtils.EMPTY);
    }

    /**
     * Overridden to return an empty String; thus, the calling code
     * should NOT rely on this method for retrieving an actual separator.
     * 
     * @see org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl#separator()
     */
    @Override
    public String separator() {
        return StringUtils.EMPTY;
    }

    /**
     * Overridden to avoid using the path-prefixing code and to also use
     * File.separator directly instead of separator(), but otherwise
     * uses the same code and logic from the superclass.
     * 
     * @see org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl#emptyDirectory(java.lang.String)
     */
    @Override
    public void emptyDirectory(String dirname) {
        LOG.debug("emptyDirectory() started");

        if (!directoryExists(dirname)) {
            LOG.error("emptyDirectory() Unable to empty directory, it does not exist");
            throw new FileStorageException("Unable to empty directory");
        }

        File dir = new File(dirname);
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                emptyDirectory(dirname + File.separator + f.getName());
            } else {
                delete(dirname + File.separator + f.getName());
            }
        }
    }

    /**
     * Overridden to prevent setting the pathPrefix programmatically.
     * 
     * @see org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl#setPathPrefix(java.lang.String)
     */
    @Override
    public void setPathPrefix(String pathPrefix) {
        throw new UnsupportedOperationException("Cannot change pathPrefix on this instance");
    }

}
