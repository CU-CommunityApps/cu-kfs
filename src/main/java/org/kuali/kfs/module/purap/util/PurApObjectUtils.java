/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.web.format.FormatException;
import org.kuali.kfs.core.web.format.PhoneNumberFormatter;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.context.SpringContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ====
 * CU Customization: Backported the FINP-11153 changes.
 *                   This overlay can be removed when we upgrade to the 2024-07-17 financials patch.
 * ====
 * 
 * Purap Object Utils.
 * Similar to the nervous system ObjectUtils this class contains methods to reflectively set and get values on
 * BusinessObjects that are passed in.
 */
public final class PurApObjectUtils {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private PurApObjectUtils() {
    }

    /**
     * Populates a class using a base class to determine fields
     *
     * @param base                   the class to determine what fields to copy
     * @param src                    the source class
     * @param target                 the target class
     * @param supplementalUncopyable a list of fields to never copy
     */
    public static void populateFromBaseClass(
            final Class base, final BusinessObject src, final BusinessObject target,
            final Map supplementalUncopyable) {
        final List<String> fieldNames = new ArrayList<>();
        final Field[] fields = base.getDeclaredFields();

        for (final Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers())) {
                fieldNames.add(field.getName());
            } else {
                LOG.debug("field {} is transient, skipping ", field::getName);
            }
        }
        int counter = 0;
        for (final String fieldName : fieldNames) {
            if (isProcessableField(base, fieldName, PurapConstants.KNOWN_UNCOPYABLE_FIELDS)
                    && isProcessableField(base, fieldName, supplementalUncopyable)) {
                attemptCopyOfFieldName(base.getName(), fieldName, src, target, supplementalUncopyable);
                counter++;
            }
        }
        final int loggableCounter = counter;
        LOG.debug(
                "Population complete for {} fields out of a total of {} potential fields in object with base "
                + "class '{}'",
                () -> loggableCounter,
                fieldNames::size,
                () -> base
        );
    }

    /**
     * Copies based on a class template it does not copy fields in Known Uncopyable Fields
     *
     * @param base   the base class
     * @param src    source
     * @param target target
     */
    public static void populateFromBaseClass(final Class base, final BusinessObject src, final BusinessObject target) {
        populateFromBaseClass(base, src, target, new HashMap());
    }

    /**
     * True if a field is processable
     *
     * @param baseClass          the base class
     * @param fieldName          the field name to determine if processable
     * @param excludedFieldNames field names to exclude
     * @return true if a field is processable
     */
    protected static boolean isProcessableField(final Class baseClass, final String fieldName, final Map excludedFieldNames) {
        if (excludedFieldNames.containsKey(fieldName)) {
            final Class potentialClassName = (Class) excludedFieldNames.get(fieldName);
            return ObjectUtils.isNotNull(potentialClassName) && !potentialClassName.equals(baseClass);
        }
        return true;
    }

    /**
     * Attempts to copy a field
     *
     * @param baseClassName          the base class
     * @param fieldName              the field name to determine if processable
     * @param sourceObject           source object
     * @param targetObject           target object
     * @param supplementalUncopyable
     */
    protected static void attemptCopyOfFieldName(
            final String baseClassName, final String fieldName, final BusinessObject sourceObject,
            final BusinessObject targetObject, final Map supplementalUncopyable) {
        try {
            final Object propertyValue = ObjectUtils.getPropertyValue(sourceObject, fieldName);
            if (ObjectUtils.isNotNull(propertyValue) && Collection.class.isAssignableFrom(propertyValue.getClass())) {
                LOG.debug(
                        "attempting to copy collection field '{}' using base class '{}' and property value class "
                        + "'{}'",
                        () -> fieldName,
                        () -> baseClassName,
                        propertyValue::getClass
                );
                copyCollection(fieldName, targetObject, (Collection) propertyValue, supplementalUncopyable);
            } else {
                final String propertyValueClass = ObjectUtils.isNotNull(propertyValue) ?
                        propertyValue.getClass().toString() : "(null)";
                LOG.debug(
                        "attempting to set field '{}' using base class '{}' and property value class '{}'",
                        fieldName,
                        baseClassName,
                        propertyValueClass
                );
                // ==== CU Customization: Backport the FINP-11153 fix. ====
                // Now that we correctly inherit PO attributes, we are also getting the formatter from the PO. In
                // this case we do not want that formatter for phone numbers, as this removes critical characters.
                // This is not ideal but feels like the safest fix while maintaining the current bean hierarchy.
                final var formatter = ObjectUtils.getFormatterWithDataDictionary(targetObject, fieldName);
                if (formatter instanceof PhoneNumberFormatter) {
                    ObjectUtils.setObjectProperty(null, targetObject, fieldName, null, propertyValue);
                } else {
                    ObjectUtils.setObjectProperty(targetObject, fieldName, propertyValue);
                }
                // ==== End CU Customization ====
            }
        } catch (final Exception e) {
            // purposefully skip for now (I wish objectUtils getPropertyValue threw named errors instead of runtime)
            // so I could selectively skip
            final String exceptionClassname = e.getClass().getName();
            LOG.debug(
                    "couldn't set field '{}' using base class '{}' due to exception with class name '{}'",
                    fieldName,
                    baseClassName,
                    exceptionClassname,
                    e
            );
        }
    }

    /**
     * Copies a collection
     *
     * @param fieldName              field to copy
     * @param targetObject           the object of the collection
     * @param sourceList             value to copy
     * @param supplementalUncopyable uncopyable fields
     * @throws FormatException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    protected static <T extends BusinessObject> void copyCollection(
            final String fieldName, final BusinessObject targetObject,
            final Collection<T> sourceList, final Map supplementalUncopyable) throws FormatException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (ObjectUtils.isNotNull(sourceList)) {
            ObjectUtils.materializeObjects(sourceList);
        }

        Collection listToSet;
        // ArrayList requires argument so handle differently than below
        try {
            listToSet = sourceList.getClass().newInstance();
        } catch (final Exception e) {
            LOG.debug(
                    "couldn't set class '{}' on collection...{} using {}",
                    sourceList::getClass,
                    () -> fieldName,
                    sourceList::getClass
            );
            listToSet = new ArrayList<T>();
        }

        for (final BusinessObject sourceCollectionObject : sourceList) {
            LOG.debug("attempting to copy collection member with class '{}'", sourceCollectionObject::getClass);
            final BusinessObject targetCollectionObject = ObjectUtils.createNewObjectFromClass(
                    sourceCollectionObject.getClass());
            populateFromBaseWithSuper(sourceCollectionObject, targetCollectionObject, supplementalUncopyable,
                    new HashSet<>());
            final Map pkMap = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(
                    targetCollectionObject);
            final Set<String> pkFields = pkMap.keySet();
            for (final String field : pkFields) {
                ObjectUtils.setObjectProperty(targetCollectionObject, field, null);
            }
            listToSet.add(targetCollectionObject);
        }
        ObjectUtils.setObjectProperty(targetObject, fieldName, listToSet);
    }

    /**
     * Populates from a base class traversing up the object hierarchy.
     *
     * @param sourceObject                     object to copy from
     * @param targetObject                     object to copy to
     * @param supplementalUncopyableFieldNames fields to exclude
     * @param classesToExclude                 classes to exclude
     */
    public static void populateFromBaseWithSuper(
            final BusinessObject sourceObject, final BusinessObject targetObject,
            final Map supplementalUncopyableFieldNames, final Set<Class> classesToExclude) {
        final List<Class> classesToCopy = new ArrayList<>();
        Class sourceObjectClass = sourceObject.getClass();
        classesToCopy.add(sourceObjectClass);
        while (sourceObjectClass.getSuperclass() != null) {
            sourceObjectClass = sourceObjectClass.getSuperclass();
            if (!classesToExclude.contains(sourceObjectClass)) {
                classesToCopy.add(sourceObjectClass);
            }
        }
        for (int i = classesToCopy.size() - 1; i >= 0; i--) {
            final Class temp = classesToCopy.get(i);
            populateFromBaseClass(temp, sourceObject, targetObject, supplementalUncopyableFieldNames);
        }
    }

    // ***** following changes are to work around an ObjectUtils bug and are copied from ObjectUtils.java

    /**
     * Compares a business object with a List of BOs to determine if an object with the same key as the BO exists in
     * the list. If it does, the item is returned.
     *
     * @param controlList The list of items to check
     * @param bo          The BO whose keys we are looking for in the controlList
     */
    public static BusinessObject retrieveObjectWithIdentitcalKey(final Collection controlList, final BusinessObject bo) {
        BusinessObject returnBo = null;

        for (final Object control : controlList) {
            final BusinessObject listBo = (BusinessObject) control;
            if (equalByKeys(listBo, bo)) {
                returnBo = listBo;
            }
        }

        return returnBo;
    }

    /**
     * Compares two business objects for equality of type and key values.
     *
     * @param bo1
     * @param bo2
     * @return boolean indicating whether the two objects are equal.
     */
    public static boolean equalByKeys(final BusinessObject bo1, final BusinessObject bo2) {
        if (bo1 == null && bo2 == null) {
            return true;
        } else if (bo1 == null || bo2 == null) {
            return false;
        } else if (!bo1.getClass().getName().equals(bo2.getClass().getName())) {
            return false;
        } else {
            final Map bo1Keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(bo1);
            final Map bo2Keys = SpringContext.getBean(PersistenceService.class).getPrimaryKeyFieldValues(bo2);
            for (final Object key : bo1Keys.keySet()) {
                final String keyName = (String) key;
                if (bo1Keys.get(keyName) != null && bo2Keys.get(keyName) != null) {
                    if (!bo1Keys.get(keyName).toString().equals(bo2Keys.get(keyName).toString())) {
                        return false;
                    }
                } else {
                    // CHANGE FOR PurapOjbCollectionHelper change if one is null we are likely looking at a new object
                    // (sequence) which is definitely not equal
                    return false;
                }
            }
        }

        return true;
    }
    // ***** END copied from ObjectUtils.java changes
}
