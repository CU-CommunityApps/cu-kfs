/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.businessobject;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.BusinessObjectBase;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.batch.MasterCardHolderDetailFieldUtil;

/**
 * Business object to hold the master card card holder data from the flat file input.
 * This object is not persisted in the database but is required to be a BusinessObject
 * so that it can use the DataDictionary service to assist in parsing the flat file input.
 */
public class MasterCardHolderDetail extends BusinessObjectBase
{
  private static final long serialVersionUID = 1L;
  private MasterCardHolderDetailFieldUtil mchDetailFieldUtil;

  private String recordType;
  private String creditCardNumber;
  private String cardHolderName;
  private String cardHolderAltName;
  private String cardHolderLine1Address;
  private String cardHolderLine2Address;
  private String cardHolderCity;
  private String cardHolderStateCode;
  private String cardHolderCountry;
  private String cardHolderZipCode;
  private String cardHolderPhoneNumber;
  private String cardHolderAccountNumber;
  private KualiDecimal cardLimit;
  private String filler1;
  private String filler2;
  private String filler3;
  private String filler4;
  private String filler5;

  /**
   * Initializes the fieldUtils class for this business object.
   *
   * @return instance of MasterCardHolderDetailFieldUtil
   */
  public BusinessObjectStringParserFieldUtils getFieldUtil()
  {
    if (mchDetailFieldUtil == null)
    {
      mchDetailFieldUtil = new MasterCardHolderDetailFieldUtil();
    }
    return mchDetailFieldUtil;
  }

  /**
   * Parses a record type 4300 and stores fields as attributes of this class.
   *
   * @param inputRecord from master card flat file
   */
  public void parseInput(String inputRecord)
  {
    creditCardNumber        = getValue(inputRecord, CuFPPropertyConstants.CREDIT_CARD_NUMBER, CuFPPropertyConstants.FILLER2);
    cardHolderName          = getValue(inputRecord, CuFPPropertyConstants.CARD_HOLDER_NAME, CuFPPropertyConstants.CARD_HOLDER_ALT_NAME);
    cardHolderLine1Address  = getValue(inputRecord, CuFPPropertyConstants.ADRESS_LINE1, CuFPPropertyConstants.ADDRESS_LINE2);
    cardHolderLine2Address  = getValue(inputRecord, CuFPPropertyConstants.ADDRESS_LINE2, CuFPPropertyConstants.CITY);
    cardHolderCity          = getValue(inputRecord, CuFPPropertyConstants.CITY, CuFPPropertyConstants.STATE);
    cardHolderStateCode     = getValue(inputRecord, CuFPPropertyConstants.STATE, CuFPPropertyConstants.COUNTRY);
    setCardHolderZipCode(     getValue(inputRecord, CuFPPropertyConstants.ZIP_CODE, CuFPPropertyConstants.PHONE_NUMBER));
    setCardHolderPhoneNumber( getValue(inputRecord, CuFPPropertyConstants.PHONE_NUMBER, CuFPPropertyConstants.FILLER3));
    cardHolderAccountNumber = getValue(inputRecord, CuFPPropertyConstants.ACCOUNT_NUMBER, CuFPPropertyConstants.FILLER4);
    setCardLimit(getValue(inputRecord, CuFPPropertyConstants.CARD_LIMIT, CuFPPropertyConstants.FILLER5));
  }

    /**
     * Uses the MasterCardHolderDetailFieldUtil class to obtain start and end
     * positions of propertyName and extracts the value for propertyName from
     * the input record
     *
     * @param input 4300 input line from master card file
     * @param propertyName the name of the property to extract
     * @param nextPropertyName the property immediately following propertyName in the file
     * @return value of property propertyName
     */
  private String getValue(String input, String propertyName, String nextPropertyName)
  {
    BusinessObjectStringParserFieldUtils fieldUtil = getFieldUtil();
    final Map<String, Integer> pMap = fieldUtil.getFieldBeginningPositionMap();
    int startIndex = pMap.get(propertyName);
    int endIndex   = pMap.get(nextPropertyName);
    return input != null ? StringUtils.substring(input, startIndex, endIndex).trim() : null;
  }

  /**
   * populates the ProcurementCardHolderDetail business object with the properties
   * in this instance.
   *
   * @param procurementCardHolderDetail
   */
  public void populateCardHolderObject(ProcurementCardHolderDetail procurementCardHolderDetail)
  {
    procurementCardHolderDetail.setCreditCardNumber(creditCardNumber);
    procurementCardHolderDetail.setCardHolderName(cardHolderName);
    procurementCardHolderDetail.setCardHolderLine1Address(cardHolderLine1Address);
    procurementCardHolderDetail.setCardHolderLine2Address(cardHolderLine2Address);
    procurementCardHolderDetail.setCardHolderCityName(cardHolderCity);
    procurementCardHolderDetail.setCardHolderStateCode(cardHolderStateCode);
    procurementCardHolderDetail.setCardHolderZipCode(cardHolderZipCode);
    procurementCardHolderDetail.setCardHolderWorkPhoneNumber(cardHolderPhoneNumber);
    procurementCardHolderDetail.setAccountNumber(cardHolderAccountNumber);
    procurementCardHolderDetail.setCardLimit(cardLimit);

    procurementCardHolderDetail.setChartOfAccountsCode(CuFPPropertyConstants.COA_CODE);
    procurementCardHolderDetail.setFinancialObjectCode(CuFPPropertyConstants.FIN_OBJ_CODE);
  }

