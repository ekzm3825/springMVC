package com.diquest.disa4.config;

import com.diquest.disa4.module.common.service.IdGenServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

public class IdGenConfig {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Bean
    public IdGenServiceImpl idGenService() {
        IdGenServiceImpl service = new IdGenServiceImpl();
        service.setDataSource(dataSource);
        service.setTable("IDS");
        service.setTablePrefix(env.getProperty("ir.table.prefix"));
        service.setTableNameFieldName("ID_NAME");
        service.setNextIdFieldName("NEXT_ID");
        service.setBlockSize(10);

        service.addIdDefine("CHAT_BASIC", "ID");

        return service;
    }

}
