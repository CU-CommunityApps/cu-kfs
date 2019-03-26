package edu.cornell.kfs.rass.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.rass.batch.service.RassService;

public class RassStep extends AbstractStep{
	
	private RassService rassService;

	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		rassService.readXML();
		rassService.updateKFS();
		return true;
	}

	public void setRassService(RassService rassService) {
		this.rassService = rassService;
	}

}
