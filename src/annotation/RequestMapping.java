package annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author JDUSER
 */
@Documented
@Target({ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value() default "";

    RequestMethod[] method() default {};
}
