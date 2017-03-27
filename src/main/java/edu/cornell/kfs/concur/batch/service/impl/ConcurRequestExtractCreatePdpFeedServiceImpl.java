package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Collections;
import java.util.List;

import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileService;

public class ConcurRequestExtractCreatePdpFeedServiceImpl implements ConcurRequestExtractCreatePdpFeedService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpFeedServiceImpl.class);
    protected FileStorageService fileStorageService;
    protected ConcurRequestExtractFileService concurRequestExtractFileService;

    @Override
    public void createPdpFeedsFromRequestExtracts() {
        List<String> filesToProcess = getConcurRequestExtractFileService().getUnprocessedRequestExtractFiles();
        if (filesToProcess.isEmpty()) {
            LOG.info("No Request Extract files found to process.");
        } else {
            LOG.info("Found " + filesToProcess.size() + " file(s) to process.");

            for (String requestExtractFullyQualifiedFileName : filesToProcess) {
                try {
                    LOG.info("Begin processing for filename: " + requestExtractFullyQualifiedFileName + ".");
                    if (getConcurRequestExtractFileService().processFile(requestExtractFullyQualifiedFileName)) {
                        LOG.info("SUCCESSFUL processing for Request Extract File: " + requestExtractFullyQualifiedFileName);
                    }
                    else {
                        LOG.error("Processing was UNSUCCESSFUL for Request Extract File: " + requestExtractFullyQualifiedFileName);
                    }
                } catch (Exception e) {
                    LOG.error("Processing to create PDP Files from Request Extract [" + requestExtractFullyQualifiedFileName + "] genertated Exception: " + e.getMessage());
                } finally {
                    getFileStorageService().removeDoneFiles(Collections.singletonList(requestExtractFullyQualifiedFileName));
                }
            }
        }
    }

    public void setConcurRequestExtractFileService(ConcurRequestExtractFileService concurRequestExtractFileService) {
        this.concurRequestExtractFileService = concurRequestExtractFileService;
    }

    public ConcurRequestExtractFileService getConcurRequestExtractFileService() {
        return concurRequestExtractFileService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

}
