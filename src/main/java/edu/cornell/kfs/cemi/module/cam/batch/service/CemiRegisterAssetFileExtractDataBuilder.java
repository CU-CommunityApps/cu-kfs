package edu.cornell.kfs.cemi.module.cam.batch.service;

import java.util.Iterator;

import org.kuali.kfs.module.cam.businessobject.Asset;

public interface CemiRegisterAssetFileExtractDataBuilder {

    void writeRegisterAssetFileRegisterAssetTabExtractDataToIntermediateStorage(final Iterator<Asset> legacyObjects);

}
