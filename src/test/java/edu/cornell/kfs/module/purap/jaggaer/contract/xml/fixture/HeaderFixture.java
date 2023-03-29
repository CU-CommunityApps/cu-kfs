package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.time.ZonedDateTime;

import edu.cornell.kfs.sys.xmladapters.ZonedStringToJavaDateXmlAdapter;

public enum HeaderFixture {

    SINGLE_TEST_CONTRACT("SingleTestContract", "2023-03-16T17:45:33.888-05:00", "CornellU", "CornellSecret");

    public final String messageId;
    public final ZonedDateTime timestamp;
    public final String identity;
    public final String sharedSecret;

    private HeaderFixture(String messageId, String timestamp, String identity, String sharedSecret) {
        this.messageId = messageId;
        this.timestamp = ZonedStringToJavaDateXmlAdapter.parseToZonedDateTime(timestamp);
        this.identity = identity;
        this.sharedSecret = sharedSecret;
    }

}
