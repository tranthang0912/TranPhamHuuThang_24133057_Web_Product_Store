<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="posted" value="${pageContext.request.method == 'POST'}"/>
<c:set var="formName" value="${posted ? param.categoryname : cate.categoryname}"/>
<c:set var="formStatus" value="${posted ? param.status : (editMode ? cate.status : 1)}"/>
<c:set var="formImage" value="${posted ? param.images : (fn:startsWith(cate.images, 'http') ? cate.images : '')}"/>
<form data-validate="true" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/category/${editMode ? 'update' : 'insert'}">
    <c:if test="${editMode}"><input type="hidden" name="categoryid" value="${cate.categoryid}"></c:if>
    <div class="field"><label for="categoryname">Tên danh mục</label><input class="form-control" id="categoryname" name="categoryname" maxlength="50" required value="<c:out value='${formName}'/>"></div>
    <c:if test="${not empty cate.images}">
        <c:url value="/image" var="localImage"><c:param name="fname" value="${cate.images}"/></c:url>
        <c:set var="currentImage" value="${fn:startsWith(cate.images, 'http') ? cate.images : localImage}"/>
        <p><img class="current-image" src="<c:out value='${currentImage}'/>" alt="Ảnh danh mục hiện tại"></p>
    </c:if>
    <div class="field"><label for="images">Link ảnh</label><input class="form-control" id="images" name="images" type="url" maxlength="500" placeholder="https://example.com/image.jpg" value="<c:out value='${formImage}'/>"></div>
    <div class="field"><label for="images1">Hoặc tải ảnh lên</label><input class="form-control" id="images1" name="images1" type="file" accept="image/jpeg,image/png,image/gif"><small>JPG, PNG hoặc GIF, tối đa 5 MB và 16 triệu điểm ảnh. Bỏ trống để giữ ảnh hiện tại.</small></div>
    <fieldset class="field"><legend class="fs-6">Trạng thái</legend><div class="radio-group">
        <label><input class="form-check-input" type="radio" name="status" value="1" required ${formStatus == '1' ? 'checked' : ''}> Hoạt động</label>
        <label><input class="form-check-input" type="radio" name="status" value="0" required ${formStatus == '0' ? 'checked' : ''}> Tạm khóa</label>
    </div></fieldset>
    <div class="actions"><button class="btn btn-primary" type="submit">Lưu danh mục</button><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/categories">Hủy bỏ</a></div>
</form>
