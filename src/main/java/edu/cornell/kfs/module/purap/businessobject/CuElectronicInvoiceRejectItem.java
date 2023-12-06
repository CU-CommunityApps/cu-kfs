package edu.cornell.kfs.module.purap.businessobject;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceRejectItem;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;

public class CuElectronicInvoiceRejectItem extends ElectronicInvoiceRejectItem {

    public CuElectronicInvoiceRejectItem() {
        super();
    }

    public CuElectronicInvoiceRejectItem(
            final ElectronicInvoiceRejectDocument electronicInvoiceRejectDocument, 
            final ElectronicInvoiceItem eii) {
        super();
        // KFSUPGRADE-478 : change unitprice compare to "!=-1"
       // setup the sub total amount so that the reject prints to the files correctly
        if (StringUtils.isEmpty(eii.getSubTotalAmount())) {
            // the sub total amount of this electronic invoice item was not given
            if (getInvoiceItemQuantity() != null && BigDecimal.ZERO.compareTo(getInvoiceItemQuantity()) != 0
                    && getInvoiceItemUnitPrice() != null
                    && BigDecimal.ZERO.compareTo(getInvoiceItemUnitPrice()) != -1) {
                // unit price and quantity are valid... calculate subtotal
                setInvoiceItemSubTotalAmount(getInvoiceItemQuantity().multiply(getInvoiceItemUnitPrice()));
            }
            else if (getInvoiceItemQuantity() == null
                    && getInvoiceItemUnitPrice() != null
                    && BigDecimal.ZERO.compareTo(getInvoiceItemUnitPrice()) != -1) {
                // quantity is empty but unit cost exists... use it
            	setInvoiceItemSubTotalAmount(getInvoiceItemUnitPrice());
            }
            
        }
    }

}
