package edu.cornell.kfs.concur.service;


public interface ConcurAccessTokenService {
    
    String getAccessToken();
    
    void requestNewAccessToken();

    void refreshAccessToken();

    void revokeAndReplaceAccessToken();
    
    void revokeAccessToken();
    
    void resetTokenToEmptyStringInDatabase();

    boolean currentAccessTokenExists();

}