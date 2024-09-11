package com.rsmart.kuali.kfs.cr.batch.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum StaleCheckFileFixture {

    FILE_WITH_BAD_HEADERS("stale_check_test_bad_headers", false),
    FILE_WITH_HEADER_ROW_ONLY("stale_check_test_header_only", false),
    FILE_WITH_SINGLE_SUCCESSFUL_LINE("stale_check_test_good_one_line", StaleCheckRowFixture.CHECK_999_JPCD_STAL_925_VALID),
    FILE_MIXED_LINES("stale_check_test_mixed_lines",
            StaleCheckRowFixture.CHECK_999_JPCD_STAL_925_VALID,
            StaleCheckRowFixture.CHECK_123_JPCD_STAL_29252_VALID,
            StaleCheckRowFixture.CHECK_999_JPCD_BLANK_INVALID,
            StaleCheckRowFixture.CHECK_123_BLANK_STAL_19223_INVALID,
            StaleCheckRowFixture.CHECK_199_JPCD_STAL_212319_VALID),
    FILE_MULTIPLE_VALID_LINES("stale_check_test_multi_good",
            StaleCheckRowFixture.CHECK_999_JPCD_STAL_925_VALID,
            StaleCheckRowFixture.CHECK_123_JPCD_STAL_29252_VALID,
            StaleCheckRowFixture.CHECK_199_JPCD_STAL_212319_VALID),
    FILE_MULTIPLE_INVALID_STATUS_LINES("stale_check_test_multi_bad_status",
            StaleCheckRowFixture.CHECK_399_JPCD_VOID_951,
            StaleCheckRowFixture.CHECK_111_JPCD_CLRD_123,
            StaleCheckRowFixture.CHECK_211_JPCD_STOP_123,
            StaleCheckRowFixture.CHECK_311_JPCD_STAL_123,
            StaleCheckRowFixture.CHECK_411_JPCD_EXCP_123,
            StaleCheckRowFixture.CHECK_511_JPCD_CDIS_321);

    public final String baseFileName;
    public final List<StaleCheckRowFixture> staleCheckRows;
    public final boolean processableFile;

    StaleCheckFileFixture(String baseFileName, boolean processableFile) {
        this.baseFileName = baseFileName;
        this.staleCheckRows = Collections.emptyList();
        this.processableFile = processableFile;
    }

    StaleCheckFileFixture(String baseFileName, StaleCheckRowFixture... rowResults) {
        this.baseFileName = baseFileName;
        this.staleCheckRows = Collections.unmodifiableList(Arrays.asList(rowResults));
        this.processableFile = true;
    }

    public String getBaseFileName() {
        return baseFileName;
    }

}
