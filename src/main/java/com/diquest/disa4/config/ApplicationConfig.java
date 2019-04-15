package com.diquest.disa4.config;

import com.diquest.disa4.core.servlet.CustomExcel2ViewResolver;
import com.diquest.disa4.core.servlet.view.encoding.CustomFilenameEncoder;
import com.diquest.disa4.web.common.interceptor.SiteInterceptor;
import kr.qusi.spring.http.ExtendedMediaType;
import kr.qusi.spring.servlet.JsonViewResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.tiles3.SpringBeanPreparerFactory;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableWebMvc
@EnableAsync(mode = AdviceMode.PROXY, proxyTargetClass = true)
@EnableCaching(mode = AdviceMode.PROXY, proxyTargetClass = true)
@EnableScheduling
@ComponentScan("com.diquest.disa4")
@PropertySources({
        @PropertySource(value = "classpath:config/application.properties")
        // ir.rdbms.properties 는 classpath 와 INFOCHATTER2_HOME 검색
        // 두군데 모두 존재하는 경우 classpath 의 설정이 사용됨
        , @PropertySource(value = "file:${infochatter2.home}/rdbms/ir.rdbms.properties", ignoreResourceNotFound = true)
        , @PropertySource(value = "classpath:config/ir.rdbms.properties", ignoreResourceNotFound = true)
        // 엔진 접속정보
        , @PropertySource(value = "classpath:config/engine.properties", ignoreResourceNotFound = true)
})
@Import({
        EhCacheConfig.class,
        DataAccessConfig.class, TransactionConfig.class,
        IdGenConfig.class
})
public class ApplicationConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private Environment env;

    /**
     * {@link org.springframework.beans.factory.annotation.Value} 를 사용하기 위해선
     * PropertySourcesPlaceholderConfigurer 를 Bean 으로 등록해야한다.
     *
     * @see <a href="http://kwonnam.pe.kr/wiki/springframework/propertysource">springframework:propertysource [권남]</a>
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                // eGovFrame
                "classpath:message/egovframe/message-common", "classpath:messages/egovframe/idgnr"
                // Application
                , "classpath:messages/error", "classpath:messages/validation"
        );
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);

        return messageSource;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적리소스
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
        // files
        String attachDir = env.getProperty("app.attach.fileDir");
        String attachUri = SystemUtils.IS_OS_WINDOWS ? "file:///" : "file:";
        attachUri += attachDir + "/";
        registry.addResourceHandler("/files/**").addResourceLocations(attachUri);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(siteInterceptor());
    }

    @Bean
    public SiteInterceptor siteInterceptor() {
        return new SiteInterceptor();
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorPathExtension(false)
                .ignoreAcceptHeader(true)
                .favorParameter(true)
                .useJaf(false)
                .parameterName("_format")
                .defaultContentType(MediaType.TEXT_HTML)
                .mediaType("html", MediaType.TEXT_HTML)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xls", ExtendedMediaType.APPLICATION_XLS);
    }

    @Bean
    public ContentNegotiatingViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
        // Tiles ViewResolver
        TilesViewResolver tilesViewResolver = new TilesViewResolver();

        // JSP ViewResolver
        InternalResourceViewResolver jspViewResolver = new InternalResourceViewResolver();
        jspViewResolver.setViewClass(JstlView.class);
        jspViewResolver.setPrefix("/WEB-INF/view/");
        jspViewResolver.setSuffix(".jsp");

        // JSON ViewResolver
        JsonViewResolver jsonViewResolver = new JsonViewResolver();

        // Excel ViewResolver
        CustomExcel2ViewResolver xlsViewResolver = new CustomExcel2ViewResolver();
        xlsViewResolver.setContentType(ExtendedMediaType.APPLICATION_XLS_VALUE);
        xlsViewResolver.setPrefix("/WEB-INF/excel/");
        xlsViewResolver.setSuffix(".xls");
        xlsViewResolver.setFilenameEncoder(new CustomFilenameEncoder());

        // Content Negotiating ViewResolver
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(manager);
        resolver.setUseNotAcceptableStatusCode(true);

        List<ViewResolver> resolvers = new ArrayList<ViewResolver>();
        resolvers.add(tilesViewResolver);
        resolvers.add(jspViewResolver);
        resolvers.add(jsonViewResolver);
        resolvers.add(xlsViewResolver);

        resolver.setViewResolvers(resolvers);
        return resolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        // Tiles
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions("classpath:config/tiles-config.xml");
        tilesConfigurer.setCheckRefresh(true);
        tilesConfigurer.setPreparerFactoryClass(SpringBeanPreparerFactory.class);

        return tilesConfigurer;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxInMemorySize(0);
        multipartResolver.setMaxUploadSize(-1);
        multipartResolver.setDefaultEncoding("UTF-8");

        return multipartResolver;
    }

}
