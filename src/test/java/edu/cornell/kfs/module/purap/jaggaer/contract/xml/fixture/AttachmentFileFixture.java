package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AttachmentFileFixture {

    JOHN_COMPILED_DOCUMENT("1161789", 1, 308, "3/16/2023", "Compiled Document",
            "John Adobe Sign Test 2 (no pre-set adobe fields).pdf",
            "CompiledDocument", null, "This is a test compiled document"),
    JOHN_SIGN_TEST2("1162987", 1, 382, "3/16/2023", "John Adobe Sign Test 2 (no pre-set adobe fields).pdf",
            "John Adobe Sign Test 2 (no pre-set adobe fields).pdf",
            "SignedDocument", null, "This doc was signed!"),
    JOHN_MAIN_DOCUMENT("1163555", 2, 36, "3/16/2023", "Main Document",
            "2023-03-01 ContractTemplate-1122334455667-8989898 - Buytest - Long Form Professional Services "
                    + "Agreement Amendment (version 2).docx",
            "MainDocument", null, "THE MAIN DOCUMENT"),
    JOHN_SIGN_TEST2_NO_PRESET("1163556", 1, 207, "3/16/2023",
            "John_Adobe_Sign_Test_2_(no_pre-set_adobe_fields) (print to pdf).pdf",
            "John_Adobe_Sign_Test_2_(no_pre-set_adobe_fields) (print to pdf).pdf",
            "Attachment", null, "Another attachment to test");

    public final String id;
    public final Integer version;
    public final Integer size;
    public final DateTime dateUploaded;
    public final String attachmentDisplayName;
    public final String attachmentFileName;
    public final String attachmentType;
    public final String attachmentFTPpath;
    public final String attachmentAsPlainText;

    private AttachmentFileFixture(String id, Integer version, Integer size, String dateUploaded,
            String attachmentDisplayName, String attachmentFileName, String attachmentType,
            String attachmentFTPpath, String attachmentAsPlainText) {
        this.id = id;
        this.version = version;
        this.size = size;
        this.dateUploaded = StringToJavaDateAdapter.parseToDateTime(dateUploaded);
        this.attachmentDisplayName = attachmentDisplayName;
        this.attachmentFileName = attachmentFileName;
        this.attachmentType = attachmentType;
        this.attachmentFTPpath = attachmentFTPpath;
        this.attachmentAsPlainText = attachmentAsPlainText;
    }

    public String getAttachmentAsBase64String() {
        return StringUtils.isNotBlank(attachmentAsPlainText)
                ? Base64.encodeBase64String(attachmentAsPlainText.getBytes(StandardCharsets.UTF_8))
                : null;
    }

}
