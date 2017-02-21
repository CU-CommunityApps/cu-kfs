package edu.cornell.kfs.concur.batch.service.impl;

import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileService;

public class ConcurRequestExtractCreatePdpFeedServiceImpl implements ConcurRequestExtractCreatePdpFeedService {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpFeedServiceImpl.class);
	protected ConcurRequestExtractFileService concurRequestExtractFileService;

	@Override
    public void createPdpFeedsFromRequestExtracts() {
        /* Get list of all Request Extract files that have not been processed into PDP Import files
         * If there are files to process then
         *     Loop through list
         *        call method this.createPdpFeedFromRequestExtract() to process each file
         * Else
         *     report-log no files found to process
         */
    }

    private void createPdpFeedFromRequestExtract() {
        /* Validate request extract header row to file contents
         * If request extract file header validates to file contents then
         *     Do not process line one of file as it is the header row
         *     Prepare PDP feed file to receive output data
         *     While there are lines of data in request extract file
         *         If data line is REQUEST DETAIL line
         *             processRequestDetailLine()
         *         Else
         *             bypass that data line
         *     End-loop
         *     Perform final tasks for Request Extract file
         *     Perform final tasks for PDP feed file
         * Else
         *   Perform bad Request Extract file tasks
         */
    }

    private void processRequestDetailLine() {
        /* Validate individual pieces of data performing error processing as necessary
         * Validate report# / emplid / amount combination has not been previously paid performing error processing as necessary
         * If no errors encountered
         *     Add Request Detail data to PDP feed file
         */
    }

    public void setConcurRequestExtractFileService(ConcurRequestExtractFileService concurRequestExtractFileService) {
        this.concurRequestExtractFileService = concurRequestExtractFileService;       
    }

    public ConcurRequestExtractFileService getConcurRequestExtractFileService() {
        return concurRequestExtractFileService;
    }
}
