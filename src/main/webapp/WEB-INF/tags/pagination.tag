<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="ref" required="true" type="com.diquest.disa4.util.Pagination" %>

<c:if test="${ref.valid}">
    <ul class="paging">
        <li><a href="javascript:void(0);" class="first" onclick="${ref.callJsFunc(ref.firstPage)}">&nbsp;</a></li>
        <li><a href="javascript:void(0);" class="prev" onclick="${ref.callJsFunc(ref.prevBlockPage)}">&nbsp;</a></li>
        <c:forEach begin="0" end="${ref.pageBlock - 1}" varStatus="status">
            <c:set var="item" value="${ref.startBlockPage + status.index}"/>
            <c:if test="${item <= ref.lastPage}">
                <li><a href="javascript:void(0);" <c:if test="${item == ref.page}">class='on'</c:if> onclick="${ref.callJsFunc(item)}">${item}</a></li>
            </c:if>
        </c:forEach>
        <li><a href="javascript:void(0);" class="next" onclick="${ref.callJsFunc(ref.nextBlockPage)}">&nbsp;</a></li>
        <li><a href="javascript:void(0);" class="last" onclick="${ref.callJsFunc(ref.lastPage)}">&nbsp;</a></li>
    </ul>
    ${ref.jsFunc}
    ${ref.form}
</c:if>

