package edu.cornell.kfs.concur.service;


public interface ConcurAccessTokenService {
    
    String getAccessToken();

    String getAccessTokenExpirationDate();
    
    void requestNewAccessToken();

    void refreshAccessToken();

    void revokeAndReplaceAccessToken();
    
    void revokeAccessToken();
    
    void resetTokenToEmptyStringInDataStorage();

    boolean currentAccessTokenExists();

    boolean isAccessTokenRefreshEnabled();

}