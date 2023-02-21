
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "annualSalesYear", "annualSalesCurrencyCode", "annualSalesAmountString" })
@XmlRootElement(name = "AnnualSales")
public class AnnualSales {

    @XmlElement(name = "AnnualSalesYear")
    protected String annualSalesYear;
    @XmlElement(name = "AnnualSalesCurrencyCode")
    protected String annualSalesCurrencyCode;
    @XmlElement(name = "AnnualSalesAmountString")
    protected String annualSalesAmountString;

    public String getAnnualSalesYear() {
        return annualSalesYear;
    }

    public void setAnnualSalesYear(String value) {
        this.annualSalesYear = value;
    }

    public String getAnnualSalesCurrencyCode() {
        return annualSalesCurrencyCode;
    }

    public void setAnnualSalesCurrencyCode(String value) {
        this.annualSalesCurrencyCode = value;
    }

    public String getAnnualSalesAmountString() {
        return annualSalesAmountString;
    }

    public void setAnnualSalesAmountString(String value) {
        this.annualSalesAmountString = value;
    }

}
