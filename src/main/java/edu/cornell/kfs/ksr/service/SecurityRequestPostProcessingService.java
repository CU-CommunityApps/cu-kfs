package edu.cornell.kfs.ksr.service;

import edu.cornell.kfs.ksr.document.SecurityRequestDocument;

public interface SecurityRequestPostProcessingService {
    
    public void postProcessSecurityRequest(SecurityRequestDocument document);

}
