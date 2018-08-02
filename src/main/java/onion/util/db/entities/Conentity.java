package onion.util.db.entities;

/**
 * 数据库驱动 DRIVER
 * 数据库地址URL
 * 用户名 USER
 * 密码 PASSWORD
 * 数据库类型 dbname
 */
public class Conentity {
	private String DRIVER;
	private String URL;
	private String USER;
	private String PASSWORD;
	private String dbname;

	@Override
	public String toString() {
		return "Conentity [DRIVER=" + DRIVER + ", PASSWORD=" + PASSWORD
				+ ", URL=" + URL + ", USER=" + USER + ", dbname=" + dbname
				+ "]";
	}

	public Conentity(String dRIVER, String uRL, String uSER, String pASSWORD,
			String dbname) {
		super();
		DRIVER = dRIVER;
		URL = uRL;
		USER = uSER;
		PASSWORD = pASSWORD;
		this.dbname = dbname;
	}

	public Conentity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getDRIVER() {
		return DRIVER;
	}

	public void setDRIVER(String dRIVER) {
		DRIVER = dRIVER;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getUSER() {
		return USER;
	}

	public void setUSER(String uSER) {
		USER = uSER;
	}

	public String getPASSWORD() {
		return PASSWORD;
	}

	public void setPASSWORD(String pASSWORD) {
		PASSWORD = pASSWORD;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
}


