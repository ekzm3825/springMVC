package com.diquest.disa4.exception;

/**
 * <b>BizException</b>
 * <p/>
 * Business Layer (Service) 에서 에러발생시 사용한다.
 * Presentation Layer (Controller) 에서 에러 핸들링이 필요 한 경우,
 * {@link SiteException} 으로 다시 감싸거나 소화한다.
 *
 * @author yongseoklee
 */
public class BizException extends BaseException {

    public BizException(String messageKey) {
        super(messageKey);
    }

    public BizException(String messageKey, Object[] messageArgs) {
        super(messageKey, messageArgs);
    }

    public BizException(String messageKey, Throwable throwable) {
        super(messageKey, throwable);
    }

    public BizException(String messageKey, Object[] messageArgs, Throwable throwable) {
        super(messageKey, messageArgs, throwable);
    }

}
