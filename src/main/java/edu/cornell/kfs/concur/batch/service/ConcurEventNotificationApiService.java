package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.ConcurWebRequest;

public interface ConcurEventNotificationApiService {
    
    /*
     * Builds a JSON annotated java POJO from a concur web service endpoint
     */
    <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType, String logMessageDetail);

    <T> T callConcurEndpoint(String accessToken, ConcurWebRequest<T> webRequest, String logMessageDetail);

}
