package edu.cornell.kfs.concur.batch.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedAccountingEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedDetailEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedFileBaseEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedGroupEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedHeaderEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedPayeeIdEntry;
import edu.cornell.kfs.concur.batch.xmlObjects.PdpFeedTrailerEntry;

public class PdpFeedFileBaseEntryFixture {
    public static PdpFeedFileBaseEntry buildPdpFile() {
        PdpFeedFileBaseEntry pdpFile = new PdpFeedFileBaseEntry();
        pdpFile.setHeader(buildHeader());
        pdpFile.setVersion("1.0");
        pdpFile.setTrailer(buildTrailer());
        pdpFile.setGroup(buildGroups());
        return pdpFile;
    }

    private static PdpFeedHeaderEntry buildHeader() {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setChart("IT");
        header.setCreationDate("02/17/2017");
        header.setSubUnit("CLIF");
        header.setUnit("CRNL");
        return header;
    }

    private static PdpFeedTrailerEntry buildTrailer() {
        PdpFeedTrailerEntry trailer = new PdpFeedTrailerEntry();
        trailer.setDetailCount(new Integer(1));
        trailer.setDetailTotAmt(new KualiDecimal(96.95));
        return trailer;
    }

    private static List<PdpFeedGroupEntry> buildGroups() {
        List<PdpFeedGroupEntry> groups = new ArrayList();
        groups.add(buildGroup("Bimbo Foods Inc"));
        return groups;
    }
    
    private static PdpFeedGroupEntry buildGroup(String payeeName){
        PdpFeedGroupEntry group = new PdpFeedGroupEntry();
        group.setPayeeName(payeeName);

        PdpFeedPayeeIdEntry payeeId = new PdpFeedPayeeIdEntry();
        payeeId.setIdType("V");
        payeeId.setContent("13086-0");
        group.setPayeeId(payeeId);
        group.setPayeeOwnCd("xyz");
        group.setCustomerInstitutionIdentifier("18901");
        
        group.setPaymentDate("02/03/2017");
        group.setCombineGroupInd("Y");
        group.setBankCode("DISB");
        group.setDetail(buildDetails());
        return group;
    }

    private static List<PdpFeedDetailEntry> buildDetails() {
        List<PdpFeedDetailEntry> details = new ArrayList();

        PdpFeedDetailEntry detail = new PdpFeedDetailEntry();
        detail.setSourceDocNbr("C16326");
        detail.setInvoiceNbr("66432520714");
        detail.setInvoiceDate("02/03/2017");
        detail.setOrigInvoiceAmt(new KualiDecimal(105.28));
        detail.setFsOriginCd("Z1");
        detail.setFdocTypCd("APCL");
        detail.setAccounting(buildAccounting());

        details.add(detail);
        return details;
    }

    private static List<PdpFeedAccountingEntry> buildAccounting() {
        List<PdpFeedAccountingEntry> accountings = new ArrayList();

        PdpFeedAccountingEntry accounting = new PdpFeedAccountingEntry();
        accounting.setCoaCd("IT");
        accounting.setAccountNbr("H833810");
        accounting.setObjectCd("6000");
        accounting.setSubObjectCd("365");
        accounting.setAmount(new KualiDecimal(25.50));
        
        PdpFeedAccountingEntry accounting2 = new PdpFeedAccountingEntry();
        accounting2.setCoaCd("IT");
        accounting2.setAccountNbr("H833810");
        accounting2.setObjectCd("6000");
        accounting2.setSubObjectCd("350");
        accounting2.setAmount(new KualiDecimal(71.45));

        accountings.add(accounting);
        accountings.add(accounting2);
        return accountings;
    }
}
