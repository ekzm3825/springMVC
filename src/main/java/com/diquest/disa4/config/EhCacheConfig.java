package com.diquest.disa4.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class EhCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

        EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();
        factoryBean.setShared(true);
        factoryBean.setConfigLocation(resourceResolver.getResource("classpath:config/ehcache.xml"));
        factoryBean.afterPropertiesSet();

        EhCacheCacheManager cacheManager = new EhCacheCacheManager();
        cacheManager.setCacheManager(factoryBean.getObject());

        return cacheManager;
    }

}
