<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Đăng ký - Product Store</title>
</head>
<body>
<main class="auth-page">
    <div class="auth-layout">
        <aside class="auth-intro">
            <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span>Product Store</span></a>
            <h2>Tạo tài khoản trong vài bước đơn giản.</h2>
            <p>Sau khi đăng ký, mã OTP gồm 6 chữ số sẽ được gửi đến email để xác nhận tài khoản.</p>
        </aside>
        <section class="auth-card">
            <h1>Đăng ký tài khoản</h1>
            <p class="muted">Điền đầy đủ thông tin bên dưới để tiếp tục.</p>
            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/register">
                <div class="field">
                    <label for="username">Tên đăng nhập</label>
                    <input class="form-control" id="username" type="text" name="username" minlength="3" pattern="[A-Za-z0-9_.\-]{3,50}" maxlength="50" autocomplete="username" placeholder="Ví dụ: nguyenvana" required value="<c:out value='${param.username}'/>">
                </div>
                <div class="field">
                    <label for="fullname">Họ và tên</label>
                    <input class="form-control" id="fullname" type="text" name="fullname" maxlength="100" autocomplete="name" placeholder="Nguyễn Văn A" required value="<c:out value='${param.fullname}'/>">
                </div>
                <div class="field">
                    <label for="email">Email</label>
                    <input class="form-control" id="email" type="email" name="email" maxlength="150" autocomplete="email" placeholder="name@example.com" required value="<c:out value='${param.email}'/>">
                </div>
                <div class="field">
                    <label for="password">Mật khẩu</label>
                    <input class="form-control" id="password" type="password" maxlength="128" name="password" minlength="8" autocomplete="new-password" placeholder="Từ 8 đến 128 ký tự" required>
                </div>
                <div class="field">
                    <label for="confirmPassword">Xác nhận mật khẩu</label>
                    <input class="form-control" id="confirmPassword" type="password" maxlength="128" name="confirmPassword" data-match="password" minlength="8" autocomplete="new-password" placeholder="Nhập lại mật khẩu" required>
                </div>
                <button class="btn" type="submit">Đăng ký và nhận OTP <span aria-hidden="true">→</span></button>
            </form>
            <p class="auth-footer">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login"><strong>Đăng nhập</strong></a></p>
        </section>
    </div>
</main>
</body>
</html>
