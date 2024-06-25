
package edu.cornell.kfs.module.purap.businessobject.xml;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.cornell.kfs.module.purap.businessobject.xml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SourceNumber_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "sourceNumber");
    private final static QName _VendorName_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "vendorName");
    private final static QName _AccountNumber_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "accountNumber");
    private final static QName _ServicePerformedOnCampus_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "servicePerformedOnCampus");
    private final static QName _AmountOrPercent_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "amountOrPercent");
    private final static QName _DepartmentLevelOrganization_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "departmentLevelOrganization");
    private final static QName _ItemUnitOfMeasureCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "itemUnitOfMeasureCode");
    private final static QName _AdHocRouteToNetID_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "adHocRouteToNetID");
    private final static QName _ItemUnitPrice_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "itemUnitPrice");
    private final static QName _RequestorPhoneNumber_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "requestorPhoneNumber");
    private final static QName _ItemQuantity_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "itemQuantity");
    private final static QName _MimeTypeCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "mimeTypeCode");
    private final static QName _DeliverToPhoneNumber_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "deliverToPhoneNumber");
    private final static QName _FinancialSubObjectCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "financialSubObjectCode");
    private final static QName _AccountDescriptionTxt_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "accountDescriptionTxt");
    private final static QName _UseAmountOrPercent_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "useAmountOrPercent");
    private final static QName _CollegeLevelOrganization_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "collegeLevelOrganization");
    private final static QName _SubAccountNumber_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "subAccountNumber");
    private final static QName _OrganizationReferenceId_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "organizationReferenceId");
    private final static QName _DeliverToNetID_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "deliverToNetID");
    private final static QName _PurchasingCommodityCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "purchasingCommodityCode");
    private final static QName _CommentsAndSpecialInstructions_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "commentsAndSpecialInstructions");
    private final static QName _BusinessPurpose_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "businessPurpose");
    private final static QName _DeliverToEmailAddress_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "deliverToEmailAddress");
    private final static QName _SameAsInitiator_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "sameAsInitiator");
    private final static QName _VendorId_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "vendorId");
    private final static QName _Goods_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "goods");
    private final static QName _RequestorAddress_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "requestorAddress");
    private final static QName _FinancialObjectCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "financialObjectCode");
    private final static QName _AttachmentType_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "attachmentType");
    private final static QName _DeliverToAddress_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "deliverToAddress");
    private final static QName _Initiator_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "initiator");
    private final static QName _ChartOfAccountsCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "chartOfAccountsCode");
    private final static QName _NoteText_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "noteText");
    private final static QName _SameAsRequestor_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "sameAsRequestor");
    private final static QName _FileName_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "fileName");
    private final static QName _RequestorEmailAddress_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "requestorEmailAddress");
    private final static QName _RequestorNetID_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "requestorNetID");
    private final static QName _VendorDescription_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "vendorDescription");
    private final static QName _ItemCatalogNumber_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "itemCatalogNumber");
    private final static QName _ItemDescription_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "itemDescription");
    private final static QName _ProjectCode_QNAME = new QName("http://www.kuali.org/kfs/purap/iWantDocument", "projectCode");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.cornell.kfs.module.purap.businessobject.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Note }
     * 
     */
    public Note createNote() {
        return new Note();
    }

    /**
     * Create an instance of {@link IWantDocFile }
     * 
     */
    public IWantDocFile createIWantDocFile() {
        return new IWantDocFile();
    }

    /**
     * Create an instance of {@link IWantDocument }
     * 
     */
    public IWantDocument createIWantDocument() {
        return new IWantDocument();
    }

    /**
     * Create an instance of {@link Item }
     * 
     */
    public Item createItem() {
        return new Item();
    }

    /**
     * Create an instance of {@link Account }
     * 
     */
    public Account createAccount() {
        return new Account();
    }

    /**
     * Create an instance of {@link Attachment }
     * 
     */
    public Attachment createAttachment() {
        return new Attachment();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "sourceNumber")
    public JAXBElement<String> createSourceNumber(String value) {
        return new JAXBElement<String>(_SourceNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "vendorName")
    public JAXBElement<String> createVendorName(String value) {
        return new JAXBElement<String>(_VendorName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "accountNumber")
    public JAXBElement<String> createAccountNumber(String value) {
        return new JAXBElement<String>(_AccountNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IndicatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "servicePerformedOnCampus")
    public JAXBElement<IndicatorType> createServicePerformedOnCampus(IndicatorType value) {
        return new JAXBElement<IndicatorType>(_ServicePerformedOnCampus_QNAME, IndicatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "amountOrPercent")
    public JAXBElement<BigDecimal> createAmountOrPercent(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_AmountOrPercent_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "departmentLevelOrganization")
    public JAXBElement<String> createDepartmentLevelOrganization(String value) {
        return new JAXBElement<String>(_DepartmentLevelOrganization_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "itemUnitOfMeasureCode")
    public JAXBElement<String> createItemUnitOfMeasureCode(String value) {
        return new JAXBElement<String>(_ItemUnitOfMeasureCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "adHocRouteToNetID")
    public JAXBElement<String> createAdHocRouteToNetID(String value) {
        return new JAXBElement<String>(_AdHocRouteToNetID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "itemUnitPrice")
    public JAXBElement<BigDecimal> createItemUnitPrice(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_ItemUnitPrice_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "requestorPhoneNumber")
    public JAXBElement<String> createRequestorPhoneNumber(String value) {
        return new JAXBElement<String>(_RequestorPhoneNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "itemQuantity")
    public JAXBElement<BigDecimal> createItemQuantity(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_ItemQuantity_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "mimeTypeCode")
    public JAXBElement<String> createMimeTypeCode(String value) {
        return new JAXBElement<String>(_MimeTypeCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "deliverToPhoneNumber")
    public JAXBElement<String> createDeliverToPhoneNumber(String value) {
        return new JAXBElement<String>(_DeliverToPhoneNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "financialSubObjectCode")
    public JAXBElement<String> createFinancialSubObjectCode(String value) {
        return new JAXBElement<String>(_FinancialSubObjectCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "accountDescriptionTxt")
    public JAXBElement<String> createAccountDescriptionTxt(String value) {
        return new JAXBElement<String>(_AccountDescriptionTxt_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AmountOrPercentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "useAmountOrPercent")
    public JAXBElement<AmountOrPercentType> createUseAmountOrPercent(AmountOrPercentType value) {
        return new JAXBElement<AmountOrPercentType>(_UseAmountOrPercent_QNAME, AmountOrPercentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "collegeLevelOrganization")
    public JAXBElement<String> createCollegeLevelOrganization(String value) {
        return new JAXBElement<String>(_CollegeLevelOrganization_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "subAccountNumber")
    public JAXBElement<String> createSubAccountNumber(String value) {
        return new JAXBElement<String>(_SubAccountNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "organizationReferenceId")
    public JAXBElement<String> createOrganizationReferenceId(String value) {
        return new JAXBElement<String>(_OrganizationReferenceId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "deliverToNetID")
    public JAXBElement<String> createDeliverToNetID(String value) {
        return new JAXBElement<String>(_DeliverToNetID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "purchasingCommodityCode")
    public JAXBElement<String> createPurchasingCommodityCode(String value) {
        return new JAXBElement<String>(_PurchasingCommodityCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "commentsAndSpecialInstructions")
    public JAXBElement<String> createCommentsAndSpecialInstructions(String value) {
        return new JAXBElement<String>(_CommentsAndSpecialInstructions_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "businessPurpose")
    public JAXBElement<String> createBusinessPurpose(String value) {
        return new JAXBElement<String>(_BusinessPurpose_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "deliverToEmailAddress")
    public JAXBElement<String> createDeliverToEmailAddress(String value) {
        return new JAXBElement<String>(_DeliverToEmailAddress_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IndicatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "sameAsInitiator")
    public JAXBElement<IndicatorType> createSameAsInitiator(IndicatorType value) {
        return new JAXBElement<IndicatorType>(_SameAsInitiator_QNAME, IndicatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "vendorId")
    public JAXBElement<String> createVendorId(String value) {
        return new JAXBElement<String>(_VendorId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IndicatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "goods")
    public JAXBElement<IndicatorType> createGoods(IndicatorType value) {
        return new JAXBElement<IndicatorType>(_Goods_QNAME, IndicatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "requestorAddress")
    public JAXBElement<String> createRequestorAddress(String value) {
        return new JAXBElement<String>(_RequestorAddress_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "financialObjectCode")
    public JAXBElement<String> createFinancialObjectCode(String value) {
        return new JAXBElement<String>(_FinancialObjectCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "attachmentType")
    public JAXBElement<String> createAttachmentType(String value) {
        return new JAXBElement<String>(_AttachmentType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "deliverToAddress")
    public JAXBElement<String> createDeliverToAddress(String value) {
        return new JAXBElement<String>(_DeliverToAddress_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "initiator")
    public JAXBElement<String> createInitiator(String value) {
        return new JAXBElement<String>(_Initiator_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "chartOfAccountsCode")
    public JAXBElement<String> createChartOfAccountsCode(String value) {
        return new JAXBElement<String>(_ChartOfAccountsCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "noteText")
    public JAXBElement<String> createNoteText(String value) {
        return new JAXBElement<String>(_NoteText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IndicatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "sameAsRequestor")
    public JAXBElement<IndicatorType> createSameAsRequestor(IndicatorType value) {
        return new JAXBElement<IndicatorType>(_SameAsRequestor_QNAME, IndicatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "fileName")
    public JAXBElement<String> createFileName(String value) {
        return new JAXBElement<String>(_FileName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "requestorEmailAddress")
    public JAXBElement<String> createRequestorEmailAddress(String value) {
        return new JAXBElement<String>(_RequestorEmailAddress_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "requestorNetID")
    public JAXBElement<String> createRequestorNetID(String value) {
        return new JAXBElement<String>(_RequestorNetID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "vendorDescription")
    public JAXBElement<String> createVendorDescription(String value) {
        return new JAXBElement<String>(_VendorDescription_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "itemCatalogNumber")
    public JAXBElement<String> createItemCatalogNumber(String value) {
        return new JAXBElement<String>(_ItemCatalogNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "itemDescription")
    public JAXBElement<String> createItemDescription(String value) {
        return new JAXBElement<String>(_ItemDescription_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kuali.org/kfs/purap/iWantDocument", name = "projectCode")
    public JAXBElement<String> createProjectCode(String value) {
        return new JAXBElement<String>(_ProjectCode_QNAME, String.class, null, value);
    }

}
