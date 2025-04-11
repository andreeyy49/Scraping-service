package webscraping.authservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private long startTime;

    private String methodName;

    private String className;

    private LoggingLevel level;

    private String beforeLog = ">> method:{}({}) uri:{} class:{}";

    private String afterLog = "<< method:{}({}):{}ms uri:{} class:{}";

    @Before(value = "@within(logging)", argNames = "joinPoint,logging")
    public void log(JoinPoint joinPoint, Logging logging) {
        Object[] args = joinPoint.getArgs();
        List<Object> argList = initData(args, joinPoint, logging);

        printLog(argList, true);

        startTime = System.currentTimeMillis();
    }

    @After(value = "@within(logging)", argNames = "joinPoint,logging")
    public void logReturn(JoinPoint joinPoint, Logging logging) {
        Object[] args = joinPoint.getArgs();
        List<Object> argList = initData(args, joinPoint, logging);

        printLog(argList, false);
    }

    private List<Object> initData(Object[] args, JoinPoint joinPoint, Logging logging) {
        level = logging.level();
        List<Object> argList = Arrays.asList(args);
        argList = argList.stream().map(el -> {
            if (el.getClass().equals(String.class) && ((String) el).length() > 60) {
                el = "image";
            }
            return el;
        }).toList();
        className = joinPoint.getTarget().getClass().getName();
        className = className.replace("social.network.socialnetworkgeo.", "");
        methodName = joinPoint.getSignature().getName();

        return argList;
    }

    private void printLog(List<Object> argList, boolean isBefore) {
        String requestUri = getRequestUriSafe();

        if (isBefore) {
            switch (level) {
                case ERROR -> log.error(beforeLog, methodName, argList, requestUri, className);
                case INFO -> log.info(beforeLog, methodName, argList, requestUri, className);
                case DEBUG -> log.debug(beforeLog, methodName, argList, requestUri, className);
                case WARNING -> log.warn(beforeLog, methodName, argList, requestUri, className);
            }
        } else {
            switch (level) {
                case ERROR -> log.error(afterLog, methodName, argList, System.currentTimeMillis() - startTime, requestUri, className);
                case INFO -> log.info(afterLog, methodName, argList, System.currentTimeMillis() - startTime, requestUri, className);
                case DEBUG -> log.debug(afterLog, methodName, argList, System.currentTimeMillis() - startTime, requestUri, className);
                case WARNING -> log.warn(afterLog, methodName, argList, System.currentTimeMillis() - startTime, requestUri, className);
            }
        }
    }

    private String getRequestUriSafe() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest().getRequestURI();
        }
        return "N/A";
    }
}
