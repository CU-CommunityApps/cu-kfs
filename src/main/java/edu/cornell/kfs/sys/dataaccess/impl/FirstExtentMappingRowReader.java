package edu.cornell.kfs.sys.dataaccess.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.accesslayer.RowReaderDefaultImpl;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;

/**
 * Custom row reader implementation that forces queries for the current class
 * to automatically map to the first extent class instead.
 */
public class FirstExtentMappingRowReader extends RowReaderDefaultImpl {

    private static final long serialVersionUID = 7021842271224735930L;

    private ClassDescriptor classDescriptor;

    public FirstExtentMappingRowReader(ClassDescriptor classDescriptor) {
        super(classDescriptor);
        this.classDescriptor = classDescriptor;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected ClassDescriptor selectClassDescriptor(Map row) throws PersistenceBrokerException {
        List<?> extentClasses = classDescriptor.getExtentClasses();
        if (CollectionUtils.isNotEmpty(extentClasses)) {
            Class<?> firstExtentClass = (Class<?>) extentClasses.get(0);
            DescriptorRepository repository = classDescriptor.getRepository();
            if (repository.hasDescriptorFor(firstExtentClass)) {
                return repository.getDescriptorFor(firstExtentClass);
            } else {
                throw new PersistenceBrokerException("Could not find descriptor for extent class " + firstExtentClass.getName());
            }
        } else {
            throw new PersistenceBrokerException("No extent classes found for " + classDescriptor.getClassNameOfObject());
        }
    }

}
