package edu.cornell.kfs.cemi.module.cg.batch.translatetable;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;

public class CemiAwardTranslateTableFactory {
    
    private static final Logger LOG = LogManager.getLogger();

    private AwardTranslateTables translationTableToCreate;
    private CemiAwardExtractDao cemiAwardExtractDao;
    
    public static Map<String, String> createAwardTranslateTableFor(
            final AwardTranslateTables translationTableToCreate,
            final CemiAwardExtractDao cemiAwardExtractDao) {
        final CemiAwardTranslateTableFactory factory = 
                new CemiAwardTranslateTableFactory(translationTableToCreate, cemiAwardExtractDao);
        
        switch (translationTableToCreate) {
            case SPONSOR_AWARD_TYPES_QUERY:
                return factory.createSponsorAwardTypesMap();
                
            case AWARD_PURPOSE_QUERY:
                return factory.createAwardPurposeMapMap();

            case AWARD_LINE_LIFECYCLE_STATUS_QUERY:
                return factory.createAwardLineLifecycleStatusMap();

            case AWARD_LINE_TYPES_QUERY:
                return factory.createAwardLineTypesMap();

            default:
                LOG.error("createAwardTranslateTableFor: missing case for {} ", translationTableToCreate.name());
        }
        return null;
    }
    
    public CemiAwardTranslateTableFactory (final AwardTranslateTables translationTableToCreate,
            final CemiAwardExtractDao cemiAwardExtractDao) {
        this.translationTableToCreate = translationTableToCreate;
        this.cemiAwardExtractDao = cemiAwardExtractDao;
    }
    
    private Map<String, String> createSponsorAwardTypesMap() {
        return cemiAwardExtractDao.buildTranslationForTable(AwardTranslateTables.SPONSOR_AWARD_TYPES_QUERY);
    }
    
    private Map<String, String> createAwardPurposeMapMap() {
        return cemiAwardExtractDao.buildTranslationForTable(AwardTranslateTables.AWARD_PURPOSE_QUERY);
    }
    
    private Map<String, String> createAwardLineLifecycleStatusMap() {
        return cemiAwardExtractDao.buildTranslationForTable(AwardTranslateTables.AWARD_LINE_LIFECYCLE_STATUS_QUERY);
    }
    
    private Map<String, String> createAwardLineTypesMap() {
        return cemiAwardExtractDao.buildTranslationForTable(AwardTranslateTables.AWARD_LINE_TYPES_QUERY);
    }
   
}
