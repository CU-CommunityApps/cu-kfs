function loadDeliverToInfoSameAsInitiator(sameAsInitiatorFieldName, requestorNetIDFieldName, deliverToNetIDFieldName,
		requestorNameFieldName, deliverToNameFieldName, requestorPhoneNbrFieldName, deliverToPhoneNbrFieldName, requestorEmailFieldName,
		deliverToEmailFieldName, requestorAddressFieldName, deliverToAddressFieldName) {

	var sameAsInitiatorChecked;

	var radioButton=(document.getElementsByName("document.sameAsInitiator"))[0];
	if (radioButton.checked) {
		sameAsInitiatorChecked = radioButton.value;
	}

	if(sameAsInitiatorChecked == 'on') {
		var requestorNetID = dwr.util.getValue( requestorNetIDFieldName ).trim();
		var deliverToNetID = dwr.util.getValue( deliverToNetIDFieldName ).trim();
		var requestorName = dwr.util.getValue( requestorNameFieldName ).trim();
		var deliverToName = dwr.util.getValue( deliverToNameFieldName ).trim();
		var requestorPhoneNbr = dwr.util.getValue( requestorPhoneNbrFieldName ).trim();
		var requestorEmail = dwr.util.getValue( requestorEmailFieldName ).trim();
		var requestorAddress = dwr.util.getValue( requestorAddressFieldName ).trim();

		setRecipientValue( deliverToNetIDFieldName, requestorNetID );
		setRecipientValue( deliverToNameFieldName, requestorName );
		setRecipientValue( deliverToPhoneNbrFieldName, requestorPhoneNbr );
		setRecipientValue( deliverToEmailFieldName, requestorEmail );
		setRecipientValue( deliverToAddressFieldName, requestorAddress );
	} else {
		clearRecipients( deliverToNetIDFieldName );
		clearRecipients( deliverToNameFieldName );
		clearRecipients( deliverToPhoneNbrFieldName );
		clearRecipients( deliverToEmailFieldName );
		clearRecipients( deliverToAddressFieldName );
	}
}

function loadDeliverToInfo(sameAsInitiatorFieldName, deliverToNetIDFieldName, deliverToNameFieldName,
		deliverToPhoneNbrFieldName, deliverToEmailFieldName, deliverToAddressFieldName) {

	var deliverToNetID = dwr.util.getValue( deliverToNetIDFieldName ).trim();

	if (deliverToNetID == "") {
		// clearRecipients( deliverToNameFieldName );
		clearRecipients( deliverToPhoneNbrFieldName );
		clearRecipients( deliverToEmailFieldName );
		clearRecipients( deliverToAddressFieldName );
	} else {
		var dwrReply = {
				callback:function(data) {
					if ( data != null && typeof data == 'object' ) {

						//setRecipientValue( deliverToNameFieldName, data.personName );
						setRecipientValue( deliverToPhoneNbrFieldName, data.phoneNumber );
						setRecipientValue( deliverToEmailFieldName, data.emailAddress );

						setRecipientValue( deliverToAddressFieldName, data.campusAddress );

					} else {

						//clearRecipients( deliverToNameFieldName );
						clearRecipients( deliverToPhoneNbrFieldName );
						clearRecipients( deliverToEmailFieldName );
						clearRecipients( deliverToAddressFieldName );

						// setRecipientValue( deliverToNameFieldName, wrapError( "Person not found" ), true );
					} },
					errorHandler:function( errorMessage ) {
						//alert(errorMessage);

						//clearRecipients( deliverToNameFieldName );
						clearRecipients( deliverToPhoneNbrFieldName );
						clearRecipients( deliverToEmailFieldName );
						clearRecipients( deliverToAddressFieldName );

						// setRecipientValue( deliverToNameFieldName, wrapError( "Person not found error" ), true );
					}
		};
		IWantDocumentService.getPersonData( deliverToNetID, dwrReply );
	}
}

function loadRequestorInfo(sameAsInitiatorFieldName, requestorNetIDFieldName, requestorNameFieldName,
		requestorPhoneNbrFieldName, requestorEmailFieldName, requestorAddressFieldName) {

	var requestorNetID = dwr.util.getValue( requestorNetIDFieldName ).trim();


	if (requestorNetID == "") {
		//clearRecipients( requestorNameFieldName );
		clearRecipients( requestorPhoneNbrFieldName );
		clearRecipients( requestorEmailFieldName );
		clearRecipients( requestorAddressFieldName );
	} else {
		var dwrReply = {
				callback:function(data) {
					if ( data != null && typeof data == 'object' ) {

						//setRecipientValue( requestorNameFieldName, data.personName );
						setRecipientValue( requestorPhoneNbrFieldName, data.phoneNumber );
						setRecipientValue( requestorEmailFieldName, data.emailAddress );

						setRecipientValue( requestorAddressFieldName, data.campusAddress );

					} else {

						// clearRecipients( requestorNameFieldName );
						clearRecipients( requestorPhoneNbrFieldName );
						clearRecipients( requestorEmailFieldName );
						clearRecipients( requestorAddressFieldName );

						// setRecipientValue( requestorNameFieldName, wrapError( "Person not found" ), true );
					} },
					errorHandler:function( errorMessage ) {

						// clearRecipients( requestorNameFieldName );
						clearRecipients( requestorPhoneNbrFieldName );
						clearRecipients( requestorEmailFieldName );
						clearRecipients( requestorAddressFieldName );

						// setRecipientValue( requestorNameFieldName, wrapError( "Person not found error" ), true );
					}
		};
		IWantDocumentService.getPersonData( requestorNetID, dwrReply );
	}
}

