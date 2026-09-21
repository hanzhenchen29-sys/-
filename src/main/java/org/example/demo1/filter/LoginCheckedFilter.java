package org.example.demo1.filter;


import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.demo1.pojo.Result;
import org.example.demo1.util.JWTUtil;
import org.springframework.util.StringUtils;


import java.io.IOException;


//只要请求就拦截
@WebFilter(urlPatterns = "/*")
public class LoginCheckedFilter implements Filter {


   /* @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }*/
    //拦截方法，只要资源链接被拦截到，就会触发此方法
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse)servletResponse;

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,token");
        response.setHeader("Access-Control-Expose-Headers", "token"); // 关键：必须在 OPTIONS 处理之后设置


        //获取请求路径
        String url = request.getRequestURI().toString();

        //判断请求路径，是否包含login,包含就放行，不做token验证
        if (url.contains("login")){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        if (url.contains("login")){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }

        //获取请求头,返回前端携带过来的令牌
        String jwt = request.getHeader("token");
        if (StringUtils.hasLength(jwt)){
            //返回登录页面
           Result  notLogin= Result.error("请求失败",5000);
           //把notLogin转换成String
            String jsonString= JSONObject.toJSONString(notLogin);

           //给前端返回json格式数据
           response.getWriter().write(jsonString);
           return;
        }

        //解析令牌
        try {
            JWTUtil.parseJWT(jwt);
        }catch (Exception e){
            //令牌存在问题
            //返回登录页面
            Result  notLogin= Result.error("请求失败",5000);
            //把
            String jsonString=JSONObject.toJSONString(notLogin);
            //给前端返回json格式数据
            response.getWriter().write(jsonString);
            return;
        }



        //令牌有效，放行
        filterChain.doFilter(servletRequest,servletResponse);

    }
}
