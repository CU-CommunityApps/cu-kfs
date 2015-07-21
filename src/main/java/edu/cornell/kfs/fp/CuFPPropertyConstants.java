package edu.cornell.kfs.fp;


public class CuFPPropertyConstants {

    // MasterCardBatch properties
    public static final String BATCH_ACCT_NBR = "account_nbr";
  
	// MasterCard file properties
	public static final String RECORD_TYPE_4300 = "4300";
    public static final String RECORD_TYPE_5000 = "5000";
    public static final String ADDENDUM_TYPE_0  = "0";
    public static final String ADDENDUM_TYPE_1  = "1";
    public static final String ADDENDUM_TYPE_11 = "11";
    public static final String ADDENDUM_TYPE_2  = "2";
    public static final String ADDENDUM_TYPE_21 = "21";
    public static final String ADDENDUM_TYPE_3  = "3";
    public static final String ADDENDUM_TYPE_4  = "4";
    public static final String ADDENDUM_TYPE_5  = "5";
    public static final String ADDENDUM_TYPE_6  = "6";
    public static final String ADDENDUM_TYPE_61 = "61";
    public static final String ADDENDUM_TYPE_7  = "7";

    public static final String COA_CODE = "UC";
    public static final String FIN_OBJ_CODE = "7130";

    public static final String DEBIT_CODE = "D";
    public static final String CREDIT_CODE = "C";

	// MasterCardHolderDetail properties
	public static final String RECORD_TYPE          = "recordType";
	public static final String CREDIT_CARD_NUMBER   = "creditCardNumber";
	public static final String CARD_HOLDER_NAME     = "cardHolderName";
	public static final String CARD_HOLDER_ALT_NAME = "cardHolderAltName";
	public static final String ADRESS_LINE1         = "cardHolderLine1Address";
	public static final String ADDRESS_LINE2        = "cardHolderLine2Address";
	public static final String CITY                 = "cardHolderCity";
	public static final String STATE                = "cardHolderStateCode";
	public static final String COUNTRY              = "cardHolderCountry";
	public static final String ZIP_CODE             = "cardHolderZipCode";
	public static final String PHONE_NUMBER         = "cardHolderPhoneNumber";
	public static final String ACCOUNT_NUMBER       = "cardHolderAccountNumber";
	public static final String CARD_LIMIT           = "cardLimit";

	public static final String FILLER1  = "filler1";
	public static final String FILLER2  = "filler2";
	public static final String FILLER3  = "filler3";
	public static final String FILLER4  = "filler4";
	public static final String FILLER5  = "filler5";
	public static final String FILLER6  = "filler6";
	public static final String FILLER7  = "filler7";
	public static final String FILLER8  = "filler8";
	public static final String FILLER9  = "filler9";
	public static final String FILLER10 = "filler10";

	// MasterCardTransactionDetail properties
	public static final String ADDENDUM_TYPE        = "addendumType";
	public static final String DEBIT_CREDIT_IND     = "debitCreditInd";
	public static final String TRANSACTION_AMOUNT   = "transactionAmount";
	public static final String POSTING_DATE         = "postingDate";
	public static final String TRANSACTION_DATE     = "transactionDate";
	public static final String TRANS_REF_NUMBER     = "transactionRefNumber";
	public static final String MERCHANT_NAME        = "merchantName";
	public static final String MERCHANT_CITY        = "merchantCity";
	public static final String MERCHANT_STATE       = "merchantState";
	public static final String MERCHANT_COUNTRY     = "merchantCountry";
	public static final String MERCHANT_ADDRESS     = "merchantAddress";
	public static final String CORPORATION_NAME     = "corporationName";
	public static final String CATEGORY_CODE        = "categoryCode";
	public static final String ORIG_CURRENCY_AMOUNT = "origCurrencyAmount";
    public static final String ORIG_CURRENCY_CODE   = "origCurrencyCode";
    public static final String CUSTOMER_CODE        = "customerCode";
	public static final String SALES_TAX_AMOUNT     = "salesTaxAmount";

    // MasterCardTransactionDetail Addendum 1
    public static final String PRODUCT_CODE     = "productCode";
    public static final String ITEM_DESCRIPTION = "itemDescription";
    public static final String ITEM_QUANTITY    = "itemQuantity";
    public static final String ITEM_UOM         = "itemUnitOfMeasure";
    public static final String EXT_ITEM_AMOUNT  = "extendedItemAmount";
    public static final String NET_GROSS_IND    = "netGrossIndicator";
    public static final String TAX_RATE_APPLIED = "taxRateApplied";
    public static final String TAX_TYPE_APPLIED = "taxTypeApplied";
    public static final String TAX_AMOUNT       = "taxAmount";
    public static final String DISCOUNT_IND     = "discountIndicator";
    public static final String DISCOUNT_AMOUNT  = "discountAmount";

