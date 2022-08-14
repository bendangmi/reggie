package com.bdm.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.bdm.reggie.common.BaseContext;
import com.bdm.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @code Description 检查用户是否已经完成登录
 * @code author 本当迷
 * @code date 2022/8/4-6:45
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 专门进行路径比较，路径匹配器，支持通配符
    public static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1.获取本次请求的URL
        final String requestURI = request.getRequestURI();

        String[] urls = {
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg", // 移动端发送短信
                "/user/login", // 移动端登录
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        // 2.判断本次请求是否需要处理
        final boolean check = check(urls, requestURI);

        // 3.如果不需要处理，则直接放行
        if(check){
            log.info("静态请求不需要处理：{}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-1.判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录放行：{}，用户id为{}", requestURI, request.getSession().getAttribute("employee") );

            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);


            filterChain.doFilter(request, response);

            return;
        }

        // 4-2.判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录放行：{}，用户id为{}", requestURI, request.getSession().getAttribute("user") );

            Long userId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);

            return;
        }



        // 5.如果未登录则返回未登录结果, 通过输出流方式向客户端响应数据
        log.info("用户未登录：{}", requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURL
     * @return
     */
    public boolean check(String[]urls, String requestURL){
        for (String url :
                urls) {
            final boolean match = ANT_PATH_MATCHER.match(url, requestURL);
            if(match) return true;
        }
        return  false;
    }
}
