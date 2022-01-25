package edu.cornell.kfs.concur.batch.service;

public interface ConcurEventNotificationV2WebserviceService {
    
    <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType, String logMessageDetail);

}
