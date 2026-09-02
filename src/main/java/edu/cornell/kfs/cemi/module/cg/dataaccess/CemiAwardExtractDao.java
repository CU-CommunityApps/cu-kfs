package edu.cornell.kfs.cemi.module.cg.dataaccess;

import java.util.Map;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;

public interface CemiAwardExtractDao {
    
    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    
    void updateAwardScheduleExtractDependentQuerySettings(final String awardScheduleJobRunDate);
   
    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    
    boolean awardScheduleContainsAwardExtractBuiltReferenceId(String awardExtractionBuiltAwardScheduleReferenceId);
    
    Map<String, String> buildTranslationForTable(AwardTranslateTables queryString);
    
    String findOrganizationCodeForInScopeAward(String inScopeAwardForPrimaryOrganizationLookup);

}