  /**
   *
   */
  public String getRecordType()
  {
    return recordType;
  }

  /**
   *
   */
  public void setRecordType(String recordType)
  {
    this.recordType = recordType;
  }

  /**
   *
   */
  public String getCreditCardNumber()
  {
    return creditCardNumber;
  }

  /**
   *
   */
  public void setCreditCardNumber(String creditCardNumber)
  {
    this.creditCardNumber = creditCardNumber;
  }

  /**
   *
   */
  public String getCardHolderName()
  {
    return cardHolderName;
  }

  /**
   *
   */
  public void setCardHolderName(String cardHolderName)
  {
    this.cardHolderName = cardHolderName;
  }

  /**
   *
   */
  public String getCardHolderAltName()
  {
    return cardHolderAltName;
  }

  /**
   *
   */
  public void setCardHolderAltName(String cardHolderAltName)
  {
    this.cardHolderAltName = cardHolderAltName;
  }

  /**
   *
   */
  public String getCardHolderLine1Address()
  {
    return cardHolderLine1Address;
  }

  /**
   *
   */
  public void setCardHolderLine1Address(String cardHolderLine1Address)
  {
    this.cardHolderLine1Address = cardHolderLine1Address;
  }

  /**
   *
   */
  public String getCardHolderLine2Address()
  {
    return cardHolderLine2Address;
  }

  /**
   *
   */
  public void setCardHolderLine2Address(String cardHolderLine2Address)
  {
    this.cardHolderLine2Address = cardHolderLine2Address;
  }

  /**
   *
   */
  public String getCardHolderCity()
  {
    return cardHolderCity;
  }

  /**
   *
   */
  public void setCardHolderCity(String cardHolderCity)
  {
    this.cardHolderCity = cardHolderCity;
  }

  /**
   *
   */
  public String getCardHolderStateCode()
  {
    return cardHolderStateCode;
  }

  /**
   *
   */
  public void setCardHolderStateCode(String cardHolderStateCode)
  {
    this.cardHolderStateCode = cardHolderStateCode;
  }

  /**
   *
   */
  public String getCardHolderCountry()
  {
    return cardHolderCountry;
  }

  /**
   *
   */
  public void setCardHolderCountry(String cardHolderCountry)
  {
    this.cardHolderCountry = cardHolderCountry;
  }

  /**
   *
   */
  public String getCardHolderZipCode()
  {
    return cardHolderZipCode;
  }

  /**
   * Sets the zip code field. If the argument is 9 digits (zip + 4) then
   * a hyphen is inserted.
   */
  public void setCardHolderZipCode(String cardHolderZipCode)
  {
    if (cardHolderZipCode != null && cardHolderZipCode.length() == 9)
    {
      String zip5 = cardHolderZipCode.substring(0, 5);
      String zipSuffix = cardHolderZipCode.substring(5, 9);
      this.cardHolderZipCode = zip5 + "-" + zipSuffix;
    }
    else
    {
      this.cardHolderZipCode = cardHolderZipCode;
    }
  }

  /**
   *
   */
  public String getCardHolderPhoneNumber()
  {
    return cardHolderPhoneNumber;
  }

  /**
   *
   */
  public void setCardHolderPhoneNumber(String cardHolderPhoneNumber)
  {
    this.cardHolderPhoneNumber = cardHolderPhoneNumber;
  }

  /**
   *
   */
  public String getCardHolderAccountNumber()
  {
    return cardHolderAccountNumber;
  }

  /**
   *
   */
  public void setCardHolderAccountNumber(String cardHolderAccountNumber)
  {
    this.cardHolderAccountNumber = cardHolderAccountNumber;
  }

  /**
   *
   */
  public KualiDecimal getCardLimit()
  {
    return cardLimit;
  }

  /**
   *
   */
  public void setCardLimit(KualiDecimal cardLimit)
  {
    this.cardLimit = cardLimit;
  }

  /**
   *
   */
  public void setCardLimit(String cardLimitString)
  {
    if (NumberUtils.isDigits(cardLimitString))
    {
      // card limit has 4 implied decimal places
      double cardLimitDbl = Double.valueOf(cardLimitString) / 10000;
      this.cardLimit = new KualiDecimal(cardLimitDbl);
    }
    else
    {
      this.cardLimit = new KualiDecimal(0);
    }
  }

  /**
   *
   */
  public String getFiller1()
  {
    return filler1;
  }

  /**
   *
   */
  public void setFiller1(String filler1)
  {
    this.filler1 = filler1;
  }

  /**
   *
   */
  public String getFiller2()
  {
    return filler2;
  }

  /**
   *
   */
  public void setFiller2(String filler2)
  {
    this.filler2 = filler2;
  }

  /**
   *
   */
  public String getFiller3()
  {
    return filler3;
  }

  /**
   *
   */
  public void setFiller3(String filler3)
  {
    this.filler3 = filler3;
  }

  /**
   *
   */
  public String getFiller4()
  {
    return filler4;
  }

  /**
   *
   */
  public void setFiller4(String filler4)
  {
    this.filler4 = filler4;
  }

  /**
   *
   */
  public String getFiller5()
  {
    return filler5;
  }

  /**
   *
   */
  public void setFiller5(String filler5)
  {
    this.filler5 = filler5;
  }

  /**
   * @see org.kuali.rice.krad.bo.BusinessObject#refresh()
   */
  @Override
  public void refresh()
  {
    // not used
  }

}
