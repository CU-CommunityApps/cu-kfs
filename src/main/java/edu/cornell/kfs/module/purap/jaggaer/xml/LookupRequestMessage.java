package edu.cornell.kfs.module.purap.jaggaer.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "resultCount", "startResult",
        "supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber", "relationship",
        "includeDeleted", "internalERPName" })
@XmlRootElement(name = "LookupRequestMessage")
public class LookupRequestMessage {

    @XmlElement(name = "ResultCount")
    protected String resultCount;
    @XmlElement(name = "StartResult")
    protected String startResult;
    @XmlElements({ @XmlElement(name = "SupplierSQId", required = true, type = SupplierSQId.class),
            @XmlElement(name = "SupplierNumber", required = true, type = SupplierNumber.class),
            @XmlElement(name = "TaxIdentificationNumber", required = true, type = TaxIdentificationNumber.class),
            @XmlElement(name = "DUNS", required = true, type = DUNS.class),
            @XmlElement(name = "ThirdPartyRefNumber", required = true, type = ThirdPartyRefNumber.class) })
    protected List<Object> supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber;
    @XmlElement(name = "Relationship")
    protected String relationship;
    @XmlElement(name = "IncludeDeleted")
    protected String includeDeleted;
    @XmlElement(name = "InternalERPName")
    protected String internalERPName;

    public String getResultCount() {
        return resultCount;
    }

    public void setResultCount(String value) {
        this.resultCount = value;
    }

    public String getStartResult() {
        return startResult;
    }

    public void setStartResult(String value) {
        this.startResult = value;
    }

    public List<Object> getSupplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber() {
        if (supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber == null) {
            supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber = new ArrayList<Object>();
        }
        return this.supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String value) {
        this.relationship = value;
    }

    public String getIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(String value) {
        this.includeDeleted = value;
    }

    public String getInternalERPName() {
        return internalERPName;
    }

    public void setInternalERPName(String value) {
        this.internalERPName = value;
    }

}
