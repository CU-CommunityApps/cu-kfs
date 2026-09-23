package edu.cornell.kfs.cemi.module.cam.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.module.cam.businessobject.Asset;

public interface CemiRegisterAssetExtractOrmDao {

    Stream<Asset> getAssetsForCemiRegisterAssetExtractAsCloseableStream();

}
