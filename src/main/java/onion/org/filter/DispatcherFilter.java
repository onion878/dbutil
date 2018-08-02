package onion.org.filter;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import onion.org.scan.InitScan;
import onion.org.servlet.doRequest;
import onion.util.db.ThreadConnection;

/**
 * 统一跳转过滤 进入该类如果成功执行到请求则不会继续走过滤器 配置最好放在web.xml最后一个过滤器后面
 * 
 * @author yc
 *
 */
public class DispatcherFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		Map<String, File> serpath = InitScan.getPaths();
		String con = request.getContextPath();
		String conkey = request.getRequestURI().replaceFirst(con, "");
		File file = serpath.get(conkey);
		if (file == null)
			arg2.doFilter(request, response);
		try {
			Object object = doRequest.doMethod(request, response, file, request.getRequestURI());
			ThreadConnection.Commit();
			response.getWriter().append(object.toString());
		} catch (Exception e) {
			try {
				ThreadConnection.Rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			response.getWriter().append("{\"success\":false,\"msg\":\"" + e.getMessage() + "\"}");
			e.printStackTrace();
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}

