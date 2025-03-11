package edu.cornell.kfs.sys.util.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.sql.Types;

import edu.cornell.kfs.sys.util.TestDateUtils;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface SqlParameterFixture {

    int type() default Types.VARCHAR;

    String value();



    public static final class Utils {

        public static Object getParsedValue(final SqlParameterFixture fixture) {
            final String stringValue = fixture.value();
            switch (fixture.type()) {
                case Types.VARCHAR:
                    return stringValue;

                case Types.INTEGER:
                    return Integer.valueOf(stringValue);

                case Types.BIGINT:
                    return Long.valueOf(stringValue);

                case Types.DECIMAL:
                    return new BigDecimal(stringValue);

                case Types.DATE:
                    return TestDateUtils.toSqlDate(stringValue);

                default:
                    throw new IllegalArgumentException("Unknown or unsupported SQL/JDBC type: " + fixture.type());
            }
        }

    }

}
