package edu.cornell.kfs.module.purap.fixture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.util.PurApDateFormatUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.CuPurapTestConstants;

public class CuElectronicInvoiceHelperServiceFixture {
        private static final String ATTRIBUTE_NAME_VALUE_FORMAT = "%s=\"%s\"";

        private static String vendorDUNSNumber;
        private static String poNumber;
        private static String docNumber;
        private static String invoiceDate;
        private static String itemQty; 
        
        public static String getCorruptedCXML(String vendorDUNS,String poNbr){
            vendorDUNSNumber = vendorDUNS;
            poNumber = poNbr;
            //Adding some text at the end of a valid cxml
            return getXMLChunk().concat("TestForCorruptedXML");
        }
        
        public static String getCorruptedCXML(String vendorDuns, String poNbr,
                String xmlnsAttributeName, String xmlnsXsiAttributeName) {
            return getCXMLForPaymentDocCreation(vendorDuns, poNbr, xmlnsAttributeName, xmlnsXsiAttributeName)
                    .concat("TestForCorruptedXML");
        }
        
        public static String getCXMLForPaymentDocCreation(String vendorDuns, String poNbr) {
            return getCXMLForPaymentDocCreation(vendorDuns, poNbr,
                    CuPurapTestConstants.XMLNS_ATTRIBUTE, CuPurapTestConstants.XMLNS_XSI_ATTRIBUTE);
        }
        
        public static String getCXMLForPaymentDocCreation(String vendorDuns, String poNbr,
                String xmlnsAttributeName, String xmlnsXsiAttributeName) {
            vendorDUNSNumber = vendorDuns;
            poNumber = poNbr;
            itemQty = "1";
            return getXMLChunk(xmlnsAttributeName, xmlnsXsiAttributeName);
        }

        public static String getCXMLForRejectDocCreation(String vendorDUNS,String poNbr){
            vendorDUNSNumber = vendorDUNS;
            poNumber = poNbr;
            itemQty = "100";
            return getXMLChunk();
        }
        
        private static String getXMLChunk(){
            return getXMLChunk(CuPurapTestConstants.XMLNS_ATTRIBUTE, CuPurapTestConstants.XMLNS_XSI_ATTRIBUTE);
        }
        
        private static String getXMLChunk(String xmlnsAttributeName, String xmlnsXsiAttributeName){
            
            StringBuffer xmlChunk = new StringBuffer();

            xmlChunk.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
            xmlChunk.append("<!-- ******Testing tool generated XML****** Version 1.2.\n\n");
            xmlChunk.append("Generated On").append(getCXMLDate(true)).append(" for PO ").append(poNumber).append(" Doc# ").append(docNumber).append(" -->\n\n");
            xmlChunk.append("<!-- All the cXML attributes are junk values -->\n");
            
            xmlChunk.append("<cXML payloadID=\"200807260401062080.964@eai002\"\n");
            xmlChunk.append("timestamp=").
                            append(getCXMLDate(true)).append("\n");
            
            xmlChunk.append("version=\"1.2.014\" xml:lang=\"en\"\n");
            
            if (StringUtils.isNotBlank(xmlnsAttributeName)) {
                xmlChunk.append(String.format(ATTRIBUTE_NAME_VALUE_FORMAT, xmlnsAttributeName, CuPurapTestConstants.EINVOICE_NAMESPACE_URL));
                xmlChunk.append(KFSConstants.NEWLINE);
            }
            
            if (StringUtils.isNotBlank(xmlnsXsiAttributeName)) {
                xmlChunk.append(String.format(ATTRIBUTE_NAME_VALUE_FORMAT, xmlnsXsiAttributeName, CuPurapTestConstants.XSI_NAMESPACE_URL));
            }
            xmlChunk.append(">\n");
            
            
            xmlChunk.append(getHeaderXMLChunk());
            xmlChunk.append(getRequestXMLChunk());
            
            xmlChunk.append("</cXML>");
            
            return xmlChunk.toString();
        }
        
