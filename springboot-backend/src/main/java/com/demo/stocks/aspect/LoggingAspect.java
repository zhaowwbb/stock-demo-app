package com.demo.stocks.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.demo.stocks.service.*.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {

        System.out.println("AOP BEFORE METHOD: "
                + joinPoint.getSignature().getName());
    }

    @Around("execution(* com.demo.stocks.service.*.*(..))")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();

        System.out.println("Execution Time: " + (end - start));

        return result;
    }
}