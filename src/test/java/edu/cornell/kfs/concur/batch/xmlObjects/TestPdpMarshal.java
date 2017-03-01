package edu.cornell.kfs.concur.batch.xmlObjects;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPdpMarshal {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws JAXBException {
        PdpFile pdpFile = buildPdpFile();
        JAXBContext jaxbContext = JAXBContext.newInstance( PdpFile.class );
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal( pdpFile, System.out );


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
        trailer.setDetailCount(new BigInteger("1"));
        trailer.setDetailTotAmt(new BigDecimal(500.23));
        return trailer;
    }
    
    private List<Group> buildGroups() {
        List<Group> groups = new ArrayList();
        
        Group group = new Group();
        group.setPayeeName("Doe, John, Q");
        /**
         * @todo fix this
         */
        //group.setPayeeId(value);
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
