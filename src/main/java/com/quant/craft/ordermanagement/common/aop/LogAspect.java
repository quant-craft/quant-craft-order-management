package com.quant.craft.ordermanagement.common.aop;

import com.quant.craft.ordermanagement.exception.ExchangeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAspect {


    @Around("within(com.quant.craft.ordermanagement.service.*) && !within(com.quant.craft.ordermanagement.service.DataLoaderService) || within(com.quant.craft.ordermanagement.EMS.*)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("Entering method: {}.{}", className, methodName);

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        log.info("Exiting method: {}.{} - Execution time: {} ms", className, methodName, executionTime);

        return result;
    }

    @AfterThrowing(pointcut = "execution(* com.quant.craft.ordermanagement..*(..))", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String exceptionName = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        if (exception instanceof ExchangeException) {
            ExchangeException ex = (ExchangeException) exception;
            log.error("ExchangeException in {}.{}: {} - {} - ExchangeType: {} - ErrorCode: {}",
                    className, methodName, exceptionName, exceptionMessage,
                    ex.getExchangeType(), ex.getErrorCode());
        } else {
            log.warn("Exception in {}.{}: {} - {}", className, methodName, exceptionName, exceptionMessage);
        }
    }
}