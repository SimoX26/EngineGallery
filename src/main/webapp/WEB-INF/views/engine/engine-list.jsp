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
    <title>Engine Gallery • <c:out value="${pageTitle}" /></title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"  rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
    <style>
        .engine-card-link-main {
            height: auto;
        }
    </style>
</head>

<body>

<div class="engine-gallery-page">

    <!-- NAVBAR -->
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <div class="page-header-with-search">
            <div class="page-header">
                <h1><c:out value="${pageTitle}" /></h1>
            </div>

            <form method="get" action="${pageContext.request.contextPath}${archiveMode ? '/engine/archive' : '/engine/list'}" class="mb-0 search-panel-compact">
                <label for="engineKeywordSearch" class="form-label fw-semibold mb-0">Ricerca per parola chiave</label>
                <input type="search"
                       id="engineKeywordSearch"
                       name="keyword"
                       value="<c:out value='${filterKeyword}' />"
                       class="form-control"
                       placeholder="cerca...">
                <div class="modal fade" id="engineFiltersModal" tabindex="-1" aria-labelledby="engineFiltersModalLabel" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title fw-semibold" id="engineFiltersModalLabel">Filtri motori</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                            </div>
                            <div class="modal-body">
                                <div class="row g-2">
                                    <div class="col-md-6">
                                        <label for="engineStatusFilter" class="form-label small mb-1">Stato</label>
                                        <select id="engineStatusFilter" name="status" class="form-select" ${archiveMode ? 'disabled' : ''}>
                                            <option value="">Tutti gli stati</option>
                                            <option value="WAITING" ${filterStatus == 'WAITING' ? 'selected' : ''}>In attesa</option>
                                            <option value="WORK_IN_PROGRESS" ${filterStatus == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                                            <option value="READY" ${filterStatus == 'READY' ? 'selected' : ''}>Pronto</option>
                                            <option value="DELIVERED" ${filterStatus == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                                        </select>
                                        <c:if test="${archiveMode}">
                                            <input type="hidden" name="status" value="DELIVERED">
                                        </c:if>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="engineCustomerFilter" class="form-label small mb-1">Cliente</label>
                                        <select id="engineCustomerFilter" name="customerId" class="form-select">
                                            <option value="">Tutti i clienti</option>
                                            <c:forEach var="entry" items="${customerNames}">
                                                <option value="${entry.key}" ${filterCustomerId == entry.key ? 'selected' : ''}>
                                                    <c:out value="${entry.value}" default="—" />
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer filters-modal-footer">
                                <a href="${pageContext.request.contextPath}${archiveMode ? '/engine/archive' : '/engine/list'}"
                                   id="engineFiltersReset"
                                   class="btn btn-sm btn-outline-secondary filters-btn-secondary">Reimposta filtri</a>
                                <button type="submit" class="btn btn-sm btn-engine filters-btn-primary">Applica filtri</button>
                            </div>
                        </div>
                    </div>
                </div>
            </form>

            <div class="page-header-filter">
                <button class="btn btn-outline-secondary btn-sm"
                        type="button"
                        aria-expanded="false"
                        aria-controls="engineFiltersModal"
                        id="engineFiltersToggle">
                    Filtri
                </button>
            </div>

            <div class="page-header-actions">
                <c:if test="${not archiveMode}">
                    <div class="btn-group btn-group-sm d-none d-lg-inline-flex" role="group" aria-label="Cambia vista motori">
                        <button type="button"
                                id="engineViewListBtn"
                                class="btn btn-outline-secondary engine-view-toggle-btn"
                                title="Vista lista"
                                aria-label="Vista lista">
                            <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <line x1="5" y1="7" x2="19" y2="7"></line>
                                <line x1="5" y1="12" x2="19" y2="12"></line>
                                <line x1="5" y1="17" x2="19" y2="17"></line>
                            </svg>
                            <span class="visually-hidden">Lista</span>
                        </button>
                        <button type="button"
                                id="engineViewGalleryBtn"
                                class="btn btn-outline-secondary active engine-view-toggle-btn"
                                title="Vista galleria"
                                aria-label="Vista galleria">
                            <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <rect x="4" y="4" width="7" height="7" rx="1"></rect>
                                <rect x="13" y="4" width="7" height="7" rx="1"></rect>
                                <rect x="4" y="13" width="7" height="7" rx="1"></rect>
                                <rect x="13" y="13" width="7" height="7" rx="1"></rect>
                            </svg>
                            <span class="visually-hidden">Galleria</span>
                        </button>
                        <button type="button"
                                id="engineViewKanbanBtn"
                                class="btn btn-outline-secondary engine-view-toggle-btn"
                                title="Vista Kanban"
                                aria-label="Vista Kanban">
                            <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                <rect x="4" y="5" width="4" height="14" rx="1"></rect>
                                <rect x="10" y="8" width="4" height="11" rx="1"></rect>
                                <rect x="16" y="3" width="4" height="16" rx="1"></rect>
                            </svg>
                            <span class="visually-hidden">Kanban</span>
                        </button>
                    </div>
                    <a href="<%= request.getContextPath() %>/upload" class="btn btn-sm btn-add-plus">Aggiungi +</a>
                </c:if>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-warning">${error}</div>
        </c:if>
        <c:if test="${param.statusUpdated == '1'}">
            <div class="alert alert-success">Stato aggiornato correttamente.</div>
        </c:if>
        <c:if test="${not empty param.statusUpdateError}">
            <div class="alert alert-danger"><c:out value="${param.statusUpdateError}" /></div>
        </c:if>

        <c:if test="${empty engines}">
            <div id="engineKeywordEmptyState" class="alert alert-light border mt-3 mb-3">
                Nessun motore corrisponde alla ricerca.
            </div>
        </c:if>

        <div id="engineListView" class="engine-list-view d-none">
            <c:forEach var="engine" items="${engines}">
                <c:set var="st" value="${engine.status}" />
                <c:set var="coverFilename" value="${coverImages[engine.id]}" />
                <a class="engine-list-row" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                    <c:choose>
                        <c:when test="${not empty coverFilename}">
                            <div class="engine-image"
                                 style="background-image: url('<%= request.getContextPath() %>/uploads/engines/${engine.engineRef}/${coverFilename}');">
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="engine-image engine-image-empty d-flex align-items-center justify-content-center">
                                <span class="engine-image-empty-label">Nessuna immagine</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <div class="engine-list-row__body">
                        <div class="engine-list-row__main">${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" /></div>
                        <div class="engine-list-row__sub">${engine.engineRef}</div>
                    </div>
                    <button type="button"
                            class="badge-status quick-status-trigger
                            ${st == 'WAITING' ? 'status-stoccato' : ''}
                            ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                            ${st == 'READY' ? 'status-ready' : ''}
                            ${st == 'DELIVERED' ? 'status-consegnato' : ''}"
                            data-quick-status-trigger
                            data-engine-ref="${engine.engineRef}"
                            data-current-status="${st}"
                            data-redirect-to="${archiveMode ? 'archive' : 'list'}"
                            aria-haspopup="dialog"
                            aria-expanded="false"
                            aria-controls="quickStatusModal"
                            title="Clicca per modificare rapidamente lo stato">
                        <c:choose>
                            <c:when test="${st == 'WAITING'}">In attesa</c:when>
                            <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                            <c:when test="${st == 'READY'}">Pronto</c:when>
                            <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                            <c:otherwise>${st}</c:otherwise>
                        </c:choose>
                    </button>
                </a>
            </c:forEach>
        </div>

        <div class="row g-4" id="engineGalleryGrid">

            <c:forEach var="engine" items="${engines}">
                <c:set var="st" value="${engine.status}" />
                <div class="col-xl-3 col-lg-4 col-md-6 engine-card-col">

                    <div class="engine-gallery-card">
                        <a class="engine-card-link engine-card-link-main" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                            <c:set var="coverFilename" value="${coverImages[engine.id]}" />
                            <c:choose>
                                <c:when test="${not empty coverFilename}">
                                    <div class="engine-image"
                                         style="background-image: url('<%= request.getContextPath() %>/uploads/engines/${engine.engineRef}/${coverFilename}');">
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="engine-image engine-image-empty d-flex align-items-center justify-content-center">
                                        <span class="engine-image-empty-label">Nessuna immagine</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="engine-body pb-2">
                                <div class="engine-code">
                                    ${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" />
                                </div>

                                <div class="engine-client">
                                    ${engine.engineRef}
                                </div>
                            </div>
                        </a>

                        <div class="engine-body pt-0">
                            <div class="engine-footer">
                                <button type="button"
                                        class="badge-status quick-status-trigger
                                        ${st == 'WAITING' ? 'status-stoccato' : ''}
                                        ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                                        ${st == 'READY' ? 'status-ready' : ''}
                                        ${st == 'DELIVERED' ? 'status-consegnato' : ''}"
                                        data-quick-status-trigger
                                        data-engine-ref="${engine.engineRef}"
                                        data-current-status="${st}"
                                        data-redirect-to="${archiveMode ? 'archive' : 'list'}"
                                        aria-haspopup="dialog"
                                        aria-expanded="false"
                                        aria-controls="quickStatusModal"
                                        title="Clicca per modificare rapidamente lo stato">

                                    <c:choose>
                                        <c:when test="${st == 'WAITING'}">In attesa</c:when>
                                        <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                                        <c:when test="${st == 'READY'}">Pronto</c:when>
                                        <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                                        <c:otherwise>${st}</c:otherwise>
                                    </c:choose>
                                </button>
                            </div>
                            <c:if test="${st == 'DELIVERED' && engine.deliveryDate != null}">
                                <div class="small text-muted mt-1">
                                    <fmt:parseDate value="${engine.deliveryDate}" pattern="yyyy-MM-dd" var="engineCardDeliveryDateParsed" />
                                    Consegnato il <fmt:formatDate value="${engineCardDeliveryDateParsed}" pattern="dd / MM / yyyy" />
                                </div>
                            </c:if>

                        </div>
                    </div>

                </div>
            </c:forEach>

        </div>

        <c:if test="${not archiveMode}">
            <div id="engineKanbanBoard" class="engine-kanban-board d-none">
                <div class="engine-kanban-board__scroll">
                    <div class="engine-kanban-col">
                        <div class="engine-kanban-col__header engine-kanban-col__header--waiting">
                            <span>In attesa</span>
                        </div>
                        <div class="engine-kanban-col__body">
                            <c:forEach var="engine" items="${engines}">
                                <c:if test="${engine.status == 'WAITING'}">
                                    <c:set var="coverFilename" value="${coverImages[engine.id]}" />
                                    <a class="engine-kanban-card" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                                        <c:choose>
                                            <c:when test="${not empty coverFilename}">
                                                <div class="engine-image"
                                                     style="background-image: url('<%= request.getContextPath() %>/uploads/engines/${engine.engineRef}/${coverFilename}');">
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="engine-image engine-image-empty d-flex align-items-center justify-content-center">
                                                    <span class="engine-image-empty-label">Nessuna immagine</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="engine-kanban-card__body">
                                            <div class="engine-code">
                                                ${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" />
                                            </div>
                                            <div class="engine-client">${engine.engineRef}</div>
                                            <div class="mt-2">
                                                <span class="badge-status status-stoccato">In attesa</span>
                                            </div>
                                        </div>
                                    </a>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="engine-kanban-col">
                        <div class="engine-kanban-col__header engine-kanban-col__header--working">
                            <span>In lavorazione</span>
                        </div>
                        <div class="engine-kanban-col__body">
                            <c:forEach var="engine" items="${engines}">
                                <c:if test="${engine.status == 'WORK_IN_PROGRESS'}">
                                    <c:set var="coverFilename" value="${coverImages[engine.id]}" />
                                    <a class="engine-kanban-card" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                                        <c:choose>
                                            <c:when test="${not empty coverFilename}">
                                                <div class="engine-image"
                                                     style="background-image: url('<%= request.getContextPath() %>/uploads/engines/${engine.engineRef}/${coverFilename}');">
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="engine-image engine-image-empty d-flex align-items-center justify-content-center">
                                                    <span class="engine-image-empty-label">Nessuna immagine</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="engine-kanban-card__body">
                                            <div class="engine-code">
                                                ${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" />
                                            </div>
                                            <div class="engine-client">${engine.engineRef}</div>
                                            <div class="mt-2">
                                                <span class="badge-status status-lavorazione">In lavorazione</span>
                                            </div>
                                        </div>
                                    </a>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="engine-kanban-col">
                        <div class="engine-kanban-col__header engine-kanban-col__header--ready">
                            <span>Pronto</span>
                        </div>
                        <div class="engine-kanban-col__body">
                            <c:forEach var="engine" items="${engines}">
                                <c:if test="${engine.status == 'READY'}">
                                    <c:set var="coverFilename" value="${coverImages[engine.id]}" />
                                    <a class="engine-kanban-card" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                                        <c:choose>
                                            <c:when test="${not empty coverFilename}">
                                                <div class="engine-image"
                                                     style="background-image: url('<%= request.getContextPath() %>/uploads/engines/${engine.engineRef}/${coverFilename}');">
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="engine-image engine-image-empty d-flex align-items-center justify-content-center">
                                                    <span class="engine-image-empty-label">Nessuna immagine</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="engine-kanban-card__body">
                                            <div class="engine-code">
                                                ${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" />
                                            </div>
                                            <div class="engine-client">${engine.engineRef}</div>
                                            <div class="mt-2">
                                                <span class="badge-status status-ready">Pronto</span>
                                            </div>
                                        </div>
                                    </a>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

    </div>

    <div class="modal fade" id="quickStatusModal" tabindex="-1" aria-labelledby="quickStatusModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title fw-semibold" id="quickStatusModalLabel">Cambia stato motore</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                </div>
                <div class="modal-body">
                    <form id="quickStatusModalForm"
                          action="<%= request.getContextPath() %>/engine/detail"
                          method="post">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                        <input type="hidden" name="redirectTo" value="${archiveMode ? 'archive' : 'list'}" data-quick-status-redirect>
                        <input type="hidden" name="ref" value="" data-quick-status-ref>
                        <input type="hidden" name="status" value="" data-quick-status-value>
                        <div class="quick-status-options" role="listbox" aria-label="Seleziona nuovo stato">
                            <button type="button" class="quick-status-option quick-status-option--waiting" data-quick-status-option data-status-value="WAITING">In attesa</button>
                            <button type="button" class="quick-status-option quick-status-option--working" data-status-value="WORK_IN_PROGRESS" data-quick-status-option>In lavorazione</button>
                            <button type="button" class="quick-status-option quick-status-option--ready" data-status-value="READY" data-quick-status-option>Pronto</button>
                            <button type="button" class="quick-status-option quick-status-option--delivered" data-status-value="DELIVERED" data-quick-status-option>Consegnato</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const filtersToggle = document.getElementById('engineFiltersToggle');
        const filtersModalEl = document.getElementById('engineFiltersModal');
        const quickStatusTriggers = document.querySelectorAll('[data-quick-status-trigger]');
        const engineViewListBtn = document.getElementById('engineViewListBtn');
        const engineViewGalleryBtn = document.getElementById('engineViewGalleryBtn');
        const engineViewKanbanBtn = document.getElementById('engineViewKanbanBtn');
        const engineListView = document.getElementById('engineListView');
        const engineGalleryGrid = document.getElementById('engineGalleryGrid');
        const engineKanbanBoard = document.getElementById('engineKanbanBoard');
        const filtersModal = bootstrap.Modal.getOrCreateInstance(filtersModalEl);
        const queryParams = new URLSearchParams(window.location.search);
        const quickStatusScrollKey = 'engineList.quickStatus.scrollY';
        const viewStorageKey = 'engineList.viewMode';
        const quickStatusModalEl = document.getElementById('quickStatusModal');
        const quickStatusModal = bootstrap.Modal.getOrCreateInstance(quickStatusModalEl);
        const quickStatusModalForm = document.getElementById('quickStatusModalForm');
        const quickStatusRefInput = quickStatusModalForm.querySelector('[data-quick-status-ref]');
        const quickStatusValueInput = quickStatusModalForm.querySelector('[data-quick-status-value]');
        const quickStatusRedirectInput = quickStatusModalForm.querySelector('[data-quick-status-redirect]');
        const quickStatusOptions = quickStatusModalForm.querySelectorAll('[data-quick-status-option]');

        const hasActiveFilters = () => {
            const params = ['keyword', 'status', 'customerId'];
            return params.some((p) => {
                const v = (queryParams.get(p) || '').trim();
                return v.length > 0;
            });
        };

        if (hasActiveFilters()) {
            filtersToggle.classList.remove('btn-outline-secondary');
            filtersToggle.classList.add('btn-secondary');
        }

        const restoreScrollAfterQuickStatusSubmit = () => {
            const hasQuickStatusFeedback = queryParams.get('statusUpdated') === '1'
                || queryParams.has('statusUpdateError');
            if (!hasQuickStatusFeedback) {
                return;
            }

            const savedScrollY = Number(sessionStorage.getItem(quickStatusScrollKey));
            if (!Number.isFinite(savedScrollY) || savedScrollY < 0) {
                sessionStorage.removeItem(quickStatusScrollKey);
                return;
            }

            requestAnimationFrame(() => {
                window.scrollTo(0, savedScrollY);
                sessionStorage.removeItem(quickStatusScrollKey);
            });
        };

        filtersModalEl.addEventListener('shown.bs.modal', () => {
            filtersToggle.setAttribute('aria-expanded', 'true');
        });
        filtersModalEl.addEventListener('hidden.bs.modal', () => {
            filtersToggle.setAttribute('aria-expanded', 'false');
            if (!hasActiveFilters()) {
                filtersToggle.classList.add('btn-outline-secondary');
                filtersToggle.classList.remove('btn-secondary');
            }
        });

        filtersToggle.addEventListener('click', () => {
            filtersModal.show();
        });

        const resetQuickStatusOptions = () => {
            quickStatusOptions.forEach((button) => {
                button.disabled = false;
                button.classList.remove('is-current');
            });
            delete quickStatusModalForm.dataset.submitting;
        };

        quickStatusModalEl.addEventListener('hidden.bs.modal', () => {
            resetQuickStatusOptions();
            quickStatusTriggers.forEach((trigger) => trigger.setAttribute('aria-expanded', 'false'));
        });

        quickStatusTriggers.forEach((trigger) => {
            trigger.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                resetQuickStatusOptions();
                quickStatusRefInput.value = trigger.dataset.engineRef || '';
                quickStatusRedirectInput.value = trigger.dataset.redirectTo || '${archiveMode ? "archive" : "list"}';
                const currentStatus = trigger.dataset.currentStatus || '';
                quickStatusModalForm.dataset.currentStatus = currentStatus;

                quickStatusOptions.forEach((button) => {
                    const isCurrent = button.dataset.statusValue === currentStatus;
                    button.classList.toggle('is-current', isCurrent);
                    button.disabled = isCurrent;
                });

                trigger.setAttribute('aria-expanded', 'true');
                quickStatusModal.show();
            });
        });

        quickStatusOptions.forEach((button) => {
            button.addEventListener('click', () => {
                if (quickStatusModalForm.dataset.submitting === '1') {
                    return;
                }
                const selectedStatus = button.dataset.statusValue;
                if (!selectedStatus) {
                    return;
                }
                quickStatusModalForm.dataset.submitting = '1';
                quickStatusValueInput.value = selectedStatus;
                sessionStorage.setItem(quickStatusScrollKey, String(window.scrollY));
                quickStatusOptions.forEach((opt) => {
                    opt.disabled = true;
                });
                quickStatusModal.hide();
                quickStatusModalForm.requestSubmit();
            });
        });

        const isDesktop = () => window.matchMedia('(min-width: 992px)').matches;
        const applyViewMode = (mode) => {
            let safeMode = 'gallery';
            if (mode === 'list' || mode === 'gallery' || mode === 'kanban') {
                safeMode = mode;
            }
            if (!engineGalleryGrid || !engineKanbanBoard || !engineListView) {
                return;
            }
            if (!isDesktop()) {
                engineListView.classList.add('d-none');
                engineGalleryGrid.classList.remove('d-none');
                engineKanbanBoard.classList.add('d-none');
                return;
            }
            const showKanban = safeMode === 'kanban';
            const showList = safeMode === 'list';
            const showGallery = safeMode === 'gallery';
            engineListView.classList.toggle('d-none', !showList);
            engineGalleryGrid.classList.toggle('d-none', !showGallery);
            engineKanbanBoard.classList.toggle('d-none', !showKanban);
            if (engineViewListBtn && engineViewGalleryBtn && engineViewKanbanBtn) {
                engineViewListBtn.classList.toggle('active', showList);
                engineViewGalleryBtn.classList.toggle('active', showGallery);
                engineViewKanbanBtn.classList.toggle('active', showKanban);
            }
            sessionStorage.setItem(viewStorageKey, safeMode);
        };

        if (engineViewListBtn && engineViewGalleryBtn && engineViewKanbanBtn) {
            engineViewListBtn.addEventListener('click', () => applyViewMode('list'));
            engineViewGalleryBtn.addEventListener('click', () => applyViewMode('gallery'));
            engineViewKanbanBtn.addEventListener('click', () => applyViewMode('kanban'));
        }
        window.addEventListener('resize', () => applyViewMode(sessionStorage.getItem(viewStorageKey) || 'gallery'));
        applyViewMode(sessionStorage.getItem(viewStorageKey) || 'gallery');

        restoreScrollAfterQuickStatusSubmit();
    </script>

</div>
</body>
</html>
