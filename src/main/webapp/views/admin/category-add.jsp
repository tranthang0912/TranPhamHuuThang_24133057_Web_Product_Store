<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Thêm danh mục - Product Store</title></head>
<body>
<c:set var="editMode" value="false"/>
<main class="container admin-shell">
    <section class="form-card">
        <h1>Thêm danh mục</h1>
        <%@ include file="/common/admin/category-form.jsp" %>
    </section>
</main>
</body>
</html>
