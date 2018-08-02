package onion.util.db;


public class Logger {
	public static final int DEBUG = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int CHECK = 3;
	private static int logTo = 1;
	private static int level = 0;

	static {
		String v = PropertyReader.get("logTo");
		try {
			logTo = Integer.parseInt(v);
		} catch (Exception e) {
			logTo = 1;
		}
		v = PropertyReader.get("logLevel");
		try {
			level = Integer.parseInt(v);
		} catch (Exception e) {
			level = 0;
		}
	}

	public Logger() {
	}

	public static void lognoln(int lvl, Object msg) {
		switch (logTo) {
		case 1:
			if (lvl >= level) {

				System.out.print(msg);
			}
		case 0:
		case 2:
		default:
		}
	}

	public static void log(int lvl, Object msg) {
		switch (logTo) {
		case 1:
			if (lvl >= level) {
				System.out.print("###LOGTIME " + DateUtil.getnow() + " = ");
				System.out.println(msg);
			}
		case 0:
		case 2:
		default:
		}
	}

	public static void logln(int lvl, Object msg) {
		switch (logTo) {
		case 1:
			if (lvl >= level) {
				System.out.println(msg);
			}
		case 0:
		case 2:
		default:
		}
	}

	public static void info(Object msg) {
		System.out.print("###LOGTIME [INFO] " + DateUtil.getnow() + " = ");
		System.out.println(msg);
	}

	public static void error(Object msg) {
		System.err.print("###LOGTIME [ERROR] " + DateUtil.getnow() + " = ");
		System.err.println(msg);
	}
}


