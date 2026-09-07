<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title><c:out value="${product.productname}"/> - Product Store</title>
</head>
<body>
<main class="container">
    <div class="breadcrumb"><a href="${pageContext.request.contextPath}/home">Trang chủ</a><span>/</span><a href="${pageContext.request.contextPath}/product">Sản phẩm</a><span>/</span><span><c:out value="${product.productname}"/></span></div>
    <article class="detail-card">
        <div class="detail-layout">
            <div class="detail-media">
                <c:choose>
                    <c:when test="${empty product.images}"><div class="image-placeholder detail-image">Chưa có ảnh sản phẩm</div></c:when>
                    <c:when test="${fn:startsWith(product.images, 'http://') || fn:startsWith(product.images, 'https://')}">
                        <img class="detail-image" src="<c:out value='${product.images}'/>" alt="<c:out value='${product.productname}'/>">
                    </c:when>
                    <c:otherwise>
                        <c:url value="/image" var="imageUrl"><c:param name="fname" value="${product.images}"/></c:url>
                        <img class="detail-image" src="${imageUrl}" alt="<c:out value='${product.productname}'/>">
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="detail-content">
                <span class="category-chip"><c:out value="${product.category.categoryname}"/></span>
                <h1><c:out value="${product.productname}"/></h1>
                <p class="price"><fmt:formatNumber value="${product.price}" pattern="#,##0.##"/> ₫</p>
                <div class="stock-line"><span class="stock-dot"></span><span>Còn <strong>${product.quantity}</strong> sản phẩm trong kho</span></div>
                <div class="description-box">
                    <h3>Thông tin sản phẩm</h3>
                    <c:choose><c:when test="${empty product.description}"><p class="muted">Sản phẩm chưa có mô tả chi tiết.</p></c:when><c:otherwise><p><c:out value="${product.description}"/></p></c:otherwise></c:choose>
                </div>
                <div class="actions"><a class="btn" href="${pageContext.request.contextPath}/product">← Quay lại danh sách</a><a class="btn btn-secondary" href="${pageContext.request.contextPath}/home">Về trang chủ</a></div>
            </div>
        </div>
    </article>
</main>
</body>
</html>
