package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;

public interface ConcurRequestV4Service {

    List<ConcurEventNotificationResponse> processTravelRequests(String accessToken);

}
