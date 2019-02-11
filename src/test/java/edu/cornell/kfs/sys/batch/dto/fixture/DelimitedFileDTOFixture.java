package edu.cornell.kfs.sys.batch.dto.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DelimitedFileDTOFixture {
    GOOD_FILE("GoodFile", "This file should parse correctly",
            DelimitedFileLineDTOFixture.GOOD_FILE_LINE_1,
            DelimitedFileLineDTOFixture.GOOD_FILE_LINE_2),
    GOOD_QUOTE_FILE("GoodQuoteFile", "This file should parse correctly, because its data commas are quoted",
            DelimitedFileLineDTOFixture.GOOD_QUOTE_FILE_LINE_1,
            DelimitedFileLineDTOFixture.GOOD_QUOTE_FILE_LINE_2),
    GOOD_SEMICOLON_QUOTE_FILE("GoodQuoteFile", "This file should parse correctly; its data semicolons are quoted",
            DelimitedFileLineDTOFixture.GOOD_SEMICOLON_QUOTE_FILE_LINE_1,
            DelimitedFileLineDTOFixture.GOOD_SEMICOLON_QUOTE_FILE_LINE_2);

    public final String fileId;
    public final String description;
    public final List<DelimitedFileLineDTOFixture> fileLines;

    private DelimitedFileDTOFixture(String fileId, String description, DelimitedFileLineDTOFixture... fileLines) {
        this.fileId = fileId;
        this.description = description;
        this.fileLines = Collections.unmodifiableList(Arrays.asList(fileLines));
    }

}
