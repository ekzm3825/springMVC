<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.diquest.disa4.exception.SiteException" %>
<%
    // Exception 이 Alert 요청인지 확인
    Object exception = request.getAttribute("exception");
    if (exception instanceof SiteException && ((SiteException) exception).getAlertType() != null)
        request.setAttribute("isAlertException", ((SiteException) exception).getAlertType() != null);
%>
<%-- #################################### --%>
<%-- Alert 페이지 --%>
<%-- #################################### --%>
<c:if test="${isAlertException}">
    <script>
        function redirect(url) {
            if (url == undefined || url == "") {

            } else if (url == "-1") {
                history.back();
            } else if (url == "-2") {
                self.close();
            } else {
                location.href = url;
            }
        }
    </script>

    <c:if test="${'ALERT' eq exception.alertType}">
        <script>
            window.alert("${message}");
            redirect("${exception.confirmUrl}");
        </script>
    </c:if>

    <c:if test="${'CONFIRM' eq exception.alertType}">
        <script>
            if (window.confirm("${message}") === true) {
                redirect("${exception.confirmUrl}");
            }
            else {
                redirect("${exception.cancelUrl}");
            }
        </script>
    </c:if>
    
    <c:if test="${'NONE' eq exception.alertType}">
        <script>
       		redirect("${exception.cancelUrl}");
       	</script>
    </c:if>
</c:if>

<%-- #################################### --%>
<%-- 일반 페이지 --%>
<%-- #################################### --%>
<c:if test="${!isAlertException}">
    <div class="msg_error">
        <span>
            ${message == null ? '오류가 발생했습니다. 다시 시도하세요.' : message} ${code == null ? '' : '(' + code + ')'}<br>
            <a href="javascript:history.back();">← 이전으로</a>
        </span>
    </div>
</c:if>
