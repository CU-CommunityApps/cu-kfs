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
package org.kuali.kfs.sys.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants.SystemGroupParameterNames;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides batch input file management, including listing files, parsing, downloading, storing, and deleting.
 */
/* CU customization to change method access from private to public */
public class BatchInputFileServiceImpl implements BatchInputFileService {

    private static final Logger LOG = LogManager.getLogger();
    private ParameterService parameterService;

    /**
     * Delegates to the batch input file type to parse the file.
     */
    @Override
    public Object parse(final BatchInputFileType batchInputFileType, final byte[] fileByteContent) {
        try {
            LOG.info("parse() - Begin parsing");
            final Object parsedObject = batchInputFileType.parse(fileByteContent);
            LOG.info("parse() - End parsing");
            return parsedObject;
        } catch (final ParseException e) {
            LOG.error("Error encountered parsing file", e);
            throw e;
        }
    }

    /**
     * Defers to batch type to do any validation on the parsed contents.
     */
    @Override
    public boolean validate(final BatchInputFileType inputType, final Object parsedFileContents) {
        if (inputType == null || parsedFileContents == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        LOG.info("validate() - Begin validation");
        final boolean isValid = inputType.validate(parsedFileContents);
        LOG.info("validate() - End validation");
        return isValid;
    }

    @Override
    public String save(
            final Person user, final BatchInputFileType inputType, final String fileUserIdentifier,
            final InputStream fileContents, final Object parsedObject) throws AuthorizationException, FileStorageException {
        if (user == null || inputType == null || fileContents == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        if (!isFileUserIdentifierProperlyFormatted(fileUserIdentifier)) {
            LOG.error("The following file user identifier was not properly formatted: {}", fileUserIdentifier);
            throw new IllegalArgumentException("The following file user identifier was not properly formatted: " +
                    fileUserIdentifier);
        }

        // defer to batch input type to add any security or other needed information to the file name
        String saveFileName = inputType.getDirectoryPath() + "/" +
                              inputType.getFileName(user.getPrincipalName(), parsedObject, fileUserIdentifier);
        if (StringUtils.isNotBlank(inputType.getFileExtension())) {
            saveFileName += "." + inputType.getFileExtension();
        }

        if (inputType.shouldSave()) {
            // construct the file object and check for existence
            final File fileToSave = new File(saveFileName);
            if (fileToSave.exists()) {
                LOG.error("cannot store file, name already exists {}", saveFileName);
                throw new FileStorageException("Cannot store file because the name " + saveFileName +
                        " already exists on the file system.");
            }

            try {
                final FileOutputStream fos = new FileOutputStream(fileToSave);
                while (fileContents.available() > 0) {
                    fos.write(fileContents.read());
                }
                fos.flush();
                fos.close();

                createDoneFile(fileToSave, inputType);

                LOG.info("process() - Begin processing");
                inputType.process(saveFileName, parsedObject);
                LOG.info("process() - End processing");
            } catch (final IOException e) {
                LOG.error("unable to save contents to file {}", saveFileName, e);
                throw new RuntimeException("errors encountered while writing file " + saveFileName, e);
            }
        } else {
            LOG.info("process() - Begin processing");
            inputType.process(saveFileName, parsedObject);
            LOG.info("process() - End processing");
        }
        return saveFileName;
    }

    /**
     * Creates a '.done' file with the name of the batch file.
     */
    // CU customization to change method access from private to public
    public void createDoneFile(final File batchFile, final BatchInputFileType batchInputFileType) {
        final String fileExtension = batchInputFileType.getFileExtension();
        final File doneFile = generateDoneFileObject(batchFile, fileExtension);
        final String doneFileName = doneFile.getName();

        if (!doneFile.exists()) {
            final boolean doneFileCreated;
            try {
                doneFileCreated = doneFile.createNewFile();
            } catch (final IOException e) {
                LOG.error("unable to create done file {}", doneFileName, e);
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " +
                        doneFileName, e);
            }

            if (!doneFileCreated) {
                LOG.error("unable to create done file {}", doneFileName);
                throw new RuntimeException("Errors encountered while saving the file: Unable to create .done file " +
                        doneFileName);
            }
        }
    }

    /**
     * This method is responsible for creating a File object that represents the done file. The real file represented
     * on disk may not exist
     *
     * @return a File object representing the done file. The real file may not exist on disk, but the return value can
     *         be used to create that file.
     */
    private File generateDoneFileObject(final File batchInputFile, final String fileExtension) {
        final String doneFileName = fileExtension != null ? StringUtils.substringBeforeLast(batchInputFile.getPath(), ".") + ".done" :
            batchInputFile.getPath() + ".done";
        return new File(doneFileName);
    }

    @Override
    public boolean isBatchInputTypeActive(final BatchInputFileType batchInputFileType) {
        if (batchInputFileType == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        final List<String> activeInputTypes = new ArrayList<>(parameterService
                .getParameterValuesAsString(KfsParameterConstants.FINANCIAL_SYSTEM_BATCH.class,
                        SystemGroupParameterNames.ACTIVE_INPUT_TYPES_PARAMETER_NAME));

        return !activeInputTypes.isEmpty() && activeInputTypes.contains(batchInputFileType.getFileTypeIdentifier());
    }

    /**
     * Fetches workgroup for batch type from system parameter and verifies user is a member. Then a list of all files
     * for the batch type are retrieved. For each file, the file and user is sent through the checkAuthorization
     * method of the batch input type implementation for finer grained security. If the method returns true, the
     * filename is added to the user's list.
     */
    @Override
    public List<String> listBatchTypeFilesForUser(final BatchInputFileType batchInputFileType, final Person user) throws
            AuthorizationException {
        if (batchInputFileType == null || user == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        final List<String> userFileNamesList = new ArrayList<>();
        final List<File> userFileList = listBatchTypeFilesForUserAsFiles(batchInputFileType, user);

        for (final File userFile : userFileList) {
            userFileNamesList.add(userFile.getAbsolutePath());
        }

        return userFileNamesList;
    }

    private List<File> listBatchTypeFilesForUserAsFiles(final BatchInputFileType batchInputFileType, final Person user) throws
            AuthorizationException {
        final File[] filesInBatchDirectory = listFilesInBatchTypeDirectory(batchInputFileType);

        final List<File> userFileList = new ArrayList<>();
        if (filesInBatchDirectory != null) {
            for (final File batchFile : filesInBatchDirectory) {
                final String fileExtension = StringUtils.substringAfterLast(batchFile.getName(), ".");
                if (StringUtils.isBlank(batchInputFileType.getFileExtension())
                        || batchInputFileType.getFileExtension().equals(fileExtension)) {
                    if (user.getPrincipalName().equals(batchInputFileType.getAuthorPrincipalName(batchFile))) {
                        userFileList.add(batchFile);
                    }
                }
            }
        }
        return userFileList;
    }

    /**
     * Returns List of filenames for existing files in the directory given by the batch input type.
     */
    private File[] listFilesInBatchTypeDirectory(final BatchInputFileType batchInputFileType) {
        final File batchTypeDirectory = new File(batchInputFileType.getDirectoryPath());
        return batchTypeDirectory.listFiles();
    }

    @Override
    public List<String> listInputFileNamesWithDoneFile(final BatchInputFileType batchInputFileType) {
        if (batchInputFileType == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        final File batchTypeDirectory = new File(batchInputFileType.getDirectoryPath());
        final File[] doneFiles = batchTypeDirectory.listFiles(new DoneFilenameFilter());

        final List<String> batchInputFiles = new ArrayList<>();
        for (final File doneFile : doneFiles) {
            String dataFileName = StringUtils.substringBeforeLast(doneFile.getPath(), ".");
            if (StringUtils.isNotBlank(batchInputFileType.getFileExtension())) {
                dataFileName += "." + batchInputFileType.getFileExtension();
            }

            final File dataFile = new File(dataFileName);
            if (dataFile.exists()) {
                batchInputFiles.add(dataFile.getPath());
            }
        }

        return batchInputFiles;
    }

    /**
     * For this implementation, a file user identifier must consist of letters and digits
     */
    @Override
    public boolean isFileUserIdentifierProperlyFormatted(final String fileUserIdentifier) {
        if (ObjectUtils.isNull(fileUserIdentifier)) {
            return false;
        }
        for (int i = 0; i < fileUserIdentifier.length(); i++) {
            final char c = fileUserIdentifier.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Retrieves files in a directory with the .done extension.
     */
    protected class DoneFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".done");
        }
    }
}
