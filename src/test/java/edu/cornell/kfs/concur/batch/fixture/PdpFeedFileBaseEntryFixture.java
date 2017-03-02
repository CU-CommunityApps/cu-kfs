package edu.cornell.kfs.concur.batch.fixture;

import java.util.ArrayList;
import java.util.List;

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
        pdpFile.setVersion("1");
        pdpFile.setTrailer(buildTrailer());
        pdpFile.setGroup(buildGroups());
        return pdpFile;
    }

    private static PdpFeedHeaderEntry buildHeader() {
        PdpFeedHeaderEntry header = new PdpFeedHeaderEntry();
        header.setChart("IT");
        header.setCreationDate("03/01/2017");
        header.setSubUnit("CNCR");
        header.setUnit("CRNL");
        return header;
    }

    private static PdpFeedTrailerEntry buildTrailer() {
        PdpFeedTrailerEntry trailer = new PdpFeedTrailerEntry();
        trailer.setDetailCount(new Integer(1));
        trailer.setDetailTotAmt(new Double(500.23));
        return trailer;
    }

    private static List<PdpFeedGroupEntry> buildGroups() {
        List<PdpFeedGroupEntry> groups = new ArrayList();
        groups.add(buildGroup("Doe, John, Q"));
        groups.add(buildGroup("Doe, Jane, S"));
        return groups;
    }
    
    private static PdpFeedGroupEntry buildGroup(String payeeName){
        PdpFeedGroupEntry group = new PdpFeedGroupEntry();
        group.setPayeeName(payeeName);

        PdpFeedPayeeIdEntry payeeId = new PdpFeedPayeeIdEntry();
        payeeId.setIdType("E");
        payeeId.setContent("3660172");
        group.setPayeeId(payeeId);

        group.setPaymentDate("03/01/2017");
        group.setCombineGroupInd("Y");
        group.setBankCode("DISB");
        group.setDetail(buildDetails());
        return group;
    }

    private static List<PdpFeedDetailEntry> buildDetails() {
        List<PdpFeedDetailEntry> details = new ArrayList();

        PdpFeedDetailEntry detail = new PdpFeedDetailEntry();
        detail.setSourceDocNbr("abc123");
        detail.setFsOriginCd("Z6");
        detail.setFdocTypCd("APTR");
        detail.setAccounting(buildAccounting());

        details.add(detail);
        return details;
    }

    private static List<PdpFeedAccountingEntry> buildAccounting() {
        List<PdpFeedAccountingEntry> accountings = new ArrayList();

        PdpFeedAccountingEntry accounting = new PdpFeedAccountingEntry();
        accounting.setCoaCd("IT");
        accounting.setAccountNbr("G234715");
        accounting.setSubAccountNbr("subA1");
        accounting.setObjectCd("6666");
        accounting.setSubObjectCd("subO1");
        accounting.setOrgRefId("orgRef");
        accounting.setProjectCd("proj1");
        accounting.setAmount("500.23");

        accountings.add(accounting);
        return accountings;
    }
}
