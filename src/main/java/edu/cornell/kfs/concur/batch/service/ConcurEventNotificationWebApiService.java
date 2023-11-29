package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.ConcurWebRequest;
import edu.cornell.kfs.concur.exception.ConcurWebserviceException;

public interface ConcurEventNotificationWebApiService {
    
    /*
     * Builds a JSON annotated java POJO from a concur web service endpoint
     */
    <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType, String logMessageDetail) throws ConcurWebserviceException;

    <T> T callConcurEndpoint(String accessToken, ConcurWebRequest<T> webRequest, String logMessageDetail) throws ConcurWebserviceException;

}
