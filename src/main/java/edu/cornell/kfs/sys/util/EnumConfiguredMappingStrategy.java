package edu.cornell.kfs.sys.util;

import java.util.Arrays;
import java.util.function.Function;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class EnumConfiguredMappingStrategy<T, E extends Enum<E>> extends ColumnPositionMappingStrategy<T> {

    private Class<E> enumClass;
    private Function<E, String> enumToHeaderLabelMapper;
    private Function<E, String> enumToPojoPropertyNameMapper;

    private String[] headerLabels;

    public EnumConfiguredMappingStrategy(Class<E> enumClass, Function<E, String> enumToHeaderLabelMapper,
            Function<E, String> enumToPojoPropertyNameMapper) {
        super();
        
        if (enumClass == null) {
            throw new IllegalArgumentException("enumClass cannot be null");
        } else if (enumToHeaderLabelMapper == null) {
            throw new IllegalArgumentException("enumToHeaderLabelMapper cannot be null");
        } else if (enumToPojoPropertyNameMapper == null) {
            throw new IllegalArgumentException("enumToPojoPropertyNameMapper cannot be null");
        }
        
        this.enumClass = enumClass;
        this.enumToHeaderLabelMapper = enumToHeaderLabelMapper;
        this.enumToPojoPropertyNameMapper = enumToPojoPropertyNameMapper;
        
        initializeOrderedHeaderLabelMappings();
        initializeOrderedPojoPropertyNameMappings();
    }

    private void initializeOrderedHeaderLabelMappings() {
        this.headerLabels = Arrays.stream(enumClass.getEnumConstants())
                .map(enumToHeaderLabelMapper)
                .toArray(String[]::new);
    }

    private void initializeOrderedPojoPropertyNameMappings() {
        String[] propertyNames = Arrays.stream(enumClass.getEnumConstants())
                .map(enumToPojoPropertyNameMapper)
                .toArray(String[]::new);
        setColumnMapping(propertyNames);
    }

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        return Arrays.copyOf(headerLabels, headerLabels.length);
    }

}
