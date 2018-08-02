package onion.org.servlet;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import onion.org.annontation.ReqMap;
import onion.org.tools.changeParameters;
import onion.util.db.BeanUtil;
/**
 * 请求转发以及参数封装
 * 方法可以获取到request，response，Map<String,String>,实体类和气list的封装
 * 不会处理流数据.
 * @author yc
 *
 */
public class doRequest {

	public static Object doMethod(HttpServletRequest request,HttpServletResponse response,File file,String serpath)
			throws Exception {
		String filename = file.getPath();
		String className = filename.substring(filename.indexOf("WEB-INF\\classes\\") + 16, filename.indexOf(".class"))
				.replaceAll("\\\\", ".");// 在运行期获取的类名字符串
		Class<?> class1 = Class.forName(className);
		Method[] method = class1.getMethods();
		Object object = null;
		String rMapping="",name="",simplename="";
		if (class1.isAnnotationPresent(ReqMap.class)) {
			ReqMap reqMap=(ReqMap)class1.getAnnotation(ReqMap.class);
			rMapping=reqMap.value();
		}
		for (Method method2 : method) {
			if (method2.isAnnotationPresent(ReqMap.class)) {
				Object sObj=class1.newInstance();
				ReqMap reqMap=method2.getAnnotation(ReqMap.class);
				if (request.getServletPath().equals(rMapping+reqMap.value())) {
					Class<?>[] ccs=method2.getParameterTypes();
					Parameter[] param=method2.getParameters();
					Object data[]=new Object[ccs.length];
					for (int i = 0; i < ccs.length; i++) {
						name=ccs[i].getName();
						simplename=ccs[i].getSimpleName();
						if (name.equals("java.util.Map")) {
							data[i]=changeParameters.getStringParams(request);
						}else if(name.equals("javax.servlet.http.HttpServletRequest")){
							data[i]=request;
						}else if(name.equals("javax.servlet.http.HttpServletResponse")){
							data[i]=response;
						}else{
							Class<?> ex=param[i].getType();
							data[i]=BeanUtil.BeanFromMap(ex, changeParameters.getStringParams(request));
						}
					}
					object=method2.invoke(sObj,data);
				}
			}
		}
		return object;
	}
	
	
}


