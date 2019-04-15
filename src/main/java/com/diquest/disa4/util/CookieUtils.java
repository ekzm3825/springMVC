package com.diquest.disa4.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {
	private CookieUtils() {

    }
	/**
     * 쿠기 생성
     *
     * @param response
     * @param name 쿠키이름
     * @param value 쿠키값 
     * @return 
     */
    public static void setCookie(HttpServletResponse response, String name, String value) {
    	setCookie(response, name, value, null);
    }
    /**
     * 쿠기 생성
     *
     * @param response 
     * @param name 쿠키이름
     * @param value 쿠키값 
     * @param maxAge 유효기간. 단위는 초. 
     * @return 
     */
    public static void setCookie(HttpServletResponse response, String name, String value, Integer maxAge) {
    	setCookie(response, name, value, maxAge, null);
    }
    
    /**
     * 쿠기 생성
     *
     * @param response 
     * @param name 쿠키이름
     * @param value 쿠키값 
     * @param maxAge 유효기간. 단위는 초. 
     * @param domain 도메인. 
     * @return 
     */
    public static void setCookie(HttpServletResponse response, String name, String value, Integer maxAge, String domain) {
    	Cookie cookie = new Cookie(name, value);
    	if(null != maxAge)
    		cookie.setMaxAge(maxAge); // 유효기간 설정
    	if(null != domain)
    		cookie.setDomain(domain);
    	response.addCookie(cookie);
    }
    
    /**
     * 쿠기 값 가져오기
     *
     * @param request
     * @param name 쿠키이름
     * @return cookie값.
     */
    public static String getCookie(HttpServletRequest request, String name) {
    	return getCookie(request, name, null);
    }
    
    /**
     * 쿠기 값 가져오기
     *
     * @param request
     * @param name 쿠키이름
     * @param defaultValue 기본 반환값.
     * @return cookie값.
     */
    public static String getCookie(HttpServletRequest request, String name, String defaultValue) {
    	Cookie[] cookies = request.getCookies();
    	for (Cookie e : cookies) {
    	   if(name.equals(e.getName()))
    		   return e.getValue();
    	}
    	return defaultValue;
    }
}
