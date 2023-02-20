
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
