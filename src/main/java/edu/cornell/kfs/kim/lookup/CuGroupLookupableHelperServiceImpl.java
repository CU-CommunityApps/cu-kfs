package edu.cornell.kfs.kim.lookup;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.group.GroupBo;
import org.kuali.kfs.kim.lookup.GroupLookupableHelperServiceImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.cynergy.CynergyConstants;

public class CuGroupLookupableHelperServiceImpl extends GroupLookupableHelperServiceImpl {

    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject bo, List pkNames) {
        GroupBo group = (GroupBo) bo;
        if (ObjectUtils.isNotNull(group) && StringUtils.equalsIgnoreCase(
                CynergyConstants.DEFAULT_PERMIT_NAMESPACE, group.getNamespaceCode())) {
            return Collections.emptyList();
        } else {
            return super.getCustomActionUrls(bo, pkNames);
        }
    }

}
