package com.diquest.disa4.core.servlet;

import com.diquest.disa4.core.servlet.view.excel.CustomExcel2View;
import kr.qusi.spring.servlet.view.encoding.DefaultFilenameEncoder;
import kr.qusi.spring.servlet.view.encoding.FilenameEncoder;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class CustomExcel2ViewResolver extends UrlBasedViewResolver {

    /** 파일명 인코더 */
    private FilenameEncoder filenameEncoder = new DefaultFilenameEncoder();

    public CustomExcel2ViewResolver() {
        setViewClass(requiredViewClass());
    }

    @Override
    protected Class<?> requiredViewClass() {
        return CustomExcel2View.class;
    }

    @Override
    protected CustomExcel2View buildView(String viewName) throws Exception {
        CustomExcel2View view = (CustomExcel2View) super.buildView(viewName);
        view.setViewName(viewName);
        view.setPrefix(getPrefix());
        view.setSuffix(getSuffix());

        return view;
    }

    @Override
    public void setSuffix(String suffix) {
        super.setSuffix(suffix == null ? null : suffix.toLowerCase());
    }

    public FilenameEncoder getFilenameEncoder() {
        return filenameEncoder;
    }

    public void setFilenameEncoder(FilenameEncoder filenameEncoder) {
        this.filenameEncoder = filenameEncoder;
    }

}
