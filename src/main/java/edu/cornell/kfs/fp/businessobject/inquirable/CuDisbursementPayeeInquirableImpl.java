package edu.cornell.kfs.fp.businessobject.inquirable;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.inquirable.DisbursementPayeeInquirableImpl;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.UrlFactory;

import edu.cornell.kfs.vnd.businessobject.CuVendorDetail;

public class CuDisbursementPayeeInquirableImpl extends DisbursementPayeeInquirableImpl {

    public HtmlData getInquiryUrl(BusinessObject businessObject, String attributeName, boolean forceInquiry) {
        if (businessObject instanceof DisbursementPayee && KFSPropertyConstants.PAYEE_NAME.equals(attributeName)) {
            DisbursementPayee payee = (DisbursementPayee) businessObject;

            boolean isVendor = SpringContext.getBean(DisbursementVoucherPayeeService.class).isVendor(payee);
            if (isVendor) {
                return this.getVendorInquiryUrl(payee);
            } 
        }

        return super.getInquiryUrl(businessObject, attributeName, forceInquiry);
    }

    private HtmlData getVendorInquiryUrl(DisbursementPayee payee) {
        String payeeIdNumber = payee.getPayeeIdNumber();
        String vendorHeaderGeneratedIdentifier = StringUtils.substringBefore(payeeIdNumber, "-");
        String vendorDetailAssignedIdentifier = StringUtils.substringAfter(payeeIdNumber, "-");

        Properties params = new Properties();
        params.put(KFSConstants.DISPATCH_REQUEST_PARAMETER, KFSConstants.START_METHOD);
        params.put(KFSConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, CuVendorDetail.class.getName());
        params.put(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID, vendorHeaderGeneratedIdentifier);
        params.put(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, vendorDetailAssignedIdentifier);

        String url = UrlFactory.parameterizeUrl(KRADConstants.INQUIRY_ACTION, params);

        Map<String, String> fieldList = new HashMap<String, String>();
        fieldList.put(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID, vendorHeaderGeneratedIdentifier);
        fieldList.put(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, vendorDetailAssignedIdentifier);

        return this.getHyperLink(CuVendorDetail.class, fieldList, url);
    }

}
