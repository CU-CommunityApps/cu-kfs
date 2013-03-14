function loadItemUnitOfMeasureInfo( itemUnitOfMeasureCode, itemUnitOfMeasureFieldName ) {
    var purchasingItemUnitOfMeasureCode = DWRUtil.getValue( itemUnitOfMeasureCode );
    var containerDiv = document.getElementById(itemUnitOfMeasureFieldName + divSuffix);

    if (purchasingItemUnitOfMeasureCode == "") {
        DWRUtil.setValue( containerDiv.id, "&nbsp;" );
    } else {
        var dwrReply = {
            callback:function(data) {
            if ( data != null && typeof data == 'object' ) {
                DWRUtil.setValue(containerDiv.id, data.itemUnitOfMeasureDescription, {escapeHtml:true} );
            } else {
                DWRUtil.setValue(containerDiv.id, wrapError( "item unit of measure code not found" ));
            } },
            errorHandler:function( errorMessage ) { 
                DWRUtil.setValue(containerDiv.id, wrapError( "item unit of measure code not found" ));
            }
        };
        ItemUnitOfMeasureService.getByPrimaryId( purchasingItemUnitOfMeasureCode, dwrReply );
    }
}

// KFSPTS-1768
function loadCommodityCodeDescription( purCommodityCode, commodityCodeFieldName ) {
    var purchasingCommodityCode = DWRUtil.getValue( purCommodityCode );
    var containerDiv = document.getElementById(commodityCodeFieldName + divSuffix);

    if (purchasingCommodityCode == "") {
    	DWRUtil.setValue( containerDiv.id, " " );
    } else {
        var dwrReply = {
            callback:function(data) {
            if ( data != null && typeof data == 'object' ) {
            	DWRUtil.setValue(containerDiv.id, data.commodityDescription, {escapeHtml:true} );
            } else {
            	setRecipientValue(containerDiv.id, wrapError("Commodity Code not found"), true);            	
            } },
            errorHandler:function( errorMessage ) { 
            	setRecipientValue(containerDiv.id, wrapError("Commodity Code not found"), true);
            }
        };
        CommodityCodeService.getByPrimaryId( purchasingCommodityCode, dwrReply );
    }
}

