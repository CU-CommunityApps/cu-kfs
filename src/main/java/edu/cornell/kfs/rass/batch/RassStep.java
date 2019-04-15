package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.rass.RassConstants.RassResultCode;
import edu.cornell.kfs.rass.batch.service.RassService;

public class RassStep extends AbstractStep{

    private RassService rassService;
    private static final Logger LOG = LogManager.getLogger(RassStep.class);

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        List<RassXmlFileParseResult> parseResults = rassService.readXML();
        List<RassXmlFileParseResult> successfulResults = getSuccessfullyParsedFileResults(parseResults);
        List<RassXmlObjectGroupResult> processingResults = rassService.updateKFS(successfulResults);
        RassXmlReport report = new RassXmlReport(parseResults, processingResults);
        LOG.info("execute, the number of files found to process is: " + report.getFileParseResults().size());
        return true;
    }

    public List<RassXmlFileParseResult> getSuccessfullyParsedFileResults(List<RassXmlFileParseResult> parseResults) {
        return parseResults.stream()
                .filter(parseResult -> RassResultCode.SUCCESS.equals(parseResult.getResultCode()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setRassService(RassService rassService) {
        this.rassService = rassService;
    }

}
