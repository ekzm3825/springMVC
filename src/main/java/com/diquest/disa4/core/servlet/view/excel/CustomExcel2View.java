package com.diquest.disa4.core.servlet.view.excel;

import kr.qusi.spring.servlet.view.excel.Excel2View;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

public class CustomExcel2View extends Excel2View {

    @Override
    protected String getBasename(HttpServletRequest request, Bundle bundle) throws FileNotFoundException {
        String filename = bundle.getFilename() != null ? bundle.getFilename() : getTemplate(request, bundle).getFilename();

        return filename;
    }

}
