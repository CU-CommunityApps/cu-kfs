package edu.cornell.kfs.cemi.module.cam.batch.translatetable;

import java.util.Map;
import java.util.Objects;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants.RegisterAssetTranslateTables;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractDao;

public class CemiRegisterAssetTranslateTableFactory {

    private CemiRegisterAssetTranslateTableFactory() {
        throw new UnsupportedOperationException("do not call");
    }

    public static Map<String, String> createRegisterAssetTranslateTableFor(
            final RegisterAssetTranslateTables translationTableToCreate,
            final CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao) {
        Objects.requireNonNull(translationTableToCreate, "translationTableToCreate must not be null");
        Objects.requireNonNull(cemiRegisterAssetExtractDao, "cemiRegisterAssetExtractDao must not be null");
        return cemiRegisterAssetExtractDao.buildTranslationForTable(translationTableToCreate);
    }

}
