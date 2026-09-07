<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đăng nhập - Product Store</title>
</head>
<body>
<main class="auth-page">
    <div class="auth-layout">
        <aside class="auth-intro">
            <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span>Product Store</span></a>
            <h2>Chào mừng bạn quay trở lại.</h2>
            <p>Đăng nhập để quản lý sản phẩm, danh mục và trải nghiệm cửa hàng đầy đủ hơn.</p>
        </aside>
        <section class="auth-card">
            <h1>Đăng nhập</h1>
            <p class="muted">Nhập tên đăng nhập hoặc email của bạn.</p>

            <c:if test="${param.activated == 'success'}"><p class="message success">Kích hoạt tài khoản thành công. Bạn có thể đăng nhập.</p></c:if>
            <c:if test="${param.reset == 'success'}"><p class="message success">Đặt lại mật khẩu thành công.</p></c:if>
            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/login">
                <div class="field">
                    <label for="identifier">Tên đăng nhập hoặc email</label>
                    <input class="form-control" id="identifier" type="text" name="identifier" maxlength="150" autocomplete="username" placeholder="Nhập username hoặc email" required value="<c:out value='${param.identifier}'/>">
                </div>
                <div class="field">
                    <label for="password">Mật khẩu</label>
                    <input class="form-control" id="password" type="password" maxlength="128" name="password" autocomplete="current-password" placeholder="Nhập mật khẩu" required>
                </div>
                <button class="btn" type="submit">Đăng nhập <span aria-hidden="true">→</span></button>
            </form>

            <c:if test="${not empty inactiveEmail}">
                <form data-validate="true" method="post" action="${pageContext.request.contextPath}/activate/resend">
                    <input type="hidden" name="email" maxlength="150" value="<c:out value='${inactiveEmail}'/>">
                    <button class="btn btn-secondary" type="submit">Gửi lại OTP kích hoạt</button>
                </form>
            </c:if>

            <div class="auth-links">
                <a href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
                <a href="${pageContext.request.contextPath}/home">Về trang chủ</a>
            </div>
            <p class="auth-footer">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register"><strong>Đăng ký ngay</strong></a></p>
        </section>
    </div>
</main>
</body>
</html>
