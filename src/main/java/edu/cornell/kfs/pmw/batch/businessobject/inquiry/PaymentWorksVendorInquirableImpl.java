package edu.cornell.kfs.pmw.batch.businessobject.inquiry;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.inquiry.KualiInquirableImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

@SuppressWarnings("deprecation")
public class PaymentWorksVendorInquirableImpl extends KualiInquirableImpl {

    /**
     * Overridden to forcibly create an inquiry hyperlink for the PaymentWorksVendor ID field,
     * given that PaymentWorksVendor uses a non-key field for the title attribute (thus causing
     * the superclass to skip creating the ID field inquiry link).
     */
    @Override
    public HtmlData getInquiryUrl(final BusinessObject businessObject, final String attributeName,
            final boolean forceInquiry) {

        if (StringUtils.equals(attributeName, KFSPropertyConstants.ID)) {
            final Integer id = ((PaymentWorksVendor) businessObject).getId();
            final String idAsString = Objects.toString(id);
            final Map<String, String> fieldList = Map.of(KFSPropertyConstants.ID, idAsString);
            final Map<String, String> parameters = Map.ofEntries(
                Map.entry(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.START_METHOD),
                Map.entry(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, PaymentWorksVendor.class.getName()),
                Map.entry(KFSPropertyConstants.ID, idAsString)
            );
            return getHyperLink(PaymentWorksVendor.class, fieldList,
                    UrlFactory.parameterizeUrl(KRADConstants.INQUIRY_ACTION, parameters));
        }

        return super.getInquiryUrl(businessObject, attributeName, forceInquiry);
    }

}
