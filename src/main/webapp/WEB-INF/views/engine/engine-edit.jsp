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
    <title>Modifica motore</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body data-back-guard-form="1"
      data-back-guard-fallback="<%= request.getContextPath() %>/engine/detail?ref=${engineRef}">

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 560px; width: 100%;">

        <h2 class="mb-3">Modifica motore</h2>

        <p class="text-muted mb-4">
            Aggiorna i dati tecnici del motore selezionato
        </p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                ${error}
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/engine/edit?ref=${engineRef}"
              method="post"
              enctype="multipart/form-data"
              class="form-click-guides">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Immagini motore</label>
                <div class="engine-images-edit-list mb-3" data-image-list>
                    <c:choose>
                        <c:when test="${not empty engineImages}">
                            <c:forEach var="image" items="${engineImages}">
                                <div class="engine-image-edit-card" data-image-card>
                                    <button type="button"
                                            class="engine-image-delete-btn"
                                            data-delete-image
                                            data-filename="${fn:escapeXml(image.filename)}"
                                            title="Elimina immagine"
                                            aria-label="Elimina immagine">X</button>
                                    <img src="<%= request.getContextPath() %>/uploads/engines/${engineRef}/${image.filename}"
                                         alt="Immagine motore"
                                         class="engine-image-edit-preview">
                                    <div class="small text-muted mt-1 text-truncate">
                                        <c:out value="${image.filename}" />
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted small">Nessuna immagine presente.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div data-deletions-container></div>
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Aggiungi nuove immagini</label>
                <div class="file-input-wrap">
                    <input type="file"
                           id="newImagesInput"
                           name="newImages"
                           class="file-input-native"
                           accept="image/*"
                           multiple>
                    <label for="newImagesInput" class="file-input-visual file-input-visual-label media-action-button mb-0">
                        Seleziona immagini
                    </label>
                </div>
                <div id="newImagesPreviewList" class="engine-images-edit-list mt-2 d-none"></div>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Riferimento</label>
                <input type="text" class="form-control" value="${engineRef}" readonly>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Nome cliente</label>
                <input type="text"
                       name="customer"
                       class="form-control"
                       value="${customer}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Codice motore</label>
                <input type="text"
                       name="engineCode"
                       class="form-control"
                       value="${engineCode}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold d-inline-flex align-items-center gap-2">
                    Stato
                    <c:set var="currentStatus" value="${empty status ? 'WAITING' : status}" />
                    <span id="statusColorDot"
                          class="status-color-dot
                          ${currentStatus == 'WAITING' ? 'status-dot-waiting' : ''}
                          ${currentStatus == 'WORK_IN_PROGRESS' ? 'status-dot-work' : ''}
                          ${currentStatus == 'READY' ? 'status-dot-ready' : ''}
                          ${currentStatus == 'DELIVERED' ? 'status-dot-delivered' : ''}"
                          aria-hidden="true"></span>
                </label>
                <select class="form-select" name="status" required>
                    <option value="WAITING" ${status == 'WAITING' ? 'selected' : ''}>In attesa</option>
                    <option value="WORK_IN_PROGRESS" ${status == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                    <option value="READY" ${status == 'READY' ? 'selected' : ''}>Pronto</option>
                    <option value="DELIVERED" ${status == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                </select>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Data ingresso</label>
                <input type="date"
                       name="intakeDate"
                       class="form-control"
                       value="${intakeDate}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Data consegna</label>
                <input type="date"
                       name="deliveryDate"
                       class="form-control"
                       value="${deliveryDate}">
                <div class="small text-muted mt-1">
                    Usata solo se lo stato è "Consegnato".
                </div>
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="note"
                          class="form-control"
                          rows="3">${note}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a href="<%= request.getContextPath() %>/engine/detail?ref=${engineRef}" class="btn btn-cancel-action w-50">
                    Annulla
                </a>
                <button type="submit" class="btn btn-save-action w-50">
                    Salva
                </button>
            </div>

        </form>

    </div>
</div>

<script>
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

    (() => {
        const list = document.querySelector('[data-image-list]');
        const deletionsContainer = document.querySelector('[data-deletions-container]');
        if (!list || !deletionsContainer) {
            return;
        }

        const addDeletionInput = (filename) => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'deleteFilenames';
            input.value = filename;
            deletionsContainer.appendChild(input);
        };

        list.addEventListener('click', function (event) {
            const button = event.target.closest('[data-delete-image]');
            if (!button) {
                return;
            }

            const filename = button.getAttribute('data-filename');
            if (!filename) {
                return;
            }

            const confirmed = window.confirm('Sei sicuro di voler eliminare questa immagine?');
            if (!confirmed) {
                return;
            }

            addDeletionInput(filename);

            const card = button.closest('[data-image-card]');
            if (card) {
                card.remove();
            }
        });
    })();

    (() => {
        const input = document.getElementById('newImagesInput');
        const previewList = document.getElementById('newImagesPreviewList');
        if (!input || !previewList) {
            return;
        }

        const renderPreview = () => {
            previewList.innerHTML = '';
            const files = input.files ? Array.from(input.files) : [];
            const hasImages = files.some((file) => file.type && file.type.startsWith('image/'));
            if (!hasImages) {
                previewList.classList.add('d-none');
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
                    renderPreview();
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
                previewList.appendChild(card);
            });

            previewList.classList.remove('d-none');
        };

        input.addEventListener('change', function () {
            renderPreview();
        });
    })();
</script>

</body>
</html>
