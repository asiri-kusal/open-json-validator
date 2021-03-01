package com.github.asirikusal;

import java.util.Map;

import com.github.asirikusal.configuration.exception.OpenValidatorException;
import com.github.asirikusal.model.ErrorWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Aspect
@Component
@ConditionalOnExpression("${aspect.enabled:true}")
public class JsonMapValidatorAop {

    @Autowired
    private DynamicJsonValidator jsonValidator;

    //Defines a pointcut that we can use in the @Before,@After, @AfterThrowing, @AfterReturning,@Around specifications
    //The pointcut will look for the @YourAnnotation
    @Pointcut("@annotation(param)")
    public void annotationPointCutDefinition(JsonMapValidator param) {
    }

    //Defines a pointcut that we can use in the @Before,@After, @AfterThrowing, @AfterReturning,@Around specifications
    //The pointcut is a catch-all pointcut with the scope of execution
    @Pointcut("execution(* *(..))")
    public void atExecution() {
    }

    //Defines a pointcut where the @YourAnnotation exists
    //and combines that with a catch-all pointcut with the scope of execution
    @Around("annotationPointCutDefinition(param) && atExecution()")
    //ProceedingJointPoint = the reference of the call to the method.
    //The difference between ProceedingJointPoint and JointPoint is that a JointPoint can't be continued (proceeded)
    //A ProceedingJointPoint can be continued (proceeded) and is needed for an Around advice
    public Object validateMap(ProceedingJoinPoint point, JsonMapValidator param) throws Throwable {
        Object[] arguments = point.getArgs();
        if (arguments == null) {
            throw new RuntimeException("Json binding failure");
        }
        ErrorWrapper errorWrapper = jsonValidator.errorList((Map<String, Object>) arguments[0], param.schemaName());
        if (!CollectionUtils.isEmpty(errorWrapper.getErrorList())) {
            throw new OpenValidatorException(errorWrapper);
        }
        return point.proceed();
    }
}
