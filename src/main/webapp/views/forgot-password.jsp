<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Quên mật khẩu - Product Store</title>
</head>
<body>
<main class="auth-page">
    <div class="auth-layout">
        <aside class="auth-intro">
            <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span>Product Store</span></a>
            <h2>Lấy lại quyền truy cập tài khoản.</h2>
            <p>Chúng tôi sẽ gửi mã OTP đến email đã đăng ký để xác nhận chính chủ.</p>
        </aside>
        <section class="auth-card">
            <h1>Quên mật khẩu?</h1>
            <p class="muted">Nhập email đã đăng ký để nhận mã xác nhận.</p>
            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/forgot-password">
                <div class="field"><label for="email">Email đăng ký</label><input class="form-control" id="email" type="email" name="email" maxlength="150" autocomplete="email" placeholder="name@example.com" required value="<c:out value='${email}'/>"></div>
                <button class="btn" type="submit">Gửi mã OTP <span aria-hidden="true">→</span></button>
            </form>
            <p class="auth-footer"><a href="${pageContext.request.contextPath}/login">← Quay lại đăng nhập</a></p>
        </section>
    </div>
</main>
</body>
</html>
