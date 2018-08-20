package edu.cornell.kfs.sys.batch.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.rice.kim.api.identity.Person;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.batch.CuBatchInputFileType;

public class CuBatchInputFileServiceImpl extends BatchInputFileServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuBatchInputFileServiceImpl.class);
    private static final String INVALID_ARGUEMENT = "an invalid(null) argument was given";
    private static final String NOT_PROPERLY_FORMATTED  = "The following file user identifer was not properly formatted: ";
    
    @Override
    public String save(Person user, BatchInputFileType batchInputFileType, String fileUserIdentifier, 
            InputStream fileContents, Object parsedObject) throws FileStorageException {
        if (user == null || batchInputFileType == null || fileContents == null) {
            LOG.error(INVALID_ARGUEMENT);
            throw new IllegalArgumentException(INVALID_ARGUEMENT);
        }

        if (!isFileUserIdentifierProperlyFormatted(fileUserIdentifier)) {
            LOG.error(NOT_PROPERLY_FORMATTED + fileUserIdentifier);
            throw new IllegalArgumentException(NOT_PROPERLY_FORMATTED + fileUserIdentifier);
        }

        // defer to batch input type to add any security or other needed information to the file name
        String saveFileName = batchInputFileType.getDirectoryPath() + "/" 
                + batchInputFileType.getFileName(user.getPrincipalName(), parsedObject, fileUserIdentifier);
        if (!StringUtils.isBlank(batchInputFileType.getFileExtension())) {
            saveFileName += "." + batchInputFileType.getFileExtension();
        }

        // construct the file object and check for existence
        File fileToSave = new File(saveFileName);
        if (fileToSave.exists()) {
            LOG.error("cannot store file, name already exists " + saveFileName);
            throw new FileStorageException("Cannot store file because the name " + saveFileName + " already exists on the file system.");
        }

        try {
            FileOutputStream fos = new FileOutputStream(fileToSave);
            while (fileContents.available() > 0) {
                fos.write(fileContents.read());
            }
            fos.flush();
            fos.close();
            
            //CU Mod  If the input file type is not CuBatchInputFileType then we create the
            //done file.  If it is of type CuBatchInputFileType then we also must call the
            //isDoneFileRequired function.
            if (!(batchInputFileType instanceof CuBatchInputFileType) 
                    || ((CuBatchInputFileType) batchInputFileType).isDoneFileRequired()) {
                createDoneFile(fileToSave, batchInputFileType);
            }
            //CU Mod
            
            batchInputFileType.process(saveFileName, parsedObject);
            
        } catch (IOException e) {
            LOG.error("unable to save contents to file " + saveFileName, e);
            throw new RuntimeException("errors encountered while writing file " + saveFileName, e);
        }

        return saveFileName;
    }
}
