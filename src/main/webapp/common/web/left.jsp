<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="border-end bg-white" id="sidebar-wrapper" aria-label="Menu chính">
    <div class="sidebar-heading border-bottom bg-light">Product Store</div>
    <div class="list-group list-group-flush">
        <a class="list-group-item list-group-item-action list-group-item-light p-3" href="${pageContext.request.contextPath}/home">Trang chủ</a>
        <a class="list-group-item list-group-item-action list-group-item-light p-3" href="${pageContext.request.contextPath}/product">Sản phẩm</a>
        <a class="list-group-item list-group-item-action list-group-item-light p-3" href="${pageContext.request.contextPath}/profile">Hồ sơ cá nhân</a>
        <c:if test="${not empty sessionScope.account}"><a class="list-group-item list-group-item-action list-group-item-light p-3" href="${pageContext.request.contextPath}/admin/products">Quản lý cửa hàng</a></c:if>
    </div>
</aside>
