package edu.cornell.kfs.concur.service;

import java.util.Date;

public interface ConcurAccessTokenService {
    
    String getAccessToken();

    Date getAccessTokenExpirationDate();
    
    void requestNewAccessToken();

    void refreshAccessToken();

    void revokeAndReplaceAccessToken();
    
    void revokeAccessToken();
    
    void resetTokenToEmptyStringInDataStorage();

    boolean currentAccessTokenExists();

    boolean isAccessTokenRefreshEnabled();

}