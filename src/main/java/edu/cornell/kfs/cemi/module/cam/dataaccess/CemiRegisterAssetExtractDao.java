package edu.cornell.kfs.cemi.module.cam.dataaccess;

import java.util.Map;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants.RegisterAssetTranslateTables;

public interface CemiRegisterAssetExtractDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    
    Map<String, String> buildTranslationForTable(RegisterAssetTranslateTables queryString);

}
