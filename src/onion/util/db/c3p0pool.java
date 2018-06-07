//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package onion.util.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;

public class c3p0pool {
    private static final c3p0pool instance = new c3p0pool();
    private static String DRIVER;
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static int type = 1;
    private static ComboPooledDataSource cpds = new ComboPooledDataSource(true);

    static {
        if(type == 1) {
        	if(PropertyReader.get("driver").toString().equals("true")){
        		DRIVER = PropertyReader.get("driver").toString();
                URL = PropertyReader.get("url").toString();
                USER = PropertyReader.get("user").toString();
                PASSWORD = PropertyReader.get("password").toString();
                Logger.log(1,"DEBUG");
        	}else{
        		DRIVER = PropertyReader.get("driver").toString();
                URL = DESUtils.getDecryptString(PropertyReader.get("url").toString());
                USER = DESUtils.getDecryptString(PropertyReader.get("user").toString());
                PASSWORD = DESUtils.getDecryptString(PropertyReader.get("password").toString());
        	}
        }

        Logger.log(0, "DRIVER:" + DRIVER);
        Logger.log(0, "URL:" + URL);
        Logger.log(0, "USER:" + USER);
        Logger.log(0, "PASSWORD:" + PASSWORD);
        Logger.log(0, "DBTYPE:" + PropertyReader.get("db"));
        Logger.log(0, "start c3po ...");
        cpds.setDataSourceName("c3p0data");
        cpds.setJdbcUrl(URL);

        try {
            cpds.setDriverClass(DRIVER);
        } catch (PropertyVetoException var1) {
            Logger.error("start error:" + var1.getMessage());
            var1.printStackTrace();
        }

        cpds.setUser(USER);
        cpds.setPassword(PASSWORD);
        cpds.setMinPoolSize(Integer.valueOf(PropertyReader.get("minPoolSize").toString()).intValue());
        cpds.setMaxPoolSize(Integer.valueOf(PropertyReader.get("maxPoolSize").toString()).intValue());
        cpds.setMaxIdleTime(Integer.valueOf(PropertyReader.get("maxIdleTime").toString()).intValue());
        cpds.setInitialPoolSize(Integer.valueOf(PropertyReader.get("initialPoolSize").toString()).intValue());
        cpds.setIdleConnectionTestPeriod(Integer.valueOf(PropertyReader.get("idleConnectionTestPeriod").toString()).intValue());
        cpds.setAcquireRetryAttempts(Integer.valueOf(PropertyReader.get("acquireRetryAttempts").toString()).intValue());
        cpds.setBreakAfterAcquireFailure(Boolean.valueOf(PropertyReader.get("breakAfterAcquireFailure").toString()).booleanValue());
    }

    public static c3p0pool getInstance() {
        return instance;
    }

    public static int getType() {
        return type;
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

    private c3p0pool() {
    }

    public static synchronized Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (Exception var1) {
            var1.printStackTrace();
            Logger.error("c3p0 getCon error:" + var1.getMessage());
            return null;
        }
    }

    public synchronized void stop() throws Exception {
        cpds.close();
        Logger.info("c3p0 stop ...");
    }
}
