package edu.cornell.kfs.sys.util.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CuSqlQueryFixture {

    String sql();

    SqlParameterFixture[] parameters();

}
