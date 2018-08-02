package onion.util.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import onion.util.db.annontations.Column;
import onion.util.db.annontations.Key;

public class BeanUtil {
	public BeanUtil() {
	}

	public static String getBeanFromResultSettoString(Class<?> beanClz, ResultSet rs) throws Exception {
		StringBuffer sBuffer = new StringBuffer();
		if (rs == null) {
			return null;
		} else {
			Field[] flds = beanClz.getDeclaredFields();
			sBuffer.append("{");
			Object scs = null;
			String sname = "";
			for (int i = 0; i < flds.length; ++i) {
				Annotation[] as = flds[i].getAnnotations();
				String colName = null;
				if (as.length > 0) {
					if (flds[i].isAnnotationPresent(Key.class)) {
						Key value = (Key) flds[i].getAnnotation(Key.class);
						colName = value.value();
					} else if (flds[i].isAnnotationPresent(Column.class)) {
						Column var9 = (Column) flds[i].getAnnotation(Column.class);
						colName = var9.value();
					}
				}
				if (colName == null || colName.trim().equals("")) {
					continue;
				}
				Object var10 = rs.getObject(colName);
				Object v = cast(flds[i].getType(), var10);
				scs=v;
				sname = flds[i].getName().replaceAll("\"", "\\\\\"");
				if (scs instanceof String) {
					scs = ((String) scs).replaceAll("\"", "\\\\\"");
					sBuffer.append("\"" + sname + "\":\"" + scs + "\",");
				} else {
					if (scs==null) 
						scs="\"\"";
					sBuffer.append( "\"" + sname + "\":" + scs + ",");
				}
				
			}
			sBuffer.deleteCharAt(sBuffer.length() - 1);
			sBuffer.append("}");
			Logger.log(0, "BeanUtil.getBeanFromResultSet:" + sBuffer.toString());
			return sBuffer.toString();
		}
	}
	
	public static Object getBeanFromResultSet(Class<?> beanClz, ResultSet rs) throws Exception {
		if (rs == null) {
			return null;
		} else {
			Object bean = beanClz.newInstance();
			Field[] flds = beanClz.getDeclaredFields();
			for (int i = 0; i < flds.length; ++i) {
				Annotation[] as = flds[i].getAnnotations();
				String colName = null;
				if (as.length > 0) {
					if (flds[i].isAnnotationPresent(Key.class)) {
						Key value = (Key) flds[i].getAnnotation(Key.class);
						colName = value.value();
					} else if (flds[i].isAnnotationPresent(Column.class)) {
						Column var9 = (Column) flds[i].getAnnotation(Column.class);
						colName = var9.value();
					}
				}
				if (colName == null || colName.trim().equals("")) {
					continue;
				}
				Object var10 = rs.getObject(colName);
				if (var10 instanceof String) {
					var10 = ((String) var10).trim();
				}

				Object v = cast(flds[i].getType(), var10);
				flds[i].setAccessible(true);
				try {
					flds[i].set(bean, v);
				} catch (Exception e) {

				}
			}
			Logger.log(0, "BeanUtil.getBeanFromResultSet:" + bean.toString());
			return bean;
		}
	}

	public static Object getBeanFromMap(Class<?> beanClz, Map<String, Object> map) throws Exception {
		if (map == null) {
			return null;
		} else {
			Object bean = beanClz.newInstance();
			Field[] flds = beanClz.getDeclaredFields();
			for (int i = 0; i < flds.length; ++i) {
				Annotation[] as = flds[i].getAnnotations();
				String colName = null;
				if (as.length > 0) {
					if (flds[i].isAnnotationPresent(Key.class)) {
						Key value = (Key) flds[i].getAnnotation(Key.class);
						colName = value.value();
					} else if (flds[i].isAnnotationPresent(Column.class)) {
						Column var9 = (Column) flds[i].getAnnotation(Column.class);
						colName = var9.value();
					}
				}

				Object var10 = map.get(colName);
				if (var10 instanceof String) {
					var10 = ((String) var10).trim();
				}

				Object v = cast(flds[i].getType(), var10);
				flds[i].setAccessible(true);
				try {
					flds[i].set(bean, v);
				} catch (Exception e) {

				}
			}
			Logger.log(0, "BeanUtil.getBeanFromMap:" + bean.toString());
			return bean;
		}
	}

