package edu.cornell.kfs.kns.service.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.inquiry.InquiryRestrictions;
import org.kuali.kfs.kns.service.impl.BusinessObjectAuthorizationServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.krad.CUKRADPropertyConstants;

public class CuBusinessObjectAuthorizationServiceImpl extends BusinessObjectAuthorizationServiceImpl {

    private static final Logger LOG = LogManager.getLogger(CuBusinessObjectAuthorizationServiceImpl.class);

    private static final String COLLECTION_ITEM_PROPERTY_PATH_FORMAT = "{0}[{1}].{2}";

    private PermissionService permissionService;

    public CuBusinessObjectAuthorizationServiceImpl(final Environment environment,
            final ConfigurationService configurationService) {
        super(environment, configurationService);
    }

    @Override
    public InquiryRestrictions getInquiryRestrictions(final BusinessObject businessObject, final Person user) {
        final InquiryRestrictions inquiryRestrictions = super.getInquiryRestrictions(businessObject, user);
        if (inquiredObjectIsVendorDetail(businessObject) && shouldHideAttachmentLinkOnVendorInquiry(user)) {
            final VendorDetail vendorDetail = (VendorDetail) businessObject;
            final List<Note> boNotes = vendorDetail.getBoNotes();
            for (int i = 0; i < boNotes.size(); i++) {
                final String attachmentLinkPropertyPath = MessageFormat.format(COLLECTION_ITEM_PROPERTY_PATH_FORMAT,
                        CUKRADPropertyConstants.BO_NOTES, i, CUKRADPropertyConstants.ATTACHMENT_LINK);
                inquiryRestrictions.addHiddenField(attachmentLinkPropertyPath);
            }
        }
        return inquiryRestrictions;
    }

    private boolean inquiredObjectIsVendorDetail(final BusinessObject businessObject) {
        return businessObject instanceof VendorDetail;
    }

    private boolean shouldHideAttachmentLinkOnVendorInquiry(final Person user) {
        final Map<String, String> permissionDetails = Collections.singletonMap(
                KewApiConstants.DOCUMENT_TYPE_NAME_DETAIL, VendorConstants.VENDOR_DOC_TYPE);
        return !permissionService.hasPermissionByTemplate(user.getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.VIEW_NOTE_ATTACHMENT, permissionDetails);
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
        super.setPermissionService(permissionService);
    }

}
