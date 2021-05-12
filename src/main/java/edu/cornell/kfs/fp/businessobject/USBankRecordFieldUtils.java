/*
 * Copyright 2016 The Kuali Foundation.
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

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

/**
 * Provides methods for parsing USBank addendum records.
 * In the long-run, it may be wise/useful to turn this into a subclass of 
 * org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils and 
 * provide subclasses that assist in parsing, but we won't for now.
 *
 */
public class USBankRecordFieldUtils {

  /**
   * Extracts a substring less any ending whitespace for a given beginning and ending position.
   * 
   * @param line Superstring
   * @param begin Beginning index
   * @param end Ending index
   * @return The trimmed substring
   * @throws StringIndexOutOfBoundsException May occur when taking the actual substring if the provided bounds are beyond the limits of the superstring
   */
  public static String extractNormalizedString(String line, int begin, int end) throws StringIndexOutOfBoundsException {
    String theString = line.substring(begin, end);
    if(theString.trim().length()==0) {
      return null;
    }
    return theString;
  }

  /**
   * Extracts a substring less any ending whitespace for a given beginning and ending position.
   * 
   * @param line Superstring
   * @param begin Beginning index
   * @param end Ending index
   * @param required True if the value is required to not be empty
   * @param lineCount The current line number
   * @return The trimmed substring
   * @throws ParseException When a required value is missing
   * @throws StringIndexOutOfBoundsException When taking the actual substring if the provided bounds are beyond the limits of the superstring
   */
  public static String extractNormalizedString(String line, int begin, int end, boolean required, int lineCount) throws ParseException {
    String theValue = extractNormalizedString(line, begin, end);
    if (required) {
      if (theValue==null) {
        throw new ParseException("A required value was missing at " + begin + " " + end + " on line " + lineCount, lineCount);
      }
    }
    return theValue;
  }

  /**
   * Extracts a Date from a substring less any ending whitespace for a given beginning and ending position.
   * 
   * @param line Superstring
   * @param begin Beginning index
   * @param end Ending index
   * @param lineCount The current line number
   * @return The Date parsed from the trimmed substring
   * @throws ParseException When unable to parse the date
   */
  public static Date extractDate(String line, int begin, int end, int lineCount) throws ParseException {
    Date theDate;
    try {
      String sub = line.substring(begin, end);
      String year = sub.substring(0,4);
      String month = sub.substring(4,6);
      String day = sub.substring(6,8);
      theDate = Date.valueOf(year+"-"+month+"-"+day);
    } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
      // May encounter a StringIndexOutOfBoundsException if the string bounds do not match or 
      // an IllegalArgumentException if the Date does not parse correctly
      throw new ParseException("Unable to parse date from the value " + line.substring(begin,end) + " on line " + lineCount, lineCount);
    }
    return theDate;
  }

  /**
   * Extracts a KualiDecimal from a substring less any ending whitespace for a given beginning and ending position.
   * 
   * @param line Superstring
   * @param begin Beginning index
   * @param end Ending index
   * @param lineCount The current line number
   * @return The KualiDecimal parsed from the trimmed substring
   * @throws ParseException When unable to parse the KualiDecimal
   */
  public static KualiDecimal extractDecimal(String line, int begin, int end, int lineCount) throws ParseException {
    KualiDecimal theDecimal;
    try {
      String sanitized = line.substring(begin, end);
      sanitized = StringUtils.remove(sanitized, '-');
      sanitized = StringUtils.remove(sanitized, '+');
      theDecimal = new KualiDecimal(sanitized);
    } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
      // May encounter a StringIndexOutOfBoundsException if the string bounds do not match or 
      // an IllegalArgumentException if the Decimal does not parse correctly
      throw new ParseException("Unable to parse " +  line.substring(begin, end) + " into a decimal value on line " + lineCount, lineCount);
    }
    return theDecimal;
  }

  /**
   * Extracts a KualiDecimal representing dollars and cents from a substring less any ending whitespace 
   * for a given beginning and ending position.
   * 
   * @param line Superstring
   * @param begin Beginning index
   * @param end Ending index
   * @param lineCount The current line number
   * @return The KualiDecimal parsed from the trimmed substring
   * @throws ParseException When unable to parse the KualiDecimal
   */
  public static KualiDecimal extractDecimalWithCents(String line, int begin, int end, int lineCount) throws ParseException {
    KualiDecimal theDecimal;
    KualiDecimal theCents;
    try {
      String sanitized = line.substring(begin, end-2);
      sanitized = StringUtils.remove(sanitized, '-');
      sanitized = StringUtils.remove(sanitized, '+');
      theDecimal = new KualiDecimal(sanitized);
      theCents = new KualiDecimal(line.substring(end-2, end));
      theCents = theCents.multiply(new KualiDecimal("0.01"));
      theDecimal = theDecimal.add(theCents);
    } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
      // May encounter a StringIndexOutOfBoundsException if the string bounds do not match or 
      // an IllegalArgumentException if the Decimal or Cents do not parse correctly
      throw new ParseException("Unable to parse " +  line.substring(begin, end) + " into a decimal value on line " + lineCount, lineCount);
    }
    return theDecimal;
  }

  /**
   * Extracts a cycle Date from a substring less any ending whitespace for a given beginning and ending position.
   * 
   * @param line Superstring
   * @param begin Beginning index
   * @param end Ending index
   * @param lineCount The current line number
   * @return The cycle Date parsed from the trimmed substring
   * @throws ParseException When unable to parse the cycle Date
   */
  public static Date extractCycleDate(String line, int begin, int end, int lineCount) throws ParseException {
    String day;
    Calendar cal = Calendar.getInstance();
    Date theDate;
    try {
      day = line.substring(begin, end);
      theDate = KfsDateUtils.newDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), Integer.parseInt(day));
    } catch (StringIndexOutOfBoundsException | IllegalArgumentException e) {
      // May encounter a StringIndexOutOfBoundsException if the string bounds do not match or 
      // an IllegalArgumentException if the Date does not parse correctly
      throw new ParseException("Unable to parse " +  line.substring(begin, end) + " into a cycle date value on line " + lineCount, lineCount);
    }
    return theDate;
  }

  /**
   * Converts a given string to either a Debit code or a Credit code
   * @param val String to convert. Should either be "+" or "-"
   * @return "D" if Debit, "C" if Credit
   * @throws IOException When unable to determine if the provided string is equivalent to the two codes
   */
  public static String convertDebitCreditCode(String val) throws IOException {
    switch(val) {
      case "+":
        return "D";
      case "-":
        return "C";
      default:
        throw new IOException("Unable to determine whether transaction line is a debit or a credit");
    }
  }

  /**
   * Standardized line count message
   * @param lineCount The line the error occurred on
   * @return The error message
   */
  public static String lineCountMessage(int lineCount) {
    return " Error occurred on line # " + lineCount;
  }  
  
}
