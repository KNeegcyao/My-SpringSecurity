package com.example.ss.handler;

import com.alibaba.fastjson.JSON;
import com.example.ss.domain.ResponseResult;
import com.example.ss.utils.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 授权失败处理类
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult result = new ResponseResult<>(HttpStatus.FORBIDDEN.value(), "权限不足");
        String json = JSON.toJSONString(request);
        WebUtils.renderString(response,json);
    }
}
