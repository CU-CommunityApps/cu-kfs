package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.xmladapters.ZonedStringToJavaDateXmlAdapter;

public enum AttachmentFileFixture {

    FILE_01("123", 1, 1, "2023-03-10T16:17:18.333Z", "Contract Document", "ContractDocument.txt",
            "Unknown", null, "");

    public final String id;
    public final Integer version;
    public final Integer size;
    public final ZonedDateTime dateUploaded;
    public final String attachmentDisplayName;
    public final String attachmentFileName;
    public final String attachmentType;
    public final String attachmentFTPpath;
    public final String attachmentAsString;
    public final String attachmentBase64;

    private AttachmentFileFixture(String id, Integer version, Integer size, String dateUploaded,
            String attachmentDisplayName, String attachmentFileName, String attachmentType,
            String attachmentFTPpath, String attachmentAsString) {
        this.id = id;
        this.version = version;
        this.size = size;
        this.dateUploaded = ZonedDateTime.parse(dateUploaded, ZonedStringToJavaDateXmlAdapter.DATE_FORMATTER);
        this.attachmentDisplayName = attachmentDisplayName;
        this.attachmentFileName = attachmentFileName;
        this.attachmentType = attachmentType;
        this.attachmentFTPpath = attachmentFTPpath;
        this.attachmentAsString = attachmentAsString;
        this.attachmentBase64 = StringUtils.isNotBlank(attachmentAsString)
                ? Base64.encodeBase64String(attachmentAsString.getBytes(StandardCharsets.UTF_8))
                : null;
    }

}
