package org.kuali.kfs.ksr.bo.lookup;

import java.util.List;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * Also marked this class as deprecated; please use SecurityGroupLookupViewHelperServiceImpl instead.
 * ====
 * 
 * Lookup helper for the SecurityProvisioningGroup. It takes the names entered into lookup form and maps them
 * to their respective ID's if they exist
 * 
 * @deprecated
 * @author rSmart Development Team
 */
@Deprecated
public class SecurityGroupLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {
    private static final long serialVersionUID = 1L;

    /**
     * @see org.kuali.kfs.kns.lookup.AbstractLookupableHelperServiceImpl#getCustomActionUrls(org.kuali.kfs.kns.bo.BusinessObject,
     *      java.util.List)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        List<HtmlData> urls = super.getCustomActionUrls(businessObject, pkNames);
        
        if (allowsMaintenanceEditAction(businessObject)) {
            AnchorHtmlData url = (AnchorHtmlData) urls.get(0);
            AnchorHtmlData urlProv = new AnchorHtmlData();
            String href = url.getHref();
            href = href.replaceFirst("SecurityGroup", "SecurityProvisioning");
            urlProv.setHref(href);
            urlProv.setDisplayText(KsrConstants.SECURITY_PROVISIONING_URL_NAME);
            urlProv.setMethodToCall(url.getMethodToCall());
            urls.add(urlProv);
        }

        return urls;
    }

}