        private static StringBuffer getHeaderXMLChunk(){
            
            StringBuffer header = new StringBuffer();
            
            header.append("<Header>");
            
            header.append("<From>");
            header.append("<Credential domain=\"DUNS\">");
            header.append("<Identity>" + vendorDUNSNumber + "</Identity>");
            header.append("</Credential>");
            header.append("</From>");
            
            header.append("<To>");
            header.append("<Credential domain=\"NetworkId\">");
            header.append("<Identity>CU</Identity>");
            header.append("</Credential>");
            header.append("</To>");
            
            header.append("<Sender>");
            header.append("<Credential domain=\"DUNS\">");
            header.append("<Identity>" + vendorDUNSNumber + "</Identity>");
            header.append("</Credential>");
            header.append("<UserAgent/>");
            header.append("</Sender>");
            
            header.append("</Header>");
            
            return header;
        }
        
        private static StringBuffer getRequestXMLChunk(){
            
            StringBuffer request = new StringBuffer();
            
            request.append("<Request deploymentMode=\"production\">");
            request.append("<InvoiceDetailRequest>");
            request.append("<InvoiceDetailRequestHeader invoiceID=\"LDR3496\" purpose=\"standard\" invoiceDate=" + getCXMLDate(true) +">");
            request.append("<InvoiceDetailHeaderIndicator/>");
            request.append("<InvoiceDetailLineIndicator/>");
            request.append("<InvoicePartner>");
            request.append("<Contact addressID=\"808231160\" role=\"billTo\">");
            request.append("<Name xml:lang=\"en\">IT - EAST HILL OFFICE BUILDING</Name>");
            request.append("<PostalAddress>");
            request.append("<Street>395 Pine Tree Rd</Street><Street></Street><City>Ithaca</City><State>NY</State><PostalCode>14850</PostalCode>");
            request.append("<Country isoCountryCode=\"US\">United States</Country>");
            request.append("</PostalAddress>");
            request.append("<Email name=\"kme44@cornell.edu\">kme44@cornell.edu</Email>");
            request.append("<Phone name=\"607-255-0818\"><TelephoneNumber><CountryCode isoCountryCode=\"US\">1</CountryCode><AreaOrCityCode>607</AreaOrCityCode><Number>-255-0818</Number></TelephoneNumber></Phone>");
            request.append("</Contact>");
            request.append("</InvoicePartner>");
            request.append("<InvoicePartner>");
            request.append("<Contact role=\"remitTo\" addressID=\"375264644\">");
            request.append("<Name xml:lang=\"en\">Dell Marketing LP.</Name>");
            request.append("<PostalAddress><Street>One Dell Way</Street><Street>RR 8-16</Street><City>Round Rock</City><State>TX</State><PostalCode>78682</PostalCode><Country isoCountryCode=\"US\">United States</Country></PostalAddress>");
            request.append("</Contact>");
            request.append("</InvoicePartner>");
            request.append("<InvoiceDetailShipping>");
            request.append("<Contact addressID=\"2026234409\" role=\"shipTo\">");
            request.append("<Name xml:lang=\"en\">IT - EAST HILL OFFICE BUILDING</Name>");
            request.append("<PostalAddress>");
            request.append("<Street>395 Pine Tree Rd</Street>");
            request.append("<Street></Street>");
            request.append("<City>Ithaca</City>");
            request.append("<State>NY</State>");
            request.append("<PostalCode>14850</PostalCode>");
            request.append("<Country isoCountryCode=\"US\">");
            request.append("United States");
            request.append("</Country>");
            request.append("</PostalAddress>");
            request.append("<Email name=\"kme44@cornell.edu\">kme44@cornell.edu</Email>");
            request.append("<Phone name=\"607-255-0818\">");
            request.append("<TelephoneNumber>");
            request.append("<CountryCode isoCountryCode=\"US\">1</CountryCode>");
            request.append("<AreaOrCityCode>607</AreaOrCityCode>");
            request.append("<Number>-255-0818</Number>");
            request.append("</TelephoneNumber>");
            request.append("</Phone>");

            request.append("</Contact></InvoiceDetailShipping>");
            request.append("<InvoiceDetailPaymentTerm payInNumberOfDays=\"30\" percentageRate=\"0\"/>");
            request.append("</InvoiceDetailRequestHeader>");
            
            request.append(getInvoiceOrderXMLChunk());
            request.append(getInvoiceSummaryXMLChunk());
            
            request.append("</InvoiceDetailRequest>");
            request.append("</Request>");

            return request;
            
        }
        
