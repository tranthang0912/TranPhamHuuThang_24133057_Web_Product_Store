<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Chỉnh sửa sản phẩm - Product Store</title></head>
<body>
<c:set var="editMode" value="true"/>
<main class="container admin-shell">
    <section class="form-card">
        <h1>Chỉnh sửa sản phẩm</h1>
        <%@ include file="/common/admin/product-form.jsp" %>
    </section>
</main>
</body>
</html>
