package com.diquest.disa4.web.common.advice;

import com.diquest.disa4.exception.BaseException;
import com.diquest.disa4.exception.BizException;
import com.diquest.disa4.exception.SiteException;
import com.diquest.disa4.exception.ValidationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exception 을 Controller 에서 핸들링
 *
 * @author yongseoklee
 */
@Slf4j
public abstract class BaseExceptionHandler {

    /**
     * 기본 오류페이지
     * {@link #exceptionMappings} 등록된 뷰가 없는경우 사용됨
     */
    @Getter
    private final String defaultViewName;

    /**
     * Exception Name 과 ViewName 을 한쌍으로 사용함
     * 대소문자 유의해야
     */
    private Map<String, String> exceptionMappings;

    public BaseExceptionHandler(String defaultViewName) {
        this.defaultViewName = defaultViewName;
    }

    /**
     * 현재 Exception 에서 적절한 ViewName 결정
     *
     * @param e Exception
     * @return
     */
    protected String determineViewName(Exception e) {
        if (exceptionMappings != null) {
            for (String exceptionName : exceptionMappings.keySet()) {
                if (exceptionName.equals(e.getClass().getSimpleName())) {
                    return exceptionMappings.get(exceptionName);
                }
            }
        }

        return defaultViewName;
    }

    /**
     * Exception 발생시 맵핑할 뷰 추가
     *
     * @param exceptionName
     * @param viewName
     */
    protected void addExceptionMapping(String exceptionName, String viewName) {
        if (exceptionMappings == null)
            exceptionMappings = new LinkedHashMap<String, String>();

        exceptionMappings.put(exceptionName, viewName);
    }

    protected ModelAndView createModelAndView(Exception e) {
        ModelAndView modelAndView = new ModelAndView(determineViewName(e));
        modelAndView.addObject("message", e.getLocalizedMessage());
        modelAndView.addObject("exception", e);

        return modelAndView;
    }

    // #####################################
    // ExceptionHandler
    // #####################################
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView exception(Exception e) {
        log.warn("[Exception]", e);
        return createModelAndView(new SiteException("error.sys000", e));
    }

    @ExceptionHandler(value = DataAccessException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView dataAccessException(DataAccessException e) {
        log.warn("[DataAccessException]", e);
        return createModelAndView(new SiteException("error.sys001", e));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ModelAndView noHandlerFoundException(NoHandlerFoundException e) {
        log.debug("[NoHandlerFoundException]", e);
        return createModelAndView(e);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public ModelAndView httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        log.debug("[HttpMediaTypeNotAcceptableException]", e);
        return createModelAndView(e);
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ModelAndView fileNotFoundException(FileNotFoundException e) {
        log.debug("[FileNotFoundException]", e);
        return createModelAndView(e);
    }

    @ExceptionHandler(BaseException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView baseException(BaseException e) {
        log.info("[BaseException]", e);
        return createModelAndView(e);
    }

    @ExceptionHandler(SiteException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView siteException(SiteException e) {
        log.info("[SiteException]", e);
        return createModelAndView(e);
    }

    @ExceptionHandler(BizException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView bizException(BizException e) {
        log.info("[BizException]", e);
        return createModelAndView(e);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ModelAndView validationException(ValidationException e) {
        log.info("[ValidationException]", e);
        return createModelAndView(e);
    }

}