package br.com.nhac.backend_nhac.infra.security;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
@Import(WebMvcSecurityTestSupport.class)
public @interface WebMvcControllerTest {

    @AliasFor(annotation = org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};

    @AliasFor(annotation = WebMvcTest.class, attribute = "properties")
    String[] properties() default {};

    @AliasFor(annotation = WebMvcTest.class, attribute = "excludeFilters")
    ComponentScan.Filter[] excludeFilters() default {};
}