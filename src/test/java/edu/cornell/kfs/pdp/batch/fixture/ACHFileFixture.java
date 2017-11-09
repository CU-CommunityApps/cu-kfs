package edu.cornell.kfs.pdp.batch.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ACHFileFixture {
    FILE_WITH_BAD_HEADERS("pdp_ach_test_badHeaders", false),

    FILE_WITH_HEADER_ROW_ONLY("pdp_ach_test_headerOnly", false),

    FILE_WITH_ONLY_VALIDATION_FAILURE_LINES("pdp_ach_test_badLines",
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE),

    FILE_WITH_SINGLE_SUCCESSFUL_LINE("pdp_ach_test_good", ACHRowFixture.JOHN_DOE_CREATE_EMPLOYEE_CREATE_ENTITY),

    FILE_WITH_SINGLE_MATCHING_LINE("pdp_ach_test_matching", ACHRowFixture.VALID_ROW_NO_UPDATES),

    FILE_WITH_GOOD_AND_BAD_LINES("pdp_ach_test_mixGoodBadLines",
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.JANE_DOE_UPDATE_EMPLOYEE_CREATE_ENTITY,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.JANE_DOE_UPDATE_EMPLOYEE_CREATE_ENTITY, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.ROW_WITH_VALIDATION_FAILURE, ACHRowFixture.ROW_WITH_VALIDATION_FAILURE,
            ACHRowFixture.JANE_DOE_UPDATE_EMPLOYEE_CREATE_ENTITY, ACHRowFixture.JANE_DOE_UPDATE_EMPLOYEE_CREATE_ENTITY),

    FILE_WITH_MULTIPLE_SUCCESSFUL_LINES("pdp_ach_test_multiGood",
            ACHRowFixture.JOHN_DOE_CREATE_EMPLOYEE_CREATE_ENTITY, ACHRowFixture.JANE_DOE_UPDATE_EMPLOYEE_CREATE_ENTITY,
            ACHRowFixture.MARY_SMITH_UPDATE_EMPLOYEE_UPDATE_ENTITY, ACHRowFixture.ROBERT_SMITH_CREATE_EMPLOYEE_UPDATE_ENTITY),

    FILE_WITH_PARTIAL_AND_FULL_MATCHES("pdp_ach_test_multiMatching",
            ACHRowFixture.ROBERT_SMITH_CREATE_EMPLOYEE_ONLY, ACHRowFixture.JANE_DOE_CREATE_ENTITY_ONLY,
            ACHRowFixture.VALID_ROW_NO_UPDATES);

    public final String baseFileName;
    public final List<ACHRowFixture> rowResults;
    public final boolean processableFile;

    private ACHFileFixture(String baseFileName, boolean processableFile) {
        this.baseFileName = baseFileName;
        this.rowResults = Collections.emptyList();
        this.processableFile = processableFile;
    }

    private ACHFileFixture(String baseFileName, ACHRowFixture... rowResults) {
        this.baseFileName = baseFileName;
        this.rowResults = Collections.unmodifiableList(Arrays.asList(rowResults));
        this.processableFile = true;
    }

    public String getBaseFileName() {
        return baseFileName;
    }

}
