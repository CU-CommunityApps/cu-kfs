package edu.cornell.kfs.sys.batch;

import edu.cornell.kfs.sys.batch.service.CreateKualiDeveloperXmlService;

import org.kuali.kfs.sys.batch.AbstractStep;

import java.util.Date;

public class KualiDeveloperXmlFileStep extends AbstractStep {

    private CreateKualiDeveloperXmlService createKualiDeveloperXmlService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        createKualiDeveloperXmlService.createKualiDevelopersFromXml();
        return true;
    }

    public CreateKualiDeveloperXmlService getCreateKualiDeveloperXmlService(){
        return createKualiDeveloperXmlService;
    }

    public void setCreateKualiDeveloperXmlService(CreateKualiDeveloperXmlService createKualiDeveloperXmlService) {
        this.createKualiDeveloperXmlService = createKualiDeveloperXmlService;
    }

}
