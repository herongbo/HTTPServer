package annotation;

import java.lang.annotation.*;

/**
 * @author JDUSER
 */
@Documented
@Target({ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    // 作用在字段上
}