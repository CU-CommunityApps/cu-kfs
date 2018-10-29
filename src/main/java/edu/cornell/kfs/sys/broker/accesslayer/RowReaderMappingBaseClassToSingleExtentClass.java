package edu.cornell.kfs.sys.broker.accesslayer;

import java.util.List;
import java.util.Map;

import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.accesslayer.RowReaderDefaultImpl;
import org.apache.ojb.broker.metadata.ClassDescriptor;

/**
 * Helper RowReader implementation that forces OJB queries against the base class
 * to instead go against one specific extent class. Useful for situations where
 * a custom BO subclass has been introduced and it is stored in the same table,
 * but overriding the various BO service and DAO calls to reference
 * the new subclass is impractical. It also eliminates the need for storing
 * a column containing the object implementation classname, which would normally
 * be required when storing a class and all its subclasses in the same table.
 * 
 * The base class descriptor that uses this implementation must have
 * exactly one extent class defined in its OJB metadata.
 */
public class RowReaderMappingBaseClassToSingleExtentClass extends RowReaderDefaultImpl {

    private static final long serialVersionUID = -6193233283850750044L;

    public RowReaderMappingBaseClassToSingleExtentClass(ClassDescriptor classDescriptor) {
        super(classDescriptor);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected ClassDescriptor selectClassDescriptor(Map row) throws PersistenceBrokerException {
        ClassDescriptor baseClassDescriptor = getClassDescriptor();
        List<?> extentClassNames = baseClassDescriptor.getExtentClassNames();
        if (extentClassNames.size() != 1) {
            throw new PersistenceBrokerException("The class descriptor for " + baseClassDescriptor.getClassNameOfObject()
                    + " should have had exactly 1 extent class, but instead had " + extentClassNames.size());
        }
        
        String extentClassName = (String) extentClassNames.get(0);
        ClassDescriptor extentClassDescriptor = baseClassDescriptor.getRepository().getDescriptorFor(extentClassName);
        if (extentClassDescriptor == null) {
            throw new PersistenceBrokerException("Could not find " + extentClassName
                    + " extent class descriptor for base class " + baseClassDescriptor.getClassNameOfObject());
        }
        
        return extentClassDescriptor;
    }

}
