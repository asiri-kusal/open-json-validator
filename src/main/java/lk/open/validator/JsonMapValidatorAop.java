package lk.open.validator;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.open.validator.model.ErrorWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(lk.open.validator.JsonMapValidator)")
    public Object validateMap(ProceedingJoinPoint point) throws Throwable {
        Object[] arguments = point.getArgs();
        if (arguments == null) {
            throw new RuntimeException("Json binding failure");
        }
        ErrorWrapper errorWrapper = jsonValidator.errorList((Map<String, Object>) arguments[0]);
        String json = objectMapper.writeValueAsString(errorWrapper);
        if (!CollectionUtils.isEmpty(errorWrapper.getErrorList())) {
            throw new RuntimeException(json);
        }
        return point.proceed();
    }
}
