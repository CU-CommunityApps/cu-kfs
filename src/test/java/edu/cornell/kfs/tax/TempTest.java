package edu.cornell.kfs.tax;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public class TempTest {

    private static final Pattern NAME_CHARS_LOWERCASE_FIRST_UPPERCASE_SECOND = Pattern.compile("^[a-z][A-Z].*$");

    public static final class MyObj {
        private String strValue;
        private boolean boolValue;
        
        public String getStrValue() {
            return strValue;
        }
        
        public void setStrValue(final String strValue) {
            this.strValue = strValue;
        }

        public boolean isBoolValue() {
            return boolValue;
        }

        public void setBoolValue(final boolean boolValue) {
            this.boolValue = boolValue;
        }
        
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDynamicLambdaForString() throws Exception {
        final Field dtoField = MyObj.class.getDeclaredField("strValue");
        final Class<String> fieldType = (Class<String>) dtoField.getType();
        final BiConsumer<MyObj, String> mySetter = generateLambdaForDtoPropertySetter(
                MyObj.class, fieldType, dtoField);

        final MyObj myObject = new MyObj();
        System.out.println("Old Value: " + myObject.getStrValue());
        mySetter.accept(myObject, "It Worked!");
        System.out.println("New Value: " + myObject.getStrValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDynamicLambdaForBoolean() throws Exception {
        final Field dtoField = MyObj.class.getDeclaredField("boolValue");
        final Class<?> fieldType = dtoField.getType();
        final BiConsumer<MyObj, Boolean> mySetter = (BiConsumer<MyObj, Boolean>) generateLambdaForDtoPropertySetter(
                MyObj.class, fieldType, dtoField);

        final MyObj myObject = new MyObj();
        System.out.println("Old Value: " + myObject.isBoolValue());
        mySetter.accept(myObject, Boolean.TRUE);
        System.out.println("New Value: " + myObject.isBoolValue());
    }

    private <T, R> BiConsumer<T, R> generateLambdaForDtoPropertySetter(final Class<T> dtoClass,
            final Class<R> fieldType, final Field dtoField) {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final String setterMethodName = getSetterMethodName(dtoField);
            final MethodHandle setterMethodHandle = lookup.findVirtual(dtoClass, setterMethodName,
                    MethodType.methodType(void.class, fieldType));
            final MethodType setterMethodType = setterMethodHandle.type().wrap().changeReturnType(void.class);

            final CallSite factoryCallSite = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    setterMethodType.erase(),
                    setterMethodHandle,
                    setterMethodType);

            final MethodHandle biConsumerFactory = factoryCallSite.getTarget();
            return (BiConsumer<T, R>) biConsumerFactory.invoke();

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    private String getSetterMethodName(final Field dtoField) {
        final String fieldName = dtoField.getName();
        if (NAME_CHARS_LOWERCASE_FIRST_UPPERCASE_SECOND.matcher(fieldName).matches()) {
            return "set" + fieldName;
        } else {
            return "set" + StringUtils.capitalize(fieldName);
        }
    }

}
