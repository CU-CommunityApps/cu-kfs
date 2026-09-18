package edu.cornell.kfs.cemi.module.cam.batch.translatetable;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants.RegisterAssetTranslateTables;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractDao;

public class CemiRegisterAssetTranslateTableFactory {
    
    private static final Logger LOG = LogManager.getLogger();

    private RegisterAssetTranslateTables translationTableToCreate;
    private CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao;

    public static Map<String, String> createRegisterAssetTranslateTableFor(
            final RegisterAssetTranslateTables translationTableToCreate,
            final CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao) {
        final CemiRegisterAssetTranslateTableFactory factory = 
                new CemiRegisterAssetTranslateTableFactory(translationTableToCreate, cemiRegisterAssetExtractDao);

        switch (translationTableToCreate) {
            case ASSET_TYPE_CODE_QUERY:
                return factory.createAssetTypeMap();

            case ACCOUNTING_TREATMENT_QUERY:
                return factory.createAccountingTreatmentap();

            case ASSET_CLASS_QUERY:
                return factory.createAssetClassMap();

            case DEPRECIATION_PROFILE_QUERY:
                return factory.createDepreciationProfileMap();

            default:
                LOG.error("createRegisterAssetTranslateTableFor: missing case for {} ", translationTableToCreate.name());
        }
        return null;
    }

    public CemiRegisterAssetTranslateTableFactory (final RegisterAssetTranslateTables translationTableToCreate,
            final CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao) {
        this.translationTableToCreate = translationTableToCreate;
        this.cemiRegisterAssetExtractDao = cemiRegisterAssetExtractDao;
    }

    private Map<String, String> createAssetTypeMap() {
        return cemiRegisterAssetExtractDao.buildTranslationForTable(RegisterAssetTranslateTables.ASSET_TYPE_CODE_QUERY);
    }

    private Map<String, String> createAccountingTreatmentap() {
        return cemiRegisterAssetExtractDao.buildTranslationForTable(RegisterAssetTranslateTables.ACCOUNTING_TREATMENT_QUERY);
    }

    private Map<String, String> createAssetClassMap() {
        return cemiRegisterAssetExtractDao.buildTranslationForTable(RegisterAssetTranslateTables.ASSET_CLASS_QUERY);
    }

    private Map<String, String> createDepreciationProfileMap() {
        return cemiRegisterAssetExtractDao.buildTranslationForTable(RegisterAssetTranslateTables.DEPRECIATION_PROFILE_QUERY);
    }


}
