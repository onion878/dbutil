package onion.util.db;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
	private static Properties ps = new Properties();

	static {
		try {
			InputStream e = PropertyReader.class
					.getResourceAsStream("/const.properties");
			ps.load(e);
			e.close();
		} catch (Exception var1) {
			Logger.log(2, "未找到src下的const.properties");
		}

	}

	public PropertyReader() {
	}

	public static String get(String key) {
		String r = (String) ps.get(key);
		if (r == null) {
			Logger.log(2, "未找到属性值:" + key);
		}
		return r;
	}
}


