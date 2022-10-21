package edu.cornell.kfs.sys.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

public class CuJaxbProviderTest {

    @XmlRootElement(name = "example_dto")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class ExampleDTO {}

    private static final String CHARSET_UTF8_SUFFIX = ";charset=utf-8";

    private CuJaxbProvider cuJaxbProvider;

    @BeforeEach
    void setUp() throws Exception {
        this.cuJaxbProvider = new CuJaxbProvider();
    }

    @AfterEach
    void tearDown() throws Exception {
        cuJaxbProvider = null;
    }

    static Stream<String> exampleXmlMediaTypes() {
        return Stream.of(
                MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_ATOM_XML,
                MediaType.APPLICATION_SVG_XML,
                MediaType.APPLICATION_XHTML_XML
        ).flatMap(mediaType -> Stream.of(mediaType, mediaType + CHARSET_UTF8_SUFFIX));
    }

    static Stream<String> exampleNonXmlMediaTypes() {
        return Stream.of(
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_JSON,
                MediaType.MULTIPART_FORM_DATA,
                MediaType.WILDCARD
        ).flatMap(mediaType -> Stream.of(mediaType, mediaType + CHARSET_UTF8_SUFFIX));
    }

    @ParameterizedTest
    @MethodSource("exampleXmlMediaTypes")
    void testCheckXmlMediaTypeAndXmlDTO(String mediaType) throws Exception {
        assertProviderAllowsReadingAndWriting(mediaType, ExampleDTO.class);
    }

    @ParameterizedTest
    @MethodSource("exampleNonXmlMediaTypes")
    void testCheckNonXmlMediaTypeAndXmlDTO(String mediaType) throws Exception {
        assertProviderDoesNotAllowReadingOrWriting(mediaType, ExampleDTO.class);
    }

    @ParameterizedTest
    @MethodSource("exampleXmlMediaTypes")
    void testCheckXmlMediaTypeAndNonXmlObject(String mediaType) throws Exception {
        assertProviderDoesNotAllowReadingOrWriting(mediaType, String.class);
    }

    @ParameterizedTest
    @MethodSource("exampleNonXmlMediaTypes")
    void testCheckNonXmlMediaTypeAndNonXmlObject(String mediaType) throws Exception {
        assertProviderDoesNotAllowReadingOrWriting(mediaType, String.class);
    }

    private void assertProviderAllowsReadingAndWriting(String mediaType, Class<?> javaType) {
        Annotation[] annotations = javaType.getAnnotations();
        MediaType mediaTypeInstance = MediaType.valueOf(mediaType);
        assertTrue(cuJaxbProvider.isReadable(javaType, null, annotations, mediaTypeInstance),
                "Provider should have allowed reading class " + javaType.getName() + " and type " + mediaType);
        assertTrue(cuJaxbProvider.isWriteable(javaType, null, annotations, mediaTypeInstance),
                "Provider should have allowed writing class " + javaType.getName() + " and type " + mediaType);
    }

    private void assertProviderDoesNotAllowReadingOrWriting(String mediaType, Class<?> javaType) {
        Annotation[] annotations = javaType.getAnnotations();
        MediaType mediaTypeInstance = MediaType.valueOf(mediaType);
        assertFalse(cuJaxbProvider.isReadable(javaType, null, annotations, mediaTypeInstance),
                "Provider should not have allowed reading class " + javaType.getName() + " and type " + mediaType);
        assertFalse(cuJaxbProvider.isWriteable(javaType, null, annotations, mediaTypeInstance),
                "Provider should not have allowed writing class " + javaType.getName() + " and type " + mediaType);
    }

}
