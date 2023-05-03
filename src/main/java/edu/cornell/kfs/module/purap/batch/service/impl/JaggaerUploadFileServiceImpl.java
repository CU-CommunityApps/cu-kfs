package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.module.purap.batch.service.JaggaerUploadFileService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class JaggaerUploadFileServiceImpl implements JaggaerUploadFileService {
    private static final Logger LOG = LogManager.getLogger();

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType jaggaerUploadFileType;
    protected FileStorageService fileStorageService;

    public void uploadSupplierXMLFiles() {
        List<String> fileNamesToUpload = batchInputFileService.listInputFileNamesWithDoneFile(jaggaerUploadFileType);
        LOG.info("uploadSupplierXMLFiles, found {} files to upload", fileNamesToUpload.size());

        for (String fileName : fileNamesToUpload) {
            LOG.info("uploadSupplierXMLFiles. processing {}", fileName);
            byte[] uploadFileData = LoadFileUtils.safelyLoadFileBytes(fileName);
            uploadFile(uploadFileData);
        }
        fileStorageService.removeDoneFiles(fileNamesToUpload);
    }

    /*
     * @todo implement this with KFSPTS-28268
     */
    protected void uploadFile(byte[] uploadFileData) {
        LOG.info("uploadFile, will eventually send a file with {} bytes of data to Jaggaer",
                ArrayUtils.getLength(uploadFileData));
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setJaggaerUploadFileType(BatchInputFileType jaggaerUploadFileType) {
        this.jaggaerUploadFileType = jaggaerUploadFileType;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

}
