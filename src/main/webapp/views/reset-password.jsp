<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đặt lại mật khẩu - Product Store</title>
</head>
<body>
<main class="auth-page">
    <div class="auth-layout">
        <aside class="auth-intro">
            <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span>Product Store</span></a>
            <h2>Thiết lập mật khẩu mới.</h2>
            <p>Dùng OTP trong email để xác minh, sau đó chọn mật khẩu mới từ 8 đến 128 ký tự.</p>
        </aside>
        <section class="auth-card">
            <h1>Đặt lại mật khẩu</h1>
            <p class="muted">Nhập OTP và mật khẩu mới của bạn.</p>
            <c:if test="${param.sent == '1'}"><p class="message success">Nếu email thuộc tài khoản đang hoạt động, OTP đã được gửi.</p></c:if>
            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/reset-password">
                <div class="field"><label for="email">Email</label><input class="form-control" id="email" type="email" name="email" maxlength="150" autocomplete="email" required value="<c:out value='${email}'/>"></div>
                <div class="field"><label for="otp">Mã OTP</label><input class="form-control" class="otp-input" id="otp" type="text" name="otp" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" maxlength="6" placeholder="000000" required></div>
                <div class="field"><label for="password">Mật khẩu mới</label><input class="form-control" id="password" type="password" maxlength="128" name="password" minlength="8" autocomplete="new-password" placeholder="Từ 8 đến 128 ký tự" required></div>
                <div class="field"><label for="confirmPassword">Xác nhận mật khẩu mới</label><input class="form-control" id="confirmPassword" type="password" maxlength="128" name="confirmPassword" data-match="password" minlength="8" autocomplete="new-password" placeholder="Nhập lại mật khẩu" required></div>
                <button class="btn" type="submit">Cập nhật mật khẩu</button>
            </form>
            <div class="auth-links"><a href="${pageContext.request.contextPath}/forgot-password">Gửi lại OTP</a><a href="${pageContext.request.contextPath}/login">Đăng nhập</a></div>
        </section>
    </div>
</main>
</body>
</html>
