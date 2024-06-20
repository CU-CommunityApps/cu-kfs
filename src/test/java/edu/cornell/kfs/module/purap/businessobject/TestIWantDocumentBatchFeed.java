package edu.cornell.kfs.module.purap.businessobject;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBContextFactory;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

class TestIWantDocumentBatchFeed {
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/businessobject/xml/";
    private static final String MAXIMO_EXAMPLE_FILE_NAME = "IWantFeed-maximo-example.xml";
    //private static final String JAY_TEST = "jayTest.xml";
    
    private CUMarshalServiceImpl marshalservice;

    @BeforeEach
    public void setUp() throws Exception {
        marshalservice = new CUMarshalServiceImpl();
    }

    @AfterEach
    public void tearDown() throws Exception {
        marshalservice = null;
    }

    @Test
    public void testMaximoExample() throws JAXBException {
        File maximoFile = new File(INPUT_FILE_PATH + MAXIMO_EXAMPLE_FILE_NAME);
        
        //JAXBContext.
        //JAXBContext.newInstance(IWantDocumentBatchFeed.class, null);
        
        //JAXBContext jaxbContext = JAXBContext.newInstance(IWantDocumentBatchFeed.class);
        //Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        
        //IWantDocumentBatchFeed maximoFeed = (IWantDocumentBatchFeed) unmarshaller.unmarshal(maximoFile);
        
        //IWantDocumentBatchFeed maximoFeed = marshalservice.unmarshalFile(maximoFile, IWantDocumentBatchFeed.class);
        
        //Map<String, Object> properties = new HashMap<String, Object>(1);
        //properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, "blog/bindingfile/binding.xml");
        //properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, "blog/bindingfile/binding.xml");
        //JAXBContext jc = JAXBContext.newInstance("blog.bindingfile", Customer.class.getClassLoader() , properties);

        //Unmarshaller unmarshaller = jc.createUnmarshaller();
        //Customer customer = (Customer) unmarshaller.unmarshal(new File("src/blog/bindingfile/input.xml"));

        //Marshaller marshaller = jc.createMarshaller();
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.marshal(customer, System.out);
    }
    
    /*
    
    @Test
    public void testJayExample() throws JAXBException {
        File maximoFile = new File(INPUT_FILE_PATH + JAY_TEST);
        IWantDocumentBatchFeed maximoFeed = marshalservice.unmarshalFile(maximoFile, IWantDocumentBatchFeed.class);
    }*/

}
