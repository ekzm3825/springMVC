package com.diquest.disa4.config;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Apache Commons BeanUtils 설정
 */
@Component
public class BeanUtilsConfig {

    @PostConstruct
    public void init() throws Exception {
        nullSetting();
    }

    /**
     * BeanUtils 이용해서 객체 복사시 Number 값이 Null 인 경우 0으로 설정됨
     * 따라서 초기 값을 Null 하도록 설정함
     * <p>
     * https://stackoverflow.com/questions/8295895/beanutils-copyproperties-convert-integer-null-to-0
     */
    public void nullSetting() throws Exception {
        BeanUtilsBean.getInstance().getConvertUtils().register(new BigDecimalConverter(null), BigDecimal.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new BigIntegerConverter(null), BigInteger.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new BooleanConverter(null), Boolean.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new ByteConverter(null), Byte.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new CharacterConverter(null), Character.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new DoubleConverter(null), Double.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new FloatConverter(null), Float.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new IntegerConverter(null), Integer.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new LongConverter(null), Long.class);
        BeanUtilsBean.getInstance().getConvertUtils().register(new ShortConverter(null), Short.class);
    }

}