    // MasterCardTransactionDetail Addendum 11
    public static final String USER_AMOUNT = "userAmount";

    // MasterCardTransactionDetail Addendum 2
    public static final String PASSENGER_NAME     = "passengerName";
    public static final String DEPARTURE_DATE     = "departureDate";
    public static final String AIRPORT_CODE       = "airportCode";
    public static final String TRAVEL_AGENCY_CODE = "travelAgencyCode";
    public static final String TRAVEL_AGENCY_NAME = "travelAgencyName";
    public static final String TICKET_NUMBER      = "ticketNumber";
    public static final String ISSUE_DATE         = "issueDate";
    public static final String ISSUING_CARRIER    = "issuingCarrier";
    public static final String TOTAL_FARE         = "totalFare";
    public static final String TOTAL_FEES         = "totalFees";
    public static final String TOTAL_TAXES        = "totalTaxes";

    // MasterCardTransactionDetail Addendum 21
    public static final String TRIP_LEG_NUMBER        = "tripLegNumber";
    public static final String CARRIER_CODE           = "carrierCode";
    public static final String SERVICE_CLASS          = "serviceClass";
    public static final String STOP_OVER_CODE         = "stopOverCode";
    public static final String CITY_OF_ORIGIN         = "cityOfOrigin";
    public static final String CONJUNCTION_TICKET     = "conjunctionTicket";
    public static final String TRAVEL_DATE            = "travelDate";
    public static final String EXCHANGE_TICKET        = "exchangeTicket";
    public static final String COUPON_NUMBER          = "couponNumber";
    public static final String CITY_OF_DESTINATION    = "cityOfDestination";
    public static final String FARE_BASE_CODE         = "fareBaseCode";
    public static final String FLIGHT_NUMBER          = "flightNumber";
    public static final String DEPARTURE_TIME         = "departureTime";
    public static final String DEPARTURE_TIME_SEGMENT = "departureTimeSegment";
    public static final String ARRIVAL_TIME           = "arrivalTime";
    public static final String ARRIVAL_TIME_SEGMENT   = "arrivalTimeSegment";
    public static final String FARE                   = "fare";
    public static final String FEE                    = "fee";
    public static final String TAXES                  = "taxes";
    public static final String ENDORSEMENTS           = "endorsements";

    // MasterCardTransactionDetail Addendum 3
    public static final String ARRIVAL_DATE              = "arrivalDate";
    public static final String FOLIO_NUMBER              = "folioNumber";
    public static final String PROPERTY_PHONE_NUMBER     = "propertyPhoneNumber";
    public static final String CUSTOMER_SERVICE_NUMBER   = "customerServiceNumber";
    public static final String ROOM_RATE                 = "roomRate";
    public static final String ROOM_TAX                  = "roomTax";
    public static final String PROGRAM_CODE              = "programCode";
    public static final String TELEPHONE_CHARGES         = "telephoneCharges";
    public static final String ROOM_SERVICE              = "roomService";
    public static final String BAR_CHARGES               = "barCharges";
    public static final String GIFT_SHOP_CHARGES         = "giftShopCharges";
    public static final String LAUNDRY_CHARGES           = "laundryCharges";
    public static final String OTHER_SERVICES_CODE       = "otherServicesCode";
    public static final String OTHER_SERVICES_CHARGES    = "otherServicesCharges";
    public static final String BILLING_ADJUSTMENT_IND    = "billingAdjustmentIndicator";
    public static final String BILLING_ADJUSTMENT_AMOUNT = "billingAdjustmentAmount";

    // MasterCardTransactionDetail Addendum 4
    public static final String RENTAL_AGREEMENT_NUMBER = "rentalAgreementNumber";
    public static final String RENTER_NAME             = "renterName";
    public static final String RENTAL_RETURN_CITY      = "rentalReturnCity";
    public static final String RENTAL_RETURN_STATE     = "rentalReturnState";
    public static final String RENTAL_RETURN_COUNTRY   = "rentalReturnCountry";
    public static final String RENTAL_RETURN_DATE      = "rentalReturnDate";
    public static final String RETURN_LOCATION_ID      = "returnLocationId";
    public static final String RENTAL_CLASS            = "rentalClass";
    public static final String DAILY_RENTAL_RATE        = "dailyRentalRate";
    public static final String RATE_PER_MILE           = "ratePerMile";
    public static final String TOTAL_MILES             = "totalMiles";
    public static final String MAX_FREE_MILES          = "maxFreeMiles";
    public static final String INSURANCE_IND           = "insuranceIndicator";
    public static final String INSURANCE_CHARGES       = "insuranceCharges";
    public static final String ADJUSTED_AMOUNT_IND     = "adjustedAmountIndicator";
    public static final String ADJUSTED_AMOUNT         = "adjustedAmount";
    public static final String CHECKOUT_DATE           = "checkoutDate";

