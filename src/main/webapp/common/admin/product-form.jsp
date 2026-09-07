<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="posted" value="${pageContext.request.method == 'POST'}"/>
<c:set var="formName" value="${posted ? param.productname : product.productname}"/>
<c:set var="formPrice" value="${posted ? param.price : product.price}"/>
<c:set var="formQuantity" value="${posted ? param.quantity : (editMode ? product.quantity : 0)}"/>
<c:set var="formDescription" value="${posted ? param.description : product.description}"/>
<c:set var="formStatus" value="${posted ? param.status : (editMode ? product.status : 1)}"/>
<c:set var="formCategory" value="${posted ? param.categoryid : product.category.categoryid}"/>
<c:set var="formImage" value="${posted ? param.images : (fn:startsWith(product.images, 'http') ? product.images : '')}"/>
<form data-validate="true" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/product/${editMode ? 'update' : 'insert'}">
    <c:if test="${editMode}"><input type="hidden" name="productid" value="${product.productid}"></c:if>
    <div class="form-grid">
        <div class="field"><label for="productname">Tên sản phẩm</label><input class="form-control" id="productname" name="productname" maxlength="150" required value="<c:out value='${formName}'/>"></div>
        <div class="field"><label for="categoryid">Danh mục</label><select class="form-select" id="categoryid" name="categoryid" required>
            <option value="">-- Chọn danh mục --</option>
            <c:forEach items="${categories}" var="category"><option value="${category.categoryid}" ${fn:trim(formCategory) == fn:trim(category.categoryid) ? 'selected' : ''}><c:out value="${category.categoryname}"/></option></c:forEach>
        </select></div>
        <div class="field"><label for="price">Giá bán (VNĐ)</label><input class="form-control" id="price" name="price" type="number" min="0" max="9999999999999999.99" step="0.01" required value="<c:out value='${formPrice}'/>"></div>
        <div class="field"><label for="quantity">Số lượng</label><input class="form-control" id="quantity" name="quantity" type="number" min="0" max="2147483647" step="1" required value="<c:out value='${formQuantity}'/>"></div>
        <div class="field field-full"><label for="description">Mô tả</label><textarea class="form-control" id="description" name="description" maxlength="10000" rows="4"><c:out value="${formDescription}"/></textarea></div>
        <c:if test="${not empty product.images}"><div class="field field-full">
            <c:url value="/image" var="localImage"><c:param name="fname" value="${product.images}"/></c:url>
            <c:set var="currentImage" value="${fn:startsWith(product.images, 'http') ? product.images : localImage}"/>
            <img class="current-image" src="<c:out value='${currentImage}'/>" alt="Ảnh sản phẩm hiện tại">
        </div></c:if>
        <div class="field"><label for="images">Link ảnh</label><input class="form-control" id="images" name="images" type="url" maxlength="500" placeholder="https://example.com/image.jpg" value="<c:out value='${formImage}'/>"></div>
        <div class="field"><label for="images1">Hoặc tải ảnh lên</label><input class="form-control" id="images1" name="images1" type="file" accept="image/jpeg,image/png,image/gif"><small>JPG, PNG hoặc GIF, tối đa 5 MB và 16 triệu điểm ảnh. Bỏ trống để giữ ảnh hiện tại.</small></div>
        <fieldset class="field field-full"><legend class="fs-6">Trạng thái</legend><div class="radio-group">
            <label><input class="form-check-input" type="radio" name="status" value="1" required ${formStatus == '1' ? 'checked' : ''}> Hoạt động</label>
            <label><input class="form-check-input" type="radio" name="status" value="0" required ${formStatus == '0' ? 'checked' : ''}> Tạm khóa</label>
        </div></fieldset>
        <div class="actions field-full"><button class="btn btn-primary" type="submit">Lưu sản phẩm</button><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/products">Hủy bỏ</a></div>
    </div>
</form>
