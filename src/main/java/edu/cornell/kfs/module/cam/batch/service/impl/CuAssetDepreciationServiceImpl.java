package edu.cornell.kfs.module.cam.batch.service.impl;

import java.text.ParseException;

import org.kuali.kfs.module.cam.batch.service.impl.AssetDepreciationServiceImpl;

public class CuAssetDepreciationServiceImpl extends AssetDepreciationServiceImpl {

	@Override
	protected boolean runAssetDepreciation() throws ParseException {
		return true;
	}
}
