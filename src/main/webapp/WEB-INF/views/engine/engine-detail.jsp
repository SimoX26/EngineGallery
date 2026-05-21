<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>RML • Dettaglio Motore</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
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

        .engine-tech-panel .engine-detail-header {
            display: flex !important;
            align-items: center !important;
            justify-content: space-between !important;
            gap: 12px;
        }

        .engine-tech-panel .engine-detail-list {
            margin: 0;
        }

        .engine-tech-panel .engine-ref-value {
            font-weight: 400;
            margin-bottom: 14px;
            word-break: break-word;
        }

        .engine-tech-panel .engine-tech-row {
            display: flex;
            align-items: baseline;
            gap: 6px;
            margin-bottom: 12px;
            min-width: 0;
        }

        .engine-tech-panel .engine-tech-key {
            font-weight: 700;
            flex: 0 0 auto;
        }

        .engine-tech-panel .engine-tech-value {
            min-width: 0;
            flex: 1 1 auto;
            overflow-wrap: anywhere;
            font-weight: 400;
        }

        .engine-tech-panel .engine-tech-row:not(.engine-tech-row--notes) .engine-tech-value {
            white-space: nowrap;
        }

        .engine-tech-panel .engine-tech-value .badge-status {
            vertical-align: middle;
        }

        .engine-tech-panel .engine-technical-share-btn {
            width: 44px;
            height: 44px;
            margin: 0;
            padding: 0;
            border-radius: 50%;
            border: 1.5px solid rgba(148, 163, 184, 0.35);
            background: var(--primary);
            color: #f8fafc;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 8px 24px rgba(2, 6, 23, 0.35);
            transition: transform 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
            flex: 0 0 auto;
        }

        .engine-tech-panel .engine-technical-share-btn:hover,
        .engine-tech-panel .engine-technical-share-btn:focus-visible {
            background: #374151;
            border-color: #e5e7eb;
            color: #ffffff;
            transform: scale(1.05);
        }

        .engine-tech-panel .engine-technical-share-btn:active {
            transform: scale(0.95);
        }

        .engine-tech-panel .engine-technical-share-btn svg {
            display: block;
            width: 18px;
            height: 18px;
        }

        @media (max-width: 768px) {
            .engine-tech-panel .engine-detail-header {
                gap: 10px;
            }

            .engine-tech-panel .engine-tech-row {
                align-items: flex-start;
                flex-wrap: wrap;
                gap: 4px 6px;
                margin-bottom: 10px;
            }

            .engine-tech-panel .engine-tech-row:not(.engine-tech-row--notes) .engine-tech-value {
                white-space: normal;
            }

            .engine-tech-panel .engine-technical-share-btn {
                width: 40px;
                height: 40px;
            }

            .engine-tech-panel .engine-technical-share-btn svg {
                width: 17px;
                height: 17px;
            }
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
    <c:if test="${param.statusUpdated == '1'}">
        <div class="alert alert-success" role="alert">
            Stato aggiornato correttamente.
        </div>
    </c:if>
    <c:if test="${not empty param.statusUpdateError}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${param.statusUpdateError}" />
        </div>
    </c:if>
    <div class="row g-4 card-base">

        <div class="col-lg-5">
            <div class="engine-detail-section engine-tech-panel">
                <c:set var="engineDeliveryDateShare" value="" />
                <c:if test="${detail.engine.deliveryDate != null}">
                    <fmt:parseDate value="${detail.engine.deliveryDate}" pattern="yyyy-MM-dd" var="engineDeliveryDateShareParsed" />
                    <fmt:formatDate value="${engineDeliveryDateShareParsed}" pattern="dd/MM/yyyy" var="engineDeliveryDateShare" />
                </c:if>

                <div class="engine-detail-header mb-3">
                    <h5 class="fw-semibold mb-0">Dati tecnici</h5>
                    <button
                            id="engineTechnicalShareBtn"
                            type="button"
                            class="share-icon-btn engine-technical-share-btn js-engine-share-btn"
                            aria-label="Condividi scheda tecnica"
                            title="Condividi scheda tecnica"
                            data-engine-code="${detail.engine.engineCode}"
                            data-share-source="#engineDetailGallery"
                            data-image-selector=".clickable-image"
                            data-engine-status="${detail.engine.status}"
                            data-delivery-date="${engineDeliveryDateShare}">
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <circle cx="18" cy="5" r="3" fill="none" stroke="currentColor" stroke-width="2.25"></circle>
                            <circle cx="6" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="2.25"></circle>
                            <circle cx="18" cy="19" r="3" fill="none" stroke="currentColor" stroke-width="2.25"></circle>
                            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"></line>
                            <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"></line>
                        </svg>
                    </button>
                </div>

                <div class="engine-detail-list">
                    <div class="engine-ref-value">${detail.engine.engineRef}</div>

                    <div class="engine-tech-row">
                        <span class="engine-tech-key">Codice motore:</span>
                        <span class="engine-tech-value">${detail.engine.engineCode}</span>
                    </div>

                    <div class="engine-tech-row">
                        <span class="engine-tech-key">Cliente:</span>
                        <span class="engine-tech-value">${detail.engine.customerName}</span>
                    </div>

                    <div class="engine-tech-row">
                        <span class="engine-tech-key">Stato:</span>
                        <c:set var="st" value="${detail.engine.status}" />
                        <span class="engine-tech-value">
                            <button type="button"
                                    class="badge-status quick-status-trigger
                                    ${st == 'WAITING' ? 'status-stoccato' : ''}
                                    ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                                    ${st == 'READY' ? 'status-ready' : ''}
                                    ${st == 'DELIVERED' ? 'status-consegnato' : ''}"
                                    data-quick-status-trigger
                                    data-engine-ref="${detail.engine.engineRef}"
                                    data-current-status="${st}"
                                    aria-expanded="false"
                                    aria-controls="quickStatusModalDetail"
                                    aria-haspopup="dialog"
                                    title="Clicca per modificare rapidamente lo stato">
                                <c:choose>
                                    <c:when test="${st == 'WAITING'}">In attesa</c:when>
                                    <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                                    <c:when test="${st == 'READY'}">Pronto</c:when>
                                    <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                                    <c:otherwise>${st}</c:otherwise>
                                </c:choose>
                            </button>
                        </span>
                    </div>

                    <div class="engine-tech-row">
                        <span class="engine-tech-key">Data ingresso:</span>
                        <span class="engine-tech-value">
                            <fmt:parseDate value="${detail.engine.intakeDate}" pattern="yyyy-MM-dd" var="engineIntakeDateParsed" />
                            <fmt:formatDate value="${engineIntakeDateParsed}" pattern="dd / MM / yyyy" />
                        </span>
                    </div>

                    <div class="engine-tech-row">
                        <span class="engine-tech-key">Data consegna:</span>
                        <span class="engine-tech-value">
                            <c:choose>
                                <c:when test="${detail.engine.deliveryDate != null}">
                                    <fmt:parseDate value="${detail.engine.deliveryDate}" pattern="yyyy-MM-dd" var="engineDeliveryDateParsed" />
                                    <fmt:formatDate value="${engineDeliveryDateParsed}" pattern="dd / MM / yyyy" />
                                </c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="engine-tech-row engine-tech-row--notes">
                        <span class="engine-tech-key">Note:</span>
                        <span class="engine-tech-value">${detail.engine.notes}</span>
                    </div>
                </div>
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

<div class="modal fade" id="quickStatusModalDetail" tabindex="-1" aria-labelledby="quickStatusModalDetailLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold" id="quickStatusModalDetailLabel">Cambia stato motore</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
            </div>
            <div class="modal-body">
                <form id="quickStatusModalFormDetail"
                      action="<%= request.getContextPath() %>/engine/detail"
                      method="post">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                    <input type="hidden" name="ref" value="${detail.engine.engineRef}" data-quick-status-ref>
                    <input type="hidden" name="status" value="" data-quick-status-value>
                    <div class="quick-status-options" role="listbox" aria-label="Seleziona nuovo stato">
                        <button type="button" class="quick-status-option quick-status-option--waiting" data-quick-status-option data-status-value="WAITING">In attesa</button>
                        <button type="button" class="quick-status-option quick-status-option--working" data-quick-status-option data-status-value="WORK_IN_PROGRESS">In lavorazione</button>
                        <button type="button" class="quick-status-option quick-status-option--ready" data-quick-status-option data-status-value="READY">Pronto</button>
                        <button type="button" class="quick-status-option quick-status-option--delivered" data-quick-status-option data-status-value="DELIVERED">Consegnato</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        const quickStatusTrigger = document.querySelector('[data-quick-status-trigger]');
        const quickStatusModalEl = document.getElementById('quickStatusModalDetail');
        const quickStatusModalForm = document.getElementById('quickStatusModalFormDetail');
        if (!quickStatusTrigger || !quickStatusModalEl || !quickStatusModalForm) {
            return;
        }

        const quickStatusModal = bootstrap.Modal.getOrCreateInstance(quickStatusModalEl);
        const statusValueInput = quickStatusModalForm.querySelector('[data-quick-status-value]');
        const statusOptions = quickStatusModalForm.querySelectorAll('[data-quick-status-option]');
        const quickStatusRef = quickStatusModalForm.querySelector('[data-quick-status-ref]');

        quickStatusTrigger.addEventListener('click', () => {
            const currentStatus = quickStatusTrigger.dataset.currentStatus || '';
            quickStatusModalForm.dataset.currentStatus = currentStatus;
            quickStatusRef.value = quickStatusTrigger.dataset.engineRef || quickStatusRef.value;
            statusOptions.forEach((button) => {
                const isCurrent = button.dataset.statusValue === currentStatus;
                button.disabled = isCurrent;
                button.classList.toggle('is-current', isCurrent);
            });
            quickStatusTrigger.setAttribute('aria-expanded', 'true');
            quickStatusModal.show();
            const firstOption = quickStatusModalForm.querySelector('[data-quick-status-option]:not(:disabled)');
            if (firstOption) {
                firstOption.focus();
            }
        });

        quickStatusModalEl.addEventListener('hidden.bs.modal', () => {
            quickStatusTrigger.setAttribute('aria-expanded', 'false');
            statusOptions.forEach((button) => {
                button.disabled = false;
                button.classList.remove('is-current');
            });
            delete quickStatusModalForm.dataset.submitting;
        });

        statusOptions.forEach((optionButton) => {
            optionButton.addEventListener('click', () => {
                if (quickStatusModalForm.dataset.submitting === '1') {
                    return;
                }

                const selectedStatus = optionButton.dataset.statusValue;
                const currentStatus = quickStatusModalForm.dataset.currentStatus;

                if (!selectedStatus) {
                    return;
                }

                if (selectedStatus === currentStatus) {
                    quickStatusModal.hide();
                    return;
                }

                if (statusValueInput) {
                    statusValueInput.value = selectedStatus;
                }
                quickStatusModalForm.dataset.submitting = '1';
                statusOptions.forEach((button) => {
                    button.disabled = true;
                });
                quickStatusModal.hide();
                quickStatusModalForm.requestSubmit();
            });
        });
    })();

    window.engineDetailViewerConfig = {
        engineRef: '${detail.engine.engineRef}',
        contextPath: '<%= request.getContextPath() %>'
    };
</script>
<script type="module" src="<%= request.getContextPath() %>/assets/js/engine-detail-viewer.js"></script>

</body>
</html>
