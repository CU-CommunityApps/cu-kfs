package edu.cornell.kfs.module.cam.document.service.impl;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.cam.businessobject.AssetGlpeSourceDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.document.AssetTransferDocument;
import org.kuali.kfs.module.cam.document.service.impl.AssetTransferServiceImpl;
import edu.cornell.kfs.module.cam.document.service.CuAssetSubAccountService;

public class CuAssetTransferServiceImpl extends AssetTransferServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetTransferServiceImpl.class);

    protected CuAssetSubAccountService cuAssetSubAccountService;

    /**
     * Overridden to forcibly set the sub-account number on the generated postable object to null, if the account number
     * is flagged as being one that should not have a sub-account number defined on related source details.
     * The "ASSET_PLANT_ACCOUNTS_TO_FORCE_CLEARING_OF_GLPE_SUB_ACCOUNTS" parameter governs whether such clearing behavior should be applied
     * see CuAssetSubAccountService.shouldPreserveSubAccount() for details.
     * 
     * @see org.kuali.kfs.module.cam.document.service.impl.AssetTransferServiceImpl#createAssetGlpePostable(
     * org.kuali.kfs.module.cam.document.AssetTransferDocument, org.kuali.kfs.coa.businessobject.Account,
     * org.kuali.kfs.module.cam.businessobject.AssetPayment, boolean, org.kuali.kfs.module.cam.document.service.impl.AssetTransferServiceImpl.AmountCategory)
     */
    @Override
    protected AssetGlpeSourceDetail createAssetGlpePostable(
            AssetTransferDocument document, Account plantAccount, AssetPayment assetPayment, boolean isSource, AmountCategory amountCategory) {
        AssetGlpeSourceDetail postable = super.createAssetGlpePostable(document, plantAccount, assetPayment, isSource, amountCategory);
        getCuAssetSubAccountService().clearSubAccountIfNecessary(postable);
        return postable;
    }

    public void setCuAssetSubAccountService(CuAssetSubAccountService cuAssetSubAccountService) {
        this.cuAssetSubAccountService = cuAssetSubAccountService;
    }

    public CuAssetSubAccountService getCuAssetSubAccountService() {
        return this.cuAssetSubAccountService;
    }

}