	public static Object BeanFromMap(Class<?> beanClz, Map<String, String> map) throws Exception {
		if (map == null) {
			return null;
		} else {
			Object bean = beanClz.newInstance();
			Field[] flds = beanClz.getDeclaredFields();
			for (int i = 0; i < flds.length; ++i) {
				String colName =flds[i].getName();
				Object var10 = map.get(colName);
				if (var10 instanceof String) {
					var10 = ((String) var10).trim();
				}
				Object v = cast(flds[i].getType(), var10);
				flds[i].setAccessible(true);
				try {
					if (v!=null) {
						flds[i].set(bean, v);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Logger.log(0, "BeanUtil.BeanFromMap:" + bean.toString());
			return bean;
		}
	}


	private static Object cast(Class<?> type, Object value) {
		Object v = null;
		if (value == null || "".equals(value.toString()) || "null".equals(value.toString())) {
			return null;
		} else {
			String typea = type.getName().toLowerCase();
			if (typea.endsWith("string")) {
				v = value.toString();
			} else if (typea.endsWith("integer")) {
				v = value.toString().trim().isEmpty() ? Integer.valueOf("0").intValue()
						: Integer.valueOf(value.toString()).intValue();
			} else if (typea.endsWith("int")) {
				v = value.toString().trim().isEmpty() ? Integer.parseInt("0") : Integer.parseInt(value.toString());
			} else if (typea.endsWith("float")) {
				v = value.toString().trim().isEmpty() ? Float.valueOf("0").floatValue()
						: Float.valueOf(value.toString()).floatValue();
			} else if (typea.endsWith("date")) {
				v = castDate(value.toString());
			} else if (typea.endsWith("double")) {
				v = value.toString().trim().isEmpty() ? Double.valueOf("0").doubleValue()
						: Double.valueOf(value.toString()).doubleValue();
			} else if (typea.endsWith("long")) {
				v = value.toString().trim().isEmpty() ? Long.valueOf("0").longValue()
						: Long.valueOf(value.toString()).longValue();
			} else if (typea.endsWith("short")) {
				v = value.toString().trim().isEmpty() ? Short.valueOf("0").shortValue()
						: Short.valueOf(value.toString()).shortValue();
			} else if (typea.endsWith("boolean")) {
				v = value.toString().trim().isEmpty() ? false : Boolean.valueOf(value.toString()).booleanValue();
			} else {
				v = value;
			}
		}
		return v;
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	public static Object castDate(String str) {
		Object v = null;
		if (isNumeric(str)) {
			v = new Date(Long.valueOf(str).longValue());
		} else {
			if (str.length() > 10) {
				v = Help.tostandarDatehms(str);
			} else {
				v = Help.tostandarDate(str);
			}
		}
		return v;
	}

	public static Object parse(String value, String typeName) {
		return typeName
				.equals("String")
						? value
						: (!typeName.equals("Integer") && !typeName.equals("int")
								? (!typeName.equals("Float") && !typeName.equals("float")
										? (!typeName.equals("Double") && !typeName.equals("double")
												? (!typeName.equals("Boolean") && !typeName.equals("boolean") ? null
														: Boolean.valueOf(Boolean.parseBoolean(value.toString())))
												: Double.valueOf(Double.parseDouble(value.toString())))
										: Float.valueOf(Float.parseFloat(value.toString())))
								: Integer.valueOf(Integer.parseInt(value.toString())));
	}

	@SuppressWarnings("unchecked")
	public static List convertList(ResultSet rs) throws SQLException {
		List list = new ArrayList();

		ResultSetMetaData md = rs.getMetaData();

		int columnCount = md.getColumnCount(); // Map rowData;
		for (int i = 1; i <= columnCount; i++) {
			Logger.log(0, "BeanUtil.cast:" + md.getColumnName(i));
		}
		while (rs.next()) { // rowData = new HashMap(columnCount);

			Map rowData = new HashMap();

			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i), rs.getObject(i));
			}

			list.add(rowData);

		}
		return list;

	}

	public static String convertJSON(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		StringBuffer sBuffer=new StringBuffer();
		sBuffer.append("[");
		int columnCount = md.getColumnCount(); // Map rowData;
		for (int i = 1; i <= columnCount; i++) {
			Logger.log(0, "BeanUtil.cast:" + md.getColumnName(i));
		}
		while (rs.next()) {
			sBuffer.append("{");
			for (int i = 1; i <= columnCount; i++) {
				String sname = md.getColumnName(i);
				Object scs = rs.getObject(i);
				sname = sname.replaceAll("\"", "\\\\\"");
				if (scs instanceof String) {
					scs = ((String) scs).replaceAll("\"", "\\\\\"");
					sBuffer.append("\"" + sname + "\":\"" + scs + "\",");
				} else {
					if (scs==null) 
						scs="\"\"";
					sBuffer.append( "\"" + sname + "\":" + scs + ",");
				}

			}
			sBuffer.deleteCharAt(sBuffer.length() - 1);
			sBuffer.append("},");
		}
		sBuffer.deleteCharAt(sBuffer.length() - 1);
		sBuffer.append("]");
		Logger.log(0, "BeanUtil.convertJSON:" + sBuffer.toString());
		return sBuffer.toString();
	}
	
	public static String convertListToJSON(List list) throws Exception {
		StringBuffer sBuffer = new StringBuffer();
		Object scs = null;
		String sname = "";
		sBuffer.append("[");
		for (Object object : list) {
			sBuffer.append("{");
			Class cla = Class.forName(object.getClass().getTypeName());
			Field[] fields = cla.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				scs = field.get(object);
				sname = field.getName();
				if (scs instanceof String) {
					scs = ((String) scs).replaceAll("\"", "\\\\\"");
					sBuffer.append("\"" + sname + "\":\"" + scs + "\",");
				} else {
					if (scs == null)
						scs = "\"\"";
					sBuffer.append("\"" + sname + "\":" + scs + ",");
				}
			}
			sBuffer.deleteCharAt(sBuffer.length() - 1);
			sBuffer.append("},");
		}
		sBuffer.deleteCharAt(sBuffer.length() - 1);
		sBuffer.append("]");
		Logger.log(0, "BeanUtil.convertListToJSON:" + sBuffer.toString());
		return sBuffer.toString();
	}
	
