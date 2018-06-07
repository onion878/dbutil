package onion.org.scan;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import onion.org.annontation.ReqMap;
import onion.util.db.Logger;
import onion.util.db.PropertyReader;
public class InitScan implements ServletContextListener{
	public static Map<String,File> paths=new HashMap<>();
	private String path="";
	private String allpath="";
	public String getPath() {
		return path;
	}
	
	public static Map<String, File> getPaths() {
		return paths;
	}

	public static void setPaths(Map<String, File> paths) {
		InitScan.paths = paths;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public void getFile() throws ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, SQLException {
		String path1 = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() ;
		String pathall =path1.substring(0,path1.indexOf("WEB-INF/")+8)+"classes/";
        pathall=pathall.substring(1,pathall.indexOf("classes/")+8);
        allpath=pathall=pathall+path+"/";
        ScanFile();
	}
	
	private void ScanFile() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, SQLException {
		File file=new File(allpath);
		File[] array=file.listFiles();
		for (int i = 0; i < array.length; i++) {
			if (array[i].isFile()) {
				doMethod(array[i]);
			}else{
				allpath=array[i].getPath();
				getFile();
			}
		}
	}
	
	public static void doMethod(File file)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		String filename = file.getPath();
		String className = filename.substring(filename.indexOf("WEB-INF\\classes\\") + 16, filename.indexOf(".class"))
				.replaceAll("\\\\", ".");// 在运行期获取的类名字符串
		Class class1 = Class.forName(className);
		Method[] method = class1.getMethods();
		String rMapping = "";
		Object object = null;
		if (class1.isAnnotationPresent(ReqMap.class)) {
			ReqMap ReqMap = (ReqMap) class1.getAnnotation(ReqMap.class);
			rMapping = ReqMap.value();
		}
		for (Method method2 : method) {
			if (method2.isAnnotationPresent(ReqMap.class)) {
				Object sObj = class1.newInstance();
				ReqMap ReqMap = method2.getAnnotation(ReqMap.class);
				String reqpath=rMapping + ReqMap.value();
				paths.put(reqpath,file);
				Logger.log(3,"扫描到的请求路径:"+reqpath);
			}
		}
	}


	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		if (getPath().equals("")) {
			setPath(PropertyReader.get("scanpackge"));
		}
		try {
			getFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}


