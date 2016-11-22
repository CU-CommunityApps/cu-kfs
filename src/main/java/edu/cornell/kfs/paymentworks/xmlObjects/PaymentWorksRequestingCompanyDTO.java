package edu.cornell.kfs.paymentworks.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "requesting_company")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRequestingCompanyDTO {

	private String id;
	private String legal_name;
	private String desc;
	private String name;
	private PaymentWorksTaxClassificationDTO tax_classification;
	private String url;
	private String tin;
	private String tin_type;
	private String tax_country;
	private String w8_w9;
	private String telephone;
	private String duns;
	private PaymentWorksCorpAddressDTO corp_address;
	private PaymentWorksRemittanceAddressesDTO remittance_addresses;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLegal_name() {
		return legal_name;
	}

	public void setLegal_name(String legal_name) {
		this.legal_name = legal_name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTin() {
		return tin;
	}

	public void setTin(String tin) {
		this.tin = tin;
	}

	public String getW8_w9() {
		return w8_w9;
	}

	public void setW8_w9(String w8_w9) {
		this.w8_w9 = w8_w9;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getDuns() {
		return duns;
	}

	public void setDuns(String duns) {
		this.duns = duns;
	}

	public PaymentWorksCorpAddressDTO getCorp_address() {
		return corp_address;
	}

	public void setCorp_address(PaymentWorksCorpAddressDTO corp_address) {
		this.corp_address = corp_address;
	}

	public PaymentWorksRemittanceAddressesDTO getRemittance_addresses() {
		return remittance_addresses;
	}

	public void setRemittance_addresses(PaymentWorksRemittanceAddressesDTO remittance_addresses) {
		this.remittance_addresses = remittance_addresses;
	}

	public PaymentWorksTaxClassificationDTO getTax_classification() {
		return tax_classification;
	}

	public void setTax_classification(PaymentWorksTaxClassificationDTO tax_classification) {
		this.tax_classification = tax_classification;
	}

	public String getTin_type() {
		return tin_type;
	}

	public void setTin_type(String tin_type) {
		this.tin_type = tin_type;
	}

	public String getTax_country() {
		return tax_country;
	}

	public void setTax_country(String tax_country) {
		this.tax_country = tax_country;
	}

}
