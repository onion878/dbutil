package onion.util.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionPool {
    private static String DRIVER;
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private static ConnectionPool connectionPool = new ConnectionPool();

    public static ConnectionPool getInstanse() {
        return connectionPool;
    }

    public static String getDRIVER() {
        return DRIVER;
    }

    public static void setDRIVER(String dRIVER) {
        DRIVER = dRIVER;
    }

    public static String getURL() {
        return URL;
    }

    public static void setURL(String uRL) {
        URL = uRL;
    }

    public static String getUSER() {
        return USER;
    }

    public static void setUSER(String uSER) {
        USER = uSER;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

    public static void setPASSWORD(String pASSWORD) {
        PASSWORD = pASSWORD;
    }

    private ArrayList<Connection> cons = new ArrayList();
    private int conNums = 4;

    static {
        try {
            DRIVER = PropertyReader.get( "driver" );
            if (PropertyReader.get( "key" ) == null) {
                URL = PropertyReader.get( "url" ).toString();
                USER = PropertyReader.get( "user" ).toString();
                PASSWORD = PropertyReader.get( "password" ).toString();
                Logger.log( 1, "DEBUG" );
            } else {
                URL = DESUtils.getDecryptString( PropertyReader.get( "url" ).toString() );
                USER = DESUtils.getDecryptString( PropertyReader.get( "user" ).toString() );
                PASSWORD = DESUtils.getDecryptString( PropertyReader.get( "password" ).toString() );
            }
            if (DRIVER != null) {
                Class.forName( DRIVER );
            }
        } catch (ClassNotFoundException var1) {
            var1.printStackTrace();
        }

    }

    private ConnectionPool() {
    }

    public ConnectionPool(int num) {
        this.conNums = num;
    }

    public synchronized Connection getConnection() throws SQLException {
        Connection con = null;
        if (this.cons.size() > 0) {
            con = (Connection) this.cons.remove( 0 );
            Logger.log( 0, "ConnectionPool.getCon" );
        } else {
            Connection realCon = DriverManager.getConnection( URL, USER,
                    PASSWORD );
            Logger.log( 0, "ConnectionPool.getCon.URL:" + URL );
            Logger.log( 0, "ConnectionPool.USER:" + USER );
            Logger.log( 0, "ConnectionPool.PWD:" + PASSWORD );
            con = (Connection) Proxy.newProxyInstance(
                    PoolConnection.class.getClassLoader(),
                    new Class[]{PoolConnection.class},
                    new PoolConHandler( realCon ) );
        }

        return con;
    }

    public synchronized void stop() throws Exception {
        Iterator var2 = this.cons.iterator();
        while (var2.hasNext()) {
            Connection con = (Connection) var2.next();
            PoolConnection pc = (PoolConnection) con;
            if (!pc.isClosed()) {
                pc.closeConnection();
            }
        }
        this.cons.clear();
    }

    private class PoolConHandler implements InvocationHandler {
        private Connection realCon = null;

        public PoolConHandler(Connection con) {
            this.realCon = con;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            Object result = null;
            if (method.getName().equals( "close" )) {
                synchronized (ConnectionPool.this.cons) {
                    if (ConnectionPool.this.cons.size() < ConnectionPool.this.conNums) {
                        ConnectionPool.this.cons.add( (Connection) proxy );
                    } else {
                        this.realCon.close();
                    }
                }
            } else if (method.getName().equals( "closeConnection" )) {
                this.realCon.close();
            } else {
                result = method.invoke( this.realCon, args );
            }
            return result;
        }
    }

    private interface PoolConnection extends Connection {
        void closeConnection();
    }
}


