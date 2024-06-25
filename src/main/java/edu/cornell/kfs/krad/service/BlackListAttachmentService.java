package edu.cornell.kfs.krad.service;

public interface BlackListAttachmentService {
    public boolean attachmentFileExtensionIsDisallowed(String uploadedFileName);
}
