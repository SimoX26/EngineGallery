document.addEventListener('DOMContentLoaded', () => {
    const department = document.getElementById('warehouseDepartment');
    const category = document.getElementById('warehouseCategory');
    const injectorFields = document.querySelector('[data-injector-fields]');
    const rows = document.getElementById('warehouseCompatibilityRows');
    const add = document.getElementById('warehouseAddCompatibility');
    if (!department || !category || !rows) return;

    const updateCategory = () => {
        const selectedDepartment = department.value;
        Array.from(category.options).forEach(option => {
            if (!option.value) return;
            option.hidden = option.dataset.department !== selectedDepartment;
            option.disabled = option.hidden;
        });
        if (category.selectedOptions.length && category.selectedOptions[0].disabled) category.value = '';
        const isInjector = category.selectedOptions.length && category.selectedOptions[0].dataset.injector === 'true';
        injectorFields.hidden = !isInjector;
        injectorFields.querySelectorAll('input').forEach(input => {
            input.required = Boolean(isInjector);
            input.disabled = !isInjector;
        });
    };
    department.addEventListener('change', updateCategory);
    category.addEventListener('change', updateCategory);
    updateCategory();

    let nextIndex = rows.querySelectorAll('[data-compatibility-row]').length;
    add.addEventListener('click', () => {
        const row = document.createElement('div');
        row.className = 'd-flex flex-wrap gap-2 align-items-center mb-2';
        row.dataset.compatibilityRow = '';
        const code = document.createElement('input');
        code.className = 'form-control flex-grow-1';
        code.style.width = '12rem';
        code.name = 'engineCode';
        code.maxLength = 120;
        code.required = true;
        code.setAttribute('aria-label', 'Codice motore compatibile');
        const label = document.createElement('label');
        label.className = 'small text-nowrap';
        const verified = document.createElement('input');
        verified.type = 'checkbox';
        verified.name = 'verifiedIndex';
        verified.value = String(nextIndex++);
        label.append(verified, ' Verificata');
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'btn btn-outline-secondary btn-sm';
        remove.dataset.removeCompatibility = '';
        remove.setAttribute('aria-label', 'Rimuovi compatibilità');
        remove.textContent = '×';
        row.append(code, label, remove);
        rows.append(row);
        code.focus();
    });
    rows.addEventListener('click', event => {
        const remove = event.target.closest('[data-remove-compatibility]');
        if (remove && rows.querySelectorAll('[data-compatibility-row]').length > 1) remove.closest('[data-compatibility-row]').remove();
    });
    rows.closest('form').addEventListener('submit', () => {
        rows.querySelectorAll('[data-compatibility-row]').forEach((row, index) => {
            row.querySelector('[name="verifiedIndex"]').value = String(index);
        });
    });
});
