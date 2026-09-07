<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Quản lý danh mục - Product Store</title></head>
<body>
<main class="container admin-shell">
    <header class="admin-heading">
        <div><div class="admin-kicker">Khu vực quản trị</div><h1>Quản lý danh mục</h1><p class="muted">Tổ chức sản phẩm theo từng nhóm rõ ràng.</p></div>
        <a class="btn" href="${pageContext.request.contextPath}/admin/category/add">+ Thêm danh mục</a>
    </header>

    <form data-validate="true" class="search-bar" method="get" action="${pageContext.request.contextPath}/admin/categories">
        <input class="form-control" type="search" name="keyword" maxlength="50" placeholder="Tìm theo tên danh mục..." value="<c:out value='${param.keyword}'/>">
        <button class="btn" type="submit">Tìm kiếm</button>
        <c:if test="${not empty param.keyword}"><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/categories">Xóa lọc</a></c:if>
    </form>

    <div class="table-wrap">
        <table>
            <thead><tr><th>STT</th><th>Ảnh</th><th>Tên danh mục</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <c:forEach items="${listcate}" var="cate" varStatus="stt">
                <tr>
                    <td>${stt.index + 1}</td>
                    <td><c:choose><c:when test="${empty cate.images}"><div class="thumb image-placeholder">—</div></c:when><c:when test="${fn:startsWith(cate.images, 'http')}"><img class="thumb" src="<c:out value='${cate.images}'/>" alt="<c:out value='${cate.categoryname}'/>"></c:when><c:otherwise><c:url value="/image" var="imgUrl"><c:param name="fname" value="${cate.images}"/></c:url><img class="thumb" src="${imgUrl}" alt="<c:out value='${cate.categoryname}'/>"></c:otherwise></c:choose></td>
                    <td><span class="table-title"><c:out value="${cate.categoryname}"/></span></td>
                    <td><c:choose><c:when test="${cate.status == 1}"><span class="status-badge status-active">Hoạt động</span></c:when><c:otherwise><span class="status-badge status-locked">Đang khóa</span></c:otherwise></c:choose></td>
                    <td><div class="table-actions"><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/admin/category/edit?id=${cate.categoryid}">Sửa</a><form data-validate="true" method="post" action="${pageContext.request.contextPath}/admin/category/delete" onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này?')"><input type="hidden" name="id" value="${cate.categoryid}"><button class="btn btn-danger btn-sm" type="submit">Xóa</button></form></div></td>
                </tr>
            </c:forEach>
            <c:if test="${empty listcate}"><tr><td colspan="5"><div class="empty-state"><strong>Không tìm thấy danh mục</strong><span class="muted">Thử từ khóa khác hoặc thêm danh mục mới.</span></div></td></tr></c:if>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