function loadProcessorInfo(processorNetIDFieldName, processorNameFieldName) {
	IWantDocumentService.getPersonData( processorNetID, dwrReply );
}

function loadDepartments(form) {
	// set the previously selected org
	setRecipientValue( 'previousSelectedOrg', 'document.collegeLevelOrganization' );

	submitForm();
}

function loadAccountName(accountNumberFieldName, chartFieldName, accountNameFieldName) {
	var accountNumber = dwr.util.getValue( accountNumberFieldName );
	var chart = dwr.util.getValue( chartFieldName );

	if (accountNumber != '') {
		accountNumber = accountNumber.toUpperCase();
		setRecipientValue(accountNumberFieldName, accountNumber);	
	}

	if (accountNumber =='' || chart == '') {
		setRecipientValue(accountNameFieldName, "");
	} else {
		var dwrReply = {
				callback:function(data) {
					if ( data != null && typeof data == 'object' ) {
						setRecipientValue(accountNameFieldName, data.accountName);
					} else {
						setRecipientValue(accountNameFieldName, wrapError( "account not found"), true);	
					} },
					errorHandler:function(errorMessage ) {
						setRecipientValue(accountNameFieldName, wrapError( "account not found"), true);	
					}
		};

		AccountService.getByPrimaryIdWithCaching(chart, accountNumber, dwrReply);
	}
 }

function updateAccountsTotal(totalDollarAmountFieldName, totalAccountsField, itemsNbr, lineNbr) {
	var itemsTotal = getElementValue(totalDollarAmountFieldName);

	var useAmountsOrPercents = new Array();
	var accountAmountsOrPercents = new Array();

	var i = 0;
	for (i = 0; i < lineNbr; i++) {
		useAmountsOrPercents[i] = dwr.util.getValue('document.account[' + i + '].useAmountOrPercent');
		accountAmountsOrPercents[i] = dwr.util.getValue('document.account[' + i + '].amountOrPercent');
		if (isNaN(accountAmountsOrPercents[i])) {
			accountAmountsOrPercents[i] = "";
		}
	}

	var updateTotalsCallback = {callback:function(totals){
		// Update totals accordingly upon server callback.
		updateTotal(totalAccountsField, totals[0]);
		updateItemAndAccountDifference(totals[1]);
	}};

	// Send the data to the server for computation.
	IWantAmountUtil.calculateTotalsForAccountChange(useAmountsOrPercents, accountAmountsOrPercents, itemsTotal, updateTotalsCallback);
}

function updateItemsTotal(totalDollarAmountFieldName, totalAccountsField, itemsNbr, accountsNbr, totalItemAmountFieldName, itemNbr) {

	var i = 0;

	var itemQuantities = new Array();
	var itemUnitPrices = new Array();
	var useAmountsOrPercents = new Array();
	var accountAmountsOrPercents = new Array();

	for (i = 0; i < itemsNbr; i++) {
		itemQuantities[i] = dwr.util.getValue('document.item[' + i + '].itemQuantity');
		if (isNaN(itemQuantities[i])) {
			itemQuantities[i] = "";
		}
		itemUnitPrices[i] = dwr.util.getValue('document.item[' + i + '].itemUnitPrice');
		if (isNaN(itemUnitPrices[i])) {
			itemUnitPrices[i] = "";
		}
	}

	for (i = 0; i < accountsNbr; i++) {
		useAmountsOrPercents[i] = dwr.util.getValue('document.account[' + i + '].useAmountOrPercent');
		accountAmountsOrPercents[i] = dwr.util.getValue('document.account[' + i + '].amountOrPercent');
		if (isNaN(accountAmountsOrPercents[i])) {
			accountAmountsOrPercents[i] = "";
		}
	}

	var updateTotalsCallback = {callback:function(totals){
		// Update relevant total amounts fields upon server callback.
		updateTotal(totalItemAmountFieldName, totals[0]);
		updateTotal(totalDollarAmountFieldName, totals[1]);
		updateTotal(totalAccountsField, totals[2]);
		updateItemAndAccountDifference(totals[3]);
	}};

	// Send the data to the server for computation.
	IWantAmountUtil.calculateTotalsForItemChange(itemQuantities, itemUnitPrices, itemNbr, useAmountsOrPercents, accountAmountsOrPercents, updateTotalsCallback);
}

function updateTotal(totalAmountFieldName, total) {
	total = addCommas(total);

	setRecipientValue(totalAmountFieldName, total);
}

function updateNewItemTotal() {
	var quantity = dwr.util.getValue('newIWantItemLine.itemQuantity');
	if (isNaN(quantity)) {
		quantity = "";
	}
	var price = dwr.util.getValue('newIWantItemLine.itemUnitPrice');
	if (isNaN(price)) {
		price = "";
	}

	var updateTotalsCallback = {callback:function(total){
		// Update line total upon server callback.
		updateTotal('newIWantItemLine.totalAmount', total);
	}};

	// Send quantity and price to the server to perform the computation.
	IWantAmountUtil.calculateSingleItemTotal(quantity, price, updateTotalsCallback);
}

function updateItemAndAccountDifference(total) {
	total = addCommas(total);

	setRecipientValue('document.itemAndAccountDifference', total);
}

function addCommas(nStr) {
	nStr += '';
	x = nStr.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2;
}