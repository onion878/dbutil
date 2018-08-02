package onion.org.annontation;

import java.lang.annotation.*;
/**
 * 请求路径
 * @author yc
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface ReqMap {
	String value();
}


