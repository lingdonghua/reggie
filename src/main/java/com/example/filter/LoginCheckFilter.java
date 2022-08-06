package com.example.filter;

import com.alibaba.fastjson.JSON;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.domain.Employee;
import com.example.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
过滤器，阻挡未登录访问的页面
 */
@WebFilter(urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        //获取本次访问的url
        String requestURI = request.getRequestURI();
        //设置放行的url
        String[] urls =new String[]{"/employee/login", "/employee/logout", "/backend/**", "/front/**","/user/sendMsg","/user/login"};
        //定义不需要处理的路径
        //log.info("请求的url："+requestURI);
        boolean result =Check(urls,requestURI);
        //如果不需要处理即放行
        if(result){
            filterChain.doFilter(request,response);
            return;
        }
        //判断是否登录
        if(request.getSession().getAttribute("employee")!=null){
            //存入id到Thread缓冲区，方便公共字段自动填充时取
            Employee employee = (Employee) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(employee.getId());
            //已经登录，放行
            filterChain.doFilter(request,response);
            return;
        }
        //判断user是否登录
        if(request.getSession().getAttribute("user")!=null){
            //存入id到Thread缓冲区，方便公共字段自动填充时取
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            //已经登录，放行
            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登录");
//        filterChain.doFilter(request,response);
//        return;
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSON.toJSONString(R.error("未登录")));
    }
    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public static boolean Check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
//            log.info(match);
            if(match){
//                log.info("请求的路径:"+requestURI+"放行");
                return true;
            }
        }
        return false;
    }
}
