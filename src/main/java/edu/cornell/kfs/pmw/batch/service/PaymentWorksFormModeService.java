package edu.cornell.kfs.pmw.batch.service;

public interface PaymentWorksFormModeService {
    
    boolean shouldUseForeignFormProcessingMode();
    
    boolean shouldUseLegacyFormProcessingMode();
    
    String getFormModeDescription();

}
