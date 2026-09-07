<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty fieldErrors}">
    <div class="container mt-4"><div class="alert alert-danger" role="alert">
        <strong>Vui lòng kiểm tra lại thông tin:</strong>
        <ul class="mb-0"><c:forEach items="${fieldErrors}" var="entry"><li data-error-field="<c:out value='${entry.key}'/>"><c:out value="${entry.value}"/></li></c:forEach></ul>
    </div></div>
</c:if>
<c:if test="${not empty error}"><div class="container mt-4"><p class="alert alert-danger" role="alert"><c:out value="${error}"/></p></div></c:if>
