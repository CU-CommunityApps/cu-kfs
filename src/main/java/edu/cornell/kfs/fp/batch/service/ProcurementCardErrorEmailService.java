package edu.cornell.kfs.fp.batch.service;

import java.util.ArrayList;

public interface ProcurementCardErrorEmailService {
	
	public void sendErrorEmail(ArrayList<String> errorMessages);

}
