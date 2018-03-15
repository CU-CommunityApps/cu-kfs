package edu.cornell.kfs.fp.batch.service;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.GroupLevel;

public interface AwsAccountingXmlDocumentAccountingLineService {

    AccountingXmlDocumentAccountingLine createAccountingXmlDocumentAccountingLine(GroupLevel groupLevel, DefaultKfsAccountForAws defaultKfsAccountForAws) throws IllegalArgumentException;

}
