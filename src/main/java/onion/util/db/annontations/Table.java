package onion.util.db.annontations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface Table {
	/**
	 * 数据库表名称
	 * 
	 * @return
	 */
	String value() default "";
}


