package onion.util.db.annontations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Key {
	
	/**
	 *  数据库字段名称
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * 是否是标识字段
	 * 
	 * @return
	 */
	int identity() default 0;

	/**
	 * 数据库字段长度
	 * @return
	 */
	int length() default 50;
}


