package edu.cornell.kfs.module.cam.document.service;

import org.kuali.kfs.module.cam.businessobject.AssetGlpeSourceDetail;

public interface CuAssetSubAccountService {

    /**
     * Clears out the sub-account number on the source detail if the account number matches a particular pattern.
     * In the event that a badly-formatted pattern causes an exception while matching, the source detail will be left as-is.
     *
     * This was separated into its own method for unit testing convenience.
     *
     * @param postable The GLPE source detail whose sub-account number may need to be cleared based on account number.
     */
    public void clearSubAccountIfNecessary(AssetGlpeSourceDetail postable);

}
