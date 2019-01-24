package onion.util.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import onion.util.db.annontations.Key;
import onion.util.db.annontations.Table;

public class DBUtil {
    private static char $db_divid_start = 96;
    private static char $db_divid_end = 96;
    private static String db;
    private static boolean autoCommit = true;

    static {
        try {
            db = PropertyReader.get("db");
            if (PropertyReader.get("autoCommit") == null) {
                autoCommit = true;
            } else {
                autoCommit = Boolean.valueOf(PropertyReader.get("autoCommit"));
            }
        } catch (Exception var1) {

        }

    }

    private DBUtil() {
    }

    private static final DBUtil instance = new DBUtil();

    public static DBUtil getInstance() {
        return instance;
    }

    public void setAutoCommit(boolean bool) throws Exception {
        getCon().setAutoCommit(bool);
    }

    private PreparedStatement getStatement(String sql, Connection con, Object... args) throws Exception {
        String asql = sql;
        PreparedStatement ps = con.prepareStatement(sql);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                ps.setObject(i + 1, args[i]);
                if (args[i] != null) {
                    if (args[i] instanceof String) {
                        asql = asql.replaceFirst("[?]", "'" + args[i].toString() + "'");
                    } else {
                        asql = asql.replaceFirst("[?]", args[i].toString());
                    }
                } else if (asql.contains("?")) {
                    asql = asql.replaceFirst("[?]", "null");
                }

                Logger.log(0, "DBUtil.getStatement:预编译参数:" + args[i]);
            }
        } else {
            Logger.log(0, "DBUtil.getStatement:没有预编译参数");
        }

        Logger.log(3, "DBUtil.getStatement:执行sql:" + SQLFormatter.format(asql));
        return ps;
    }

    private PreparedStatement getStatementList(String sql, Connection con, List<Object> args) throws Exception {
        String asql = sql;
        PreparedStatement ps = con.prepareStatement(sql);
        if (args != null && args.size() > 0) {
            for (int i = 0; i < args.size(); ++i) {
                ps.setObject(i + 1, args.get(i));
                if (args.get(i) != null) {
                    if (args.get(i) instanceof String) {
                        asql = asql.replaceFirst("[?]", "'" + args.get(i).toString() + "'");
                    } else {
                        asql = asql.replaceFirst("[?]", args.get(i).toString());
                    }
                } else if (asql.contains("?")) {
                    asql = asql.replaceFirst("[?]", "null");
                }

                Logger.log(0, "DBUtil.getStatement:预编译参数:" + args.get(i));
            }
        } else {
            Logger.log(0, "DBUtil.getStatement:没有预编译参数");
        }

        Logger.log(3, "DBUtil.getStatement:执行sql:" + SQLFormatter.format(asql));
        return ps;
    }

    public Connection getCon() throws SQLException {
        Connection conn = ThreadConnection.getThreadCon().get();
        if (conn == null) {
            conn = this.initCon();
        }
        if (conn.isClosed()) {
            ThreadConnection.getThreadCon().remove();
            conn = this.initCon();
        }
        return conn;
    }

    private Connection initCon() throws SQLException {
        Connection conn = DButilsDataSource.getDatasource().getConnection();
        try {
            conn.setAutoCommit(autoCommit);
            ThreadConnection.getThreadCon().set(conn);
            Logger.log(0, "获取连接DBUtil.getCon()=" + conn.getClass().getInterfaces()[0].getName());
        } catch (Exception var3) {
            var3.printStackTrace();
            Logger.error("获取连接数失败请检查数据库配置！");
        }
        return conn;
    }

    public static void Rollback(Exception e, String sql, Object... args) throws Exception {
        Connection conn = (Connection) ThreadConnection.getThreadCon().get();
        Logger.error("出现异常信息:");
        e.printStackTrace();
        Logger.error("异常sql:" + SQLFormatter.format(sql));
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                Logger.error("异常参数:" + args[i]);
            }
        } else {
            Logger.error("无异常参数请检查sql是否正确！");
        }
        if (conn != null && !conn.isClosed()) {
            conn.rollback();
            conn.close();
            Logger.error("执行数据回滚!");
        }
        ThreadConnection.getThreadCon().remove();
        throw e;
    }

    public void Rollback() throws SQLException {
        Connection conn = (Connection) ThreadConnection.getThreadCon().get();
        if (conn != null && !conn.isClosed()) {
            conn.rollback();
            conn.close();
            Logger.error("ִ数据回滚!");
        }

        ThreadConnection.getThreadCon().remove();
    }

    public void Commit() throws Exception {
        Connection conn = (Connection) ThreadConnection.getThreadCon().get();
        if (conn != null && !conn.isClosed()) {
            conn.commit();
            conn.close();
            Logger.log(0, "事务提交!");
        }
        ThreadConnection.getThreadCon().remove();
    }

    public void closeCon() throws Exception {
        Connection conn = (Connection) ThreadConnection.getThreadCon().get();
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        ThreadConnection.getThreadCon().remove();
    }

    public static void shutDown() {
        try {
            DButilsDataSource.getDatasource().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int update(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, sql);
        PreparedStatement ps = null;
        int r = 0;
        try {
            ps = this.getStatement(sql, con, args);
            r = ps.executeUpdate();
        } catch (Exception var10) {
            Rollback(var10, sql, args);
        } finally {
            ps.close();
        }
        return r;
    }

    public int update(Connection con, String sql, Object... args) throws Exception {
        Logger.log(3, sql);
        PreparedStatement ps = null;
        int r = 0;
        try {
            ps = this.getStatement(sql, con, args);
            r = ps.executeUpdate();
        } catch (Exception var10) {
            Rollback(var10, sql, args);
        } finally {
            ps.close();
        }

        return r;
    }

    public int delete(Class<? extends BaseEntity> clazz, String args) throws Exception {
        Table table = (Table) clazz.getAnnotation(Table.class);
        String tableName = table.value();
        Field[] flds = clazz.getDeclaredFields();
        String keyName = null;

        for (int i = 0; i < flds.length; ++i) {
            if (flds[i].isAnnotationPresent(Key.class)) {
                Key key = (Key) flds[i].getAnnotation(Key.class);
                keyName = key.value();
                break;
            }
        }

        if (args.contains("'")) {
            throw new Exception("出现非法字符串\"'\"");
        } else {
            String sql = "delete from " + tableName + " where " + keyName + " in(" + args + ") ";
            return this.delete(sql, new Object[0]);
        }
    }

    public int delete(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        PreparedStatement ps = null;
        Logger.log(3, sql);
        int r = 0;

        try {
            ps = this.getStatement(sql, con, args);
            r = ps.executeUpdate();
        } catch (Exception var10) {
            Rollback(var10, sql, args);
        } finally {
            ps.close();
        }

        return r;
    }

    public int insert(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, sql);
        PreparedStatement ps = null;
        int r = 0;

        try {
            ps = this.getStatement(sql, con, args);
            r = ps.executeUpdate();
        } catch (Exception var10) {
            Rollback(var10, sql, args);
        } finally {
            ps.close();
        }

        return r;
    }

    public Object uniqueBean(Class<?> beanClz, String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.uniqueBean.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var8) {
            Rollback(var8, sql, args);
        }

        Object bean = null;
        if (rs.next()) {
            bean = BeanUtil.getBeanFromResultSet(beanClz, rs);
        } else {
            Logger.log(0, "DBUtil.uniqueBean:结果集为空");
        }

        rs.close();
        ps.close();
        return bean;
    }

    public List query(Class<?> beanClz, String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.query.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var9) {
            Rollback(var9, sql, args);
        }

        ArrayList list = new ArrayList();

        while (rs.next()) {
            Object bean = BeanUtil.getBeanFromResultSet(beanClz, rs);
            list.add(bean);
        }

        Logger.log(0, "DBUtil.query结果集长度:" + list.size());
        rs.close();
        ps.close();
        return list;
    }

    public List query(Class<?> beanClz, String sql, List<Object> args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.query.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatementList(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var9) {
            Rollback(var9, sql, new Object[]{args});
        }

        ArrayList list = new ArrayList();

        while (rs.next()) {
            Object bean = BeanUtil.getBeanFromResultSet(beanClz, rs);
            list.add(bean);
        }

        Logger.log(0, "DBUtil.query结果集长度" + list.size());
        rs.close();
        ps.close();
        return list;
    }

    public Object getById(Class<?> beanClz, Object key) throws Exception {
        String tableName = "";
        if (beanClz.isAnnotationPresent(Table.class)) {
            Table keyName = (Table) beanClz.getAnnotation(Table.class);
            tableName = keyName.value();
        } else {
            tableName = beanClz.getSimpleName();
        }

        Logger.log(0, "DBUtil.getById.tableName:" + tableName);
        String var7 = "";
        Field[] fld = beanClz.getDeclaredFields();

        for (int sql = 0; sql < fld.length; ++sql) {
            if (fld[sql].isAnnotationPresent(Key.class)) {
                Key r = (Key) fld[sql].getAnnotation(Key.class);
                var7 = r.value();
            }
        }

        Logger.log(0, "DBUtil.getById.keyName:" + var7);
        String var8 = "select * from " + $db_divid_start + tableName + $db_divid_end + " where " + $db_divid_start + var7 + $db_divid_end + "=?";
        Logger.log(3, "DBUtil.getById.sql:" + var8);
        Object var9 = this.uniqueBean(beanClz, var8, new Object[]{key});
        return var9;
    }

    public Object uniqueValue(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.uniqueValue.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var7) {
            Rollback(var7, sql, args);
        }

        Object r = null;
        if (rs.next()) {
            r = rs.getObject(1);
            Logger.log(0, "DBUtil.uniqueValue.rs:" + r);
        } else {
            Logger.log(0, "DBUtil.uniqueValue.rs为空");
        }

        rs.close();
        ps.close();
        return r;
    }

    public Object uniqueValue(String sql, List<Object> args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.uniqueValue.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatementList(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var7) {
            Rollback(var7, sql, new Object[]{args});
        }

        Object r = null;
        if (rs.next()) {
            r = rs.getObject(1);
            Logger.log(0, "DBUtil.uniqueValue:rs:" + r);
        } else {
            Logger.log(0, "DBUtil.uniqueValue==null");
        }

        rs.close();
        ps.close();
        return r;
    }

    public DataTable getTable(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.getTable.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var7) {
            Rollback(var7, sql, args);
        }

        DataTable table = new DataTable(rs);
        rs.close();
        ps.close();
        return table;
    }

    public List<Map<String, Object>> queryMap(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.queryMap.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var7) {
            Rollback(var7, sql, args);
        }

        List<Map<String, Object>> list = BeanUtil.convertList(rs);
        Logger.log(0, "DBUtil.query.size:" + list.size());
        rs.close();
        ps.close();
        return list;
    }

    public Map<String, Object> queryoneMap(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.queryoneMap.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var8) {
            Rollback(var8, sql, args);
        }

        List<Map<String, Object>> list = BeanUtil.convertList(rs);
        Map<String, Object> map = new HashMap();
        if (list.size() > 1) {
            Logger.log(3, "DBUtil.query.size" + list.size());
        }

        map.putAll((Map) list.get(0));
        rs.close();
        ps.close();
        return map;
    }

    public String queryJSON(String sql, Object... args) throws Exception {
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.queryJSON.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatement(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var7) {
            Rollback(var7, sql, args);
        }

        String list = BeanUtil.convertJSON(rs);
        rs.close();
        ps.close();
        return list;
    }


    public String queryJSON(Class<?> beanClz, String sql, List<Object> args) throws Exception {
        StringBuffer sBuffer = new StringBuffer();
        Connection con = this.getCon();
        Logger.log(3, "DBUtil.query.sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getStatementList(sql, con, args);
            rs = ps.executeQuery();
        } catch (Exception var9) {
            Rollback(var9, sql, new Object[]{args});
        }

        sBuffer.append("[");

        while (rs.next()) {
            String bean = BeanUtil.getBeanFromResultSettoString(beanClz, rs);
            sBuffer.append(bean + ",");
        }

        sBuffer.deleteCharAt(sBuffer.length() - 1);
        sBuffer.append("]");
        rs.close();
        ps.close();
        return sBuffer.toString();
    }

    public static String success(String data) {
        return "{\"success\":true,\"msg\":\"" + data.replaceAll("\"", "\\\\\"") + "\"}";
    }

    public static String datagrid(Map<String, Object> lists) {
        String data = lists.get("rows").toString();
        if (lists.get("total").toString().equals("0")) {
            data = "[]";
        }

        return "{\"total\":" + lists.get("total") + ",\"rows\":" + data + "}";
    }
}
