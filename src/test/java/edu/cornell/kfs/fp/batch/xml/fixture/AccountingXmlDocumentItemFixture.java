package edu.cornell.kfs.fp.batch.xml.fixture;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.fp.businessobject.InternalBillingItem;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentItem;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingXmlDocumentItemFixture {
    STAPLERS_QTY_5_COST_20_00("02/10/2018", "1234567", "Staplers", 5, CuFPTestConstants.UOM_EACH, 20.00),
    HEADPHONES_QTY_1_COST_50_00("02/10/2018", "5555555", "Headphones", 1, CuFPTestConstants.UOM_DOZEN, 50.00);

    public final String serviceDate;
    public final String stockNumber;
    public final String description;
    public final BigDecimal quantity;
    public final String unitOfMeasureCode;
    public final KualiDecimal itemCost;

    private AccountingXmlDocumentItemFixture(int quantity, double unitCost) {
        this(null, null, null, quantity, null, unitCost);
    }

    private AccountingXmlDocumentItemFixture(String serviceDate, String stockNumber, String description,
            int quantity, String unitOfMeasureCode, double itemCost) {
        this.serviceDate = serviceDate;
        this.stockNumber = stockNumber;
        this.description = description;
        this.quantity = BigDecimal.valueOf(quantity);
        this.unitOfMeasureCode = unitOfMeasureCode;
        this.itemCost = new KualiDecimal(itemCost);
    }

    public AccountingXmlDocumentItem toItemPojo() {
        AccountingXmlDocumentItem xmlItem = new AccountingXmlDocumentItem();
        
        setServiceDateIfDefined(xmlItem::setServiceDate, DateTime::toDate);
        xmlItem.setStockNumber(stockNumber);
        xmlItem.setDescription(description);
        xmlItem.setQuantity(quantity);
        xmlItem.setUnitOfMeasureCode(unitOfMeasureCode);
        xmlItem.setItemCost(itemCost);
        
        return xmlItem;
    }

    public InternalBillingItem toInternalBillingItem(String documentNumber) {
        InternalBillingItem item = new InternalBillingItem();
        
        item.setDocumentNumber(documentNumber);
        setServiceDateIfDefined(item::setItemServiceDate, (dateTime) -> new Timestamp(dateTime.getMillis()));
        item.setItemStockNumber(stockNumber);
        item.setItemStockDescription(description);
        item.setItemQuantity(quantity);
        item.setUnitOfMeasureCode(unitOfMeasureCode);
        item.setItemUnitAmount(itemCost);
        
        return item;
    }

    private <T extends Date> void setServiceDateIfDefined(
            Consumer<T> datePropertySetter, Function<DateTime,T> dateTimeToDateObjectConverter) {
        if (StringUtils.isNotBlank(serviceDate)) {
            DateTime parsedServiceDate = StringToJavaDateAdapter.parseToDateTime(serviceDate);
            T dateObject = dateTimeToDateObjectConverter.apply(parsedServiceDate);
            datePropertySetter.accept(dateObject);
        }
    }

}
