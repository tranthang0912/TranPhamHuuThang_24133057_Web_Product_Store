<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Hồ sơ cá nhân - Product Store</title></head>
<body>
<main class="container profile-page">
    <div class="page-heading">
        <span class="eyebrow">Tài khoản của bạn</span>
        <h1>Hồ sơ cá nhân</h1>
        <p class="muted">Cập nhật thông tin liên hệ và ảnh đại diện của bạn.</p>
    </div>
    <c:if test="${not empty success}"><p class="message success" role="status"><c:out value="${success}"/></p></c:if>
    <section class="profile-card">
        <aside class="profile-summary">
            <c:choose>
                <c:when test="${not empty sessionScope.account.images}">
                    <c:url value="/image" var="avatarUrl"><c:param name="fname" value="${sessionScope.account.images}"/></c:url>
                    <img class="avatar" src="<c:out value='${avatarUrl}'/>" alt="Ảnh đại diện hiện tại">
                </c:when>
                <c:otherwise><div class="avatar avatar-placeholder" aria-label="Chưa có ảnh đại diện">Ảnh đại diện</div></c:otherwise>
            </c:choose>
            <h2><c:out value="${sessionScope.account.fullname}"/></h2>
            <p class="muted">@<c:out value="${sessionScope.account.username}"/></p>
            <p class="muted"><c:out value="${sessionScope.account.email}"/></p>
        </aside>
        <form data-validate="true" class="profile-form" method="post" action="${pageContext.request.contextPath}/profile" enctype="multipart/form-data">
            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.profileCsrfToken}'/>">
            <div class="field">
                <label for="fullname">Họ và tên</label>
                <input class="form-control" id="fullname" name="fullname" type="text" maxlength="100" autocomplete="name" required value="<c:out value='${fullname}'/>">
            </div>
            <div class="field">
                <label for="phone">Số điện thoại</label>
                <input class="form-control" id="phone" name="phone" type="tel" maxlength="16" autocomplete="tel" pattern="[+]?[0-9]{9,15}" required aria-describedby="phone-help" value="<c:out value='${phone}'/>">
                <small id="phone-help">Từ 9 đến 15 chữ số, có thể bắt đầu bằng +. Ví dụ: 0912345678.</small>
            </div>
            <div class="field">
                <label for="images">Ảnh đại diện mới</label>
                <input class="form-control" id="images" name="images" type="file" accept="image/jpeg,image/png,image/gif" aria-describedby="image-help">
                <small id="image-help">JPG, PNG hoặc GIF; tối đa 5 MB và 16 triệu điểm ảnh. GIF sử dụng khung hình đầu tiên. Không chọn tệp để giữ ảnh hiện tại.</small>
            </div>
            <div class="actions">
                <button class="btn" type="submit">Lưu thay đổi</button>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/home">Về trang chủ</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
