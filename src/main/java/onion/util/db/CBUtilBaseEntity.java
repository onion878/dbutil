package onion.util.db;

import onion.util.db.annontations.Column;
import onion.util.db.annontations.Key;
import onion.util.db.annontations.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class CBUtilBaseEntity {
	public static final int PERSIST_NEW = 0;
	public static final int PERSIST_SAVED = 1;
	public static final int PERSIST_INVALID = 2;
	private char $sql_divid_start = '\000';
	private char $sql_divid_end = '\000';
	private CBUtil cbUtil = CBUtil.getInstance();

	@SuppressWarnings("unchecked")
	private String getTableName() {
		Class clz = getClass();
		Table table = (Table) clz.getAnnotation(Table.class);
		Logger.log(0, "BaseEntity.getTableName:" + table.value());
		return table.value();
	}

	public CBUtilBaseEntity() {
		String db = PropertyReader.get("db");
		if (db == null) {
			db = "object";
		}
		if (db.equalsIgnoreCase("oracle")) {
			this.$sql_divid_start = this.$sql_divid_end = 34;
		} else if (db.equalsIgnoreCase("mysql")) {
			this.$sql_divid_start = this.$sql_divid_end = 96;
		} else if (db.equalsIgnoreCase("sqlserver")) {
			this.$sql_divid_start = 91;
			this.$sql_divid_end = 93;
		}
	}

	@SuppressWarnings("unchecked")
	public int save() throws Exception {
		String tableName = getTableName();
		Field[] flds = getClass().getDeclaredFields();
		String sql1 = "insert into " + this.$sql_divid_start + tableName + this.$sql_divid_end + "(";
		String sql2 = " values(";
		List params = new ArrayList();
		for (int i = 0; i < flds.length; i++) {
			String fldName = null;
			int idenTity = 0;
			if (flds[i].isAnnotationPresent(Key.class)) {
				Key key = (Key) flds[i].getAnnotation(Key.class);
				fldName = key.value();
				idenTity = key.identity();
			} else if (flds[i].isAnnotationPresent(Column.class)) {
				Column col = (Column) flds[i].getAnnotation(Column.class);
				fldName = col.value();
				idenTity = col.identity();
			}
			if (fldName != null && idenTity == 0) {
				flds[i].setAccessible(true);
				Object value = flds[i].get(this);
				if (value != null && !"".equals(value)) {
					if (flds[i].getType().getName().equals("java.util.Date")) {
						Date d = (Date) value;
						value = DateUtil.toStamp(d);
					}
					sql1 = sql1 + this.$sql_divid_start + fldName + this.$sql_divid_end + ",";
					sql2 = sql2 + "?,";
					params.add(value);
					Logger.log(0, "BaseEntity.save:(key)" + fldName + "(value):" + value);
				}
			}
		}
		sql1 = sql1.substring(0, sql1.length() - 1) + ")";
		sql2 = sql2.substring(0, sql2.length() - 1) + ")";
		String sql = sql1 + sql2;
		int r = 0;
		if (params.size() == 0) {
			Logger.error("参数为空保存失败!");
		} else {
			r = cbUtil.insert(sql, params.toArray());
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	public int update() throws Exception {
		String tableName = getTableName();
		Field[] flds = getClass().getDeclaredFields();
		String keyName = null;
		Field keyField = null;
		String sql = " set ";
		List params = new ArrayList();
		for (int i = 0; i < flds.length; i++) {
			if (flds[i].isAnnotationPresent(Key.class)) {
				Key key = (Key) flds[i].getAnnotation(Key.class);
				keyName = key.value();
				keyField = flds[i];
			} else if (flds[i].isAnnotationPresent(Column.class)) {
				Column col = (Column) flds[i].getAnnotation(Column.class);
				String colName = col.value();
				flds[i].setAccessible(true);
				Object value = flds[i].get(this);
				if (value != null && !"".equals(value)) {
					if (flds[i].getType().getName().equals("java.util.Date")) {
						Date d = (Date) value;
						value = DateUtil.toStamp(d);
					}
					sql = sql + this.$sql_divid_start + colName + this.$sql_divid_end + "=?,";
					params.add(value);
					Logger.log(0, "BaseEntity.update:(key)" + colName + "(value):" + value);
				} else {
					sql = sql + this.$sql_divid_start + colName + this.$sql_divid_end + "=null,";
				}
			}
		}
		keyField.setAccessible(true);
		Object keyValue = keyField.get(this);
		params.add(keyValue);
		Logger.log(0, "BaseEntity.update:(key):" + keyName + "(value):" + keyValue);
		sql = "update " + this.$sql_divid_start + tableName + this.$sql_divid_end
				+ sql.substring(0, sql.length() - 1);
		sql = sql + " where " + this.$sql_divid_start + keyName + this.$sql_divid_end + "=?";
		int r = 0;
		if (params.size() == 0) {
			Logger.error("参数为空保存失败!");
		} else {
			r = cbUtil.update(sql, params.toArray());
		}
		return r;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List query() throws Exception {
		String tableName = getTableName();
		Field[] flds = getClass().getDeclaredFields();
		String sql1 = "select * from " + this.$sql_divid_start + tableName + this.$sql_divid_end
				+ " where ";
		List<Object> params = new ArrayList();
		for (int i = 0; i < flds.length; i++) {
			String fldName = null;
			if (flds[i].isAnnotationPresent(Key.class)) {
				Key key = (Key) flds[i].getAnnotation(Key.class);
				fldName = key.value();
				flds[i].setAccessible(true);
				Object value = flds[i].get(this);
				sql1 = sql1 + " " + this.$sql_divid_start + fldName + this.$sql_divid_end
						+ " like '%'+?+'%' and";
				params.add(value);
				Logger.log(0, "BaseEntity.query:(key):" + fldName + "(value):" + value);
			}
		}
		sql1 = sql1.substring(0, sql1.length() - 3);
		String sql = sql1;
		List r = cbUtil.query(getClass(), sql, params.toArray());
		return r;
	}

	public int delete() throws Exception {
		String tableName = getTableName();
		Field[] flds = getClass().getDeclaredFields();
		String keyName = null;
		Field keyField = null;
		for (int i = 0; i < flds.length; i++) {
			if (flds[i].isAnnotationPresent(Key.class)) {
				Key key = (Key) flds[i].getAnnotation(Key.class);
				keyName = key.value();
				keyField = flds[i];
				break;
			}
		}
		String sql = "delete from " + this.$sql_divid_start + tableName + this.$sql_divid_end + " where "
				+ this.$sql_divid_start + keyName + this.$sql_divid_end + "=?";
		keyField.setAccessible(true);
		Object param = keyField.get(this);
		Logger.log(0, "BaseEntity.delete:(key):" + keyName + "(value):" + param);
		int r = cbUtil.update(sql, new Object[] { param });
		return r;
	}

	public int saveOrupdate() throws Exception {
		String tableName = getTableName();
		Field[] flds = getClass().getDeclaredFields();
		String keyName = null;
		Field keyField = null;
		for (int i = 0; i < flds.length; i++) {
			if (flds[i].isAnnotationPresent(Key.class)) {
				Key key = (Key) flds[i].getAnnotation(Key.class);
				keyName = key.value();
				keyField = flds[i];
				break;
			}
		}

		String sql = "select count(" + this.$sql_divid_start + keyName + this.$sql_divid_end
				+ ") as size from " + this.$sql_divid_start + tableName + this.$sql_divid_end + " where "
				+ this.$sql_divid_start + keyName + this.$sql_divid_end + "=?";
		keyField.setAccessible(true);
		Object param = keyField.get(this);
		Object size = cbUtil.uniqueValue(sql, new Object[] { param });
		if (size != null && Integer.valueOf(size + "") == 1){
			Logger.log(2, "BaseEntity.saveOrupdate:更新数据");
			return this.update();
		}else{
			Logger.log(2, "BaseEntity.saveOrupdate:添加数据");
			return this.save();
		}
	}

}


