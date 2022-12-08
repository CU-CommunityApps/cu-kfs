package edu.cornell.kfs.kns.service.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.inquiry.InquiryRestrictions;
import org.kuali.kfs.kns.service.impl.BusinessObjectAuthorizationServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.krad.CUKRADPropertyConstants;
import edu.cornell.kfs.vnd.CUVendorConstants;

public class CuBusinessObjectAuthorizationServiceImpl extends BusinessObjectAuthorizationServiceImpl {

    private static final Logger LOG = LogManager.getLogger(CuBusinessObjectAuthorizationServiceImpl.class);

    private static final String COLLECTION_ITEM_PROPERTY_PATH_FORMAT = "{0}[{1}].{2}";

    private PermissionService permissionService;

    @Override
    public InquiryRestrictions getInquiryRestrictions(BusinessObject businessObject, Person user) {
        InquiryRestrictions inquiryRestrictions = super.getInquiryRestrictions(businessObject, user);
        if (inquiredObjectIsVendorDetail(businessObject) && shouldHideAttachmentLinkOnVendorInquiry(user)) {
            VendorDetail vendorDetail = (VendorDetail) businessObject;
            List<Note> boNotes = vendorDetail.getBoNotes();
            for (int i = 0; i < boNotes.size(); i++) {
                String attachmentLinkPropertyPath = MessageFormat.format(COLLECTION_ITEM_PROPERTY_PATH_FORMAT,
                        CUKRADPropertyConstants.BO_NOTES, i, CUKRADPropertyConstants.ATTACHMENT_LINK);
                inquiryRestrictions.addHiddenField(attachmentLinkPropertyPath);
            }
        }
        return inquiryRestrictions;
    }

    private boolean inquiredObjectIsVendorDetail(BusinessObject businessObject) {
        return businessObject instanceof VendorDetail;
    }

    private boolean shouldHideAttachmentLinkOnVendorInquiry(Person user) {
        Map<String, String> permissionDetails = Collections.singletonMap(
                KewApiConstants.DOCUMENT_TYPE_NAME_DETAIL, CUVendorConstants.VENDOR_DOCUMENT_TYPE_NAME);
        return !getPermissionService().hasPermissionByTemplate(user.getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.VIEW_NOTE_ATTACHMENT, permissionDetails);
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = KimApiServiceLocator.getPermissionService();
        }
        return permissionService;
    }

}
