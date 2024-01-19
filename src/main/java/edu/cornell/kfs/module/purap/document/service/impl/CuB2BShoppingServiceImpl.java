package edu.cornell.kfs.module.purap.document.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.RequisitionStatuses;
import org.kuali.kfs.module.purap.businessobject.B2BInformation;
import org.kuali.kfs.module.purap.businessobject.B2BShoppingCartItem;
import org.kuali.kfs.module.purap.businessobject.BillingAddress;
import org.kuali.kfs.module.purap.businessobject.DefaultPrincipalAddress;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.dataaccess.B2BDao;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchasingService;
import org.kuali.kfs.module.purap.document.service.impl.B2BShoppingServiceImpl;
import org.kuali.kfs.module.purap.exception.B2BShoppingException;
import org.kuali.kfs.module.purap.util.PurApDateFormatUtils;
import org.kuali.kfs.module.purap.util.cxml.B2BParserHelper;
import org.kuali.kfs.module.purap.util.cxml.B2BShoppingCart;
import org.kuali.kfs.module.purap.util.cxml.PunchOutSetupResponse;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContract;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.vnd.service.PhoneNumberService;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.document.service.CuB2BShoppingService;
import edu.cornell.kfs.module.purap.document.service.CuPurapService;
import edu.cornell.kfs.module.purap.service.JaggaerRoleService;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;

public class CuB2BShoppingServiceImpl extends B2BShoppingServiceImpl implements CuB2BShoppingService {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String TRUE = "true";
    private B2BDao b2bDao;
    private BusinessObjectService businessObjectService;
    private DocumentService documentService;
    private ParameterService parameterService;
    private PersistenceService persistenceService;
    private PhoneNumberService phoneNumberService;
    private PurchasingService purchasingService;
    private CuPurapService purapService;
    private VendorService vendorService;

    private UserFavoriteAccountService userFavoriteAccountService;
    private JaggaerRoleService jaggaerRoleService;

