package edu.cornell.kfs.sys.businessobject.lookup;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;

public class ApiFrameworkLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public List<HtmlData> getCustomActionUrls(final BusinessObject businessObject, final List pkNames) {
        LOG.debug("getCustomActionUrls, entering");
        List<HtmlData> htmlDataList = super.getCustomActionUrls(businessObject, pkNames);

        return htmlDataList.stream()
                .filter(data -> !StringUtils.equalsIgnoreCase(data.getMethodToCall(), KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL))
                .collect(Collectors.toList());
    }


}
