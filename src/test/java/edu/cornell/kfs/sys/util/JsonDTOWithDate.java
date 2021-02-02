package edu.cornell.kfs.sys.util;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import edu.cornell.kfs.sys.CUKFSConstants;

public class JsonDTOWithDate {
    @JsonFormat(pattern = CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS,
            locale = CUKFSConstants.US_LOCALE_STRING)
    private Date dateValue;

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }
}
