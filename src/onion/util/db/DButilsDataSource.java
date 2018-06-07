package onion.util.db;

import javax.sql.DataSource;

public class DButilsDataSource{
	
	private static DataSource datasource;

	public synchronized static DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSource datasource) {
		DButilsDataSource.datasource = datasource;
	}
	
	
}


