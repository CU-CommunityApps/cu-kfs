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

import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum7FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 7 records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum7 extends MasterCardTransactionDetail
{
  private static final long serialVersionUID = 1L;
  private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum7.class);

  protected String merchantAddress;
  protected String corporationName;
  protected String filler8;
  protected String filler9;
  protected String filler10;

  /**
   * Constructor creates a new MasterCardTransactionDetail
   */
  public MasterCardTransactionDetailAddendum7()
  {
    super();
  }


  /**
   * Parses inputRecord setting class attributes. Dates and currency are converted
   * into respective java objects.
   *
   * @param inputRecord
   */
  @Override
  public void parseInput(String inputRecord)
  {
    //addendumType = getValue(inputRecord, CuFPPropertyConstants.ADDENDUM_TYPE, CuFPPropertyConstants.FILLER2);
    // remove the field util used for addendum type so that the correct addendum 7 field util is used
    //mctdFieldUtil = null;
    merchantAddress = getValue(inputRecord, CuFPPropertyConstants.MERCHANT_ADDRESS, CuFPPropertyConstants.FILLER9);
    corporationName = getValue(inputRecord, CuFPPropertyConstants.CORPORATION_NAME, CuFPPropertyConstants.FILLER10);
  }

  /**
   *
   */
  @Override
  public String getMerchantAddress()
  {
    return merchantAddress;
  }

  /**
   *
   */
  @Override
  public void setMerchantAddress(String merchantAddress)
  {
    this.merchantAddress = merchantAddress;
  }

  /**
   *
   */
  @Override
  public String getCorporationName()
  {
    return corporationName;
  }

  /**
   *
   */
  @Override
  public void setCorporationName(String corporationName)
  {
    this.corporationName = corporationName;
  }

  /**
   *
   */
  public String getFiller8()
  {
    return filler8;
  }

  /**
   *
   */
  public String getFiller9()
  {
    return filler9;
  }

  /**
   *
   */
  public String getFiller10()
  {
    return filler10;
  }

  /**
   * Returns the BusinessObjectStringParserFieldUtils for addendum type 7 records.
   *
   * @return instance of MasterCardTransactionDetailAddendum7FieldUtil
   * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
   */
  @Override
  protected BusinessObjectStringParserFieldUtils getFieldUtil()
  {
    if (mctdFieldUtil == null)
    {
      mctdFieldUtil = new MasterCardTransactionDetailAddendum7FieldUtil();
    }
    return mctdFieldUtil;
  }

}
