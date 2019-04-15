package com.diquest.disa4.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends SiteException {

    @JsonIgnore
    private final Errors errors;

    public ValidationException(Errors errors) {
        super(_getMessageKey(errors), _getMessageArgs(errors));
        this.errors = errors;
        this.setAlertType(AlertType.ALERT);

        if (errors != null) {
            List<SimpleError> extra = new ArrayList<SimpleError>();

            for (ObjectError error : errors.getAllErrors()) {
                extra.add(new SimpleError(error));
            }

            this.addExtra("errors", extra);
        }
    }

    private static ObjectError getFirstError(Errors errors) {
        if (errors == null || errors.getAllErrors() == null
                || errors.getAllErrors().size() == 0 || errors.getAllErrors().get(0) == null)
            return null;

        return errors.getAllErrors().get(0);
    }

    private static String _getMessageKey(Errors errors) {
        ObjectError error = getFirstError(errors);
        return error == null ? null : error.getCode();
    }

    private static Object[] _getMessageArgs(Errors errors) {
        ObjectError error = getFirstError(errors);
        if (error == null || error.getArguments() == null)
            return null;

        List<Object> messageArgs = new ArrayList<Object>();
        for (Object argument : error.getArguments()) {
            messageArgs.add(argument instanceof String ? getMessage((String) argument) : argument);
        }

        return messageArgs.toArray(new Object[messageArgs.size()]);
    }

    @Data
    private static class SimpleError implements Serializable {

        private final String code;

        private final String field;

        public SimpleError(ObjectError error) {
            this(error.getCode(), error instanceof FieldError ? ((FieldError) error).getField() : null);
        }

        public SimpleError(String code, String field) {
            this.code = code;
            this.field = field;
        }

    }

}
