package edu.cornell.kfs.rass.batch;

import java.util.Date;
import java.util.List;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.rass.batch.service.RassService;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;

public class RassStep extends AbstractStep{
	
	private RassService rassService;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
	    List<RassXmlDocumentWrapper> rassXmlDocumentWrappers = rassService.readXML();
	    rassService.updateKFS(rassXmlDocumentWrappers);
		return true;
	}

	public void setRassService(RassService rassService) {
		this.rassService = rassService;
	}

}
