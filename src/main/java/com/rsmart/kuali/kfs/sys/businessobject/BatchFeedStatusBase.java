/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.sys.businessobject;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds general batch counts and information for batch feed audit reports
 */
public abstract class BatchFeedStatusBase {
    protected static final String REPORT_DATA_STATISTICS_KEY = "statistics";
    protected static final String REPORT_DATA_XML_MESSAGE_KEY = "xmlmessage";
    protected static final String REPORT_DATA_SUMMARY_LINES_KEY = "summarylines";
    protected static final String REPORT_DATA_DOCUMENTS_KEY = "documents";
    
    protected Map<String, Integer> statistics;
    protected String xmlParseExceptionMessage;
    
    /**
     * update the value of the entry with the given key. If the key exists, the value will be the sum of the given and existing
     * values; otherwise, create a new entry with the key and value.
     * 
     * @param key the given key
     * @param count the given count
     */
    public void updateStatistics(String key, Integer count) {
        if (statistics.containsKey(key)) {
            Integer currentCount = statistics.get(key);
            count = currentCount + count;
        }
        statistics.put(key, count);
    }
    
    /**
     * Builds the Map of data needed for the report
     * 
     * @return Map<String, Object>
     */
    public Map<String, Object> getReportData() {
        Map<String, Object> reportData = new HashMap<String, Object>();

        reportData.put(REPORT_DATA_STATISTICS_KEY, getStatistics());
        if (StringUtils.isNotBlank(getXmlParseExceptionMessage())) {
            reportData.put(REPORT_DATA_XML_MESSAGE_KEY, getXmlParseExceptionMessage());
        }
        
        return reportData;
    }
    
    /**
     * @return Returns the statistics.
     */
    public Map<String, Integer> getStatistics() {
        return statistics;
    }

    /**
     * @param statistics The statistics to set.
     */
    public void setStatistics(Map<String, Integer> statistics) {
        this.statistics = statistics;
    }
    
    /**
     * @return Returns the xmlParseExceptionMessage.
     */
    public String getXmlParseExceptionMessage() {
        return xmlParseExceptionMessage;
    }

    /**
     * @param xmlParseExceptionMessage The xmlParseExceptionMessage to set.
     */
    public void setXmlParseExceptionMessage(String xmlParseExceptionMessage) {
        this.xmlParseExceptionMessage = xmlParseExceptionMessage;
    }

}
