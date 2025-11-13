package edu.cornell.kfs.fp.batch.service;

public interface TravelMealCardEmailService {
    
    void sendErrorEmail(String toAddress, String subject, String message);
    
    String generateNewFileNotReceivedMessage();
    
    String generateNewFileNotReceivedSubject();
    
    String getFileNotReceivedRecipentEmailAddress();
    
    String generateNewFileProcessedSubject();
    
    String generateNewFileProcessedMessage();
    
    String getFileProcessedRecipentEmailAddress();
}
