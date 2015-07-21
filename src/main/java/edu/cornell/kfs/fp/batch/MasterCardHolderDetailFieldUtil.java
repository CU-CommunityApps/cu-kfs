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
package edu.cornell.kfs.fp.batch;

import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;

import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.fp.businessobject.MasterCardHolderDetail;
import edu.cornell.kfs.fp.CuFPPropertyConstants;

/**
 * Instance of BusinessObjectStringParserFieldUtils associated with MasterCardHolderDetail.
 * Used by the DataDictionary service for parsing the MasterCardHolderDetail business objects.
 */
public class MasterCardHolderDetailFieldUtil extends BusinessObjectStringParserFieldUtils
{
    /**
     * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
     */
    @Override
    public Class<MasterCardHolderDetail> getBusinessObjectClass()
    {
        return MasterCardHolderDetail.class;
    }

    /**
     * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getOrderedProperties()
     */
    @Override
    public String[] getOrderedProperties()
    {
      return new String[]
    {
      CuFPPropertyConstants.RECORD_TYPE,
      CuFPPropertyConstants.FILLER1,
      CuFPPropertyConstants.CREDIT_CARD_NUMBER,
      CuFPPropertyConstants.FILLER2,
      CuFPPropertyConstants.CARD_HOLDER_NAME,
      CuFPPropertyConstants.CARD_HOLDER_ALT_NAME,
      CuFPPropertyConstants.ADRESS_LINE1,
      CuFPPropertyConstants.ADDRESS_LINE2,
      CuFPPropertyConstants.CITY,
      CuFPPropertyConstants.STATE,
      CuFPPropertyConstants.COUNTRY,
      CuFPPropertyConstants.ZIP_CODE,
      CuFPPropertyConstants.PHONE_NUMBER,
      CuFPPropertyConstants.FILLER3,
      CuFPPropertyConstants.ACCOUNT_NUMBER,
      CuFPPropertyConstants.FILLER4,
      CuFPPropertyConstants.CARD_LIMIT,
      CuFPPropertyConstants.FILLER5
    };
  }

}
