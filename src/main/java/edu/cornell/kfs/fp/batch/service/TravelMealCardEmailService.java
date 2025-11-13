package edu.cornell.kfs.fp.batch.service;

import java.util.List;

public interface TravelMealCardEmailService {
    
    void sendErrorEmail(String toAddress, String subject, String message);
    
    void sendReportEmail(String fromAddress, List<String> toAddresses);
    
    String generateNewFileNotReceivedMessage();
    
    String generateNewFileNotReceivedSubject();
    
    String getFileNotReceivedRecipentEmailAddress();
}
