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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.BusinessObjectBase;

import edu.cornell.kfs.fp.batch.MasterCardTransactionDetailFieldUtil;

/**
 * Base class for MasterCard Transaction Detail Addendum records
 *
 * @see MasterCardTransactionDetail
 */
public class MasterCardTransactionDetailAddendumBase extends BusinessObjectBase
{

	private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(MasterCardTransactionDetailAddendumBase.class);
    protected BusinessObjectStringParserFieldUtils mctdFieldUtil;
    protected DateFormat inputDateFormatter;
    protected DateFormat outputDateFormatter;

    protected String filler1;
    protected String recordType;
    protected String filler2;
    protected String addendumType;
    protected String filler3;


    /**
     * Constructor creates a new MasterCardTransactionDetail
     */
    public MasterCardTransactionDetailAddendumBase()
    {
        super();

        String inputDatePattern = "yyyyMMdd";
        inputDateFormatter = new SimpleDateFormat(inputDatePattern);

        String outputDatePattern = "yyyy-MM-dd";
        outputDateFormatter = new SimpleDateFormat(outputDatePattern);
    }


    @Override
    public void refresh()
    {
        // TODO Auto-generated method stub
    }

    public String getRecordType()
    {
        return recordType;
    }

    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
    }

    public String getAddendumType()
    {
        return addendumType;
    }

    public void setAddendumType(String addendumType)
    {
        this.addendumType = addendumType;
    }

    /**
     * Uses the MasterCardTransactionDetailFieldUtil class to obtain start and end positions of propertyName and extracts the value
     * for propertyName from the input record
     *
     * @param input line from master card file
     * @param propertyName the name of the property to extract
     * @param nextPropertyName the property immediately following propertyName in the file
     * @return value of property propertyName
     */
    protected String getValue(String input, String propertyName, String nextPropertyName)
    {
        BusinessObjectStringParserFieldUtils fieldUtil = getFieldUtil();
        final Map<String, Integer> pMap = fieldUtil.getFieldBeginningPositionMap();
        int startIndex = pMap.get(propertyName);
        int endIndex = pMap.get(nextPropertyName);
        return input != null ? StringUtils.substring(input, startIndex, endIndex).trim() : null;
    }

    /**
     * Returns the BusinessObjectStringParserFieldUtils for addendum type 0 records.
     *
     * @return MasterCardTransactionDetailFieldUtil
     */
    protected BusinessObjectStringParserFieldUtils getFieldUtil()
    {
        if (mctdFieldUtil == null)
        {
            mctdFieldUtil = new MasterCardTransactionDetailFieldUtil();
        }

        return mctdFieldUtil;
    }

    /**
     * This method converts a String to a KualiDecimal
     *
     * @param decimal
     * @return
     */
    protected KualiDecimal convertStringToKualiDecimal(String decimal)
    {
        KualiDecimal convertedDecimal;
        if (NumberUtils.isDigits(decimal))
        {
            convertedDecimal = new KualiDecimal(Double.valueOf(decimal) / 10000);
        }
        else
        {
            convertedDecimal = new KualiDecimal(0);
        }
        return convertedDecimal;
    }

    /**
     * This method converts a String to a BigDecimal
     *
     * @param decimal
     * @return
     */
    protected BigDecimal convertStringToBigDecimal(String decimal)
    {
        BigDecimal convertedDecimal;
        if (NumberUtils.isDigits(decimal))
        {
            convertedDecimal = new BigDecimal(Double.valueOf(decimal) / 100000).setScale(5, BigDecimal.ROUND_HALF_UP);
        }
        else
        {
            convertedDecimal = new BigDecimal(0).setScale(5);
        }
        return convertedDecimal;
    }

    /**
     * This method uses the formatter to convert a date to a string.
     *
     * @param date
     * @return
     */
    public String getDateString(Date date)
    {
        return date != null ? outputDateFormatter.format(date) : null;
    }

}
