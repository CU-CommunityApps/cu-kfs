package edu.cornell.kfs.cemi.module.cam.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cam.businessobject.Asset;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants.RegisterAssetTranslateTables;
import edu.cornell.kfs.cemi.module.cam.batch.businessobject.CemiRegisterAssetFileRegisterAssetTabRowBo;
import edu.cornell.kfs.cemi.module.cam.batch.service.CemiRegisterAssetFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cam.batch.translatetable.CemiRegisterAssetTranslateTableFactory;
import edu.cornell.kfs.cemi.module.cam.batch.translatetable.CemiRegisterAssetTranslateTableMaps;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractDao;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.module.cam.businessobject.AssetExtension;

public class CemiRegisterAssetFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiRegisterAssetFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected CemiRegisterAssetExtractOrmDao cemiRegisterAssetExtractOrmDao;
    protected CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;
    protected CemiRegisterAssetTranslateTableMaps allRegisterAssetTranslateTableMaps;

    public CemiRegisterAssetFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final CemiRegisterAssetExtractOrmDao cemiRegisterAssetExtractOrmDao,
            final CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiRegisterAssetFileRegisterAssetTabRowBo.class);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        Validate.notNull(cemiRegisterAssetExtractOrmDao, "CemiRegisterAssetExtractOrmDao cannot be null");
        Validate.notNull(cemiRegisterAssetExtractDao, "cemiRegisterAssetExtractDao cannot be null");
        this.dateTimeService = dateTimeService;
        this.cemiRegisterAssetExtractOrmDao = cemiRegisterAssetExtractOrmDao;
        this.cemiRegisterAssetExtractDao = cemiRegisterAssetExtractDao;
        this.maskSensitiveData = maskSensitiveData;
        populateAllRegisterAssetTranslateTableMaps();
    }

    @Override
    public void writeRegisterAssetFileRegisterAssetTabExtractDataToIntermediateStorage(final Iterator<Asset> legacyObjects) {
        int registerAssetTabRowCount = 0;
        for (final Asset asset : IteratorUtils.asIterable(legacyObjects)) {
            registerAssetTabRowCount++;
            if (registerAssetTabRowCount % 1000 == 0) {
                LOG.info("writeRegisterAssetFileRegisterAssetTabExtractDataToIntermediateStorage, Processed {} "
                        + "Assets for Register Asset and counting...", registerAssetTabRowCount);
            }
            //Register Asset Tab
            AssetExtension assetExtendedAttribute = (AssetExtension) asset.getExtension();
            
            //Database table storage of data extract
            createAndStoreRegisterAssetFileRegisterAssetTabRow(asset, assetExtendedAttribute, jobRunDateString);
        }
        LOG.info("writeRegisterAssetFileRegisterAssetTabExtractDataToIntermediateStorage, Finished writing {} "
                + "Assets for Register Asset", registerAssetTabRowCount);
    }

    protected void createAndStoreRegisterAssetFileRegisterAssetTabRow(final Asset legacyObject, 
            final AssetExtension assetExtendedAttribute, final String jobRunDateString) {

        CemiRegisterAssetFileRegisterAssetTabRowBoFactory factoryForBo = 
                new CemiRegisterAssetFileRegisterAssetTabRowBoFactory(legacyObject, assetExtendedAttribute, jobRunDateString,
                        dateTimeService, maskSensitiveData, allRegisterAssetTranslateTableMaps);
        
        CemiRegisterAssetFileRegisterAssetTabRowBo registerAssetTabRow = factoryForBo.createCemiRegisterAssetFileRegisterAssetTabRowBo();
        storeSheetRow(registerAssetTabRow);
    }
    
    private void populateAllRegisterAssetTranslateTableMaps() {
        allRegisterAssetTranslateTableMaps = new CemiRegisterAssetTranslateTableMaps();

        allRegisterAssetTranslateTableMaps.setAssetTypeMap(CemiRegisterAssetTranslateTableFactory.createRegisterAssetTranslateTableFor(
                RegisterAssetTranslateTables.ASSET_TYPE_CODE_QUERY, cemiRegisterAssetExtractDao));

        allRegisterAssetTranslateTableMaps.setAccountingTreatmentMap(CemiRegisterAssetTranslateTableFactory.createRegisterAssetTranslateTableFor(
                RegisterAssetTranslateTables.ACCOUNTING_TREATMENT_QUERY, cemiRegisterAssetExtractDao));

        allRegisterAssetTranslateTableMaps.setAssetClassMap(CemiRegisterAssetTranslateTableFactory.createRegisterAssetTranslateTableFor(
                RegisterAssetTranslateTables.ASSET_CLASS_QUERY, cemiRegisterAssetExtractDao));

        allRegisterAssetTranslateTableMaps.setDepreciationProfileMap(CemiRegisterAssetTranslateTableFactory.createRegisterAssetTranslateTableFor(
                RegisterAssetTranslateTables.DEPRECIATION_PROFILE_QUERY, cemiRegisterAssetExtractDao));
    }


}
