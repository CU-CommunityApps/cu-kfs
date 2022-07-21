package edu.cornell.kfs.module.purap.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.module.purap.businessobject.B2BInformation;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.service.JaggaerRoleService;
import edu.cornell.kfs.module.purap.service.JaggaerXmlService;
import edu.cornell.kfs.module.purap.util.cxml.CxmlConstants;
import edu.cornell.kfs.module.purap.util.cxml.CxmlConstants.CredentialDomains;
import edu.cornell.kfs.module.purap.util.cxml.CxmlConstants.ExtrinsicFields;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.BrowserFormPostDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.BuyerCookieDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.ContactDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.CredentialDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.CxmlDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.ExtrinsicDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.FromDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.HeaderDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.IdentityDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.NameDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.PunchOutSetupRequestDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.RequestDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.SenderDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.SharedSecretDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.SupplierSetupDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.ToDTO;
import edu.cornell.kfs.module.purap.util.cxml.xmlObjects.UrlDTO;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class JaggaerXmlServiceImpl implements JaggaerXmlService {

    private static final Logger LOG = LogManager.getLogger();

    private JaggaerRoleService jaggaerRoleService;
    private CUMarshalService cuMarshalService;
    private DateTimeService dateTimeService;
    private String internalSupplierId;

    @Override
    public String getJaggaerLoginXmlForEShop(Person user, B2BInformation b2bInformation) {
        return getJaggaerLoginXmlForRoleSet(user, b2bInformation, JaggaerRoleSet.ESHOP);
    }

    @Override
    public String getJaggaerLoginXmlForContractsPlus(Person user, B2BInformation b2bInformation) {
        return getJaggaerLoginXmlForRoleSet(user, b2bInformation, JaggaerRoleSet.CONTRACTS_PLUS);
    }

    @Override
    public String getJaggaerLoginXmlForJaggaerAdmin(Person user, B2BInformation b2bInformation) {
        return getJaggaerLoginXmlForRoleSet(user, b2bInformation, JaggaerRoleSet.ADMINISTRATOR);
    }

    protected String getJaggaerLoginXmlForRoleSet(Person user, B2BInformation b2bInformation, JaggaerRoleSet roleSet) {
        try {
            List<String> roles = jaggaerRoleService.getJaggaerRoles(user, roleSet);
            if (CollectionUtils.isEmpty(roles)) {
                throw new IllegalStateException("User " + user.getPrincipalName()
                        + " has no valid Jaggaer roles for role set " + roleSet);
            }
            CxmlDTO cxmlDTO = createCxmlDTO(user, b2bInformation, roles);
            String cxml = cuMarshalService.marshalObjectToXmlStringWithSystemDocType(
                    cxmlDTO, CxmlConstants.DOCTYPE_URL);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getJaggaerLoginXmlForRoleSet, Successfully created CXML for user "
                        + user.getPrincipalName() + " and role set " + roleSet + ":\n" + cxml);
            }
            return cxml;
        } catch (Exception e) {
            LOG.error("getJaggaerLoginXmlForRoleSet, Failed to create CXML login request", e);
            throw new RuntimeException(e);
        }
    }

    private CxmlDTO createCxmlDTO(Person user, B2BInformation b2bInformation, List<String> roles) {
        Date currentDate = dateTimeService.getCurrentDate();
        String principalName = StringUtils.upperCase(user.getPrincipalName(), Locale.US);
        String department = user.getCampusCode() + user.getPrimaryDepartmentCode();
        
        return cXML(CxmlConstants.PAYLOAD_ID_IRRELEVANT, CxmlConstants.XML_LANG_EN_US, currentDate,
            header(
                from(
                    credential(CredentialDomains.NETWORK_ID,
                        identity(b2bInformation.getIdentity())
                    )
                ),
                to(
                    credential(CredentialDomains.DUNS,
                        identity(b2bInformation.getIdentity())
                    ),
                    credential(CredentialDomains.INTERNAL_SUPPLIER_ID,
                        identity(internalSupplierId)
                    )
                ),
                sender(
                    credential(CredentialDomains.TOPS_NETWORK_USER_ID,
                        identity(principalName),
                        sharedSecret(b2bInformation.getPassword())
                    ),
                    userAgent(b2bInformation.getUserAgent())
                )
            ),
            request(b2bInformation.getEnvironment(),
                punchOutSetupRequest(CxmlConstants.PUNCHOUT_OPERATION_CREATE,
                    buyerCookie(principalName),
                    extrinsic(ExtrinsicFields.USER_EMAIL, user.getEmailAddressUnmasked()),
                    extrinsic(ExtrinsicFields.UNIQUE_NAME, principalName),
                    extrinsic(ExtrinsicFields.PHONE_NUMBER, user.getPhoneNumberUnmasked()),
                    extrinsic(ExtrinsicFields.DEPARTMENT, department),
                    extrinsic(ExtrinsicFields.CAMPUS, user.getCampusCode()),
                    extrinsic(ExtrinsicFields.FIRST_NAME, user.getFirstName()),
                    extrinsic(ExtrinsicFields.LAST_NAME, user.getLastName()),
                    inlineExtrinsicsList(ExtrinsicFields.ROLE, roles),
                    browserFormPost(
                        url(b2bInformation.getPunchbackURL())
                    ),
                    contact(CxmlConstants.CONTACT_ROLE_END_USER,
                        name(CxmlConstants.XML_LANG_EN, user.getName())
                    ),
                    supplierSetup(
                        url(b2bInformation.getPunchoutURL())
                    )
                )
            )
        );
    }

    private CxmlDTO cXML(String payloadID, String xmlLang, Date timestamp, HeaderDTO header, RequestDTO request) {
        CxmlDTO cxml = new CxmlDTO();
        cxml.setPayloadID(payloadID);
        cxml.setXmlLang(xmlLang);
        cxml.setTimestamp(timestamp);
        cxml.setCxmlSections(List.of(header, request));
        return cxml;
    }

    private HeaderDTO header(FromDTO from, ToDTO to, SenderDTO sender) {
        HeaderDTO header = new HeaderDTO();
        header.setFrom(from);
        header.setTo(to);
        header.setSender(sender);
        return header;
    }

    private FromDTO from(CredentialDTO credential) {
        FromDTO from = new FromDTO();
        from.setCredentials(List.of(credential));
        return from;
    }

    private ToDTO to(CredentialDTO... credentials) {
        ToDTO to = new ToDTO();
        to.setCredentials(List.of(credentials));
        return to;
    }

    private SenderDTO sender(CredentialDTO credential, String userAgent) {
        SenderDTO sender = new SenderDTO();
        sender.setCredentials(List.of(credential));
        sender.setUserAgent(userAgent);
        return sender;
    }

    private String userAgent(String userAgent) {
        return userAgent;
    }

    private CredentialDTO credential(String domain, IdentityDTO identity, SharedSecretDTO sharedSecret) {
        CredentialDTO credential = credential(domain, identity);
        credential.setSecretValues(List.of(sharedSecret));
        return credential;
    }

    private CredentialDTO credential(String domain, IdentityDTO identity) {
        CredentialDTO credential = new CredentialDTO();
        credential.setDomain(domain);
        credential.setIdentity(identity);
        return credential;
    }

    private IdentityDTO identity(String value) {
        IdentityDTO identity = new IdentityDTO();
        identity.setContents(List.of(value));
        return identity;
    }

    private SharedSecretDTO sharedSecret(String value) {
        SharedSecretDTO sharedSecret = new SharedSecretDTO();
        sharedSecret.setContents(List.of(value));
        return sharedSecret;
    }

    private RequestDTO request(String deploymentMode, PunchOutSetupRequestDTO punchOutSetupRequest) {
        RequestDTO request = new RequestDTO();
        request.setDeploymentMode(deploymentMode);
        request.setTypedRequests(List.of(punchOutSetupRequest));
        return request;
    }

    private PunchOutSetupRequestDTO punchOutSetupRequest(String operation, Object... subElements) {
        Stream.Builder<ExtrinsicDTO> extrinsics = Stream.builder();
        Stream.Builder<ContactDTO> contacts = Stream.builder();
        PunchOutSetupRequestDTO punchOutSetupRequest = new PunchOutSetupRequestDTO();
        punchOutSetupRequest.setOperation(operation);
        
        for (Object subElement : subElements) {
            if (subElement instanceof ExtrinsicDTO) {
                extrinsics.add((ExtrinsicDTO) subElement);
            } else if (subElement instanceof List) {
                for (Object extrinsic : (List<?>) subElement) {
                    extrinsics.add((ExtrinsicDTO) extrinsic);
                }
            } else if (subElement instanceof BuyerCookieDTO) {
                punchOutSetupRequest.setBuyerCookie((BuyerCookieDTO) subElement);
            } else if (subElement instanceof BrowserFormPostDTO) {
                punchOutSetupRequest.setBrowserFormPost((BrowserFormPostDTO) subElement);
            } else if (subElement instanceof ContactDTO) {
                contacts.add((ContactDTO) subElement);
            } else if (subElement instanceof SupplierSetupDTO) {
                punchOutSetupRequest.setSupplierSetup((SupplierSetupDTO) subElement);
            } else {
                throw new IllegalArgumentException("Unrecognized sub-element type: " + subElement.getClass());
            }
        }
        
        punchOutSetupRequest.setExtrinsics(
                extrinsics.build().collect(Collectors.toUnmodifiableList()));
        punchOutSetupRequest.setContacts(
                contacts.build().collect(Collectors.toUnmodifiableList()));
        return punchOutSetupRequest;
    }

    private BuyerCookieDTO buyerCookie(String value) {
        BuyerCookieDTO buyerCookie = new BuyerCookieDTO();
        buyerCookie.setContents(List.of(value));
        return buyerCookie;
    }

    private ExtrinsicDTO extrinsic(String name, String value) {
        ExtrinsicDTO extrinsic = new ExtrinsicDTO();
        extrinsic.setName(name);
        extrinsic.setContents(List.of(value));
        return extrinsic;
    }

    private List<ExtrinsicDTO> inlineExtrinsicsList(String name, List<String> values) {
        return values.stream()
                .map(value -> extrinsic(name, value))
                .collect(Collectors.toUnmodifiableList());
    }

    private BrowserFormPostDTO browserFormPost(UrlDTO url) {
        BrowserFormPostDTO browserFormPost = new BrowserFormPostDTO();
        browserFormPost.setUrl(url);
        return browserFormPost;
    }

    private ContactDTO contact(String role, NameDTO name) {
        ContactDTO contact = new ContactDTO();
        contact.setRole(role);
        contact.setName(name);
        return contact;
    }

    private NameDTO name(String xmlLang, String value) {
        NameDTO name = new NameDTO();
        name.setXmlLang(xmlLang);
        name.setValue(value);
        return name;
    }

    private SupplierSetupDTO supplierSetup(UrlDTO url) {
        SupplierSetupDTO supplierSetup = new SupplierSetupDTO();
        supplierSetup.setUrl(url);
        return supplierSetup;
    }

    private UrlDTO url(String value) {
        UrlDTO url = new UrlDTO();
        url.setValue(value);
        return url;
    }

    public void setJaggaerRoleService(JaggaerRoleService jaggaerRoleService) {
        this.jaggaerRoleService = jaggaerRoleService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setInternalSupplierId(String internalSupplierId) {
        this.internalSupplierId = internalSupplierId;
    }

}
