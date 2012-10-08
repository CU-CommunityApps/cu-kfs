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
package com.rsmart.kuali.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;

import com.rsmart.kuali.kfs.fp.FPConstants;
import com.rsmart.kuali.kfs.sys.businessobject.BatchFeedStatusBase;

/**
 * Holds batch counts and information for the audit report
 */
public class DisbursementVoucherBatchStatus extends BatchFeedStatusBase {
    private static final String DV_UNIT_KEY = "unit";

    private String unitCode;
    private List<DisbursementVoucherBatchSummaryLine> batchSummaryLines;
    private List<DisbursementVoucherDocument> batchDisbursementVoucherDocuments;

    public DisbursementVoucherBatchStatus() {
        statistics = new HashMap<String, Integer>();
        statistics.put(FPConstants.BatchReportStatisticKeys.NUM_DV_RECORDS_READ, 0);
        statistics.put(FPConstants.BatchReportStatisticKeys.NUM_ACCOUNTING_RECORDS_READ, 0);
        statistics.put(FPConstants.BatchReportStatisticKeys.NUM_DV_RECORDS_WRITTEN, 0);
        statistics.put(FPConstants.BatchReportStatisticKeys.NUM_ACCOUNTING_RECORDS_WRITTEN, 0);
        statistics.put(FPConstants.BatchReportStatisticKeys.NUM_GLPE_RECORDS_WRITTEN, 0);

        batchDisbursementVoucherDocuments = new ArrayList<DisbursementVoucherDocument>();
        batchSummaryLines = new ArrayList<DisbursementVoucherBatchSummaryLine>();
    }

    /**
     * Gets the batchSummaryLines attribute.
     * 
     * @return Returns the batchSummaryLines.
     */
    public List<DisbursementVoucherBatchSummaryLine> getBatchSummaryLines() {
        return batchSummaryLines;
    }

    /**
     * Sets the batchSummaryLines attribute value.
     * 
     * @param batchSummaryLines The batchSummaryLines to set.
     */
    public void setBatchSummaryLines(List<DisbursementVoucherBatchSummaryLine> batchSummaryLines) {
        this.batchSummaryLines = batchSummaryLines;
    }

    /**
     * Gets the batchDisbursementVoucherDocuments attribute.
     * 
     * @return Returns the batchDisbursementVoucherDocuments.
     */
    public List<DisbursementVoucherDocument> getBatchDisbursementVoucherDocuments() {
        return batchDisbursementVoucherDocuments;
    }

    /**
     * Sets the batchDisbursementVoucherDocuments attribute value.
     * 
     * @param batchDisbursementVoucherDocuments The batchDisbursementVoucherDocuments to set.
     */
    public void setBatchDisbursementVoucherDocuments(List<DisbursementVoucherDocument> batchDisbursementVoucherDocuments) {
        this.batchDisbursementVoucherDocuments = batchDisbursementVoucherDocuments;
    }

    /**
     * @return Returns the unit.
     */
    public String getUnitCode() {
        return unitCode;
    }

    /**
     * @param unit The unit to set.
     */
    public void setUnitCode(String unit) {
        this.unitCode = unit;
    }

    /**
     * Builds the Map of data needed for the report
     * 
     * @return Map<String, Object>
     */
    public Map<String, Object> getReportData() {
        Map<String, Object> reportData = super.getReportData();

        reportData.put(DV_UNIT_KEY, getUnitCode());

        Collections.sort(batchSummaryLines, new BatchSummaryLinesComparator());
        reportData.put(REPORT_DATA_SUMMARY_LINES_KEY, getBatchSummaryLines());

        Collections.sort(batchDisbursementVoucherDocuments, new BatchDVDocumentsComparator());
        reportData.put(REPORT_DATA_DOCUMENTS_KEY, getBatchDisbursementVoucherDocuments());

        return reportData;
    }

    /**
     * Sorts summary lines by document number
     */
    protected class BatchSummaryLinesComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            DisbursementVoucherBatchSummaryLine summaryLine1 = (DisbursementVoucherBatchSummaryLine) o1;
            DisbursementVoucherBatchSummaryLine summaryLine2 = (DisbursementVoucherBatchSummaryLine) o2;

            return summaryLine1.getDisbVchrPayeeId().compareTo(summaryLine2.getDisbVchrPayeeId());
        }
    }

    /**
     * Sorts summary lines by document number
     */
    protected class BatchDVDocumentsComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            DisbursementVoucherDocument poDocument1 = (DisbursementVoucherDocument) o1;
            DisbursementVoucherDocument poDocument2 = (DisbursementVoucherDocument) o2;

            return poDocument1.getDocumentNumber().compareTo(poDocument2.getDocumentNumber());
        }
    }
}
