package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.time.ZonedDateTime;

import edu.cornell.kfs.sys.xmladapters.ZonedStringToJavaDateXmlAdapter;

public enum HeaderFixture {

    HEADER01("Header01", "2023-03-01T05:05:05.123Z", "CornellU", "CornellSecret");

    public final String messageId;
    public final ZonedDateTime timestamp;
    public final String identity;
    public final String sharedSecret;

    private HeaderFixture(String messageId, String timestamp, String identity, String sharedSecret) {
        this.messageId = messageId;
        this.timestamp = ZonedDateTime.parse(timestamp, ZonedStringToJavaDateXmlAdapter.DATE_FORMATTER);
        this.identity = identity;
        this.sharedSecret = sharedSecret;
    }

}
