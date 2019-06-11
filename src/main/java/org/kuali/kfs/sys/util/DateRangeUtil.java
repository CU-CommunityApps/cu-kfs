/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

public class DateRangeUtil {
    private static final String LESS_THAN = "<=";
    private static final String GREATER_THAN = ">=";
    private static final String RANGE = "..";

    private Date upperDate;
    private Date lowerDate;
    private SimpleDateFormat dateFormat;

    public DateRangeUtil() {
        dateFormat = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
    }

    public DateRangeUtil(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Parses date strings used in the RestAPI and lookupDao to find date based entities. Provided a date string,
     * this sets the upper and lower date values of a given string.
     *
     * @param dateString A string representing a date floor, ceiling or range. A floor is represented by
     *                   >=(timestamp), a ceiling by <=(timestamp) and range by (lowerTimestamp)..(upperTimestamp).
     *                   The format of the timestamps can be any value that the passed in date parser function
     *                   can handle
     * @param dateParser A function that takes a string and parses the value to a date object
     */
    void setDateString(String dateString, Function<String, Date> dateParser) {
        if (dateString.startsWith(LESS_THAN)) {
            dateString = StringUtils.removeStart(dateString, LESS_THAN);
            upperDate = dateParser.apply(dateString);
            lowerDate = null;
        } else if (dateString.startsWith(GREATER_THAN)) {
            dateString = StringUtils.removeStart(dateString, GREATER_THAN);
            lowerDate = dateParser.apply(dateString);
            upperDate = null;
        } else if (dateString.contains(RANGE)) {
            String[] dateStrings = StringUtils.splitByWholeSeparator(dateString, RANGE, 2);
            lowerDate = dateParser.apply(dateStrings[0]);
            upperDate = dateParser.apply(dateStrings[1]);
        }
    }

    /**
     * Convenience method to set a date string containing timestamps as long values
     *
     * @param dateString A string representing a date described in {@link #setDateString(String, Function)}
     */
    public void setDateStringWithLongValues(String dateString) {
        setDateString(dateString, (str) -> {
            try {
                return new Date(Long.parseLong(str));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unable to perform search using date string " + dateString);
            }
        });
    }

    /**
     * Returns a string representation of the currently set date range
     *
     * @return String representing the date range using the included formatter "MM/dd/yyyy"
     */
    public String toDateString() {
        if (isDateFloor()) {
            return GREATER_THAN + dateFormat.format(lowerDate);
        } else if (isDateCeiling()) {
            return LESS_THAN + dateFormat.format(upperDate);
        } else if (isDateRange()) {
            return dateFormat.format(lowerDate) + RANGE + dateFormat.format(upperDate);
        }
        return "";
    }

    public Date getLowerDate() {
        return lowerDate;
    }

    public Date getUpperDate() {
        return upperDate;
    }

    public boolean isDateRange() {
        return lowerDate != null && upperDate != null;
    }

    public boolean isDateFloor() {
        return lowerDate != null && upperDate == null;
    }

    public boolean isDateCeiling() {
        return lowerDate == null && upperDate != null;
    }

    public boolean isEmpty() {
        return lowerDate == null && upperDate == null;
    }

}