	public static String convertListMapToJSON(List<Map<String,Object>> list) throws Exception {
		StringBuffer sBuffer = new StringBuffer();
		Object scs = null;
		String sname = "";
		sBuffer.append("[");
		for (Map<String,Object> object : list) {
			sBuffer.append("{");
			for (Map.Entry<String, Object> m :object.entrySet())  {  
					sname=m.getKey();
					scs=m.getValue();
					if (scs instanceof String) {
						scs = ((String) scs).replaceAll("\"", "\\\\\"");
						sBuffer.append("\"" + sname + "\":\"" + scs + "\",");
					} else {
						if (scs == null)
							scs = "\"\"";
						sBuffer.append("\"" + sname + "\":" + scs + ",");
					}
		    }  
			sBuffer.deleteCharAt(sBuffer.length() - 1);
			sBuffer.append("},");
		}
		sBuffer.deleteCharAt(sBuffer.length() - 1);
		sBuffer.append("]");
		Logger.log(0, "BeanUtil.convertListMapToJSON:" + sBuffer.toString());
		return sBuffer.toString();
	}
	
	public static String convertMapToJSON(Map<String,Object> map) throws Exception {
		StringBuffer sBuffer = new StringBuffer();
		Object scs = null;
		String sname = "";
		sBuffer.append("{");
		for (Map.Entry<String, Object> m :map.entrySet())  {  
				sname=m.getKey();
				scs=m.getValue();
				if (scs instanceof String) {
					scs = ((String) scs).replaceAll("\"", "\\\\\"");
					sBuffer.append("\"" + sname + "\":\"" + scs + "\",");
				} else {
					if (scs == null)
						scs = "\"\"";
					sBuffer.append("\"" + sname + "\":" + scs + ",");
				}
	    }  
		sBuffer.deleteCharAt(sBuffer.length() - 1);
		sBuffer.append("}");
		Logger.log(0, "BeanUtil.convertMapToJSON:" + sBuffer.toString());
		return sBuffer.toString();
	}
}


