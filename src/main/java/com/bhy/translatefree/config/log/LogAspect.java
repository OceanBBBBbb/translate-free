package com.bhy.translatefree.config.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.UUID;

/**
 * @description: 切面日志格式
 * @author: binhy
 * @create: 2019/11/27
 **/
@Aspect
@Component
@Slf4j
public class LogAspect {


    private static ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    private static ThreadLocal<String> key = new ThreadLocal<String>();
    private static ObjectMapper objectMapper = new ObjectMapper();
    protected static final String[] NEEDS_HEADER = {"appVersion","platform","brand","network","host","user-agent", "referer"};

    @Pointcut("execution(* com.bhy.translatefree.controller..*.*(..))")
    public void controllerMethodPointcut() {
    }

    @Before(value = "controllerMethodPointcut() && @annotation(logAble)")
    public void doBefore(JoinPoint joinPoint, Log logAble) {
        if (logAble.needLog()) {
            // 请求开始时间
            startTime.set(System.currentTimeMillis());
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes sra = (ServletRequestAttributes) ra;
            HttpServletRequest request = Objects.requireNonNull(sra).getRequest();
            String requestId = UUID.randomUUID().toString();
            String uri = request.getRequestURI();
            String reqInfo = "requestId:" + requestId + ",uri:" + uri;
            StringBuilder headers = new StringBuilder();
            for (String name : NEEDS_HEADER) {
                String value = request.getHeader(name);
                headers.append(name).append(":").append(value).append(",");
            }
            String method = request.getMethod();
            StringBuilder params = new StringBuilder();
            if (HttpMethod.GET.toString().equals(method)) {
                String queryString = request.getQueryString();
                if (!StringUtils.isEmpty(queryString)) {
                    try {
                        params.append(URLEncoder.encode(queryString, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        log.error(logAble.value() + "get方法参数转码异常，UnsupportedEncodingException");
                    }
                }
            } else {//其他请求
                Object[] paramsArray = joinPoint.getArgs();
                if (paramsArray != null && paramsArray.length > 0) {
                    for (Object o : paramsArray) {
                        if (o instanceof Serializable || o instanceof ServletRequest) {
                            params.append(o.toString()).append(",");
                        } else {
                            //使用json系列化 反射等等方法 反系列化会影响请求性能建议重写tostring方法实现系列化接口
                            try {
                                String param = objectMapper.writeValueAsString(o);
                                if (!StringUtils.isEmpty(param)) {
                                    params.append(param).append(",");
                                }
                            } catch (JsonProcessingException e) {
                                log.error(logAble.value() + "doBefore obj to json exception obj={}", o);
                            }
                        }
                    }
                }
            }
            key.set(reqInfo);
            log.info("request params>>>>>> headers={},\nreqInfo={},method={},\nparams={}",headers.toString(),reqInfo, method, params);
        }
    }


    @AfterReturning(returning = "obj", pointcut = "controllerMethodPointcut() && @annotation(logAble)")
    public void doAfterReturing(Object obj, Log logAble) {

        if (logAble.needLog()) {
            long costTime = getCostTime();
            String reqInfo = getReqInfo();
            String result = null;
            if (obj instanceof Serializable) {
                result = obj.toString();
            } else {
                if (obj != null) {
                    try {
                        result = objectMapper.writeValueAsString(obj);
                        if (result.length() > 1000) {
                            result = result.substring(0, 1000) + "......省略，长度达到:" + result.length();
                        }
                    } catch (Exception e) {
                        log.error(logAble.value() + "doAfterReturing obj to json exception obj={},msg={}", obj, e);
                    }
                }
            }
            log.info("response result<<<<<<\nreqInfo={}, costTime={}ms,\nresult={}", reqInfo, costTime, result);
        }
    }


    @AfterThrowing(throwing = "ex", pointcut = "controllerMethodPointcut() && @annotation(logAble)")
    public void doAfterThrowing(Throwable ex, Log logAble) {
        if (logAble.needLog()) {
            long costTime = getCostTime();
            String reqInfo = getReqInfo();
            log.error("response 执行结果为error<<<<<<\nreqInfo={}, costTime={}ms,\nerror msg={},详见ExceptionAdvice", reqInfo, costTime, ex.getMessage());
        }
    }

    private String getReqInfo() {
        String reqInfo = "unknown";
        if (null != key.get()) {
            reqInfo = key.get();
            key.remove();
        }
        return reqInfo;
    }

    private long getCostTime() {
        long costTime = 0L;
        if (null != startTime.get()) {
            costTime = System.currentTimeMillis() - startTime.get();
            startTime.remove();
        }
        return costTime;
    }

}
