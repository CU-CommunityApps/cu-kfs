package edu.cornell.kfs.concur.batch.service;

public interface ConcurEventNotificationV2WebserviceService {
    
    /*
     * Builds a JSON annotated java POJO from a concur web service endpoint
     */
    <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType, String logMessageDetail);

}
