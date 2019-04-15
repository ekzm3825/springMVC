package com.diquest.disa4.core.xss.lucy;

import com.navercorp.lucy.security.xss.servletfilter.defender.XssSaxFilterDefender;

public class Ic2XssSaxFilterDefender extends XssSaxFilterDefender {

    @Override
    public String doFilter(String value) {
        String clean = super.doFilter(value);
        return clean == null ? null : clean.replaceAll("&gt;", ">");
    }

}
