package com.diquest.disa4.exception;

import com.diquest.disa4.config.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * <b>BaseException</b>
 * <p/>
 * Exception 는 메시지를 그대로 출력한다.
 * 하지만 BaseException 은 Application Context 에 등록된 MessageSource 에서 메시지를 조회하는데,
 * 조회시 사용자 세션에 등록된 Locale 을 참조하여 다국어 지원을 한다.
 * 만약 메시지 프로터티에 값이 없는경우 입력된 문자를 그대로 출력한다.
 * 단, BaseException 은 Spring Application 에서 작동한다.
 *
 * @author yongseoklee
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseException extends RuntimeException {

    @JsonIgnore
    private final String messageKey;

    @JsonIgnore
    private final Object[] messageArgs;

    public BaseException(String messageKey) {
        this(messageKey, null, null);
    }

    public BaseException(String messageKey, Object[] messageArgs) {
        this(messageKey, messageArgs, null);
    }

    public BaseException(String messageKey, Throwable throwable) {
        this(messageKey, null, throwable);
    }

    public BaseException(String messageKey, Object[] messageArgs, Throwable throwable) {
        super(throwable);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public String getCode() {
        return this.getMessageKey();
    }

    @Override
	public String getMessage() {
        if (this.getMessageKey() == null)
            return null;

        return getMessage(this.getMessageKey(), this.getMessageArgs(), Locale.getDefault());
    }

    @Override
	public String getLocalizedMessage() {
        if (this.getMessageKey() == null)
            return null;

        return getMessage(this.getMessageKey(), this.getMessageArgs(), getLocale());
    }

    @JsonIgnore
    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

    /**
     * MessageSource 에서 문자열 취득
     * {@link #getMessageSource()} 가 Null 또는 등록된 메시지가 없는경우 code 값이 반환됨
     *
     * @param code
     * @return Message or code
     */
    protected static String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * MessageSource 에서 문자열 취득
     * {@link #getMessageSource()} 가 Null 또는 등록된 메시지가 없는경우 code 값이 반환됨
     *
     * @param code
     * @param args
     * @return Message or code
     */
    protected static String getMessage(String code, Object[] args) {
        return getMessage(code, args, getLocale());
    }

    /**
     * MessageSource 에서 문자열 취득
     * {@link #getMessageSource()} 가 Null 또는 등록된 메시지가 없는경우 code 값이 반환됨
     *
     * @param code
     * @param args
     * @param locale
     * @return Message or code
     */
    protected static String getMessage(String code, Object[] args, Locale locale) {
        MessageSource messageSource = getMessageSource();
        return messageSource == null ? null : messageSource.getMessage(code, args, locale);
    }

    /**
     * ApplicationContext 에 등록된 MessageSource 취득
     *
     * @return {@link MessageSource} or null
     */
    protected static MessageSource getMessageSource() {
        return ApplicationContextProvider.getBean(MessageSource.class);
    }

    /**
     * 사용자 Locale 취득
     *
     * @return {@link Locale} or null
     */
    protected static Locale getLocale() {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        return localeContext == null ? null : localeContext.getLocale();
    }

}
