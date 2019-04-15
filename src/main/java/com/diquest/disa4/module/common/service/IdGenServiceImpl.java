package com.diquest.disa4.module.common.service;

import egovframework.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * ID 생성서비스
 */
@Slf4j
@Data
public class IdGenServiceImpl implements ApplicationContextAware, InitializingBean {

    private static final Map<String, EgovTableIdGnrServiceImpl> cache = new HashMap<>();

    private static final Map<String, String> idDefines = new HashMap<>();

    private static final List<String> reIndexSql = new ArrayList<>();

    private ApplicationContext applicationContext;

    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private String table = "IDS";

    private String tablePrefix;

    private String tableNameFieldName = "ID_NAME";

    private String nextIdFieldName = "NEXT_ID";

    private int blockSize = 10;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.jdbcTemplate = new JdbcTemplate(getDataSource());

        build();
    }

    private EgovTableIdGnrServiceImpl getService(String name) {
        EgovTableIdGnrServiceImpl service = cache.get(name);
        if (service == null) {
            throw new NullPointerException(
                "[IdGenService] " + name + " 서비스를 찾을 수 없습니다. 설정을 확인하세요.");
        }

        return service;
    }

    private String getTableWithPrefix(String name) {
        return StringUtils.isEmpty(getTablePrefix()) ? name : getTablePrefix() + "_" + name;
    }

    private void build() {
        log.debug("[IdGenService] Building...");

        try {
            cache.clear();
            reIndexSql.clear();

            String idsTable = getTableWithPrefix(getTable());

            for (String name : idDefines.keySet()) {
                String primaryKey = idDefines.get(name);
                // ID Gen 서비스 생성
                log.debug("[IdGenService] {}({}) 서비스 생성", name, primaryKey);

                EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
                service.setBlockSize(getBlockSize());
                service.setApplicationContext(getApplicationContext());
                service.setDataSource(getDataSource());
                service.setTable(idsTable);
                service.setTableName(name);
                service.setTableNameFieldName(getTableNameFieldName());
                service.setNextIdFieldName(getNextIdFieldName());
                service.afterPropertiesSet();

                cache.put(name, service);

                // ReIndex SQL 생성
                String srcTable = getTableWithPrefix(name);
                reIndexSql.add(String.format("DELETE FROM %s WHERE ID_NAME = '%s'", idsTable, name));
                reIndexSql.add(String.format("INSERT INTO %s(ID_NAME, NEXT_ID) VALUES ('%s', (SELECT (CASE WHEN 1000 < MAX(%s) THEN MAX(%s) + 1 ELSE 1001 END) FROM %s))", idsTable, name, primaryKey, primaryKey, srcTable));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getId(String name) {
        try {
            return getService(name).getNextIntegerId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getStringId(String name) {
        try {
            return getService(name).getNextStringId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addIdDefine(String name, String primaryKey) {
        idDefines.put(name, primaryKey);
    }

    public void reIndex() {
        log.debug("[IdGenService] Re-Indexing...");
        String[] sql = reIndexSql.toArray(new String[reIndexSql.size()]);
        jdbcTemplate.batchUpdate(sql);

        build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
