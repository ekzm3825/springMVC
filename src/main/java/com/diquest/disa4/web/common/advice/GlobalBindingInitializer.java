package com.diquest.disa4.web.common.advice;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class GlobalBindingInitializer {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // 동적 바인딩 개수 증가
        // 스프링은 기본으로 256개 자동바인딩 됨
        // 그 이상 바인딩이 필요한 경우 아래 설정필요
        binder.setAutoGrowNestedPaths(true);
        binder.setAutoGrowCollectionLimit(32767);

        /*
         * Controller 에 전달된 String 자료형 값 Trim 처리
         * {@link StringTrimmerEditor} 의 첫번째 인자 값이
         * true: 공백인 경우 Null 처리함
         * false: 공백인 경우 Null 처리하지 않음
         */
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

}
