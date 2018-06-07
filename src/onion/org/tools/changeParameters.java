package onion.org.tools;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class changeParameters {
	public static boolean ifTypes(String typeName) {
		String[] types={"String","Integer","int","Float","float","Double","double","Boolean","boolean","","","","",""};
		for (String string : types) {
			if(typeName.equals(string))
				return true;
		}
		return false;
	}
	
	
	public static Map<String,String> getStringParams(HttpServletRequest request){
		Map<String,String[]> rawParam=request.getParameterMap();
		Map<String,String> keyParam=new HashMap<String, String>();
		for (String key : rawParam.keySet()) {
			if(rawParam.get(key)!=null){
				String[] value=rawParam.get(key);
				if(value!=null&&value.length==1){
					keyParam.put(key, value[0]);
				}
			}
		}
		
		return keyParam;
	}
}


