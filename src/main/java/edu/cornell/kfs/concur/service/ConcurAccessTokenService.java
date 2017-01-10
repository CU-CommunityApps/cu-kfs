package edu.cornell.kfs.concur.service;


public interface ConcurAccessTokenService {
    
    String getConcurAccessToken();
    
    String getConcurRefreshToken();
    
    String getConcurConsumerKey();
    
    String getConcurSecretKey();
    
    void refreshAccessToken();

}