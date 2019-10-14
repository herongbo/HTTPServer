package annotation;

import java.lang.annotation.*;
/**
 * <p>
 *
 * @author Brian
 * @since 2019-07-19
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Service {

    String value() default "";
}