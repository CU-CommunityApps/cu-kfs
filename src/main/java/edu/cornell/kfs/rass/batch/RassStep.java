package edu.cornell.kfs.rass.batch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.batch.service.RassReportService;
import edu.cornell.kfs.rass.batch.service.RassService;

public class RassStep extends AbstractStep {

    private RassService rassService;
    private RassReportService rassReportService;
    
    private static final Logger LOG = LogManager.getLogger(RassStep.class);

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        List<RassXmlFileParseResult> parseResults = rassService.readXML();
        if (CollectionUtils.isEmpty(parseResults)) {
            LOG.info("execute, Skipping XML processing because no pending RASS XML files were found");
            return true;
        }
        List<RassXmlFileParseResult> successfulResults = findSuccessfullyParsedFileResults(parseResults);
        Map<String, RassXmlFileProcessingResult> processingResults = rassService.updateKFS(successfulResults);
        RassBatchJobReport report = new RassBatchJobReport(parseResults, processingResults);
        rassReportService.writeBatchJobReports(report);
        return true;
    }

    protected List<RassXmlFileParseResult> findSuccessfullyParsedFileResults(List<RassXmlFileParseResult> parseResults) {
        return parseResults.stream()
                .filter(parseResult -> RassParseResultCode.SUCCESS.equals(parseResult.getResultCode()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setRassService(RassService rassService) {
        this.rassService = rassService;
    }

    public void setRassReportService(RassReportService rassReportService) {
        this.rassReportService = rassReportService;
    }

}
