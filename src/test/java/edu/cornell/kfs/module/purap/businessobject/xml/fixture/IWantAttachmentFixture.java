package edu.cornell.kfs.module.purap.businessobject.xml.fixture;

import edu.cornell.kfs.module.purap.businessobject.xml.IWantAttachmentXml;

public enum IWantAttachmentFixture {
    
    ATTACH_TEST("mime type", "file name", "attachment type");
    
    public final String mimeTypeCode;
    public final String fileName;
    public final String attachmentType;
    
    private IWantAttachmentFixture(String mimeTypeCode, String fileName, String attachmentType) {
        this.mimeTypeCode = mimeTypeCode;
        this.fileName = fileName;
        this.attachmentType = attachmentType;
    }
    
    public IWantAttachmentXml toIWantAttachmentXml() {
        IWantAttachmentXml attach = new IWantAttachmentXml();
        attach.setAttachmentType(attachmentType);
        attach.setFileName(fileName);
        attach.setMimeTypeCode(mimeTypeCode);
        return attach;
    }

}
