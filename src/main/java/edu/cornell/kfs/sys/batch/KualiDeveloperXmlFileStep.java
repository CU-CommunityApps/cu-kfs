package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.sys.batch.service.KualiDeveloperXmlService;

public class KualiDeveloperXmlFileStep extends AbstractStep {

    private KualiDeveloperXmlService kualiDeveloperXmlService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        kualiDeveloperXmlService.createKualiDevelopersFromXml();
        return true;
    }

    public KualiDeveloperXmlService getKualiDeveloperXmlService(){
        return kualiDeveloperXmlService;
    }

    public void setKualiDeveloperXmlService(KualiDeveloperXmlService kualiDeveloperXmlService) {
        this.kualiDeveloperXmlService = kualiDeveloperXmlService;
    }

}
