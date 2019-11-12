package edu.cornell.kfs.module.cam.document.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.api.parameter.ParameterType;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.ParameterServiceImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.cornell.kfs.module.cam.CuCamsConstants;


public class CuAssetServiceImplTest {
    
    private static final String NON_EXISTING_TAG_NUMBER = "non-existing tag number";
    private static final String EXISTING_TAG_NUMBER = "existing tag number";
    private static final String RETIRED_STATUS_CD = "R";
    private static final String NON_RETIRED_STATUS_CD = "NR";
    
    private CuAssetServiceImpl cuAssetServiceImpl;

    @Before
    public void setUp() {
        cuAssetServiceImpl = new CuAssetServiceImpl();
    }

    @Test
    public void testFindActiveAssetsMatchingTagNumberNoMatches() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectService(NON_EXISTING_TAG_NUMBER, null));
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowRetiredAssets(true));

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(NON_EXISTING_TAG_NUMBER);

        assertTrue("There should have been no Assets matching the given tag number", 0 == resultsArray.size());
    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsAllowedMatchingRetiredAsset() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectService(EXISTING_TAG_NUMBER, createAssetWithGivenInventoryStatusCode(RETIRED_STATUS_CD)));
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowRetiredAssets(true));

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been matching assets when retired assets are allowed and a retired asset was retrieved", 0 != resultsArray.size());
    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsAllowedMatchingNotRetiredAsset() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectService(EXISTING_TAG_NUMBER, createAssetWithGivenInventoryStatusCode(NON_RETIRED_STATUS_CD)));
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowRetiredAssets(true));

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been matching assets when retired assets are allowed and a non-retired asset was retrieved", 0 != resultsArray.size());
    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsDisallowedNotMatchingRetiredAsset() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectService(EXISTING_TAG_NUMBER, createAssetWithGivenInventoryStatusCode(RETIRED_STATUS_CD)));
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowRetiredAssets(false));

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been no matching assets when retired assets are not allowed", 0 == resultsArray.size());
    }
    
    @Test
    public void testFindActiveAssetsMatchingTagNumberRetiredAssetsDisallowedMatchingNonRetiredAsset() {
        cuAssetServiceImpl.setBusinessObjectService(createMockBusinessObjectService(EXISTING_TAG_NUMBER, createAssetWithGivenInventoryStatusCode(NON_RETIRED_STATUS_CD)));
        cuAssetServiceImpl.setParameterService(createMockParameterServiceAllowRetiredAssets(false));

        List<Asset> resultsArray = cuAssetServiceImpl.findActiveAssetsMatchingTagNumber(EXISTING_TAG_NUMBER);

        assertTrue("There should have been matching assets when retired assets are not allowed and non retired assets exist", 0 != resultsArray.size());
    }

    protected ParameterService createMockParameterServiceAllowRetiredAssets(boolean result) {
        ParameterService parameterService = mock(ParameterServiceImpl.class);
        when(parameterService.getParameterValueAsBoolean(CamsConstants.CAM_MODULE_CODE, "Asset", CuCamsConstants.Parameters.RE_USE_RETIRED_ASSET_TAG_NUMBER, Boolean.FALSE)).thenReturn(result);
        List<String> statusCodes = new ArrayList<String>();
        statusCodes.add(RETIRED_STATUS_CD);
        when(parameterService.getParameterValuesAsString(Asset.class, CamsConstants.InventoryStatusCode.CAPITAL_ASSET_RETIRED)).thenReturn(statusCodes);

        return parameterService;
    }
    
    protected BusinessObjectService createMockBusinessObjectService(String tagNumber, List<Asset> result) {
        BusinessObjectService businessObjectService = mock(BusinessObjectService.class);
        Map<String, String> params = new HashMap<String, String>();
        params.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER, tagNumber);
        when(businessObjectService.findMatching(Asset.class, params)).thenReturn(result);

        return businessObjectService;
    }
    
    protected List<Asset> createAssetWithGivenInventoryStatusCode(String inventoryStatusCode){
        List<Asset> assetsList = new ArrayList<Asset>();
        Asset asset = new Asset();
        asset.setInventoryStatusCode(inventoryStatusCode);
        assetsList.add(asset);
        return assetsList;
    }

}