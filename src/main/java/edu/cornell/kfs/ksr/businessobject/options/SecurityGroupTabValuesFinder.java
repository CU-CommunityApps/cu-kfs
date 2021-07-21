package edu.cornell.kfs.ksr.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroupTab;
import edu.cornell.kfs.ksr.businessobject.SecurityProvisioning;

public class SecurityGroupTabValuesFinder extends KeyValuesBase {

    private static final long serialVersionUID = 1825873598309848494L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        Long securityGroupID = null;
        KualiForm form = KNSGlobalVariables.getKualiForm();
        if ((form != null) && (form instanceof KualiDocumentFormBase)) {
            Document doc = ((KualiDocumentFormBase) form).getDocument();
            if (doc instanceof MaintenanceDocument) {
                SecurityProvisioning securityProvisioning = (SecurityProvisioning) ((MaintenanceDocument)doc).getDocumentDataObject();
                securityGroupID = securityProvisioning.getSecurityGroupId();
            }
        }
        
        Collection<SecurityGroupTab> tabs;
        if (securityGroupID != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(KSRConstants.SECURITY_GROUP_ID, securityGroupID);
            tabs = SpringContext.getBean(BusinessObjectService.class).findMatching(SecurityGroupTab.class, map);
            for (SecurityGroupTab tab : tabs) {
                keyValues.add(new ConcreteKeyValue(tab.getTabId().toString(), tab.getTabOrder() + " - " + tab.getTabName()));
            }
        }

        return keyValues;
    }

}
