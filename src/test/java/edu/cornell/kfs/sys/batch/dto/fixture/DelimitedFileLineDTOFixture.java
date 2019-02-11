package edu.cornell.kfs.sys.batch.dto.fixture;

import java.sql.Date;

import org.joda.time.DateTime;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum DelimitedFileLineDTOFixture {
    GOOD_FILE_LINE_1("Line1", "This is the first line", "02/01/2019", 100.50, true),
    GOOD_FILE_LINE_2("Line2", "This is the second line", "02/04/2019", 33.00, false),
    GOOD_QUOTE_FILE_LINE_1("Line1,With,Many,Commas", "This is the first line", "02/21/2019", 101.52, false),
    GOOD_QUOTE_FILE_LINE_2("Line2", "This is the second line, and has a comma in the description", "02/22/2019", 38.88, true),
    GOOD_SEMICOLON_QUOTE_FILE_LINE_1("Line1;With;Many;Semicolons", "This is the first line", "02/21/2019", 101.52, false),
    GOOD_SEMICOLON_QUOTE_FILE_LINE_2("Line2", "This is the second line; it has a semicolon in the description", "02/22/2019", 38.88, true);

    public final String lineId;
    public final String description;
    public final DateTime lineDate;
    public final KualiDecimal lineAmount;
    public final Boolean lineFlag;

    private DelimitedFileLineDTOFixture(String lineId, String description, String lineDate, double lineAmount, boolean lineFlag) {
        this.lineId = lineId;
        this.description = description;
        this.lineDate = StringToJavaDateAdapter.parseToDateTime(lineDate);
        this.lineAmount = new KualiDecimal(lineAmount);
        this.lineFlag = Boolean.valueOf(lineFlag);
    }

    public Date getLineDateAsSqlDate() {
        return new Date(lineDate.getMillis());
    }

}
