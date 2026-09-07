<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Quản lý sản phẩm - Product Store</title>
</head>
<body>
<main class="container admin-shell">
    <header class="admin-heading">
        <div><div class="admin-kicker">Khu vực quản trị</div><h1>Quản lý sản phẩm</h1><p class="muted">Theo dõi và cập nhật toàn bộ sản phẩm trong cửa hàng.</p></div>
        <div class="actions"><span class="result-count">${fn:length(products)} sản phẩm</span><a class="btn" href="${pageContext.request.contextPath}/admin/product/add">+ Thêm sản phẩm</a></div>
    </header>

    <c:if test="${param.created == '1'}"><p class="message success">Đã thêm sản phẩm thành công.</p></c:if>
    <c:if test="${param.updated == '1'}"><p class="message success">Đã cập nhật sản phẩm thành công.</p></c:if>
    <c:if test="${param.deleted == '1'}"><p class="message success">Đã xóa sản phẩm.</p></c:if>

    <div class="table-wrap">
        <table>
            <thead><tr><th>ID</th><th>Ảnh</th><th>Sản phẩm</th><th>Danh mục</th><th>Giá bán</th><th>Tồn kho</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
            <tbody>
            <c:forEach items="${products}" var="product">
                <tr>
                    <td>#${product.productid}</td>
                    <td>
                        <c:choose>
                            <c:when test="${empty product.images}"><div class="thumb image-placeholder">—</div></c:when>
                            <c:when test="${fn:startsWith(product.images, 'http://') || fn:startsWith(product.images, 'https://')}"><img class="thumb" src="<c:out value='${product.images}'/>" alt="<c:out value='${product.productname}'/>"></c:when>
                            <c:otherwise><c:url value="/image" var="imageUrl"><c:param name="fname" value="${product.images}"/></c:url><img class="thumb" src="${imageUrl}" alt="<c:out value='${product.productname}'/>"></c:otherwise>
                        </c:choose>
                    </td>
                    <td><span class="table-title"><c:out value="${product.productname}"/></span></td>
                    <td><span class="category-chip"><c:out value="${product.category.categoryname}"/></span></td>
                    <td><strong><fmt:formatNumber value="${product.price}" pattern="#,##0.##"/> ₫</strong></td>
                    <td>${product.quantity}</td>
                    <td><c:choose><c:when test="${product.status == 1}"><span class="status-badge status-active">Hoạt động</span></c:when><c:otherwise><span class="status-badge status-locked">Đang khóa</span></c:otherwise></c:choose></td>
                    <td>
                        <div class="table-actions">
                            <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/admin/product/edit?id=${product.productid}">Sửa thông tin</a>
                            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/admin/product/delete" onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này?')">
                                <input type="hidden" name="id" value="${product.productid}">
                                <button class="btn btn-danger btn-sm" type="submit">Xóa</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty products}"><tr><td colspan="8"><div class="empty-state"><strong>Chưa có sản phẩm </strong><span class="muted">Hãy thêm sản phẩm đầu tiên cho cửa hàng.</span></div></td></tr></c:if>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>