    @Override
    public List createRequisitionsFromCxml(final B2BShoppingCart message, Person user) {
        LOG.debug("createRequisitionsFromCxml() started");
        // for returning requisitions
        final ArrayList requisitions = new ArrayList();

        // get items from the cart
        final List items = message.getItems();

        // get vendor(s) for the items
        final List vendors = getAllVendors(items);

        // create requisition(s) (one per vendor)
        for (final Iterator iter = vendors.iterator(); iter.hasNext();) {
            final VendorDetail vendor = (VendorDetail) iter.next();

            // create requisition
            final RequisitionDocument req = (RequisitionDocument) documentService.getNewDocument(PurapConstants.PurapDocTypeCodes.REQUISITION_DOCUMENT_TYPE);
            final String description = ((B2BShoppingCartItem)items.get(0)).getExtrinsic("CartName");
            final String businessPurpose = message.getMessage().getPunchOutOrderMessage().getPunchOutOrderMessageHeader().getQuoteStatus();

            req.getDocumentHeader().setDocumentDescription(description);
            req.getDocumentHeader().setExplanation(businessPurpose);

            req.setupAccountDistributionMethod();
            // set b2b contract for vendor
            final VendorContract contract = vendorService.getVendorB2BContract(vendor, user.getCampusCode());
            if (ObjectUtils.isNotNull(contract)) {
                req.setVendorContractGeneratedIdentifier(contract.getVendorContractGeneratedIdentifier());
                if (ObjectUtils.isNotNull(contract.getPurchaseOrderCostSourceCode())) {
                    // if cost source is set on contract, use it
                    req.setPurchaseOrderCostSourceCode(contract.getPurchaseOrderCostSourceCode());
                }
                else {
                    // if cost source is null on the contract, we set it by default to "Estimate"
                    req.setPurchaseOrderCostSourceCode(PurapConstants.POCostSources.ESTIMATE);
                }
            }
            else {
                LOG.error(
                        "createRequisitionsFromCxml() Contract is missing for vendor {} ({})",
                        vendor::getVendorName,
                        vendor::getVendorNumber
                );
                throw new B2BShoppingException(PurapConstants.B2B_VENDOR_CONTRACT_NOT_FOUND_ERROR_MESSAGE);
            }

            // get items for this vendor
            final List itemsForVendor = getAllVendorItems(items, vendor);
            // KFSPTS-985
            checkToPopulateFavoriteAccount(itemsForVendor, user);

            // default data from user
            req.setDeliveryCampusCode(user.getCampusCode());
            req.setDeliveryToName(user.getName());
            req.setDeliveryToEmailAddress(user.getEmailAddress());
            req.setDeliveryToPhoneNumber(SpringContext.getBean(PhoneNumberService.class).formatNumberIfPossible(user.getPhoneNumber()));

            DefaultPrincipalAddress defaultPrincipalAddress = new DefaultPrincipalAddress(user.getPrincipalId());
            final Map addressKeys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(defaultPrincipalAddress);
            defaultPrincipalAddress = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(DefaultPrincipalAddress.class, addressKeys);
            if (ObjectUtils.isNotNull(defaultPrincipalAddress) && ObjectUtils.isNotNull(defaultPrincipalAddress.getBuilding())) {
                if (defaultPrincipalAddress.getBuilding().isActive()) {
                    req.setDeliveryCampusCode(defaultPrincipalAddress.getCampusCode());
                    req.templateBuildingToDeliveryAddress(defaultPrincipalAddress.getBuilding());
                    req.setDeliveryBuildingRoomNumber(defaultPrincipalAddress.getBuildingRoomNumber());
                }
                else {
                    //since building is now inactive, delete default building record
                    SpringContext.getBean(BusinessObjectService.class).delete(defaultPrincipalAddress);
                }
            }

            final ChartOrgHolder purapChartOrg = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(user, PurapConstants.PURAP_NAMESPACE);
            if (ObjectUtils.isNotNull(purapChartOrg)) {
                req.setChartOfAccountsCode(purapChartOrg.getChartOfAccountsCode());
                req.setOrganizationCode(purapChartOrg.getOrganizationCode());
            }

            req.setRequestorPersonName(user.getName());
            req.setRequestorPersonEmailAddress(user.getEmailAddress());
            req.setRequestorPersonPhoneNumber(phoneNumberService.formatNumberIfPossible(user.getPhoneNumber()));
            req.setUseTaxIndicator(purchasingService.getDefaultUseTaxIndicatorValue(req));

            // set defaults that need to be set
            req.setVendorHeaderGeneratedIdentifier(vendor.getVendorHeaderGeneratedIdentifier());
            req.setVendorDetailAssignedIdentifier(vendor.getVendorDetailAssignedIdentifier());
            req.setVendorName(vendor.getVendorName());
            req.setVendorRestrictedIndicator(vendor.getVendorRestrictedIndicator());
            req.setItems(itemsForVendor);
            req.setRequisitionSourceCode(PurapConstants.RequisitionSources.B2B);

            req.updateAndSaveAppDocStatus(RequisitionStatuses.APPDOC_IN_PROCESS);

            //KFSPTS-1446 : Needed to move the setting of method of PO transmission to after the templateVendorAddress call because that method will set the method of PO transmission to the value on the vendor address. 
            //req.setPurchaseOrderTransmissionMethodCode(PurapConstants.POTransmissionMethods.ELECTRONIC);
            req.setOrganizationAutomaticPurchaseOrderLimit(purapService.getApoLimit(req));

            //retrieve from an item (sent in cxml at item level, but stored in db at REQ level)
            req.setExternalOrganizationB2bSupplierIdentifier(getSupplierIdFromFirstItem(itemsForVendor));

            // retrieve default PO address and set address
            final VendorAddress vendorAddress = vendorService.getVendorDefaultAddress(vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier(), VendorConstants.AddressTypes.PURCHASE_ORDER, user.getCampusCode());
            if (ObjectUtils.isNotNull(vendorAddress)) {
                req.templateVendorAddress(vendorAddress);
            }
            //KFSPTS-1446: Moved the setting of this attribute here from its original location to maintain the value of ELECTRONIC and not lose it to the templateVendorAddress value
            req.setPurchaseOrderTransmissionMethodCode(PurapConstants.POTransmissionMethods.ELECTRONIC);

            // retrieve billing address based on delivery campus and populate REQ with retrieved billing address
            BillingAddress billingAddress = new BillingAddress();
            billingAddress.setBillingCampusCode(req.getDeliveryCampusCode());
            final Map keys = persistenceService.getPrimaryKeyFieldValues(billingAddress);
            billingAddress = businessObjectService.findByPrimaryKey(BillingAddress.class, keys);
            req.templateBillingAddress(billingAddress);

            // populate receiving address with the default one for the chart/org
            req.loadReceivingAddress();

            req.fixItemReferences();

            // save requisition to database
            purapService.saveDocumentNoValidation(req);

            // add requisition to List
            requisitions.add(req);
        }
        return requisitions;
    }

