<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>RML • Dettaglio Motore</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe.css">

    <style>
        .clickable-image {
            cursor: pointer;
            transition: transform 0.2s ease, opacity 0.2s ease;
            outline: none;
        }

        .clickable-image:hover,
        .clickable-image:focus-visible {
            transform: scale(1.02);
            opacity: 0.9;
        }
    </style>
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container mt-5 mb-4" id="engineDetailGallery" data-engine-ref="${detail.engine.engineRef}">
    <c:if test="${updated}">
        <div class="alert alert-success" role="alert">
            Modifiche salvate correttamente.
        </div>
    </c:if>
    <c:if test="${statusUpdated}">
        <div class="alert alert-success" role="alert">
            Stato aggiornato correttamente.
        </div>
    </c:if>
    <c:if test="${not empty statusUpdateError}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${statusUpdateError}" />
        </div>
    </c:if>
    <div class="row g-4 card-base">

        <div class="col-lg-5">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Dati tecnici</h5>

                <dl class="engine-detail-list">
                    <dt>Riferimento:</dt>
                    <dd>${detail.engine.engineRef}</dd>

                    <dt>Codice motore:</dt>
                    <dd>${detail.engine.engineCode}</dd>

                    <dt>Cliente:</dt>
                    <dd>${detail.engine.customerName}</dd>

                    <dt>Stato:</dt>
                    <c:set var="st" value="${detail.engine.status}" />
                    <dd>
                        <button type="button"
                                id="quickStatusBadge"
                                class="badge-status quick-status-trigger
                                ${st == 'WAITING' ? 'status-stoccato' : ''}
                                ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                                ${st == 'READY' ? 'status-ready' : ''}
                                ${st == 'DELIVERED' ? 'status-consegnato' : ''}"
                                aria-expanded="${not empty statusUpdateError ? 'true' : 'false'}"
                                aria-controls="quickStatusForm"
                                title="Clicca per modificare rapidamente lo stato">
                            <c:choose>
                                <c:when test="${st == 'WAITING'}">In attesa</c:when>
                                <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                                <c:when test="${st == 'READY'}">Pronto</c:when>
                                <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                                <c:otherwise>${st}</c:otherwise>
                            </c:choose>
                        </button>

                        <form id="quickStatusForm"
                              class="quick-status-form ${not empty statusUpdateError ? '' : 'd-none'} mt-2"
                              action="<%= request.getContextPath() %>/engine/detail"
                              method="post">
                            <input type="hidden" name="ref" value="${detail.engine.engineRef}">
                            <div class="d-flex flex-column flex-sm-row align-items-stretch align-items-sm-center gap-2">
                                <select class="form-select form-select-sm" name="status" required>
                                    <option value="WAITING" ${st == 'WAITING' ? 'selected' : ''}>In attesa</option>
                                    <option value="WORK_IN_PROGRESS" ${st == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                                    <option value="READY" ${st == 'READY' ? 'selected' : ''}>Pronto</option>
                                    <option value="DELIVERED" ${st == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                                </select>
                                <button type="submit" class="btn btn-sm btn-engine quick-status-save">Salva</button>
                                <button type="button" id="quickStatusCancel" class="btn btn-sm btn-outline-secondary">Annulla</button>
                            </div>
                            <div class="small text-muted mt-1">Modifica rapida: solo stato motore.</div>
                        </form>
                    </dd>

                    <dt>Data ingresso:</dt>
                    <dd>${detail.engine.intakeDate}</dd>

                    <dt>Data consegna:</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${detail.engine.deliveryDate != null}">
                                ${detail.engine.deliveryDate}
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </dd>

                    <dt>Note:</dt>
                    <dd>${detail.engine.notes}</dd>
                </dl>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Immagini</h5>

                <div id="engineCarousel" class="carousel slide" data-bs-ride="false">
                    <div class="carousel-inner">
                        <c:forEach var="image" items="${detail.images}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <div class="engine-image-lg clickable-image"
                                     role="button"
                                     tabindex="0"
                                     data-index="${status.index}"
                                     data-image-url="<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}"
                                     data-filename="${image.filename}"
                                     style="height: 420px;
                                            width: 100%;
                                            background-image: url('<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}');
                                            background-repeat: no-repeat;
                                            background-position: center;
                                            background-size: contain;
                                            background-color: #1f2933;">
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <button class="carousel-control-prev" type="button" data-bs-target="#engineCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon"></span>
                    </button>

                    <button class="carousel-control-next" type="button" data-bs-target="#engineCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon"></span>
                    </button>
                </div>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-12 detail-actions d-flex flex-wrap justify-content-center gap-2 gap-md-3">
                <a href="<%= request.getContextPath() %>/engine/list" class="btn btn-outline-secondary px-4">
                    Indietro
                </a>

                <a href="<%= request.getContextPath() %>/engine/edit?ref=${detail.engine.engineRef}" class="btn btn-detail-edit px-4">
                    Modifica
                </a>

                <a href="<%= request.getContextPath() %>/engine/delete?engineRef=${detail.engine.engineRef}" class="btn btn-detail-delete px-4">
                    Elimina
                </a>
            </div>
        </div>

    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    window.engineDetailViewerConfig = {
        engineRef: '${detail.engine.engineRef}',
        contextPath: '<%= request.getContextPath() %>'
    };

    (() => {
        const trigger = document.getElementById('quickStatusBadge');
        const form = document.getElementById('quickStatusForm');
        const cancelButton = document.getElementById('quickStatusCancel');

        if (!trigger || !form || !cancelButton) {
            return;
        }

        const openForm = () => {
            form.classList.remove('d-none');
            trigger.setAttribute('aria-expanded', 'true');
            const statusSelect = form.querySelector('select[name="status"]');
            if (statusSelect) {
                statusSelect.focus();
            }
        };

        const closeForm = () => {
            form.classList.add('d-none');
            trigger.setAttribute('aria-expanded', 'false');
        };

        trigger.addEventListener('click', () => {
            if (form.classList.contains('d-none')) {
                openForm();
                return;
            }
            closeForm();
        });

        cancelButton.addEventListener('click', closeForm);
    })();
</script>
<script type="module" src="<%= request.getContextPath() %>/assets/js/engine-detail-viewer.js"></script>

</body>
</html>
