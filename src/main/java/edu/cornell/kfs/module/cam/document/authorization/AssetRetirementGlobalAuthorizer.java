package edu.cornell.kfs.module.cam.document.authorization;

import java.util.Map;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.module.cam.businessobject.AssetRetirementGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetRetirementGlobalDetail;
import org.kuali.kfs.sys.identity.KfsKimAttributes;

public class AssetRetirementGlobalAuthorizer extends MaintenanceDocumentAuthorizerBase {
    @Override
    protected void addRoleQualification(Object businessObject, Map<String, String> attributes) {
        super.addRoleQualification(businessObject, attributes);
        AssetRetirementGlobal assetRetirementGlobal;
        if (businessObject instanceof MaintenanceDocument) {
            assetRetirementGlobal = (AssetRetirementGlobal) ((MaintenanceDocument) businessObject).getNewMaintainableObject().getBusinessObject();
            for (AssetRetirementGlobalDetail assetRetirementGlobalDetail : assetRetirementGlobal.getAssetRetirementGlobalDetails()) {
                attributes.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, assetRetirementGlobalDetail.getAsset().getOrganizationOwnerChartOfAccountsCode());
                attributes.put(KfsKimAttributes.ORGANIZATION_CODE, assetRetirementGlobalDetail.getAsset().getOrganizationOwnerAccount().getOrganizationCode());
            }
        }
    }
}
