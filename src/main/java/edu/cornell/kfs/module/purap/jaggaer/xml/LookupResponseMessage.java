package edu.cornell.kfs.module.purap.jaggaer.xml;

import java.util.ArrayList;
import java.util.List;
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
@XmlType(name = "", propOrder = { "lookupStatus", "organizationId", "supplierOrSupplierList" })
@XmlRootElement(name = "LookupResponseMessage")
public class LookupResponseMessage {

    @XmlAttribute(name = "totalResults", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String totalResults;
    @XmlAttribute(name = "startResult", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String startResult;
    @XmlAttribute(name = "resultSize", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String resultSize;
    @XmlElement(name = "LookupStatus", required = true)
    protected LookupStatus lookupStatus;
    @XmlElement(name = "OrganizationId")
    protected String organizationId;
    @XmlElements({ @XmlElement(name = "Supplier", type = Supplier.class),
            @XmlElement(name = "SupplierList", type = SupplierList.class) })
    protected List<Object> supplierOrSupplierList;

    public String getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(String value) {
        this.totalResults = value;
    }

    public String getStartResult() {
        return startResult;
    }

    public void setStartResult(String value) {
        this.startResult = value;
    }

    public String getResultSize() {
        return resultSize;
    }

    public void setResultSize(String value) {
        this.resultSize = value;
    }

    public LookupStatus getLookupStatus() {
        return lookupStatus;
    }

    public void setLookupStatus(LookupStatus value) {
        this.lookupStatus = value;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String value) {
        this.organizationId = value;
    }

    public List<Object> getSupplierOrSupplierList() {
        if (supplierOrSupplierList == null) {
            supplierOrSupplierList = new ArrayList<Object>();
        }
        return this.supplierOrSupplierList;
    }

}
