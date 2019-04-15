package com.diquest.disa4.web.common.advice;

import com.diquest.disa4.exception.SiteException;
import com.diquest.disa4.module.common.service.IdGenServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.regex.PatternSyntaxException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @Autowired
    private IdGenServiceImpl idGenService;

    public GlobalExceptionHandler() {
        super("common/error/default");
        // Exception 개별 페이지 지정
        this.addExceptionMapping("NoHandlerFoundException", "common/error/404");
    }

    // #####################################
    // ExceptionHandler
    // #####################################
    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ModelAndView AccessDeniedException(AccessDeniedException e) {
        log.debug("[AccessDeniedException]", e);
        return createModelAndView(new SiteException("error.sec001", e));
    }

    @ExceptionHandler(value = DuplicateKeyException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView duplicateKeyException(DuplicateKeyException e) {
        log.warn("[DuplicateKeyException]", e);
        idGenService.reIndex();
        return createModelAndView(new SiteException("error.sys005", e));
    }

    @ExceptionHandler(PatternSyntaxException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView patternSyntaxException(PatternSyntaxException e) {
        log.info("[PatternSyntaxException]", e);

        String s = e.getPattern();
        if (e.getIndex() >= 0) {
            s += "\n";
            for (int i = 0; i < e.getIndex(); ++i) {
                s += " ";
            }
            s += "^";
        }

        return createModelAndView(new SiteException("error.sys006", new Object[]{e.getIndex(), s}, e));
    }

}