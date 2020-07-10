package com.xlb.service.data.web.config;

import com.xlb.base.aspect.BindingErrorControllerAspect;
import com.xlb.base.controller.CustomErrorController;
import com.xlb.base.handler.ResponseBodyMessageHandle;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Configuration
@EnableConfigurationProperties({MvcConfigProperties.class, ServerProperties.class})
public class MvcConfig {

    private final MvcConfigProperties configProperties;
    private final ServerProperties serverProperties;

    @Bean
    public ResponseBodyMessageHandle responseBodyMessageHandle() {
        return new ResponseBodyMessageHandle();
    }

    @Bean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .addProperty("hibernate.validator.fail_fast", "true") //为true时代表快速失败模式，false则为全部校验后再结束。
                .buildValidatorFactory();

        return validatorFactory.getValidator();
    }

    @Bean
    public BindingErrorControllerAspect bindingErrorControllerAspect() {
        BindingErrorControllerAspect result = new BindingErrorControllerAspect();
        if (StringUtils.isNotBlank(configProperties.getDefaultErrorCode())) {
            result.setErrorCode(configProperties.getDefaultErrorCode());
        }
        return result;
    }

    @Bean
    public Advisor bindingErrorControllerAdvisor(BindingErrorControllerAspect txAdvice) {
        DefaultBeanFactoryPointcutAdvisor advisor = new DefaultBeanFactoryPointcutAdvisor();
        advisor.setAdvice(txAdvice);
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
//        pointcut.setExpression("execution(* com.jhc..*.controller..*.*(..))");
        pointcut.setExpression(configProperties.getErrorExpression());
        advisor.setPointcut(pointcut);
        return advisor;
    }

    @Bean
    public CustomErrorController errorController(ErrorAttributes errorAttributes, List<ErrorViewResolver> viewResolvers) {
        return new CustomErrorController(errorAttributes, serverProperties.getError(), viewResolvers);
    }

}
