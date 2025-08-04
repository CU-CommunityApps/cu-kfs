package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.io.FileNotFoundException;

import org.kuali.kfs.sys.batch.BatchFile;

public class CreateDoneBatchFile extends BatchFile {
    
    public CreateDoneBatchFile() {
        super();
    }

	public CreateDoneBatchFile(String id) throws FileNotFoundException {
		super(id);
	}

	public CreateDoneBatchFile(File file) {
		super(file);
	}

}
