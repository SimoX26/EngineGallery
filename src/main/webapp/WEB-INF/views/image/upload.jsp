<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Aggiunta motore</title>

    <!-- Bootstrap (se già usato nel progetto) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Stile globale Engine Gallery -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=12">
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
              class="form-click-guides"
              action="<%= request.getContextPath() %>/upload"
              method="post"
              enctype="multipart/form-data">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">


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
                <div class="file-input-wrap">
                    <input type="file"
                           id="imagesInput"
                           name="images"
                           class="file-input-native"
                           accept="image/*"
                           multiple>
                    <label for="imagesInput" class="file-input-visual file-input-visual-label media-action-button mb-0">
                        Seleziona immagini
                    </label>
                </div>
                <div id="iosCameraFlowWrap" class="mt-2 d-none">
                    <button type="button" id="iosCameraFlowOpenBtn" class="media-action-button">
                        Fotocamera RML
                    </button>
                </div>
                <input type="file"
                       id="iosCameraCaptureInput"
                       class="d-none"
                       accept="image/*"
                       capture="environment">
                <div id="imagesPreviewList" class="engine-images-edit-list engine-form-image-previews mt-2 d-none"></div>
            </div>

            <!-- CLIENTE -->
            <div class="mb-3">
                <label class="form-label fw-semibold">Nome cliente</label>
                <div class="position-relative">
                <input type="text"
                       id="customerInput"
                       name="customer"
                       class="form-control"
                       autocomplete="new-password"
                       autocorrect="off"
                       autocapitalize="none"
                       spellcheck="false"
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

            <button type="submit" class="btn btn-save-action w-100">
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
    const imagesPreviewList = document.getElementById('imagesPreviewList');
    const iosCameraFlowWrap = document.getElementById('iosCameraFlowWrap');
    const iosCameraFlowOpenBtn = document.getElementById('iosCameraFlowOpenBtn');
    const iosCameraCaptureInput = document.getElementById('iosCameraCaptureInput');
    const customerInput = document.getElementById('customerInput');
    const customerSuggestions = document.getElementById('customerSuggestions');
    const customerNames = Array.from(document.querySelectorAll('#customerOptions option'))
        .map((option) => option.value)
        .filter((name) => name && name.trim().length > 0);
    let accumulatedSelectedFiles = [];
    let iosSessionCapturedFiles = [];
    const isIOSDevice = /iPad|iPhone|iPod/.test(navigator.userAgent)
        || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
    const isStandalonePwa = window.matchMedia && window.matchMedia('(display-mode: standalone)').matches;
    const isIosSafariOrPwa = isIOSDevice && (isStandalonePwa || /Safari/i.test(navigator.userAgent));
    const normalizeText = (value) => (value || '')
        .toString()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
    const fileKey = (file) => [file.name, file.size, file.lastModified, file.type].join('::');

    function mergeFilesDedup(baseFiles, newFiles) {
        const byKey = new Map();
        baseFiles.forEach((file) => byKey.set(fileKey(file), file));
        newFiles.forEach((file) => byKey.set(fileKey(file), file));
        return Array.from(byKey.values());
    }

    function buildFileList(files) {
        if (typeof DataTransfer === 'undefined') {
            return null;
        }
        const transfer = new DataTransfer();
        files.forEach((file) => transfer.items.add(file));
        return transfer.files;
    }

    function ensureIosCameraCollectorModal() {
        let modal = document.getElementById('iosCameraCollectorModal');
        if (modal) {
            return modal;
        }

        modal = document.createElement('div');
        modal.id = 'iosCameraCollectorModal';
        modal.className = 'd-none camera-rml-overlay';
        modal.innerHTML = `
            <div class="camera-rml-sheet-wrap">
                <div class="camera-rml-sheet card-base">
                    <div class="camera-rml-sheet-header">
                        <strong class="camera-rml-title">Fotocamera RML</strong>
                        <span id="iosCollectorCount" class="camera-rml-counter">0 foto</span>
                    </div>
                    <div class="camera-rml-sheet-body">
                        <div id="iosCollectorPreviewList" class="engine-images-edit-list d-none"></div>
                        <p id="iosCollectorEmpty" class="small text-muted mb-0">Nessuna foto scattata in questa sessione.</p>
                    </div>
                    <div class="camera-rml-sheet-actions">
                        <button type="button" id="iosCollectorCaptureBtn" class="btn btn-outline-secondary camera-rml-action-btn">Scatta un'altra foto</button>
                        <button type="button" id="iosCollectorDoneBtn" class="btn-engine camera-rml-action-btn">Fatto</button>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        return modal;
    }

    function renderIosCollectorPreview() {
        const modal = ensureIosCameraCollectorModal();
        const list = modal.querySelector('#iosCollectorPreviewList');
        const empty = modal.querySelector('#iosCollectorEmpty');
        const count = modal.querySelector('#iosCollectorCount');
        if (!list || !empty || !count) {
            return;
        }

        list.innerHTML = '';
        const files = iosSessionCapturedFiles.filter((file) => file.type && file.type.startsWith('image/'));
        count.textContent = files.length + (files.length === 1 ? ' foto' : ' foto');
        if (files.length === 0) {
            list.classList.add('d-none');
            empty.classList.remove('d-none');
            return;
        }

        empty.classList.add('d-none');
        files.forEach((file) => {
            const card = document.createElement('div');
            card.className = 'engine-image-edit-card';

            const img = document.createElement('img');
            img.className = 'engine-image-edit-preview';
            img.alt = file.name;
            const objectUrl = URL.createObjectURL(file);
            img.src = objectUrl;
            img.addEventListener('load', () => URL.revokeObjectURL(objectUrl));

            const filename = document.createElement('div');
            filename.className = 'small text-muted mt-1 text-truncate';
            filename.textContent = file.name;

            card.appendChild(img);
            card.appendChild(filename);
            list.appendChild(card);
        });
        list.classList.remove('d-none');
    }

    function openIosCollector() {
        iosSessionCapturedFiles = [];
        const modal = ensureIosCameraCollectorModal();
        modal.classList.remove('d-none');
        document.body.classList.add('modal-open');
        renderIosCollectorPreview();
    }

    function closeIosCollector() {
        const modal = ensureIosCameraCollectorModal();
        modal.classList.add('d-none');
        document.body.classList.remove('modal-open');
    }

    function commitIosCollectorToMainInput() {
        const fromInput = imagesInput && imagesInput.files ? Array.from(imagesInput.files) : [];
        const merged = mergeFilesDedup(fromInput, iosSessionCapturedFiles);
        const nextFiles = buildFileList(merged);
        if (!nextFiles || !imagesInput) {
            return;
        }
        imagesInput.files = nextFiles;
        accumulatedSelectedFiles = Array.from(imagesInput.files || []);
        renderSelectedImagesPreview(imagesInput, imagesPreviewList);
    }

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

    function renderSelectedImagesPreview(input, container) {
        container.innerHTML = '';
        const files = input.files ? Array.from(input.files) : [];
        const hasImages = files.some((file) => file.type && file.type.startsWith('image/'));
        if (!hasImages) {
            container.classList.add('d-none');
            return;
        }

        files.forEach((file, index) => {
            if (!file.type || !file.type.startsWith('image/')) {
                return;
            }

            const card = document.createElement('div');
            card.className = 'engine-image-edit-card';

            const removeButton = document.createElement('button');
            removeButton.type = 'button';
            removeButton.className = 'engine-image-delete-btn';
            removeButton.textContent = 'X';
            removeButton.setAttribute('aria-label', 'Rimuovi immagine selezionata');
            removeButton.title = 'Rimuovi';
            removeButton.addEventListener('click', function () {
                const currentFiles = input.files ? Array.from(input.files) : [];
                const transfer = new DataTransfer();
                currentFiles.forEach((currentFile, currentIndex) => {
                    if (currentIndex !== index) {
                        transfer.items.add(currentFile);
                    }
                });
                input.files = transfer.files;
                accumulatedSelectedFiles = Array.from(input.files || []);
                renderSelectedImagesPreview(input, container);
            });

            const img = document.createElement('img');
            img.className = 'engine-image-edit-preview';
            img.alt = file.name;
            const objectUrl = URL.createObjectURL(file);
            img.src = objectUrl;
            img.addEventListener('load', () => URL.revokeObjectURL(objectUrl));

            const filename = document.createElement('div');
            filename.className = 'small text-muted mt-1 text-truncate';
            filename.textContent = file.name;

            card.appendChild(removeButton);
            card.appendChild(img);
            card.appendChild(filename);
            container.appendChild(card);
        });

        container.classList.remove('d-none');
    }

    if (imagesInput && imagesPreviewList) {
        imagesInput.addEventListener('change', function () {
            const pickedFiles = imagesInput.files ? Array.from(imagesInput.files) : [];
            if (pickedFiles.length > 0) {
                const merged = mergeFilesDedup(accumulatedSelectedFiles, pickedFiles);
                const mergedFiles = buildFileList(merged);
                if (mergedFiles) {
                    imagesInput.files = mergedFiles;
                }
            }
            accumulatedSelectedFiles = Array.from(imagesInput.files || []);
            renderSelectedImagesPreview(imagesInput, imagesPreviewList);
        });
    }

    if (isIosSafariOrPwa && iosCameraFlowWrap && iosCameraFlowOpenBtn && iosCameraCaptureInput && imagesInput) {
        iosCameraFlowWrap.classList.remove('d-none');

        iosCameraFlowOpenBtn.addEventListener('click', function () {
            openIosCollector();
        });

        document.addEventListener('click', function (event) {
            const modal = document.getElementById('iosCameraCollectorModal');
            if (!modal || modal.classList.contains('d-none')) {
                return;
            }

            const captureBtn = event.target.closest('#iosCollectorCaptureBtn');
            if (captureBtn) {
                iosCameraCaptureInput.click();
                return;
            }

            const doneBtn = event.target.closest('#iosCollectorDoneBtn');
            if (doneBtn) {
                commitIosCollectorToMainInput();
                closeIosCollector();
            }
        });

        iosCameraCaptureInput.addEventListener('change', function () {
            const files = iosCameraCaptureInput.files ? Array.from(iosCameraCaptureInput.files) : [];
            const onlyImages = files.filter((file) => file.type && file.type.startsWith('image/'));
            if (onlyImages.length > 0) {
                iosSessionCapturedFiles = mergeFilesDedup(iosSessionCapturedFiles, onlyImages);
                renderIosCollectorPreview();
            }
            iosCameraCaptureInput.value = '';
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
