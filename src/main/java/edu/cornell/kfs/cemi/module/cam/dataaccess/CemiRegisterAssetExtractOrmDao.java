package edu.cornell.kfs.cemi.module.cam.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.module.cam.businessobject.Asset;

// Refer to actual implementation in service class CemiEXTRACTNAMEExtractOrmDaoOjbImpl

public interface CemiRegisterAssetExtractOrmDao {

    Stream<Asset> getAssetsForCemiRegisterAssetExtractAsCloseableStream();

}