    // MasterCardTransactionDetail Addendum 4
    public static final String GENERIC_ADDEDNUM_DATA = "genericAddendumData";

    // MasterCardTransactionDetail Addendum 6
    public static final String OIL_COMPANY_BRAND                 = "oilCompanyBrand";
    public static final String MERCHANT_STREET_ADDRESS           = "merchantStreetAddress";
    public static final String MERCHANT_POSTAL_CODE              = "merchantPostalCode";
    public static final String TIME_OF_PURCHASE                  = "timeOfPurchase";
    public static final String MOTOR_FUEL_SERVICE_TYPE           = "motorFuelServiceType";
    public static final String MOTOR_FUEL_PRODUCT_CODE           = "motorFuelProductCode";
    public static final String MOTOR_FUEL_UNIT_PRICE             = "motorFuelUnitPrice";
    public static final String MOTOR_FUEL_UOM                    = "motorFuelUnitOfMeasure";
    public static final String MOTOR_FUEL_QUANTITY               = "motorFuelQuantity";
    public static final String MOTOR_FUEL_SALE_AMOUNT            = "motorFuelSaleAmount";
    public static final String ODOMETER_READING                  = "odomoterReading";
    public static final String VEHICLE_NUMBER                    = "vehicleNumber";
    public static final String DRIVER_NUMBER                     = "driverNumber";
    public static final String MAGNETIC_STRIPE_PRODUCT_TYPE_CODE = "magneticStripeProductTypeCode";
    public static final String COUPON_DISCOUNT_AMOUNT            = "couponDiscountAmount";
    public static final String TAX_EXEMPT_AMOUNT                 = "taxExemptAmount";
    public static final String TAX_AMOUNT1                       = "taxAmount1";
    public static final String TAX_AMOUNT2                       = "taxAmount2";

    // MasterCardTransactionDetail Addendum 61
    public static final String ITEM_PRODUCT_CODE = "itemProductCode";
    public static final String ALTERNATE_TAX_ID  = "alternateTaxIdentifier";


	// ProcurementCardDocument properties
	public static String ELEMENT_CREDIT_CARD_NUMBER = "transactionCreditCardNumber";
	public static String ELEMENT_TOTAL_AMOUNT       = "financialDocumentTotalAmount";
	public static String ELEMENT_DEBIT_CREDIT       = "transactionDebitCreditCode";
	public static String ELEMENT_COA                = "chartOfAccountsCode";
	public static String ELEMENT_ACCT_NUMBER        = "accountNumber";
	public static String ELEMENT_OBJ_CODE           = "financialObjectCode";
	public static String ELEMENT_CARD_NAME          = "cardHolderName";
	public static String ELEMENT_TRANS_DATE         = "transactionDate";
	public static String ELEMENT_TRANS_REF_NUMBER   = "transactionReferenceNumber";
	public static String ELEMENT_CAT_CODE           = "transactionMerchantCategoryCode";
	public static String ELEMENT_POSTING_DATE       = "transactionPostingDate";
	public static String ELEMENT_ORIG_CUR_CODE      = "transactionOriginalCurrencyCode";
	public static String ELEMENT_ORIG_CUR_AMOUNT    = "transactionOriginalCurrencyAmount";
	public static String ELEMENT_SALES_TAX          = "transactionSalesTaxAmount";
	public static String ELEMENT_VENDOR_NAME        = "vendorName";
	public static String ELEMENT_VENDOR_ADDR1       = "vendorLine1Address";
	public static String ELEMENT_VENDOR_ADDR2       = "vendorLine2Address";
	public static String ELEMENT_VENDOR_CITY        = "vendorCityName";
	public static String ELEMENT_VENDOR_STATE       = "vendorStateCode";
	public static String ELEMENT_CC_ADDR1           = "cardHolderLine1Address";
	public static String ELEMENT_CC_ADDR2           = "cardHolderLine2Address";
	public static String ELEMENT_CC_CITY            = "cardHolderCityName";
	public static String ELEMENT_CC_STATE           = "cardHolderStateCode";
	public static String ELEMENT_CC_ZIP             = "cardHolderZipCode";
	public static String ELEMENT_CC_PHONE           = "cardHolderWorkPhoneNumber";
	public static String ELEMENT_CARD_LIMIT         = "cardLimit";

	public static final String UNIT_CODE = "unitCode";

    public static final String BATCH_ID = "batchId";
}
