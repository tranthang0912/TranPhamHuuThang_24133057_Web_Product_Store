(() => {
    'use strict';
    const maxFileSize = 5 * 1024 * 1024;
    document.querySelectorAll('form[data-validate]').forEach(form => {
        const fields = [...form.querySelectorAll('input, select, textarea')];
        function validate(field) {
            field.setCustomValidity('');
            if (field.required && field.type !== 'file' && !field.value.trim()) {
                field.setCustomValidity('Vui lòng nhập thông tin này.');
            }
            if (field.name === 'username' && !/^[A-Za-z0-9_.-]{3,50}$/.test(field.value.trim())) {
                field.setCustomValidity('Tên đăng nhập gồm 3–50 ký tự: chữ không dấu, số, chấm, gạch dưới hoặc gạch ngang.');
            }
            if (field.dataset.match && field.value !== form.elements.namedItem(field.dataset.match)?.value) {
                field.setCustomValidity('Mật khẩu xác nhận không khớp.');
            }
            if (field.name === 'price' && !/^[0-9]{1,16}(\.[0-9]{1,2})?$/.test(field.value)) {
                field.setCustomValidity('Giá không âm, tối đa 16 chữ số phần nguyên và 2 chữ số thập phân.');
            }
            if (field.type === 'file' && field.files.length) {
                const file = field.files[0];
                if (!/\.(jpe?g|png|gif)$/i.test(file.name)) field.setCustomValidity('Chỉ chấp nhận ảnh JPG, PNG hoặc GIF.');
                if (file.size > maxFileSize) field.setCustomValidity('Ảnh không được vượt quá 5 MB.');
            }
            field.classList.toggle('is-invalid', !field.validity.valid);
        }
        fields.forEach(field => {
            field.addEventListener('input', () => {
                validate(field);
                if (field.name === 'password') fields.filter(f => f.dataset.match === 'password').forEach(validate);
            });
            field.addEventListener('change', () => validate(field));
        });
        // Keep native validation available when JavaScript is disabled.
        form.noValidate = true;
        form.addEventListener('submit', event => {
            fields.forEach(validate);
            form.classList.add('was-validated');
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                form.reportValidity();
                fields.find(field => !field.validity.valid)?.focus();
            }
        });
    });
    document.querySelectorAll('[data-error-field]').forEach(message => {
        document.querySelectorAll('input, select, textarea').forEach(field => {
            if (field.name === message.dataset.errorField) {
                field.classList.add('is-invalid');
                field.setAttribute('aria-invalid', 'true');
            }
        });
    });
})();
