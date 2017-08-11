package edu.cornell.kfs.concur.service;


public interface ConcurAccessTokenService {
    
    String getAccessToken();
    
    String getRefreshToken();
    
    String getConsumerKey();
    
    String getSecretKey();

    String getLoginUsername();

    String getLoginPassword();

    void requestNewAccessToken();

    void refreshAccessToken();

    void revokeAndReplaceAccessToken();
    
    void revokeAccessToken();
    
    void resetTokenToEmptyStringInDatabase();

    boolean currentAccessTokenExists();

}