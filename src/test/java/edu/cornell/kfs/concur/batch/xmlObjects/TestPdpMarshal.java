package edu.cornell.kfs.concur.batch.xmlObjects;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TestPdpMarshal {

    private static final String BATCH_DIRECTORY = "test/opt/work/staging/concur/standardAccountingExtract/pdpOutput/";

    private File batchDirectoryFile;

    @Before
    public void setUp() throws Exception {
        batchDirectoryFile = new File(BATCH_DIRECTORY);
        batchDirectoryFile.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(batchDirectoryFile);
    }

    @Test
    public void test() throws JAXBException, IOException, SAXException {
        PdpFile pdpFile = buildPdpFile();
        JAXBContext jaxbContext = JAXBContext.newInstance(PdpFile.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal( pdpFile, System.out );

        File marchalledXml = new File(batchDirectoryFile.getAbsolutePath() + "generatedXML.xml");
        FileUtils.touch(marchalledXml);
        jaxbMarshaller.marshal(pdpFile, marchalledXml);

        assertTrue("The marshalled XML should be greater than 0", FileUtils.sizeOf(marchalledXml) > 0);
    }

    private PdpFile buildPdpFile() {
        PdpFile pdpFile = new PdpFile();
        pdpFile.setHeader(buildHeader());
        pdpFile.setVersion("1");
        pdpFile.setTrailer(buildTrailer());
        pdpFile.setGroup(buildGroups());
        return pdpFile;
    }

    private Header buildHeader() {
        Header header = new Header();
        header.setChart("IT");
        header.setCreationDate("03/01/2017");
        header.setSubUnit("CNCR");
        header.setUnit("CRNL");
        return header;
    }

    private Trailer buildTrailer() {
        Trailer trailer = new Trailer();
        trailer.setDetailCount(new Integer(1));
        trailer.setDetailTotAmt(new Double(500.23));
        return trailer;
    }

    private List<Group> buildGroups() {
        List<Group> groups = new ArrayList();

        Group group = new Group();
        group.setPayeeName("Doe, John, Q");

        PayeeId payeeId = new PayeeId();
        payeeId.setIdType("E");
        payeeId.setContent("3660172");
        group.setPayeeId(payeeId);

        group.setPaymentDate("03/01/2017");
        group.setCombineGroupInd("Y");
        group.setBankCode("DISB");
        group.setDetail(buildDetails());

        groups.add(group);
        return groups;
    }

    private List<Detail> buildDetails() {
        List<Detail> details = new ArrayList();

        Detail detail = new Detail();
        detail.setSourceDocNbr("abc123");
        detail.setFsOriginCd("Z6");
        detail.setFdocTypCd("APTR");
        detail.setAccounting(buildAccounting());

        details.add(detail);
        return details;
    }

    private List<Accounting> buildAccounting() {
        List<Accounting> accountings = new ArrayList();

        Accounting accounting = new Accounting();
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