        public static StringBuffer getInvoiceOrderXMLChunk(){
            
            StringBuffer order = new StringBuffer();
            
            order.append("<InvoiceDetailOrder>");
            order.append("<InvoiceDetailOrderInfo>");
            order.append("<OrderReference orderID=\"" + poNumber + "\" orderDate=" + getCXMLDate(false) + ">");
            order.append("<DocumentReference payloadID=\"NA\" />");
            order.append("</OrderReference>");
            order.append("</InvoiceDetailOrderInfo>");
            order.append("<InvoiceDetailItem invoiceLineNumber=\"1\" quantity=\"" + itemQty + "\">");
            order.append("<UnitOfMeasure>EA</UnitOfMeasure>");
            order.append("<UnitPrice><Money currency=\"USD\">1.00</Money></UnitPrice>");
            order.append("<InvoiceDetailItemReference lineNumber=\"1\">");
            order.append("<ItemID><SupplierPartID>1234567</SupplierPartID></ItemID>");
            order.append("<Description xml:lang=\"en\">test</Description></InvoiceDetailItemReference>");
            order.append("<SubtotalAmount><Money currency=\"USD\">1.00</Money></SubtotalAmount>");
            order.append("</InvoiceDetailItem>");
            order.append("</InvoiceDetailOrder>");
            
            return order;
        }
        
        public static StringBuffer getInvoiceSummaryXMLChunk(){
            
            StringBuffer summary = new StringBuffer();
            
            summary.append("<InvoiceDetailSummary>");
            summary.append("<SubtotalAmount><Money currency=\"USD\">1.00</Money></SubtotalAmount>");
            summary.append("<Tax>");
            summary.append("<Money currency=\"USD\">0</Money>");
            summary.append("<Description xml:lang=\"en\">TOTAL TAX</Description>");
            summary.append("</Tax>");
            summary.append("<SpecialHandlingAmount><Money currency=\"USD\">0.00</Money></SpecialHandlingAmount>\n");
            summary.append("<ShippingAmount><Money currency=\"USD\">0.00</Money></ShippingAmount>");
            summary.append("<GrossAmount><Money currency=\"USD\">1.00</Money></GrossAmount>");
            summary.append("<InvoiceDetailDiscount><Money currency=\"USD\">0.00</Money></InvoiceDetailDiscount>");

            summary.append("<NetAmount><Money currency=\"USD\">10.00</Money></NetAmount>");
            summary.append("<DepositAmount><Money currency=\"USD\">2.00</Money></DepositAmount>");
            summary.append("<DueAmount><Money currency=\"USD\">10.00</Money></DueAmount>");
            summary.append("</InvoiceDetailSummary>");
            
            return summary;
        }
        
        private static String getCXMLDate(boolean includeTime){
            
            StringBuffer dateString = new StringBuffer();
            
            Date d = new Date();
            SimpleDateFormat date = getSimpleDateFormat(PurapConstants.NamedDateFormats.CXML_SIMPLE_DATE_FORMAT);
            SimpleDateFormat time = getSimpleDateFormat(PurapConstants.NamedDateFormats.CXML_SIMPLE_TIME_FORMAT);
            
            dateString.append("\"" + date.format(d)).append("T");
            if (includeTime){
                dateString.append(time.format(d)).append("-05:00");
            }
            
            dateString.append("\"");
                    
            return dateString.toString();
            
        }

        private static SimpleDateFormat getSimpleDateFormat(String formatName) {
            if (SpringContext.isInitialized()) {
                return PurApDateFormatUtils.getSimpleDateFormat(formatName);
            } else if (StringUtils.equals(PurapConstants.NamedDateFormats.CXML_SIMPLE_DATE_FORMAT, formatName)) {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            } else if (StringUtils.equals(PurapConstants.NamedDateFormats.CXML_SIMPLE_TIME_FORMAT, formatName)) {
                return new SimpleDateFormat("HH:mm:ss.sss", Locale.US);
            } else {
                throw new IllegalArgumentException("Unexpected format name: " + formatName);
            }
        }
}
