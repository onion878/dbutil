package onion.util.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

public class DButilsDataSource {

    private static String DRIVER;
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static int type = 1;

    private static HikariDataSource datasource;

    public synchronized static HikariDataSource getDatasource() {
        if (datasource == null) {
            initHikariCP();
        }
        return datasource;
    }

    public void setDatasource(HikariDataSource datasource) {
        DButilsDataSource.datasource = datasource;
    }

    private synchronized static void initHikariCP() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceProperties( new Properties() );
        hikariConfig.setHealthCheckProperties( new Properties() );
        hikariConfig.setMinimumIdle( 1 );
        hikariConfig.setMaximumPoolSize( 5 );
        hikariConfig.setConnectionTimeout( 300L );
        hikariConfig.setValidationTimeout( 300L );
        hikariConfig.setInitializationFailTimeout( 1L );
        hikariConfig.setAutoCommit( true );
        if (type == 1 ) {
            if (PropertyReader.get( "key" ) == null) {
                DRIVER = PropertyReader.get( "driver" ).toString();
                URL = PropertyReader.get( "url" ).toString();
                USER = PropertyReader.get( "user" ).toString();
                PASSWORD = PropertyReader.get( "password" ).toString();
            } else {
                DRIVER = PropertyReader.get( "driver" ).toString();
                URL = DESUtils.getDecryptString( PropertyReader.get( "url" ).toString() );
                USER = DESUtils.getDecryptString( PropertyReader.get( "user" ).toString() );
                PASSWORD = DESUtils.getDecryptString( PropertyReader.get( "password" ).toString() );
            }
        }

        Logger.log( 0, "DRIVER:" + DRIVER );
        Logger.log( 0, "URL:" + URL );
        Logger.log( 0, "USER:" + USER );
        Logger.log( 0, "PASSWORD:" + PASSWORD );
        Logger.log( 0, "DBTYPE:" + PropertyReader.get( "db" ) );
        Logger.log( 0, "start hikaricp ..." );
        hikariConfig.setJdbcUrl( URL );
        hikariConfig.setDriverClassName( DRIVER );
        hikariConfig.setUsername( USER );
        hikariConfig.setPassword( PASSWORD );
        hikariConfig.setMinimumIdle( Integer.valueOf( PropertyReader.get( "minPoolSize" ) ).intValue() );
        hikariConfig.setMaximumPoolSize( Integer.valueOf( PropertyReader.get( "maxPoolSize" ) ).intValue() );
        datasource = new HikariDataSource( hikariConfig );
    }
}


