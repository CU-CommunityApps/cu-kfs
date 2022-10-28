package edu.cornell.kfs.coa.businessobject.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.AccountDelegateGlobal;
import org.kuali.kfs.coa.businessobject.lookup.OrganizationRoutingModelNameLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * This class overrides the getBackLocation, getReturnUrl, setFieldConversions and getActionUrls for {@link OrganizationRoutingModelName}
 */
@SuppressWarnings("deprecation")
public class CuOrganizationRoutingModelNameLookupableHelperServiceImpl extends OrganizationRoutingModelNameLookupableHelperServiceImpl {

    @Override
    public HtmlData getReturnUrl(BusinessObject businessObject, LookupForm lookupForm, List returnKeys, BusinessObjectRestrictions businessObjectRestrictions) {
        String originalBackLocation = this.backLocation;
        Map<String, String> parameters = getParameters(businessObject, lookupForm.getFieldConversions(), lookupForm.getLookupableImplServiceName(), returnKeys);
        parameters.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.MAINTENANCE_NEW_WITH_EXISTING_ACTION);
        parameters.put(KFSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, AccountDelegateGlobal.class.getName());
        parameters.put(KFSConstants.OVERRIDE_KEYS, "modelName" + KFSConstants.FIELD_CONVERSIONS_SEPERATOR + "modelChartOfAccountsCode"
                + KFSConstants.FIELD_CONVERSIONS_SEPERATOR + "modelOrganizationCode");
        setBackLocation(KFSConstants.MAINTENANCE_ACTION);
        AnchorHtmlData htmlData = (AnchorHtmlData) getReturnAnchorHtmlData(businessObject, parameters, lookupForm, returnKeys, businessObjectRestrictions);
        // set this to prevent breaking Account Delegate Model returnLocation
        setBackLocation(originalBackLocation);
        return htmlData;
    }

}
