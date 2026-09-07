<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Tất cả sản phẩm - Product Store</title>
</head>
<body>
<main class="container">
    <header class="page-heading">
        <div>
            <span class="eyebrow">Danh mục sản phẩm</span>
            <c:choose>
                <c:when test="${not empty selectedCategory}">
                    <h1><c:out value="${selectedCategory.categoryname}"/></h1>
                    <p class="muted">Các sản phẩm thuộc danh mục <c:out value="${selectedCategory.categoryname}"/>.</p>
                </c:when>
                <c:otherwise>
                    <h1>Tất cả sản phẩm</h1>
                    <p class="muted">Khám phá toàn bộ sản phẩm đang có tại cửa hàng.</p>
                </c:otherwise>
            </c:choose>
        </div>
        <span class="result-count">${totalItems} sản phẩm</span>
    </header>

    <form data-validate="true" method="get" action="${pageContext.request.contextPath}/product">
        <label for="category">Chọn danh mục:</label>
        <select class="form-select" id="category" name="category">
            <c:choose>
                <c:when test="${selectedCategoryId == 0}"><option value="0" selected>Tất cả danh mục</option></c:when>
                <c:otherwise><option value="0">Tất cả danh mục</option></c:otherwise>
            </c:choose>
            <c:forEach items="${categories}" var="category">
                <c:choose>
                    <c:when test="${category.categoryid == selectedCategoryId}">
                        <option value="${category.categoryid}" selected><c:out value="${category.categoryname}"/></option>
                    </c:when>
                    <c:otherwise>
                        <option value="${category.categoryid}"><c:out value="${category.categoryname}"/></option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </select>
        <button type="submit">Xem sản phẩm</button>
    </form>

    <c:choose>
        <c:when test="${empty products}">
            <div class="empty-state"><strong>Chưa có sản phẩm </strong><span class="muted">Vui lòng quay lại sau khi cửa hàng cập nhật.</span></div>
        </c:when>
        <c:otherwise>
            <div class="product-grid">
                <c:forEach items="${products}" var="product">
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

            <c:if test="${totalPages > 1}">
                <nav class="pagination" aria-label="Phân trang sản phẩm">
                    <c:if test="${currentPage > 1}">
                        <c:url value="/product" var="previousPageUrl">
                            <c:param name="page" value="${currentPage - 1}"/>
                            <c:param name="category" value="${selectedCategoryId}"/>
                        </c:url>
                        <a href="${previousPageUrl}" aria-label="Trang trước">←</a>
                    </c:if>
                    <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                        <c:url value="/product" var="pageUrl">
                            <c:param name="page" value="${pageNumber}"/>
                            <c:param name="category" value="${selectedCategoryId}"/>
                        </c:url>
                        <c:choose>
                            <c:when test="${pageNumber == currentPage}"><span class="active" aria-current="page">${pageNumber}</span></c:when>
                            <c:otherwise><a href="${pageUrl}">${pageNumber}</a></c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages}">
                        <c:url value="/product" var="nextPageUrl">
                            <c:param name="page" value="${currentPage + 1}"/>
                            <c:param name="category" value="${selectedCategoryId}"/>
                        </c:url>
                        <a href="${nextPageUrl}" aria-label="Trang sau">→</a>
                    </c:if>
                </nav>
            </c:if>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
