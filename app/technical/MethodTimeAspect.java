package technical;

import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class MethodTimeAspect {
    
    private static final Logger logger = LoggerFactory.getLogger("MethodExecutionTimeLogging");
    
    @Around("execution(* *(..))  && @annotation(Loggable)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        System.out.println("Aspect actually works");
        logger.error("Aspect works");
        StopWatch methodExecutionTimeSW = new StopWatch();
        methodExecutionTimeSW.start();
        Object methodInvocationResult = point.proceed();
        methodExecutionTimeSW.stop();
        logger.debug("{} execution time [{}]ms", point.getSignature().getName(), methodExecutionTimeSW.getTime());
        return methodInvocationResult;
    }
}
