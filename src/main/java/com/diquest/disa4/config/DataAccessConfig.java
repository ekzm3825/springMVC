package com.diquest.disa4.config;

import com.diquest.disa4.core.orm.mybatis.RefreshableSqlSessionFactoryBean;
import com.diquest.ir.util.encode.SimpleEncoder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ddlutils.PlatformUtils;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Properties;

@MapperScan(basePackages = "com.diquest.disa4.module.**", annotationClass = Mapper.class, sqlSessionFactoryRef = "sqlSessionFactory")
public class DataAccessConfig {

    private static final Properties SUPPORTED_DATABASES = new Properties() {{
        setProperty("MySQL", "mysql");
        setProperty("Oracle", "oracle");
    }};

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "dataSource", destroyMethod = "close")
    public HikariDataSource dataSource() {
        String password = env.getProperty("ir.jdbc.password");
        if ("true".equalsIgnoreCase(env.getProperty("ir.useEncryption", "false"))) {
            password = SimpleEncoder.decrypt(password);
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("ir.jdbc.driver.class"));
        config.setJdbcUrl(env.getProperty("ir.jdbc.url"));
        config.setUsername(env.getProperty("ir.jdbc.username"));
        config.setPassword(password);

        return new HikariDataSource(config);
    }

    @Primary
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        String databaseType = new PlatformUtils().determineDatabaseType(dataSource);
        if (databaseType == null || !SUPPORTED_DATABASES.containsKey(databaseType))
            throw new IllegalArgumentException("지원하지 않는 Database 입니다. (DatabaseType: " + databaseType + ")");

        PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

        RefreshableSqlSessionFactoryBean sessionFactoryBean = new RefreshableSqlSessionFactoryBean();
        sessionFactoryBean.setInterval(1000);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfigLocation(resourceResolver.getResource("classpath:config/mybatis-config.xml"));

        // Mapper 에서 사용가능한 변수설정
        Properties variables = new Properties();
        // - 테이블 접두어
        String tablePrefix = env.getProperty("ir.table.prefix");
        variables.put("db.tablePrefix", StringUtils.isEmpty(tablePrefix) ? "" : tablePrefix + "_");

        sessionFactoryBean.setConfigurationProperties(variables);

        // Database Id Provider
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        databaseIdProvider.setProperties(SUPPORTED_DATABASES);
        sessionFactoryBean.setDatabaseIdProvider(databaseIdProvider);

        // XML Mapper
        Resource[] mapperXmls = resourceResolver.getResources("classpath*:com/diquest/disa4/module/**/dao/*.xml");
        sessionFactoryBean.setMapperLocations(mapperXmls);

        sessionFactoryBean.afterPropertiesSet();

        return sessionFactoryBean.getObject();
    }

}
