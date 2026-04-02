<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Aggiunta motore</title>

    <!-- Bootstrap (se già usato nel progetto) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Stile globale Engine Gallery -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=7">
</head>
<body data-back-guard-form="1"
      data-back-guard-fallback="<%= request.getContextPath() %>/dashboard?navHome=1">

<!-- Navbar -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<!-- CONTENUTO PRINCIPALE -->
<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 520px; width: 100%;">

        <!-- Titolo -->
        <h2 class="mb-3">Aggiunta motore</h2>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                    ${error}
            </div>
        </c:if>

        <!-- Area upload -->
        <form id="uploadForm"
              action="<%= request.getContextPath() %>/upload"
              method="post"
              enctype="multipart/form-data">


            <div class="mb-4 text-start">
                <div class="small text-muted mt-2">
                    <span class="text-nowrap">
                        Nuovo riferimento:
                        <span class="badge bg-light text-dark border">${newEngineRef}</span>
                    </span>
                </div>
            </div>

            <!-- ENGINE REF nascosto (serve solo se nuovo motore) -->
            <input type="hidden" name="engineMode" value="new">
            <input type="hidden" name="engineRef" value="${engineRef}">

            <!-- UPLOAD -->
            <div class="mb-4">
                <label class="form-label fw-semibold">Immagini</label>
                <input type="file"
                       id="imagesInput"
                       name="images"
                       class="form-control"
                       accept="image/*"
                       multiple>
                <div id="imagesSelectionInfo" class="small text-muted mt-2">0 file selezionati</div>
            </div>

            <!-- CLIENTE -->
            <div class="mb-3">
                <label class="form-label fw-semibold">Nome cliente</label>
                <div class="position-relative">
                <input type="text"
                       id="customerInput"
                       name="customer"
                       class="form-control"
                       autocomplete="off"
                       list="customerOptions"
                       value="${customer}"
                       required>
                    <div id="customerSuggestions"
                         class="list-group position-absolute w-100 shadow-sm d-none"
                         style="z-index: 1050; max-height: 220px; overflow-y: auto;"></div>
                </div>
                <datalist id="customerOptions">
                    <c:forEach var="customerItem" items="${customers}">
                        <option value="${fn:escapeXml(customerItem.name)}"></option>
                    </c:forEach>
                </datalist>
            </div>

            <!-- CODICE MOTORE -->
            <div class="mb-3">
                <label class="form-label fw-semibold">Codice motore</label>
                <div class="position-relative">
                    <input type="text"
                           name="engineCode"
                           class="form-control"
                           value="${engineCode}"
                           required>
                </div>
            </div>

            <!-- STATO -->
            <div class="mb-3">
                <c:set var="currentStatus" value="${empty status ? 'WAITING' : status}" />
                <label class="form-label fw-semibold d-inline-flex align-items-center gap-2">
                    Stato
                    <span id="statusColorDot"
                          class="status-color-dot
                          ${currentStatus == 'WAITING' ? 'status-dot-waiting' : ''}
                          ${currentStatus == 'WORK_IN_PROGRESS' ? 'status-dot-work' : ''}
                          ${currentStatus == 'READY' ? 'status-dot-ready' : ''}
                          ${currentStatus == 'DELIVERED' ? 'status-dot-delivered' : ''}"
                          aria-hidden="true"></span>
                </label>
                <div class="position-relative">
                    <select class="form-select"
                            name="status"
                            required>
                        <option value="WAITING" ${status == 'WAITING' ? 'selected' : ''}>In attesa</option>
                        <option value="WORK_IN_PROGRESS" ${status == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                        <option value="READY" ${status == 'READY' ? 'selected' : ''}>Pronto</option>
                        <option value="DELIVERED" ${status == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                    </select>
                </div>
            </div>

            <!-- NOTE -->
            <div class="mb-4">
                <label class="form-label fw-semibold">Note</label>
                <div class="position-relative">
                    <textarea name="note"
                              class="form-control"
                              rows="3">${note}</textarea>
                </div>
            </div>

            <button type="submit" class="btn-engine w-100">
                Salva
            </button>

        </form>

    </div>
</div>

</body>


<script>
    const MAX_FILE_SIZE = 100 * 1024 * 1024;     // 100 MB
    const MAX_TOTAL_SIZE = 800 * 1024 * 1024;    // 800 MB
    const imagesInput = document.getElementById('imagesInput');
    const imagesSelectionInfo = document.getElementById('imagesSelectionInfo');
    const customerInput = document.getElementById('customerInput');
    const customerSuggestions = document.getElementById('customerSuggestions');
    const customerNames = Array.from(document.querySelectorAll('#customerOptions option'))
        .map((option) => option.value)
        .filter((name) => name && name.trim().length > 0);
    const normalizeText = (value) => (value || '')
        .toString()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();

    function hideCustomerSuggestions() {
        if (customerSuggestions) {
            customerSuggestions.classList.add('d-none');
            customerSuggestions.innerHTML = '';
        }
    }

    function renderCustomerSuggestions(query) {
        if (!customerInput || customerInput.readOnly || !customerSuggestions) {
            return;
        }

        const normalized = normalizeText(query);
        if (normalized.length === 0) {
            hideCustomerSuggestions();
            return;
        }

        const matches = customerNames
            .filter((name) => normalizeText(name).includes(normalized))
            .slice(0, 8);

        if (matches.length === 0) {
            customerSuggestions.innerHTML = '';
            const empty = document.createElement('div');
            empty.className = 'list-group-item text-muted';
            empty.textContent = 'Nessun cliente corrisponde alla ricerca.';
            customerSuggestions.appendChild(empty);
            customerSuggestions.classList.remove('d-none');
            return;
        }

        customerSuggestions.innerHTML = '';
        matches.forEach((name) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'list-group-item list-group-item-action';
            button.textContent = name;
            button.addEventListener('click', () => {
                customerInput.value = name;
                hideCustomerSuggestions();
            });
            customerSuggestions.appendChild(button);
        });

        customerSuggestions.classList.remove('d-none');
    }

    if (imagesInput && imagesSelectionInfo) {
        imagesInput.addEventListener('change', function () {
            const selectedFiles = imagesInput.files ? imagesInput.files.length : 0;
            imagesSelectionInfo.textContent = selectedFiles + ' file selezionati';
        });
    }

    document.getElementById('uploadForm').addEventListener('submit', function (event) {
        if (!imagesInput || !imagesInput.files) {
            return;
        }

        let total = 0;
        for (const file of imagesInput.files) {
            if (file.size > MAX_FILE_SIZE) {
                event.preventDefault();
                alert('Il file "' + file.name + '" supera 100MB.');
                return;
            }
            total += file.size;
        }

        if (total > MAX_TOTAL_SIZE) {
            event.preventDefault();
            alert('La dimensione totale delle immagini supera 800MB.');
        }
    });

    if (customerInput && !customerInput.readOnly) {
        customerInput.addEventListener('input', () => renderCustomerSuggestions(customerInput.value));
        customerInput.addEventListener('blur', () => {
            setTimeout(hideCustomerSuggestions, 120);
        });
    }

    (() => {
        const select = document.querySelector('select[name="status"]');
        const dot = document.getElementById('statusColorDot');
        if (!select || !dot) {
            return;
        }

        const styleByStatus = {
            WAITING: 'status-dot-waiting',
            WORK_IN_PROGRESS: 'status-dot-work',
            READY: 'status-dot-ready',
            DELIVERED: 'status-dot-delivered'
        };

        const updateDot = () => {
            dot.classList.remove('status-dot-waiting', 'status-dot-work', 'status-dot-ready', 'status-dot-delivered');
            const value = select.value;
            if (styleByStatus[value]) {
                dot.classList.add(styleByStatus[value]);
            }
        };

        select.addEventListener('change', updateDot);
        updateDot();
    })();
</script>
</html>
