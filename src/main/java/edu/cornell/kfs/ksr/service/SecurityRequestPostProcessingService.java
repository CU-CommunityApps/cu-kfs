package edu.cornell.kfs.ksr.service;

import edu.cornell.kfs.ksr.document.SecurityRequestDocument;

/**
 * Handles processing of a <code>SecurityRequestDocument</code> instance after becoming final (all approvals have
 * occurred)
 * 
 * <p>
 * From the security request document, the following updates could be made in KIM:
 * 
 * <ul>
 * <li>Principal added as a role member</li>
 * <li>Principal removed as a role member</li>
 * <li>Principal's qualifications for a qualified role adjusted (added, modified, removed)</li>
 * </ul>
 * </p>
 * 
 * @author rSmart Development Team
 */
public interface SecurityRequestPostProcessingService {
    
    /**
     * Invoked once a <code>SecurityRequestDocument</code> has become final to process the updates to KIM
     * 
     * @param document
     *            - SecurityRequestDocument instance to process
     */
    public void postProcessSecurityRequest(SecurityRequestDocument document);

}
