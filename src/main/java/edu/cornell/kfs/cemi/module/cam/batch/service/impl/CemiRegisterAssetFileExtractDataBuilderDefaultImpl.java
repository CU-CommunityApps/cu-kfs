package edu.cornell.kfs.cemi.module.cam.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
//import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cam.businessobject.Asset;

import edu.cornell.kfs.cemi.module.cam.batch.businessobject.CemiRegisterAssetFileRegisterAssetTabRowBo;
import edu.cornell.kfs.cemi.module.cam.batch.service.CemiRegisterAssetFileExtractDataBuilder;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractDao;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.module.cam.businessobject.AssetExtension;

// The code from an existing data extract was left as comments in each method to provide specific examples.
// The constructor for this class will need to accept and verify as valid any and all services required to perform
// the data gathering logic. 
//
// Extended attributes may need to be retrieved. Depending on how the data mapping template is designed,
// cardinality may be across multiple tabs OR may be a single tab where parts of a row repeats with
// the unique portion of the data on the end of the row. Meaning parent-child relationships could span 
// tabs or could need to be dealt with on a single tab. This service implementation would deal with that complexity.
//
// This service would also perform the call to the business object factory/factories needed to appropriately store 
// the data rows in table(s) that represent the actual sheets of the data extract spreadsheet being created.

public class CemiRegisterAssetFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiRegisterAssetFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();
   
    protected CemiRegisterAssetExtractOrmDao cemiRegisterAssetExtractOrmDao;
    protected CemiRegisterAssetExtractDao cemiRegisterAssetExtractDao;
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;

    //
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
    }

    @Override
    public void writeRegisterAssetFileRegisterAssetTabExtractDataToIntermediateStorage(final Iterator<Asset> legacyObjects){
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

//
// EXAMPLE: 
// This is an actual example used by a data extract. The method is called by the public routine above to create
// and store to a database table a SINGLE row of information representing a data extraction spreadsheet line.
// Depending upon this method's logic and the data objects used, this method could generate MULTIPLE lines of
// information; therefore you will need to name this protected method accordingly.
//
    protected void createAndStoreRegisterAssetFileRegisterAssetTabRow(final Asset legacyObject, 
            final AssetExtension assetExtendedAttribute, final String jobRunDateString) {

        CemiRegisterAssetFileRegisterAssetTabRowBoFactory factoryForBo = 
                new CemiRegisterAssetFileRegisterAssetTabRowBoFactory(legacyObject, assetExtendedAttribute, jobRunDateString,
                        dateTimeService, maskSensitiveData);
        
        CemiRegisterAssetFileRegisterAssetTabRowBo registerAssetTabRow = factoryForBo.createCemiRegisterAssetFileRegisterAssetTabRowBo();
        storeSheetRow(registerAssetTabRow);
    }

}
