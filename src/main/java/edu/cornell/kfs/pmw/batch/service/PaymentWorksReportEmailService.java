package edu.cornell.kfs.pmw.batch.service;

public interface PaymentWorksReportEmailService {
    void sendEmail(String toAddress, String fromAddress, String subject, String body);
}
