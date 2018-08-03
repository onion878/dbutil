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
import onion.util.db.entities.Conentity;

/**
 * 数据库简易工具
 */
public class CBUtil {
    private static ConnectionPool pool = ConnectionPool.getInstanse();
    private static char $db_divid_start = '\000';
    private static char $db_divid_end = '\000';
    private static String db;

    @SuppressWarnings("static-access")
    public void setConPro(String DRIVER, String URL, String USER,
                          String PASSWORD, String dbname) {
        pool.setDRIVER( DRIVER );
        pool.setURL( URL );
        pool.setUSER( USER );
        pool.setPASSWORD( PASSWORD );
        db = dbname;
    }

    @SuppressWarnings("static-access")
    public void setConProentity(Conentity conentity) {
        pool.setDRIVER( conentity.getDRIVER() );
        pool.setURL( conentity.getURL() );
        pool.setUSER( conentity.getUSER() );
        pool.setPASSWORD( conentity.getPASSWORD() );
        db = conentity.getDbname();
    }

    static {
        try {
            db = PropertyReader.get( "db" );
            if (db.equalsIgnoreCase( "oracle" )) {
                $db_divid_end = 34;
                $db_divid_start = 34;
            } else if (db.equalsIgnoreCase( "mysql" )) {
                $db_divid_end = 96;
                $db_divid_start = 96;
            } else if (db.equalsIgnoreCase( "sqlserver" )) {
                $db_divid_start = 91;
                $db_divid_end = 93;
            } else {
                $db_divid_start = 91;
                $db_divid_end = 93;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private CBUtil() {
    }

    private static final CBUtil instance = new CBUtil();
    public static CBUtil getInstance() {
        return instance;
    }

    private PreparedStatement getStatement(String sql, Connection con,
                                           Object... args) throws Exception {
        String asql = sql;
        PreparedStatement ps = con.prepareStatement( sql );
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                ps.setObject( i + 1, args[i] );
                if (args[i] != null) {
                    if (args[i] instanceof String) {
                        asql = asql.replaceFirst( "[?]", "'" + args[i].toString() + "'" );
                    } else {
                        asql = asql.replaceFirst( "[?]", args[i].toString() );
                    }
                } else {
                    if (asql.contains( "?" ))
                        asql = asql.replaceFirst( "[?]", "null" );
                }
                Logger.log( 0, "CBUtil.getStatement:预编译命令参数值:" + args[i] );
            }
        } else {
            Logger.log( 0, "CBUtil.getStatement:预编译命令参数个数为空或为0" );
        }
        Logger.log( 3, "CBUtil.getStatement:替换后的sql:" + asql );
        return ps;
    }

    private PreparedStatement getStatementList(String sql, Connection con,
                                               List<Object> args) throws Exception {
        String asql = sql;
        PreparedStatement ps = con.prepareStatement( sql );
        if (args != null && args.size() > 0) {
            for (int i = 0; i < args.size(); ++i) {
                ps.setObject( i + 1, args.get( i ) );
                if (args.get( i ) != null) {
                    if (args.get( i ) instanceof String) {
                        asql = asql.replaceFirst( "[?]", "'" + args.get( i ).toString() + "'" );
                    } else {
                        asql = asql.replaceFirst( "[?]", args.get( i ).toString() );
                    }
                } else {
                    if (asql.contains( "?" ))
                        asql = asql.replaceFirst( "[?]", "null" );
                }
                Logger.log( 0, "CBUtil.getStatement:预编译命令参数值:" + args.get( i ) );
            }
        } else {
            Logger.log( 0, "CBUtil.getStatement:预编译命令参数个数为空或为0" );
        }
        Logger.log( 3, "CBUtil.getStatement:替换后的sql:" + asql );
        return ps;
    }

    public Connection getCon() throws Exception {
        Connection conn = ThreadConnection.getThreadCon().get();
        if (conn == null) {
            try {
                conn = pool.getConnection();
                conn.setAutoCommit( false );
                ThreadConnection.getThreadCon().set( conn );
                Logger.log( 0, "从连接池获取连接CBUtil.getCon()="
                        + conn.getClass().getInterfaces()[0].getName() );
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error( "未配置或传入连接" );
            }
        }

        return conn;
    }


    public static void Rollback(Exception e, String sql, Object... args) throws Exception {
        Connection conn = ThreadConnection.getThreadCon().get();
        Logger.error( "发生异常,异常信息:" );
        e.printStackTrace();
        Logger.error( "异常sql:" + sql );
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Logger.error( "异常参数:" + args[i] );
            }
        } else {
            Logger.error( "异常参数:无预编译参数" );
        }
        if (conn != null && !conn.isClosed()) {
            conn.rollback();
            conn.close();
            Logger.error( "执行事务回滚并关闭连接" );
        }
        ThreadConnection.getThreadCon().remove();
        throw e;
    }

    public void Rollback() throws SQLException {
        Connection conn = ThreadConnection.getThreadCon().get();
        if (conn != null && !conn.isClosed()) {
            conn.rollback();
            conn.close();
            Logger.error( "执行事务回滚并关闭连接" );
        }
        ThreadConnection.getThreadCon().remove();
    }

    /**
     * 执行sql后必须调用改方法去提交事务以及关闭连接
     *
     * @throws Exception
     */
    public void Commit() throws Exception {
        Connection conn = ThreadConnection.getThreadCon().get();
        if (conn != null && !conn.isClosed()) {
            conn.commit();
            conn.close();
            Logger.log( 0, "关闭连接并执行事务提交" );
        }
        ThreadConnection.getThreadCon().remove();
    }

    public void closeCon() throws Exception {
        Connection conn = ThreadConnection.getThreadCon().get();
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        ThreadConnection.getThreadCon().remove();
    }

    /**
     * 关闭连接池，并初始化连接参数
     *
     * @throws Exception
     */
    public static void shutDown() throws Exception {
        pool.stop();
    }

    public int update(String sql, Object... args) throws Exception {
        Connection con = getCon();
        Logger.log( 3, sql );
        PreparedStatement ps = null;
        int r = 0;
        try {
            ps = getStatement( sql, con, args );
            r = ps.executeUpdate();
        } catch (Exception e) {
            Rollback( e, sql, args );
        } finally {
            ps.close();
        }
        return r;
    }

    public int update(Connection con, String sql, Object... args)
            throws Exception {
        Logger.log( 3, sql );
        PreparedStatement ps = null;
        int r = 0;
        try {
            ps = getStatement( sql, con, args );
            r = ps.executeUpdate();
        } catch (Exception e) {
            Rollback( e, sql, args );
        } finally {
            ps.close();
        }
        return r;
    }

    public int delete(String sql, Object... args) throws Exception {
        Connection con = getCon();
        PreparedStatement ps = null;
        int r = 0;
        try {
            ps = getStatement( sql, con, args );
            r = ps.executeUpdate();
        } catch (Exception e) {
            Rollback( e, sql, args );
        } finally {
            ps.close();
        }
        return r;
    }

    public int insert(String sql, Object... args) throws Exception {
        Connection con = getCon();
        Logger.log( 3, sql );
        PreparedStatement ps = null;
        int r = 0;
        try {
            ps = getStatement( sql, con, args );
            r = ps.executeUpdate();
        } catch (Exception e) {
            Rollback( e, sql, args );
        } finally {
            ps.close();
        }
        return r;
    }

    public Object uniqueBean(Class<?> beanClz, String sql,
                             Object... args) throws Exception {
        Connection con = getCon();

        Logger.log( 3, "CBUtil.uniqueBean查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        Object bean = null;
        if (rs.next()) {
            bean = BeanUtil.getBeanFromResultSet( beanClz, rs );
        } else {
            Logger.log( 0, "CBUtil.uniqueBean:rs结果集记录为0" );
        }
        rs.close();
        ps.close();
        return bean;
    }

    public List query(Class<?> beanClz, String sql, Object... args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.query查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        ArrayList list = new ArrayList();
        while (rs.next()) {
            Object bean = BeanUtil.getBeanFromResultSet( beanClz, rs );
            list.add( bean );
        }
        Logger.log( 0, "CBUtil.query查询集合list长度" + list.size() );
        rs.close();
        ps.close();
        return list;
    }

    public List query(Class<?> beanClz, String sql, List<Object> args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.query查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatementList( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        ArrayList list = new ArrayList();
        while (rs.next()) {
            Object bean = BeanUtil.getBeanFromResultSet( beanClz, rs );
            list.add( bean );
        }
        Logger.log( 0, "CBUtil.query查询集合list长度" + list.size() );
        rs.close();
        ps.close();
        return list;
    }

    public Object getById(Class<?> beanClz, Object key) throws Exception {
        String tableName = "";
        if (beanClz.isAnnotationPresent( Table.class )) {
            Table keyName = (Table) beanClz.getAnnotation( Table.class );
            tableName = keyName.value();
        } else {
            tableName = beanClz.getSimpleName();
        }

        Logger.log( 0, "CBUtil.getById:tableName表名:" + tableName );
        String var7 = "";
        Field[] fld = beanClz.getDeclaredFields();

        for (int sql = 0; sql < fld.length; ++sql) {
            if (fld[sql].isAnnotationPresent( Key.class )) {
                Key r = (Key) fld[sql].getAnnotation( Key.class );
                var7 = r.value();
            }
        }

        Logger.log( 0, "CBUtil.getById:keyName主键名:" + var7 );
        String var8 = "select * from " + $db_divid_start
                + tableName.toUpperCase() + $db_divid_end + " where "
                + $db_divid_start + var7.toUpperCase() + $db_divid_end + "=?";
        Logger.log( 3, "CBUtil.getById查询sql:" + var8 );
        Object var9 = uniqueBean( beanClz, var8, new Object[]{key} );
        return var9;
    }

    public Object uniqueValue(String sql, Object... args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.uniqueValue查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        Object r = null;
        if (rs.next()) {
            r = rs.getObject( 1 );
            Logger.log( 0, "CBUtil.uniqueValue:查询结果rs:" + r );
        } else {
            Logger.log( 0, "CBUtil.uniqueValue:结果集rs行数为0" );
        }
        rs.close();
        ps.close();
        return r;
    }

    public Object uniqueValue(String sql, List<Object> args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.uniqueValue查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatementList( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        Object r = null;
        if (rs.next()) {
            r = rs.getObject( 1 );
            Logger.log( 0, "CBUtil.uniqueValue:查询结果rs:" + r );
        } else {
            Logger.log( 0, "CBUtil.uniqueValue:结果集rs行数为0" );
        }
        rs.close();
        ps.close();
        return r;
    }


    public DataTable getTable(String sql, Object... args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.getTable查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        DataTable table = new DataTable( rs );
        rs.close();
        ps.close();
        return table;
    }

    public List<Map<String, Object>> queryMap(String sql, Object... args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.queryMap查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        List<Map<String, Object>> list = BeanUtil.convertList( rs );
        Logger.log( 0, "CBUtil.query查询集合list长度" + list.size() );
        rs.close();
        ps.close();
        return list;
    }

    public Map<String, Object> queryoneMap(String sql, Object... args)
            throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.queryoneMap查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        List<Map<String, Object>> list = BeanUtil.convertList( rs );
        Map<String, Object> map = new HashMap<String, Object>();
        if (list.size() > 1) {
            Logger.log( 3, "CBUtil.query查询集合list长度" + list.size() );
        }
        map.putAll( list.get( 0 ) );
        rs.close();
        ps.close();
        return map;
    }

    public String queryJSON(String sql, Object... args) throws Exception {
        Connection con = getCon();
        Logger.log( 3, "CBUtil.queryJSON查询sql:" + sql );
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getStatement( sql, con, args );
            rs = ps.executeQuery();
        } catch (Exception e) {
            Rollback( e, sql, args );
        }
        String list = BeanUtil.convertJSON( rs );
        rs.close();
        ps.close();
        return list;
    }

    /**
     * @param key       数据库字段
     * @param value     对应的值
     * @param tableName 表名
     * @param order     排序（sqlserver必须）
     * @param page      第几页
     * @param size      显示多少条
     * @return sql分页的sql语句，size当前条件下的总记录数
     * @throws Exception
     */
    public String queryAll(String[] key, String[] value,
                           String tableName, String order, int page, int size) throws Exception {
        Map<String, Object> sql = Help.SqlSting( key, value, tableName, order, page, size );
        Logger.log( 3, "CBUtil.queryAll查询数据sql:" + sql.get( "sql" ) );
        Logger.log( 3, "CBUtil.queryAll查询总数sql:" + sql.get( "size" ) );
        String jsons = queryJSON( sql.get( "sql" ).toString() );
        Object onj = uniqueValue( sql.get( "size" ).toString() );
        String list = "{\"total\":" + jsons + ",\"rows\":" + onj + "}";
        Logger.log( 3, "CBUtil.queryAll查询结果集:" + list );
        return list;
    }

    /**
     * @param key       数据库字段
     * @param value     对应的值
     * @param tableName 表名
     * @param order     排序（sqlserver必须）
     * @param page      第几页
     * @param size      显示多少条
     * @return sql分页的sql语句，size当前条件下的总记录数
     * @throws Exception
     */
    public Map<String, Object> queryAllMap(String[] key, String[] value,
                                           String tableName, String order, int page, int size) throws Exception {
        Map<String, Object> sql = Help.SqlSting( key, value, tableName, order, page, size );
        Logger.log( 3, "CBUtil.queryAllMap查询数据sql:" + sql.get( "sql" ) );
        Logger.log( 3, "CBUtil.queryAllMap查询总数sql:" + sql.get( "size" ) );
        String jsons = queryJSON( sql.get( "sql" ).toString() );
        Object onj = uniqueValue( sql.get( "size" ).toString() );
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( "rows", jsons );
        map.put( "total", onj );
        Logger.log( 3, "CBUtil.queryAll查询结果集:" + map.toString() );
        return map;
    }

    /**
     * @param beanClz   实体类
     * @param key       数据库字段
     * @param value     对应的值
     * @param tableName 表名
     * @param order     排序（sqlserver必须）
     * @param page      第几页
     * @param size      显示多少条
     * @return sql分页的sql语句，size当前条件下的总记录数
     * @throws Exception
     */
    public Map<String, Object> queryListMap(Class<?> beanClz, String[] key, String[] value,
                                            String tableName, String order, int page, int size) throws Exception {
        Map<String, Object> sql = Help.SqlSting( key, value, tableName, order, page, size );
        Logger.log( 3, "CBUtil.queryListMap查询数据sql:" + sql.get( "sql" ) );
        Logger.log( 3, "CBUtil.queryListMap查询总数sql:" + sql.get( "size" ) );
        List<Object> obj = (List) sql.get( "value" );
        Map<String, Object> map = new HashMap<String, Object>();
        Object onj = uniqueValue( sql.get( "size" ).toString(), obj );
        map.put( "rows", query( beanClz, sql.get( "sql" ).toString(), obj ) );
        map.put( "total", onj );
        Logger.log( 3, "CBUtil.queryListMap查询结果集:" + map.toString() );
        return map;
    }

    public static String success(String data) {
        return "{\"success\":true,\"msg\":\"" + data.replaceAll( "\"", "\\\\\"" ) + "\"}";
    }
}


