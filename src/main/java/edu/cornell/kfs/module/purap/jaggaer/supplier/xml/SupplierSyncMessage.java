package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerGenerateSupplierXmlStep;
import edu.cornell.kfs.sys.businessobject.XmlFragmentable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "header",
        "supplierSyncMessageItems" })
@XmlRootElement(name = "SupplierSyncMessage")
public class SupplierSyncMessage implements XmlFragmentable {

    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String version;
    @XmlElement(name = "Header", required = true)
    private Header header;
    
    private transient ParameterService parameterService;
    
    /*
     * XJC produced this XML annotation.  We only need SupplierRequestMessage and SupplierResponseMessage for the upload suppliers functionality.
     * THe following classes can be passed into this collection: LookupRequestMessage and LookupResponseMessage.
     * Those classes have been removed as they aren't required at this time.
     * If they are needed, this object can be updated to include them.  To regenerate the classes and child objects, run the following command
     * 
     * xjc -p edu.cornell.kfs.module.purap.jaggaer.supplier.xml -no-header -dtd TSMSupplierXML.dtd
     */
    @XmlElements({ @XmlElement(name = "SupplierRequestMessage", required = true, type = SupplierRequestMessage.class),
        @XmlElement(name = "SupplierResponseMessage", required = true, type = SupplierResponseMessage.class)})
    private List<SupplierSyncMessageItem> supplierSyncMessageItems;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<SupplierSyncMessageItem> getSupplierSyncMessageItems() {
        if (supplierSyncMessageItems == null) {
            supplierSyncMessageItems = new ArrayList<SupplierSyncMessageItem>();
        }
        return supplierSyncMessageItems;
    }

    @Override
    public String getXmlPrefix() {
        return getVersionTag() + KFSConstants.NEWLINE + getDocTypeTag();
    }
    
    private String getVersionTag() {
        return getParameterService().getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class, 
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_VERSION_NUMBER_TAG);
    }
    
    private String getDocTypeTag() {
        return getParameterService().getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class, 
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_DTD_DOCTYPE_TAG);
    }
    
    @Override
    public boolean shouldMarshalAsFragment() {
        return true;
    }

    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
