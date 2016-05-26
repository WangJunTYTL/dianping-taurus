package com.cip.crane.web.servlet.filter;

import com.cip.crane.springmvc.controller.InitController;
import com.dianping.lion.client.Lion;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class NonAuthFilter implements Filter {
	
	private static String[] excludes;
	
	static final String ALREADY_FILTERED_ATTRIBUTE = NonAuthFilter.class.getName() + ".FILTERED";

	private static final String CAN_SKIP_SSO = Lion.get("taurus.canskip.sso", "false");

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (filterConfig != null) {
			String ex = filterConfig.getInitParameter("excludes");
			excludes = ex.split(",");
		}
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (request.getAttribute(ALREADY_FILTERED_ATTRIBUTE) != null) {
            chain.doFilter(request, response);
            
        } else {
            request.setAttribute(ALREADY_FILTERED_ATTRIBUTE, Boolean.TRUE);
            
    		String requestURI = ((HttpServletRequest) request).getRequestURI();

			if("true".equalsIgnoreCase(CAN_SKIP_SSO)) {
				HttpServletRequest req = (HttpServletRequest) request;
				HttpSession session = req.getSession(true);
				Boolean isNonSso = (Boolean) session.getAttribute(InitController.NON_SSO_FLAG);
				String sessionAccount = (String) session.getAttribute(InitController.USER_NAME);

				if( Boolean.TRUE.equals(isNonSso) && StringUtils.isNotBlank(sessionAccount)) {
					request.getRequestDispatcher(requestURI).forward(request, response);
					return;
				}
			}
    		
    		for (String uri : excludes) {
    			
    			if (requestURI.toLowerCase().startsWith(uri)){
    				
    				request.getRequestDispatcher(requestURI).forward(request, response);
    				
    				return;
    			}
    			
    		}
    		
    		chain.doFilter(request, response);
        }
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
