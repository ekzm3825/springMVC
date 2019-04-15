package com.diquest.disa4.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    /**
     * Application 에 등록된 Bean 취득
     *
     * @param name  Bean 이름
     * @param clazz Bean 클래스
     * @return Bean or null
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        if (applicationContext == null)
            return null;

        return applicationContext.getBean(name, clazz);
    }

    /**
     * Application 에 등록된 Bean 취득
     * 클래스 유형으로 등록된 빈 반환
     *
     * @param clazz Bean 클래스
     * @return Bean or null
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null)
            return null;

        return applicationContext.getBean(clazz);
    }

}
