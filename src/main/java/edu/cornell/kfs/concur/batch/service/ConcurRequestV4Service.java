package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;

public interface ConcurRequestV4Service {

    List<ConcurEventNotificationProcessingResultsDTO> processTravelRequests(String accessToken);

}
