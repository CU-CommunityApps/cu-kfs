package edu.cornell.kfs.sys.broker.accesslayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.stream.Stream;

import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.module.cg.businessobject.CuAward;

public class RowReaderMappingBaseClassToSingleExtentClassTest {

    private DescriptorRepository repository;

    @Before
    public void setUp() throws Exception {
        this.repository = new DescriptorRepository();
    }

    @After
    public void tearDown() throws Exception {
        repository = null;
    }

    private ClassDescriptor buildClassDescriptorAndAddToRepository(Class<?> objectClass) {
        return buildClassDescriptorWithExtentsAndAddToRepository(objectClass);
    }

    private ClassDescriptor buildClassDescriptorWithExtentsAndAddToRepository(Class<?> objectClass, Class<?>... extentClasses) {
        ClassDescriptor descriptor = new ClassDescriptor(repository);
        descriptor.setClassOfObject(objectClass);
        Stream.of(extentClasses).forEach(descriptor::addExtentClass);
        repository.put(objectClass, descriptor);
        return descriptor;
    }

    @Test
    public void testMapBaseClassToSingleExtent() throws Exception {
        ClassDescriptor baseDescriptor = buildClassDescriptorWithExtentsAndAddToRepository(Award.class, CuAward.class);
        buildClassDescriptorAndAddToRepository(CuAward.class);
        assertRowReaderMapsToCorrectExtentClass(CuAward.class, baseDescriptor);
    }

    @Test
    public void testMapBaseClassToSingleExtentWhenOtherUnmappedDescriptorsExist() throws Exception {
        ClassDescriptor baseDescriptor = buildClassDescriptorWithExtentsAndAddToRepository(Award.class, CuAward.class);
        buildClassDescriptorAndAddToRepository(CuAward.class);
        buildClassDescriptorAndAddToRepository(org.kuali.kfs.integration.cg.businessobject.Award.class);
        assertRowReaderMapsToCorrectExtentClass(CuAward.class, baseDescriptor);
    }

    @Test
    public void testMappingFailsWhenNoExtentIsDefined() throws Exception {
        ClassDescriptor baseDescriptor = buildClassDescriptorAndAddToRepository(Award.class);
        assertRowReaderThrowsExceptionDueToInvalidMetadata(baseDescriptor);
    }

    @Test
    public void testMappingFailsWhenExtentDescriptorExistsButBaseDescriptorDoesNotMapToExtent() throws Exception {
        ClassDescriptor baseDescriptor = buildClassDescriptorAndAddToRepository(Award.class);
        buildClassDescriptorAndAddToRepository(CuAward.class);
        assertRowReaderThrowsExceptionDueToInvalidMetadata(baseDescriptor);
    }

    @Test
    public void testMappingFailsWhenBaseDescriptorMapsToSingleExtentButExtentDescriptorDoesNotExist() throws Exception {
        ClassDescriptor baseDescriptor = buildClassDescriptorWithExtentsAndAddToRepository(Award.class, CuAward.class);
        assertRowReaderThrowsExceptionDueToInvalidMetadata(baseDescriptor);
    }

    @Test
    public void testMappingFailsWhenMultipleExtentsAreDefined() throws Exception {
        ClassDescriptor baseDescriptor = buildClassDescriptorWithExtentsAndAddToRepository(
                Award.class, CuAward.class, org.kuali.kfs.integration.cg.businessobject.Award.class);
        buildClassDescriptorAndAddToRepository(CuAward.class);
        buildClassDescriptorAndAddToRepository(org.kuali.kfs.integration.cg.businessobject.Award.class);
        assertRowReaderThrowsExceptionDueToInvalidMetadata(baseDescriptor);
    }

    private void assertRowReaderMapsToCorrectExtentClass(Class<?> expectedExtentClass, ClassDescriptor baseDescriptor) throws Exception {
        RowReaderMappingBaseClassToSingleExtentClass rowReader = new RowReaderMappingBaseClassToSingleExtentClass(baseDescriptor);
        ClassDescriptor extentDescriptor = rowReader.selectClassDescriptor(Collections.emptyMap());
        assertNotNull("ClassDescriptor for extent class should not have been null", extentDescriptor);
        assertEquals("ClassDescriptor maps to the wrong extent class", expectedExtentClass, extentDescriptor.getClassOfObject());
    }

    private void assertRowReaderThrowsExceptionDueToInvalidMetadata(ClassDescriptor baseDescriptor) throws Exception {
        RowReaderMappingBaseClassToSingleExtentClass rowReader = new RowReaderMappingBaseClassToSingleExtentClass(baseDescriptor);
        try {
            rowReader.selectClassDescriptor(Collections.emptyMap());
            fail("The RowReader should have thrown a PersistenceBrokerException due to invalid metadata setup");
        } catch (PersistenceBrokerException e) {
            
        }
    }

}