    // KFSPTS-985
    private void checkToPopulateFavoriteAccount(final List itemsForVendor, final Person user) {
        final FavoriteAccount account = userFavoriteAccountService.getFavoriteAccount(user.getPrincipalId());
        if (account != null && CollectionUtils.isNotEmpty(itemsForVendor)) {
            for (RequisitionItem item : (List<RequisitionItem>)itemsForVendor) {  
                if (item.getSourceAccountingLines() == null) {
                    item.setSourceAccountingLines(new ArrayList<PurApAccountingLine>());
                }
                item.getSourceAccountingLines().add(userFavoriteAccountService.getPopulatedNewAccount(account, RequisitionAccount.class));
            }
        }
    }

	public UserFavoriteAccountService getUserFavoriteAccountService() {
		return userFavoriteAccountService;
	}

	public void setUserFavoriteAccountService(
			UserFavoriteAccountService userFavoriteAccountService) {
		this.userFavoriteAccountService = userFavoriteAccountService;
	}

    public void setJaggaerRoleService(final JaggaerRoleService jaggaerRoleService) {
        this.jaggaerRoleService = jaggaerRoleService;
    }

    @Override
    public void setB2bDao(final B2BDao b2bDao) {
        super.setB2bDao(b2bDao);
        this.b2bDao = b2bDao;
    }

