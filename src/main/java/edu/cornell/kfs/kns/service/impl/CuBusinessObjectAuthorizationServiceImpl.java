package edu.cornell.kfs.kns.service.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.document.IdentityManagementKimDocument;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.address.EntityAddress;
import org.kuali.kfs.kim.impl.identity.email.EntityEmail;
import org.kuali.kfs.kim.impl.identity.name.EntityName;
import org.kuali.kfs.kim.impl.identity.phone.EntityPhone;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.identity.privacy.EntityPrivacyPreferences;
import org.kuali.kfs.kim.service.KIMServiceLocatorInternal;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.kns.inquiry.InquiryRestrictions;
import org.kuali.kfs.kns.service.impl.BusinessObjectAuthorizationServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.krad.CUKRADPropertyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.vnd.CUVendorConstants;

public class CuBusinessObjectAuthorizationServiceImpl extends BusinessObjectAuthorizationServiceImpl {

    private static final Logger LOG = LogManager.getLogger(CuBusinessObjectAuthorizationServiceImpl.class);

    private static final String COLLECTION_ITEM_PROPERTY_PATH_FORMAT = "{0}[{1}].{2}";

    private IdentityService identityService;
    private UiDocumentService uiDocumentService;
    private PermissionService permissionService;
    private Map<Class<?>, Predicate<EntityPrivacyPreferences>> kimPrivacyMappings;

    public CuBusinessObjectAuthorizationServiceImpl() {
        super();
        this.kimPrivacyMappings =
                Stream.<Pair<Class<?>, Predicate<EntityPrivacyPreferences>>>of(
                        Pair.of(EntityName.class, EntityPrivacyPreferences::isSuppressName),
                        Pair.of(EntityAddress.class, EntityPrivacyPreferences::isSuppressAddress),
                        Pair.of(EntityPhone.class, EntityPrivacyPreferences::isSuppressPhone),
                        Pair.of(EntityEmail.class, EntityPrivacyPreferences::isSuppressEmail)
                ).collect(
                        Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    protected boolean canFullyUnmaskFieldForBusinessObject(Person user, Class<?> dataObjectClass, String fieldName,
            BusinessObject businessObject, Document document) {
        if (isUnmaskedKimPersonField(dataObjectClass, fieldName)) {
            return canViewUnmaskedPersonFieldOnObject(user, dataObjectClass, businessObject);
        } else {
            return super.canFullyUnmaskFieldForBusinessObject(
                    user, dataObjectClass, fieldName, businessObject, document);
        }
    }

    private boolean isUnmaskedKimPersonField(Class<?> dataObjectClass, String fieldName) {
        return dataObjectClass != null && kimPrivacyMappings.containsKey(dataObjectClass)
                && StringUtils.endsWith(fieldName, CUKFSPropertyConstants.UNMASKED_PROPERTY_SUFFIX);
    }

    private boolean canViewUnmaskedPersonFieldOnObject(
            Person user, Class<?> dataObjectClass, BusinessObject businessObject) {
        List<Principal> principalsForInquiredEntity = getPrincipalsForPersonObjectOrSubObject(businessObject);
        return principalsForInquiredEntity.stream()
                .anyMatch(principal -> canViewUnmaskedPersonFieldForPrincipal(user, dataObjectClass, principal));
    }

    private boolean canViewUnmaskedPersonFieldForPrincipal(
            Person user, Class<?> dataObjectClass, Principal principal) {
        EntityPrivacyPreferences privacyPreferences = getIdentityService()
                .getEntityPrivacyPreferences(principal.getEntityId());
        boolean isPrivacyFlagEnabledOrMissing = true;
        
        if (ObjectUtils.isNotNull(privacyPreferences)) {
            Predicate<EntityPrivacyPreferences> privacyFlagGetter = kimPrivacyMappings.get(dataObjectClass);
            if (privacyFlagGetter != null) {
                isPrivacyFlagEnabledOrMissing = privacyFlagGetter.test(privacyPreferences);
            }
        }
        
        return !isPrivacyFlagEnabledOrMissing || getUiDocumentService().canOverrideEntityPrivacyPreferences(
                user.getPrincipalId(), principal.getPrincipalId());
    }

    private List<Principal> getPrincipalsForPersonObjectOrSubObject(BusinessObject businessObject) {
        String entityId = getEntityIdFromPersonObjectOrSubObject(businessObject);
        if (StringUtils.isNotBlank(entityId)) {
            QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                    PredicateFactory.equal(KIMPropertyConstants.Person.ENTITY_ID, entityId));
            return getIdentityService().findPrincipals(criteria)
                    .getResults();
        } else {
            return Collections.emptyList();
        }
    }

    private String getEntityIdFromPersonObjectOrSubObject(BusinessObject businessObject) {
        String entityId;
        try {
            if (ObjectUtils.isNotNull(businessObject)) {
                entityId = (String) ObjectUtils.getPropertyValue(
                        businessObject, KIMPropertyConstants.Entity.ENTITY_ID);
            } else {
                entityId = null;
            }
        } catch (RuntimeException e) {
            LOG.error("getEntityIdFromPersonObjectOrSubObject: Could not retrieve entity ID, so default to null", e);
            entityId = null;
        }
        return entityId;
    }
    
    @Override
    protected DocumentAuthorizer findDocumentAuthorizerForBusinessObject(BusinessObject businessObject) {
        if (objectRepresentsKimDocumentPotentiallyOpenedInInquiryMode(businessObject)) {
            return null;
        }
        return super.findDocumentAuthorizerForBusinessObject(businessObject);
    }

    private boolean objectRepresentsKimDocumentPotentiallyOpenedInInquiryMode(BusinessObject businessObject) {
        if (businessObject instanceof IdentityManagementKimDocument) {
            IdentityManagementKimDocument kimDocument = (IdentityManagementKimDocument) businessObject;
            Optional<DocumentHeader> docHeader = Optional.ofNullable(kimDocument.getDocumentHeader());
            Optional<DocumentHeader> workflowDocument = docHeader.filter(DocumentHeader::hasWorkflowDocument);
            return workflowDocument.isEmpty();
        }
        return false;
    }

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

    private IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = KimApiServiceLocator.getIdentityService();
        }
        return identityService;
    }

    private UiDocumentService getUiDocumentService() {
        if (uiDocumentService == null) {
            uiDocumentService = KIMServiceLocatorInternal.getUiDocumentService();
        }
        return uiDocumentService;
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = KimApiServiceLocator.getPermissionService();
        }
        return permissionService;
    }

}
