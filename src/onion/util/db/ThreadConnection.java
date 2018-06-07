package onion.util.db;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadConnection{
	private static  ThreadLocal<Connection> threadCon = new ThreadLocal<Connection>();
	
	public static ThreadLocal<Connection> getThreadCon() {
		return threadCon;
	}
	
	/**
	 * 执行sql后必须调用改方法去提交事务以及关闭连接
	 * 
	 * @throws Exception
	 */
	public static void Commit() throws Exception {
		Connection conn =   ThreadConnection.getThreadCon().get();
		if (conn != null && !conn.isClosed()) {
			conn.commit();
			conn.close();
			Logger.log(0, "关闭连接并执行事务提交");
		}
		ThreadConnection.getThreadCon().remove();
	}
	
	public static void Rollback() throws SQLException   {
		Connection conn =   ThreadConnection.getThreadCon().get();
		if (conn != null && !conn.isClosed()) {
			conn.rollback();
			conn.close();
			Logger.error("执行事务回滚并关闭连接");
		}
		ThreadConnection.getThreadCon().remove();
	}
}


