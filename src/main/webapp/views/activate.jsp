<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Kích hoạt tài khoản - Product Store</title>
</head>
<body>
<main class="auth-page">
    <div class="auth-layout">
        <aside class="auth-intro">
            <a class="auth-brand" href="${pageContext.request.contextPath}/home"><span>Product Store</span></a>
            <h2>Chỉ còn một bước để hoàn tất.</h2>
            <p>Kiểm tra hộp thư đến và nhập mã OTP gồm 6 chữ số. Mã có hiệu lực trong 5 phút.</p>       
        </aside>
        <section class="auth-card">
            <h1>Kích hoạt tài khoản</h1>
            <p class="muted">Nhập email và mã OTP đã nhận.</p>
            <c:if test="${param.sent == '1'}"><p class="message success">OTP đã được gửi. Mã có hiệu lực trong 5 phút.</p></c:if>
            <c:if test="${not empty message}"><p class="message success"><c:out value="${message}"/></p></c:if>
            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/activate">
                <div class="field"><label for="email">Email</label><input class="form-control" id="email" type="email" name="email" maxlength="150" autocomplete="email" required value="<c:out value='${email}'/>"></div>
                <div class="field"><label for="otp">Mã OTP</label><input class="form-control" class="otp-input" id="otp" type="text" name="otp" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" maxlength="6" placeholder="000000" required></div>
                <button class="btn" type="submit">Xác nhận kích hoạt</button>
            </form>
            <form data-validate="true" method="post" action="${pageContext.request.contextPath}/activate/resend">
                <input type="hidden" name="email" maxlength="150" value="<c:out value='${email}'/>">
                <button class="btn btn-secondary" type="submit">Gửi lại mã OTP</button>
            </form>
            <p class="auth-footer"><a href="${pageContext.request.contextPath}/login">← Quay lại đăng nhập</a></p>
        </section>
    </div>
</main>
</body>
</html>
