package com.cpa.yusin.quiz.common.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ApiOnlyArchitectureTest {

    @Test
    void legacyWebPackageShouldNotExistOnClasspath() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver.getResources("classpath*:com/cpa/yusin/quiz/web/**/*.class");

        assertThat(resources)
                .as("API 전용 서버 구조이므로 레거시 web 패키지 클래스가 남아 있으면 안 됨")
                .isEmpty();
    }

    @Test
    void pureMvcControllerShouldNotExist() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        List<String> mvcControllers = scanner.findCandidateComponents("com.cpa.yusin.quiz").stream()
                .map(beanDefinition -> loadClass(beanDefinition.getBeanClassName()))
                .filter(controllerClass -> !AnnotatedElementUtils.hasAnnotation(controllerClass, RestController.class))
                .map(Class::getName)
                .toList();

        assertThat(mvcControllers)
                .as("서버 렌더링용 @Controller는 제거되어야 함")
                .isEmpty();
    }

    @Test
    void removedTemplateResourcesShouldNotBePackaged() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assertThat(classLoader.getResource("templates/login.html")).isNull();
        assertThat(classLoader.getResource("static/js/problem.js")).isNull();
        assertThat(classLoader.getResource("static/css/problem.css")).isNull();
    }

    @Test
    void thymeleafShouldNotBePresentAtRuntime() {
        assertThat(ClassUtils.isPresent("org.thymeleaf.TemplateEngine", getClass().getClassLoader()))
                .as("thymeleaf 의존성은 제거된 상태여야 함")
                .isFalse();
    }

    @Test
    void legacyWebPackageShouldNotExistInSourceScan() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        assertThat(scanner.findCandidateComponents("com.cpa.yusin.quiz.web"))
                .as("소스 스캔 기준으로도 legacy web 패키지가 비어 있어야 함")
                .isEmpty();
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("클래스를 로드할 수 없음: " + className, exception);
        }
    }
}
