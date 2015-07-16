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
import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailAddendum5FieldUtil;

/**
 * Adds the fields needed on the record type 5000 addendum 5 (Generic) records.
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendum5 extends MasterCardTransactionDetailAddendumBase
{
    private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendum5.class);

    protected String filler1;
    protected String filler2;
    protected String genericAddendumData;
    protected String filler3;

    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendum5()
    {
        super();
    }


    /**
     * Parses inputRecord setting class attributes. Dates and currency are converted into respective java objects.
     *
     * @param inputRecord
     */
    public void parseInput(String inputRecord)
    {
        genericAddendumData = getValue(inputRecord, CuFPPropertyConstants.GENERIC_ADDEDNUM_DATA, CuFPPropertyConstants.FILLER3);
    }

    public String getFiller1()
    {
        return filler1;
    }

    public void setFiller1(String filler1)
    {
        this.filler1 = filler1;
    }

    public String getFiller2()
    {
        return filler2;
    }

    public void setFiller2(String filler2)
    {
        this.filler2 = filler2;
    }

    public String getGenericAddendumData()
    {
        return genericAddendumData;
    }

    public void setGenericAddendumData(String genericAddendumData)
    {
        this.genericAddendumData = genericAddendumData;
    }

    public String getFiller3()
    {
        return filler3;
    }

    public void setFiller3(String filler3)
    {
        this.filler3 = filler3;
    }

    /**
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 5 records.
     *
     * @return instance of MasterCardTransactionDetailAddendum5FieldUtil
     * @see MasterCardTransactionDetailFieldUtil.getFieldUtil()
     */
    @Override
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailAddendum5FieldUtil();
        }
        return mctdFieldUtil;
    }

}
