package edu.cornell.kfs.sys.util;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class SubDirectoryWalker extends DirectoryWalker {
    private List<KeyValue> keyValues;
    private int recursiveDepth;

    public SubDirectoryWalker(List<KeyValue> keyValues) {
        super(DirectoryFileFilter.DIRECTORY, -1);
        this.keyValues = keyValues;
        this.recursiveDepth = 0;
    }

    public void addKeyValues(File startDirectory) throws IOException {
        walk(startDirectory, null);
    }

    /**
     * @see DirectoryWalker#handleDirectoryStart(File, int, java.util.Collection)
     */
    @Override
    protected void handleDirectoryStart(File directory, int depth, Collection results) throws IOException {
        super.handleDirectoryStart(directory, depth, results);
        ConcreteKeyValue entry = new ConcreteKeyValue();
        entry.setKey(BatchFileUtils.pathRelativeToRootDirectory(directory.getAbsolutePath()));
        // use the unicode literal for space....KFSMI-7392 fix
        entry.setValue(StringUtils.repeat("\u00A0", 4 * this.recursiveDepth) + directory.getName());
        keyValues.add(entry);
        this.recursiveDepth++;
    }

    /**
     * @see DirectoryWalker#handleDirectoryEnd(File, int, Collection)
     */
    @Override
    protected void handleDirectoryEnd(File directory, int depth, Collection results) throws IOException {
        super.handleDirectoryEnd(directory, depth, results);
        this.recursiveDepth--;
    }
}