    @Override
    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
        this.businessObjectService = businessObjectService;
    }

    @Override
    public void setDocumentService(final DocumentService documentService) {
        super.setDocumentService(documentService);
        this.documentService = documentService;
    }

    @Override
    public void setParameterService(final ParameterService parameterService) {
        super.setParameterService(parameterService);
        this.parameterService = parameterService;
    }

    @Override
    public void setPersistenceService(final PersistenceService persistenceService) {
        super.setPersistenceService(persistenceService);
        this.persistenceService = persistenceService;
    }

    @Override
    public void setPhoneNumberService(final PhoneNumberService phoneNumberService) {
        super.setPhoneNumberService(phoneNumberService);
        this.phoneNumberService = phoneNumberService;
    }

    @Override
    public void setPurchasingService(final PurchasingService purchasingService) {
        super.setPurchasingService(purchasingService);
        this.purchasingService = purchasingService;
    }

    @Override
    public void setPurapService(final PurapService purapService) {
        super.setPurapService(purapService);
        this.purapService = (CuPurapService) purapService;
    }

    @Override
    public void setVendorService(final VendorService vendorService) {
        super.setVendorService(vendorService);
        this.vendorService = vendorService;
    }

    @Override
    public String getPunchOutUrlForRoleSet(final Person user, final JaggaerRoleSet roleSet) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("getPunchOutUrlForRoleSet, creating punchout URL for role: " + roleSet);
    	}
        final B2BInformation b2b = getB2bShoppingConfigurationInformation();
        final String cxml = getPunchOutSetupRequestMessage(user, b2b, roleSet);
        final String response = b2bDao.sendPunchOutRequest(cxml, b2b.getPunchoutURL());
        final PunchOutSetupResponse posr = B2BParserHelper.getInstance().parsePunchOutSetupResponse(response);
        return posr.getPunchOutUrl();
    }
    
    @Override
    public String getPunchOutUrl(final Person user) {
    	LOG.debug("getPunchOutUrl, overriding to use custom link generating code.");
    	return getPunchOutUrlForRoleSet(user, JaggaerRoleSet.ESHOP);
    }

    @Override
    public String getPunchOutSetupRequestMessage(final Person user, final B2BInformation b2bInformation) {
        return getPunchOutSetupRequestMessage(user, b2bInformation, JaggaerRoleSet.ESHOP);
    }

    protected String getPunchOutSetupRequestMessage(
            final Person user, final B2BInformation b2bInformation,
            final JaggaerRoleSet roleSet) {
        final StringBuffer cxml = new StringBuffer();
        final Date currentDate = getDateTimeService().getCurrentDate();
        final SimpleDateFormat date = PurApDateFormatUtils.getSimpleDateFormat(PurapConstants.NamedDateFormats.CXML_SIMPLE_DATE_FORMAT);
        final SimpleDateFormat time = PurApDateFormatUtils.getSimpleDateFormat(PurapConstants.NamedDateFormats.CXML_SIMPLE_TIME_FORMAT);

        // doing as two parts b/c they want a T instead of space
        // between them, and SimpleDateFormat doesn't allow putting the
        // constant "T" in the string

        cxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        cxml.append("<!DOCTYPE cXML SYSTEM \"cXML.dtd\">\n");
        cxml.append("<cXML payloadID=\"irrelevant\" xml:lang=\"en-US\" timestamp=\"").append(date.format(currentDate)).append("T")
            .append(time.format(currentDate)).append("-05:00").append("\">\n");

        // note that timezone is hard coded b/c this is the format
        // they wanted, but SimpleDateFormat returns -0500, so rather than
        // parse it just hard-coded

        cxml.append("  <Header>\n");
        cxml.append("    <From>\n");
        cxml.append("      <Credential domain=\"NetworkId\">\n");
        cxml.append("        <Identity>").append(b2bInformation.getIdentity()).append("</Identity>\n");
        cxml.append("      </Credential>\n");
        cxml.append("    </From>\n");
        cxml.append("    <To>\n");
        cxml.append("      <Credential domain=\"DUNS\">\n");
        cxml.append("        <Identity>").append(b2bInformation.getIdentity()).append("</Identity>\n");
        cxml.append("      </Credential>\n");
        cxml.append("      <Credential domain=\"internalsupplierid\">\n");
        cxml.append("        <Identity>1016</Identity>\n");
        cxml.append("      </Credential>\n");
        cxml.append("    </To>\n");
        cxml.append("    <Sender>\n");
        cxml.append("      <Credential domain=\"TOPSNetworkUserId\">\n");
        cxml.append("        <Identity>").append(user.getPrincipalName().toUpperCase(Locale.US)).append("</Identity>\n");
        cxml.append("        <SharedSecret>").append(b2bInformation.getPassword()).append("</SharedSecret>\n");
        cxml.append("      </Credential>\n");
        cxml.append("      <UserAgent>").append(b2bInformation.getUserAgent()).append("</UserAgent>\n");
        cxml.append("    </Sender>\n");
        cxml.append("  </Header>\n");
        cxml.append("  <Request deploymentMode=\"").append(b2bInformation.getEnvironment()).append("\">\n");
        cxml.append("    <PunchOutSetupRequest operation=\"create\">\n");
        cxml.append("      <BuyerCookie>").append(user.getPrincipalName().toUpperCase(Locale.US)).append("</BuyerCookie>\n");
        //cxml.append(" <Extrinsic
        // name=\"UserEmail\">jdoe@TOPS.com</Extrinsic>\n"); // we can't reliably
        // get the e-mail address, so we're leaving it out
        cxml.append("      <Extrinsic name=\"UserEmail\">").append(user.getEmailAddress()).append("</Extrinsic>\n"); 
        cxml.append("      <Extrinsic name=\"UniqueName\">").append(user.getPrincipalName().toUpperCase(Locale.US)).append("</Extrinsic>\n");
        cxml.append("      <Extrinsic name=\"PhoneNumber\">").append(user.getPhoneNumber()).append("</Extrinsic>\n");
        cxml.append("      <Extrinsic name=\"Department\">").append(user.getCampusCode()).append(user.getPrimaryDepartmentCode()).append("</Extrinsic>\n");
        cxml.append("      <Extrinsic name=\"Campus\">").append(user.getCampusCode()).append("</Extrinsic>\n");
        // KFSPTS-1720
        cxml.append("      <Extrinsic name=\"FirstName\">").append(user.getFirstName()).append("</Extrinsic>\n");
        cxml.append("      <Extrinsic name=\"LastName\">").append(user.getLastName()).append("</Extrinsic>\n");
        
        for (final String role : jaggaerRoleService.getJaggaerRoles(user, roleSet)) {
            cxml.append("      <Extrinsic name=\"Role\">").append(role).append("</Extrinsic>\n");
        }

        cxml.append("      <BrowserFormPost>\n");
        cxml.append("        <URL>").append(b2bInformation.getPunchbackURL()).append("</URL>\n");
        cxml.append("      </BrowserFormPost>\n");
        cxml.append("      <Contact role=\"endUser\">\n");
        cxml.append("        <Name xml:lang=\"en\">").append(user.getName()).append("</Name>\n");
        //cxml.append(" <Email>jdoe@TOPS.com</Email>\n"); // again, we can't
        // reliably get this, so we're leaving it out
        cxml.append("      </Contact>\n");
        cxml.append("      <SupplierSetup>\n");
        cxml.append("        <URL>").append(b2bInformation.getPunchoutURL()).append("</URL>\n");
        cxml.append("      </SupplierSetup>\n");
        cxml.append("    </PunchOutSetupRequest>\n");
        cxml.append("  </Request>\n");
        cxml.append("</cXML>\n");
        return cxml.toString();
      }
    
    // KFSUPGRADE-404
    /**
     * @see org.kuali.kfs.module.purap.document.service.impl.B2BShoppingServiceImpl#createRequisitionItem(org.kuali.kfs.module.purap.businessobject.B2BShoppingCartItem, java.lang.Integer, java.lang.String)
     */
    @Override
    protected RequisitionItem createRequisitionItem(
            final B2BShoppingCartItem item,
            final Integer itemLine, String defaultCommodityCode) {
        final RequisitionItem reqItem = super.createRequisitionItem(item, itemLine, defaultCommodityCode);

        final Integer requisitionItemDescriptionMaxLength = KNSServiceLocator.getDataDictionaryService().getAttributeMaxLength(RequisitionItem.class, PurapPropertyConstants.ITEM_DESCRIPTION);
        if (StringUtils.isNotBlank(reqItem.getItemDescription()) && reqItem.getItemDescription().length() > requisitionItemDescriptionMaxLength) {
            LOG.info("createRequisitionItem: Truncating RequisitionItem Description to " + requisitionItemDescriptionMaxLength + " characters");
            reqItem.setItemDescription(reqItem.getItemDescription().substring(0, requisitionItemDescriptionMaxLength));
        }

         final boolean commCodeParam = parameterService.getParameterValueAsBoolean(RequisitionDocument.class, PurapParameterConstants.ENABLE_DEFAULT_VENDOR_COMMODITY_CODE_IND);

         if (commCodeParam) {
             if (reqItem.getCommodityCode() != null && !reqItem.getCommodityCode().isActive() && StringUtils.isNotBlank(defaultCommodityCode)) {
                 reqItem.setPurchasingCommodityCode(defaultCommodityCode);
             }
         }
  	 
    	 // KFSPTS-2257 : eshopflag
         final Map<String, String> classification = item.getClassification();
         reqItem.setControlled(StringUtils.equals(TRUE,classification.get("Controlled")));
         reqItem.setRadioactiveMinor(StringUtils.equals(TRUE,classification.get("RadioactiveMinor")));
         reqItem.setGreen(StringUtils.equals(TRUE,classification.get("GreenProduct")));
         reqItem.setHazardous(StringUtils.equals(TRUE,classification.get("Hazardous")));
         reqItem.setSelectAgent(StringUtils.equals(TRUE,classification.get("SelectAgent")));
         reqItem.setRadioactive(StringUtils.equals(TRUE,classification.get("Radioactive")));
         reqItem.setToxin(StringUtils.equals(TRUE,classification.get("Toxin")));
         reqItem.setRecycled(StringUtils.equals(TRUE,classification.get("Green")));
         reqItem.setEnergyStar(StringUtils.equals(TRUE,classification.get("EnergyStar")));
         
    	 return reqItem;
    }

}
