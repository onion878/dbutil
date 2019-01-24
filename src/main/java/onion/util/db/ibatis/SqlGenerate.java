package onion.util.db.ibatis;

import onion.util.db.DBUtil;
import onion.util.db.DateUtil;
import onion.util.db.Logger;
import onion.util.db.annontations.Column;
import onion.util.db.annontations.Key;
import onion.util.db.annontations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class SqlGenerate {

    private static final Map<String, String> map = new HashMap<>();

    private static final DBUtil dbutil = DBUtil.getInstance();

    static {
        map.put("Object[]", "ARRAY");
        map.put("Long", "BIGINT");
        map.put("Boolean", "BIT");
        map.put("byte[]", "BLOB");
        map.put("BigDecimal", "DECIMAL");
        map.put("Double", "DOUBLE");
        map.put("Float", "FLOAT");
        map.put("Integer", "INT");
        map.put("Date", "TIMESTAMP");
        map.put("Short", "TINYINT");
        map.put("String", "VARCHAR");
    }

    public static String[] toStringArray(List<String> arr) {
        String[] strings = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            strings[i] = arr.get(i);
        }
        return strings;
    }

    public static Map<String, Object> getEntityInfo(Object obj) throws IllegalAccessException {
        int identity = 0;
        Object primaryValue = null;
        String primaryKey = null, tableName = null;
        Class clz = obj.getClass();
        if (!clz.isAnnotationPresent(Table.class)) {
            Logger.error("类需要Table注解!");
            return null;
        }
        Table table = (Table) clz.getAnnotation(Table.class);
        tableName = table.value();
        List<String> fields = new ArrayList<>();
        List<String> cha = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            int mod = field.getModifiers();
            //过滤 static 和 final 类型
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(obj);
            if (value == null) {
                continue;
            }
            if (field.isAnnotationPresent(Key.class)) {
                Key key = field.getAnnotation(Key.class);
                identity = key.identity();
                primaryKey = key.value();
                primaryValue = value;
                if (identity == 0) {
                    fields.add(key.value());
                    cha.add("?");
                }
            } else if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                fields.add(col.value());
                cha.add("?");
            } else {
                continue;
            }
            if (value != null && !"".equals(value)) {
                if (field.getType().getName().equals("java.util.Date")) {
                    Date d = (Date) value;
                    value = DateUtil.toStamp(d);
                }
            }
            if (identity == 0) {
                values.add(value);
            } else {
                if (field.isAnnotationPresent(Column.class)) {
                    values.add(value);
                }
            }
        }
        Map<String, Object> m = new HashMap<>();
        m.put("primaryKey", primaryKey);
        m.put("primaryValue", primaryValue);
        m.put("tableName", tableName);
        m.put("fields", fields);
        m.put("values", values);
        m.put("valueHelp", cha);
        return m;
    }

    public static int createEntity(Object obj) throws Exception {
        Map<String, Object> map = getEntityInfo(obj);
        String tableName = (String) map.get("tableName");
        List<String> fields = (List<String>) map.get("fields");
        List<String> helps = (List<String>) map.get("valueHelp");
        List<Object> values = (List<Object>) map.get("values");
        SQL sql = new SQL() {{
            INSERT_INTO(tableName);
            INTO_COLUMNS(toStringArray(fields));
            INTO_VALUES(toStringArray(helps));
        }};
        return dbutil.insert(sql.toString(), values.toArray());
    }

    public static int updateEntity(Object obj) throws Exception {
        Map<String, Object> map = getEntityInfo(obj);
        String tableName = (String) map.get("tableName");
        List<String> fields = (List<String>) map.get("fields");
        String primaryKey = (String) map.get("primaryKey");
        String primaryValue = (String) map.get("primaryValue");
        List<Object> values = (List<Object>) map.get("values");
        SQL sql = new SQL() {{
            UPDATE(tableName);
            fields.forEach(f -> {
                SET(f + "=?");
            });
            WHERE(primaryKey + "=?");
        }};
        values.add(primaryValue);
        return dbutil.insert(sql.toString(), values.toArray());
    }

    public static List queryByEntity(Object obj) throws Exception {
        Map<String, Object> map = getEntityInfo(obj);
        String tableName = (String) map.get("tableName");
        List<String> fields = (List<String>) map.get("fields");
        List<Object> values = (List<Object>) map.get("values");
        SQL sql = new SQL() {{
            SELECT("*");
            FROM(tableName);
            fields.forEach(v -> {
                WHERE(v + "=?");
            });
        }};
        return dbutil.query(obj.getClass(), sql.toString(), values);
    }

    public static int deleteEntity(Object obj) throws Exception {
        Map<String, Object> map = getEntityInfo(obj);
        String primaryKey = (String) map.get("primaryKey");
        String tableName = (String) map.get("tableName");
        String primaryValue = (String) map.get("primaryValue");
        SQL sql = new SQL() {{
            DELETE_FROM(tableName);
            WHERE(primaryKey + "=?");
        }};
        return dbutil.update(sql.toString(), primaryValue);
    }

    public static Object getEntityById(Object obj) throws Exception {
        Map<String, Object> map = getEntityInfo(obj);
        String primaryValue = (String) map.get("primaryValue");
        String primaryKey = (String) map.get("primaryKey");
        String tableName = (String) map.get("tableName");
        SQL sql = new SQL() {{
            SELECT("*");
            FROM(tableName);
            WHERE(primaryKey + "=?");
        }};
        return dbutil.uniqueBean(obj.getClass(), sql.toString(), primaryValue);
    }

    public static void exitTable(Class clz) throws Exception {
        String primaryKey = null;
        List<String> fields = new ArrayList<>();
        if (!clz.isAnnotationPresent(Table.class)) {
            Logger.error("类需要Table注解!");
            return;
        }
        Table table = (Table) clz.getAnnotation(Table.class);
        String tableName = table.value();
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            int identity = 0;
            field.setAccessible(true);
            String type = map.get(field.getType().getSimpleName());
            if (field.isAnnotationPresent(Key.class)) {
                Key key = field.getAnnotation(Key.class);
                identity = key.identity();
                primaryKey = key.value();
                if (type == null || "VARCHAR".equals(type)) {
                    fields.add(key.value() + " varchar(" + key.length() + ")");
                } else {
                    if (identity == 0) {
                        fields.add(key.value() + " " + type);
                    } else {
                        fields.add(key.value() + " " + type + " AUTO_INCREMENT");
                    }
                }
            } else if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                identity = col.identity();
                if (type == null || "VARCHAR".equals(type)) {
                    fields.add(col.value() + " varchar(" + col.length() + ")");
                } else {
                    if (identity == 0) {
                        fields.add(col.value() + " " + type);
                    } else {
                        fields.add(col.value() + " " + type + " AUTO_INCREMENT");
                    }
                }
            } else {
                continue;
            }
        }
        String finalPrimaryKey = primaryKey;
        SQL sql = new SQL() {{
            CREATE(tableName);
            COLUMNS(toStringArray(fields));
            PRIMARY_KEY(finalPrimaryKey);
        }};
        dbutil.update(sql.toString());
    }
}
