
function loadDeliverToInfoSameAsInitiator(sameAsInitiatorFieldName, requestorNetIDFieldName, deliverToNetIDFieldName, requestorNameFieldName, deliverToNameFieldName,  requestorPhoneNbrFieldName, deliverToPhoneNbrFieldName, requestorEmailFieldName, deliverToEmailFieldName, requestorAddressFieldName, deliverToAddressFieldName ){
	//alert(requestorNetIDFieldName);
	//alert(deliverToNetIDFieldName);
	var sameAsInitiatorChecked;
	
	 var radioButton=(document.getElementsByName("document.sameAsInitiator"))[0];
	    if (radioButton.checked)
	         {
	    	sameAsInitiatorChecked = radioButton.value;
	         }
	
	
		
		if(sameAsInitiatorChecked == 'on'){
		
		var requestorNetID = DWRUtil.getValue( requestorNetIDFieldName ).trim();
		var deliverToNetID = DWRUtil.getValue( deliverToNetIDFieldName ).trim();
		var requestorName = DWRUtil.getValue( requestorNameFieldName ).trim();
		var deliverToName = DWRUtil.getValue( deliverToNameFieldName ).trim();
		var requestorPhoneNbr = DWRUtil.getValue( requestorPhoneNbrFieldName ).trim();
		var requestorEmail = DWRUtil.getValue( requestorEmailFieldName ).trim();
		var requestorAddress = DWRUtil.getValue( requestorAddressFieldName ).trim();


	    setRecipientValue( deliverToNetIDFieldName, requestorNetID );
	    setRecipientValue( deliverToNameFieldName, requestorName );
	    setRecipientValue( deliverToPhoneNbrFieldName, requestorPhoneNbr );
	    setRecipientValue( deliverToEmailFieldName, requestorEmail );
	    setRecipientValue( deliverToAddressFieldName, requestorAddress );
	    }
		else{
			clearRecipients( deliverToNetIDFieldName );
			clearRecipients( deliverToNameFieldName );
	        clearRecipients( deliverToPhoneNbrFieldName );
	        clearRecipients( deliverToEmailFieldName );
	        clearRecipients( deliverToAddressFieldName );
		}
    
}

function loadDeliverToInfo(sameAsInitiatorFieldName, deliverToNetIDFieldName, deliverToNameFieldName, deliverToPhoneNbrFieldName, deliverToEmailFieldName, deliverToAddressFieldName ){

	
	var deliverToNetID = DWRUtil.getValue( deliverToNetIDFieldName ).trim();
	

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
    

function loadRequestorInfo(sameAsInitiatorFieldName, requestorNetIDFieldName, requestorNameFieldName, requestorPhoneNbrFieldName, requestorEmailFieldName, requestorAddressFieldName ){

	
	var requestorNetID = DWRUtil.getValue( requestorNetIDFieldName ).trim();
	

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
	                    
	                    //setRecipientValue( requestorAddressFieldName, data.campusAddress );
	                    
	                } else {
	                	
	                   // clearRecipients( requestorNameFieldName );
	                    clearRecipients( requestorPhoneNbrFieldName );
	                    clearRecipients( requestorEmailFieldName );
	                    clearRecipients( requestorAddressFieldName );
	                    
	                   // setRecipientValue( requestorNameFieldName, wrapError( "Person not found" ), true );
	                } },
	            errorHandler:function( errorMessage ) {
	            	
	            //	clearRecipients( requestorNameFieldName );
                    clearRecipients( requestorPhoneNbrFieldName );
                    clearRecipients( requestorEmailFieldName );
                    clearRecipients( requestorAddressFieldName );
                    
	              //  setRecipientValue( requestorNameFieldName, wrapError( "Person not found error" ), true );
	            }
	        };
	        IWantDocumentService.getPersonData( requestorNetID, dwrReply );
	    }
	}

function loadDepartments( form){
	// set the previously selected org
	setRecipientValue( 'previousSelectedOrg', 'document.collegeLevelOrganization' );
	
    submitForm();
}

function loadAccountName(accountNumberFieldName, chartFieldName, accountNameFieldName){
	var accountNumber = DWRUtil.getValue( accountNumberFieldName );
	var chart = DWRUtil.getValue( chartFieldName );
	
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

function updateTotal(totalDollarAmountFieldName, itemsNbr) {
	var total = 0;

	for (i = 0; i < itemsNbr; i++) {
		var quantity = DWRUtil
				.getValue('document.item[' + i + '].itemQuantity');
		var price = DWRUtil.getValue('document.item[' + i + '].itemUnitPrice');

		if (isNaN(quantity)) {
			quantity = 1;
		}
		if (isNaN(price)) {
			price = 0;
		}
		total += price * quantity;
	}

	setRecipientValue(totalDollarAmountFieldName, total);
}



