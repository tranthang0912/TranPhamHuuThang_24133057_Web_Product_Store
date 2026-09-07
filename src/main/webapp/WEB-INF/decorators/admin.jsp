<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><sitemesh:write property="title"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/vendor/simple-sidebar/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <sitemesh:write property="head"/>
</head>
<body>
<div class="d-flex" id="wrapper">
    <%@ include file="/common/admin/left.jsp" %>
    <div id="page-content-wrapper" class="d-flex flex-column">
        <%@ include file="/common/web/header.jsp" %>
        <%@ include file="/common/messages.jsp" %>
        <div class="site-content"><sitemesh:write property="body"/></div>
        <%@ include file="/common/web/footer.jsp" %>
    </div>
</div>
<script src="${pageContext.request.contextPath}/assets/vendor/bootstrap/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/vendor/simple-sidebar/scripts.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/validation.js"></script>
</body>
</html>
