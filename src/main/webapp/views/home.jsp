<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Product Store - Trang chủ</title>
</head>
<body>
<main class="container">
    <section class="hero">
        <div>
            <span class="eyebrow">Bộ sưu tập mới</span>
            <h1>Khám phá sản phẩm mới, chọn điều bạn thích.</h1>
            <p class="muted">Các sản phẩm mới nhất được cập nhật liên tục, trình bày rõ ràng để bạn dễ dàng xem và lựa chọn.</p>
            <div class="hero-actions">
                <a class="btn" href="${pageContext.request.contextPath}/product">Khám phá sản phẩm <span aria-hidden="true">→</span></a>
                <c:if test="${empty sessionScope.account}">
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/register">Tạo tài khoản</a>
                </c:if>
            </div>
        </div>
        <div class="hero-panel" aria-label="Thông tin cửa hàng">
            <div class="hero-stat"><span>Sản phẩm được sắp xếp theo thời gian cập nhật</span></div>
            <div class="hero-stat"><span>Dễ dàng nhận biết từng nhóm sản phẩm</span></div>
            <div class="hero-stat"><span>Giá, tồn kho và mô tả ngay trên trang chi tiết</span></div>
        </div>
    </section>

    <section>
        <div class="section-heading">
            <div>
                <span class="eyebrow">Vừa cập nhật</span>
                <h2>10 sản phẩm mới nhất</h2>
                <p class="muted">Những lựa chọn mới vừa xuất hiện tại cửa hàng.</p>
            </div>
            <a class="card-link" href="${pageContext.request.contextPath}/product">Xem tất cả <span aria-hidden="true">→</span></a>
        </div>

        <c:choose>
            <c:when test="${empty latestProducts}">
                <div class="empty-state">
                    <strong>Chưa có sản phẩm để hiển thị</strong>
                    <span class="muted">Sản phẩm đang hoạt động sẽ xuất hiện tại đây.</span>
                </div>
            </c:when>
            <c:otherwise>
                <div class="product-grid">
                    <c:forEach items="${latestProducts}" var="product">
                        <c:url value="/product/detail" var="detailUrl"><c:param name="id" value="${product.productid}"/></c:url>
                        <article class="product-card">
                            <a class="product-media" href="${detailUrl}">
                                <c:choose>
                                    <c:when test="${empty product.images}"><div class="image-placeholder">Chưa có ảnh sản phẩm</div></c:when>
                                    <c:when test="${fn:startsWith(product.images, 'http://') || fn:startsWith(product.images, 'https://')}">
                                        <img class="product-image" src="<c:out value='${product.images}'/>" alt="<c:out value='${product.productname}'/>">
                                    </c:when>
                                    <c:otherwise>
                                        <c:url value="/image" var="imageUrl"><c:param name="fname" value="${product.images}"/></c:url>
                                        <img class="product-image" src="${imageUrl}" alt="<c:out value='${product.productname}'/>">
                                    </c:otherwise>
                                </c:choose>
                            </a>
                            <div class="product-body">
                                <span class="category-chip"><c:out value="${product.category.categoryname}"/></span>
                                <h3><a href="${detailUrl}"><c:out value="${product.productname}"/></a></h3>
                                <div class="product-footer">
                                    <span class="price"><fmt:formatNumber value="${product.price}" pattern="#,##0.##"/> ₫</span>
                                    <a class="card-link" href="${detailUrl}">Chi tiết →</a>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
