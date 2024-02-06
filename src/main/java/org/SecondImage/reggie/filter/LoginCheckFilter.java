package org.SecondImage.reggie.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.SecondImage.reggie.common.BaseContext;
import org.SecondImage.reggie.common.R;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")  //urlPatterns = /* 拦截所有请求
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求URI
        String requestURI = request.getRequestURI();

        log.info("拦截请求: {}",request.getRequestURI());
        //不需要请求的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/takeout/**",
                "/manForTakeaway/login",
                "/manForTakeaway/add"
        };
        //2.判断该次请求是否处理
        boolean check = check(urls,requestURI);
        //不处理
        if (check){
            log.info("本次请求 {} 不需要处理",request.getRequestURI());
            filterChain.doFilter(request,response);
            return;
        }
        //3.判断是否已经登录
        if (request.getSession().getAttribute("employee") != null){
            log.info("已登录，ID: {}",request.getSession().getAttribute("employee"));
            //保存当前登录用户ID到该线程中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }
        // 判断移动端用户是否登录   需要完善：当移动端用户登录后也能访问管理页面
        if (request.getSession().getAttribute("user") != null){
            log.info("已登录，ID: {}",request.getSession().getAttribute("user"));
            //保存当前登录用户ID到该线程中
            Long empId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }
        // 判断外卖员是否登录
        if (request.getSession().getAttribute("manForTakeaway") != null){
            log.info("已登录，ID: {}",request.getSession().getAttribute("manForTakeaway"));
            //保存当前登录用户ID到该线程中
            Long empId = (Long) request.getSession().getAttribute("manForTakeaway");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4.如果未登陆，返回未登录结果，通过输出流向前端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
//
//        log.info("拦截请求: {}",request.getRequestURI()); // {}是一个占位符
//        filterChain.doFilter(request,response);
    }

    /**
     * 路径匹配，检测请求是否放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for(String url: urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
