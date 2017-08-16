package edu.cornell.kfs.module.cam.document.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.document.service.impl.AssetServiceImpl;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.impl.BusinessObjectServiceImpl;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.api.parameter.ParameterType;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.ParameterServiceImpl;

import edu.cornell.kfs.module.cam.CuCamsConstants;

import org.easymock.EasyMock;

public class CuAssetServiceImplTest {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetServiceImpl.class);
    
    private static final String NON_EXISTING_TAG_NUMBER = "non-existing tag number";
    private static final String EXISTING_TAG_NUMBER = "existing tag";
    private static final String RETIRED_STATUS_CD = "R";
    private static final String NOT_RETIRED_STATUS_CD = "NR";
    private CuAssetServiceImpl cuAssetServiceImpl;

    @Before
    public void setUp() {
        cuAssetServiceImpl = new CuAssetServiceImpl();
    }

    @Test
    public void testFindActiveAssetsMatchingTagNumberNoMatches() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectServiceToReturnNoAssets());
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowReiredAssets());

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(NON_EXISTING_TAG_NUMBER);

        assertTrue("There should have been no Assets matching the given tag number", 0 == resultsArray.size());
    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsAllowedMatchingRetiredAsset() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectServiceToReturnRetiredAsset());
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowReiredAssets());

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been matching assets when retired assets are allowed", 0 != resultsArray.size());

    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsAllowedMatchingNotRetiredAsset() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectServiceToReturnNotRetiredAsset());
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowReiredAssets());

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been matching assets when retired assets are allowed", 0 != resultsArray.size());

    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsDisallowedNotMatching() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectServiceToReturnRetiredAsset());
        cuAssetServiceImpl.setParameterService(createMockParameterServiceDisallowReiredAssets());

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been no matching assets when retired assets are not allowed", 0 == resultsArray.size());

    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsDisallowedMatching() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectServiceToReturnNotRetiredAsset());
        cuAssetServiceImpl.setParameterService(createMockParameterServiceDisallowReiredAssets());

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been matching assets when retired assets are not allowed and non retired assets exist", 0 != resultsArray.size());

    }

    protected ParameterService createMockParameterServiceAllowReiredAssets() {
        ParameterService parameterService = EasyMock.createMock(ParameterServiceImpl.class);
        EasyMock.expect(parameterService.getParameterValueAsBoolean(CamsConstants.CAM_MODULE_CODE, "Asset", CuCamsConstants.Parameters.RE_USE_RETIRED_ASSET_TAG_NUMBER, Boolean.FALSE)).andStubReturn(true);
        List<String> statusCodes = new ArrayList<String>();
        statusCodes.add(RETIRED_STATUS_CD);
        EasyMock.expect(parameterService.getParameterValuesAsString(Asset.class, CamsConstants.Parameters.RETIRED_STATUS_CODES)).andStubReturn(statusCodes);

        EasyMock.replay(parameterService);
        return parameterService;
    }
    
    protected ParameterService createMockParameterServiceDisallowReiredAssets() {
        ParameterService parameterService = EasyMock.createMock(ParameterServiceImpl.class);
        EasyMock.expect(parameterService.getParameterValueAsBoolean(CamsConstants.CAM_MODULE_CODE, "Asset", CuCamsConstants.Parameters.RE_USE_RETIRED_ASSET_TAG_NUMBER, Boolean.FALSE)).andStubReturn(false);
        List<String> statusCodes = new ArrayList<String>();
        statusCodes.add(RETIRED_STATUS_CD);
        EasyMock.expect(parameterService.getParameterValuesAsString(Asset.class, CamsConstants.Parameters.RETIRED_STATUS_CODES)).andStubReturn(statusCodes);

        EasyMock.replay(parameterService);
        return parameterService;
    }
    
    protected BusinessObjectService createMockBusinessObjectServiceToReturnNoAssets() {
        BusinessObjectService businessObjectService = EasyMock.createMock(BusinessObjectServiceImpl.class);
        Map<String, String> params = new HashMap<String, String>();
        params.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER, NON_EXISTING_TAG_NUMBER);
        EasyMock.expect(businessObjectService.findMatching(Asset.class, params)).andStubReturn(null);
        
        EasyMock.replay(businessObjectService);
        return businessObjectService;
    }
    
    protected BusinessObjectService createMockBusinessObjectServiceToReturnRetiredAsset() {
        BusinessObjectService businessObjectService = EasyMock.createMock(BusinessObjectServiceImpl.class);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER, EXISTING_TAG_NUMBER);
        List<Asset> result = new ArrayList<Asset>();
        result.add(createRetiredAsset());
        EasyMock.expect(businessObjectService.findMatching(Asset.class, params)).andStubReturn(result);
        
        EasyMock.replay(businessObjectService);
        return businessObjectService;
    }
    
    protected BusinessObjectService createMockBusinessObjectServiceToReturnNotRetiredAsset() {
        BusinessObjectService businessObjectService = EasyMock.createMock(BusinessObjectServiceImpl.class);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER, EXISTING_TAG_NUMBER);
        List<Asset> result = new ArrayList<Asset>();
        result.add(createNotRetiredAsset());
        EasyMock.expect(businessObjectService.findMatching(Asset.class, params)).andStubReturn(result);
        
        EasyMock.replay(businessObjectService);
        return businessObjectService;
    }
    
    protected Asset createRetiredAsset(){
        Asset asset = new Asset();
        asset.setInventoryStatusCode(RETIRED_STATUS_CD);
        return asset;
    }
    
    protected Asset createNotRetiredAsset(){
        Asset asset = new Asset();
        asset.setInventoryStatusCode(NOT_RETIRED_STATUS_CD);
        return asset;
    }

}
