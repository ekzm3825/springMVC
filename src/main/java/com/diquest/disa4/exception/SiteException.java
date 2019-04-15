package com.diquest.disa4.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.Map;

/**
 * <b>SiteException</b>
 * <p/>
 * SiteException 는 Presentation Layer (Controller) 에서 사용한다.
 * 사용자에게 에러메시지 뿐만 아니라, Alert 이 필요한경우
 * {@link #alertType} 를 활용하여 할 수있다.
 *
 * @author yongseoklee
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SiteException extends BaseException {

    /**
     * {@link #confirmUrl} 또는 {@link #cancelUrl} 에
     * ALERT_HISTORY_BACK 를 사용하면 뒤로가기로 처리
     */
    public static final String ALERT_HISTORY_BACK = "-1";

    /**
     * {@link #confirmUrl} 또는 {@link #cancelUrl} 에
     * WINDOW_SELF_CLOSE 를 사용하면 윈도우 닫기 (=self.close());
     */
    public static final String WINDOW_SELF_CLOSE = "-2";

    /**
     * JavaScript Alert 유형
     * ALERT: 확인
     * CONFIRM: 확인, 취소
     */
    public enum AlertType {
        NONE, ALERT, CONFIRM
    }

    /**
     * Alert 종류
     */
    @Accessors(chain = true)
    private AlertType alertType;

    /**
     * Alert 확인버튼 이동 URL
     */
    @Accessors(chain = true)
    private String confirmUrl = ALERT_HISTORY_BACK;

    /**
     * Alert 취소버튼 이동 URL
     */
    @Accessors(chain = true)
    private String cancelUrl;

    /**
     * View 에 추가정보가 필요한경우 사용
     */
    private ModelMap extras;

    public SiteException(String messageKey) {
        super(messageKey);
    }

    public SiteException(String messageKey, Object[] messageArgs) {
        super(messageKey, messageArgs);
    }

    public SiteException(String messageKey, Throwable throwable) {
        super(messageKey, throwable);
    }

    public SiteException(String messageKey, Object[] messageArgs, Throwable throwable) {
        super(messageKey, messageArgs, throwable);
    }

    public String getCancelUrl() {
        return this.cancelUrl == null ? "" : this.cancelUrl;
    }

    public String getConfirmUrl() {
        return this.confirmUrl == null ? "" : this.confirmUrl;
    }

    // #####################################
    // Extras (ModelMap) delegate method
    // #####################################
    public ModelMap getExtras() {
        return this.getExtras(true);
    }

    public ModelMap getExtras(boolean autoCreate) {
        if (this.extras == null && autoCreate) {
            this.extras = new ModelMap();
        }
        return this.extras;
    }

    public boolean containsExtra(String extraName) {
        return getExtras().containsAttribute(extraName);
    }

    public SiteException mergeExtra(Map<String, ?> extras) {
        getExtras().mergeAttributes(extras);
        return this;
    }

    public SiteException addAllExtras(Map<String, ?> extras) {
        getExtras().addAllAttributes(extras);
        return this;
    }

    public SiteException addAllExtras(Collection<?> extraValues) {
        getExtras().addAllAttributes(extraValues);
        return this;
    }

    public SiteException addExtra(Object extraValue) {
        getExtras().addAttribute(extraValue);
        return this;
    }

    public SiteException addExtra(String extraName, Object extraValue) {
        getExtras().addAttribute(extraName, extraValue);
        return this;
    }

    public Object removeExtra(String extraName) {
        return getExtras().remove(extraName);
    }

}
