<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Modifica motore</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
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
        <c:if test="${imagesUpdated}">
            <div class="alert alert-success text-start" role="alert">
                Modifiche immagini salvate correttamente.
            </div>
        </c:if>
        <c:if test="${imageError}">
            <div class="alert alert-danger text-start" role="alert">
                Errore durante il salvataggio delle immagini.
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/engine/edit?ref=${engineRef}"
              method="post"
              class="form-click-guides">

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
                <label class="form-label fw-semibold">Stato</label>
                <c:set var="currentStatus" value="${empty status ? 'WAITING' : status}" />
                <select class="form-select" name="status" required>
                    <option value="WAITING" ${status == 'WAITING' ? 'selected' : ''}>In attesa</option>
                    <option value="WORK_IN_PROGRESS" ${status == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                    <option value="READY" ${status == 'READY' ? 'selected' : ''}>Pronto</option>
                    <option value="DELIVERED" ${status == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                </select>
                <div class="status-select-preview mt-2">
                    <span class="small text-muted">Colore stato:</span>
                    <span id="statusPreviewBadge"
                          class="badge-status ms-2
                          ${currentStatus == 'WAITING' ? 'status-stoccato' : ''}
                          ${currentStatus == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                          ${currentStatus == 'READY' ? 'status-ready' : ''}
                          ${currentStatus == 'DELIVERED' ? 'status-consegnato' : ''}">
                        <c:choose>
                            <c:when test="${currentStatus == 'WAITING'}">In attesa</c:when>
                            <c:when test="${currentStatus == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                            <c:when test="${currentStatus == 'READY'}">Pronto</c:when>
                            <c:when test="${currentStatus == 'DELIVERED'}">Consegnato</c:when>
                            <c:otherwise>${currentStatus}</c:otherwise>
                        </c:choose>
                    </span>
                </div>
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

            <div class="mb-4 text-start">
                <button type="button"
                        class="btn btn-outline-primary w-100"
                        data-bs-toggle="modal"
                        data-bs-target="#engineImagesModal">
                    Modifica immagini
                </button>
            </div>

            <div class="d-flex gap-2">
                <a href="<%= request.getContextPath() %>/engine/detail?ref=${engineRef}" class="btn btn-outline-secondary w-50">
                    Annulla
                </a>
                <button type="submit" class="btn-engine w-50">
                    Salva modifiche
                </button>
            </div>

        </form>

    </div>
</div>

<script>
    (() => {
        const select = document.querySelector('select[name="status"]');
        const badge = document.getElementById('statusPreviewBadge');
        if (!select || !badge) {
            return;
        }

        const styleByStatus = {
            WAITING: 'status-stoccato',
            WORK_IN_PROGRESS: 'status-lavorazione',
            READY: 'status-ready',
            DELIVERED: 'status-consegnato'
        };

        const labelByStatus = {
            WAITING: 'In attesa',
            WORK_IN_PROGRESS: 'In lavorazione',
            READY: 'Pronto',
            DELIVERED: 'Consegnato'
        };

        const updateBadge = () => {
            badge.classList.remove('status-stoccato', 'status-lavorazione', 'status-ready', 'status-consegnato');
            const value = select.value;
            if (styleByStatus[value]) {
                badge.classList.add(styleByStatus[value]);
            }
            badge.textContent = labelByStatus[value] || value || '—';
        };

        select.addEventListener('change', updateBadge);
        updateBadge();
    })();

    (() => {
        const initImageModalHandlers = () => {
        const modal = document.getElementById('engineImagesModal');
        if (!modal) {
            return;
        }

        const list = modal.querySelector('[data-image-list]');
        const deletionsContainer = modal.querySelector('[data-deletions-container]');
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
        };

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initImageModalHandlers);
            return;
        }
        initImageModalHandlers();
    })();
</script>

<div class="modal fade" id="engineImagesModal" tabindex="-1" aria-labelledby="engineImagesModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
            <form action="<%= request.getContextPath() %>/engine/edit/images"
                  method="post"
                  enctype="multipart/form-data"
                  class="form-click-guides">
                <div class="modal-header">
                    <h5 class="modal-title" id="engineImagesModalLabel">Modifica immagini</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                </div>

                <div class="modal-body">
                    <input type="hidden" name="engineRef" value="${engineRef}">
                    <div data-deletions-container></div>

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

                    <div>
                        <label class="form-label fw-semibold">Aggiungi nuove immagini</label>
                        <input type="file" name="newImages" class="form-control" accept="image/*" multiple>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Annulla</button>
                    <button type="submit" class="btn-engine">Salva modifiche immagini</button>
                </div>
            </form>
        </div>
    </div>
</div>

</body>
</html>
