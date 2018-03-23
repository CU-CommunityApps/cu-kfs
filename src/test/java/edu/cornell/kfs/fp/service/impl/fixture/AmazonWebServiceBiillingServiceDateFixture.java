package edu.cornell.kfs.fp.service.impl.fixture;

public enum AmazonWebServiceBiillingServiceDateFixture {
    JULY_2016("2016,7", "July", "7", "2016", "2016-07-01", "2016-07-31"),
    JANUARY_2017("2017,1", "January", "1", "2017", "2017-01-01", "2017-01-31"),
    DECEMBER_2015("2015,12", "December", "12", "2015", "2015-12-01", "2015-12-31"),
    FEBRUARY_2016("2016,2", "February", "2", "2016", "2016-02-01", "2016-02-29"),
    FEBRUARY_2017("2017,2", "February", "2", "2017", "2017-02-01", "2017-02-28");
    
    public final String processMonthInputParameter;
    public final String monthName;
    public final String monthNumber;
    public final String year;
    public final String startDate;
    public final String endDate;
    
    private AmazonWebServiceBiillingServiceDateFixture(String processMonthInputParameter, String monthName, String monthNumber, String year, 
            String startDate, String endDate) {
        this.processMonthInputParameter = processMonthInputParameter;
        this.monthName = monthName;
        this.monthNumber = monthNumber;
        this.year = year;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
}
