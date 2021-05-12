package edu.cornell.kfs.kim.lookup;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.lookup.GroupLookupableHelperServiceImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CuGroupLookupableHelperServiceImpl extends GroupLookupableHelperServiceImpl {

    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject bo, List pkNames) {
        Group group = (Group) bo;
        if (ObjectUtils.isNotNull(group) && StringUtils.equalsIgnoreCase(
                CUKFSConstants.LEGACY_PERMIT_NAMESPACE, group.getNamespaceCode())) {
            return Collections.emptyList();
        } else {
            return super.getCustomActionUrls(bo, pkNames);
        }
    }

}
