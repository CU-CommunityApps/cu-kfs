package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalNullPossibleXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contractId",
    "contractName",
    "contractNumber",
    "contractType",
    "contractStatus",
    "summary",
    "contractValue",
    "contractParties",
    "customFields",
    "managerList",
    "attachments"
})
@XmlRootElement(name = "Contract")
public class Contract {

    @XmlElement(name = "ContractId")
    private String contractId;

    @XmlElement(name = "ContractName")
    private String contractName;

    @XmlElement(name = "ContractNumber")
    private String contractNumber;

    @XmlElement(name = "ContractType")
    private String contractType;

    @XmlElement(name = "ContractStatus")
    private String contractStatus;

    @XmlElement(name = "Summary")
    private String summary;

    @XmlElement(name = "ContractValue")
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal contractValue;

    @XmlElementWrapper(name = "ContractPartiesList")
    @XmlElements({
        @XmlElement(name = "FirstParty", type = FirstParty.class),
        @XmlElement(name = "SecondParty", type = SecondParty.class)
    })
    private List<ContractPartyBase> contractParties;

    @XmlElementWrapper(name = "CustomFieldList")
    @XmlElement(name = "CustomField")
    private List<CustomField> customFields;

    @XmlElement(name = "ManagerList")
    private ManagerList managerList;

    @XmlElementWrapper(name = "AttachmentList")
    @XmlElement(name = "Attachment")
    private List<Attachment> attachments;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(String contractStatus) {
        this.contractStatus = contractStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public KualiDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(KualiDecimal contractValue) {
        this.contractValue = contractValue;
    }

    public List<ContractPartyBase> getContractParties() {
        if (contractParties == null) {
            contractParties = new ArrayList<>();
        }
        return contractParties;
    }

    public void setContractParties(List<ContractPartyBase> contractParties) {
        this.contractParties = contractParties;
    }

    public List<CustomField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomField> customFields) {
        this.customFields = customFields;
    }

    public ManagerList getManagerList() {
        return managerList;
    }

    public void setManagerList(ManagerList managerList) {
        this.managerList = managerList;
    }

    public List<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

}
