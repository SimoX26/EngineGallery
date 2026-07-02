<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Pronta consegna</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"  rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=12">
</head>

<body>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

    <!-- NAVBAR -->
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <div class="page-header-with-search">
            <!-- HEADER -->
            <div class="page-header">
                <h1>Pronta consegna</h1>
            </div>

            <div class="search-panel-compact">
                <label for="engineKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
                <input type="search"
                       id="engineKeywordSearch"
                       class="form-control"
                       placeholder="cerca...">
                <div id="engineKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                    Nessun motore corrisponde alla ricerca.
                </div>

                <div class="modal fade" id="readyFiltersModal" tabindex="-1" aria-labelledby="readyFiltersModalLabel" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title fw-semibold" id="readyFiltersModalLabel">Filtri pronta consegna</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                            </div>
                            <div class="modal-body">
                                <label for="readyFiltersKeywordInput" class="form-label small mb-1">Parola chiave</label>
                                <input type="search" id="readyFiltersKeywordInput" class="form-control" placeholder="cerca...">
                            </div>
                            <div class="modal-footer filters-modal-footer">
                                <button type="button" id="readyFiltersResetBtn" class="btn btn-sm btn-outline-secondary filters-btn-secondary">Reimposta filtri</button>
                                <button type="button" id="readyFiltersApplyBtn" class="btn btn-sm btn-engine filters-btn-primary">Applica filtri</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header-filter">
                <button class="btn btn-outline-secondary btn-sm"
                        type="button"
                        aria-expanded="false"
                        aria-controls="readyFiltersModal"
                        id="readyFiltersToggle">
                    Filtri
                </button>
            </div>

            <div class="page-header-actions page-header-actions--view">
                <div class="btn-group btn-group-sm page-header-view-switch" role="group" aria-label="Cambia vista pronta consegna">
                    <button type="button"
                            id="readyViewListBtn"
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
                            id="readyViewGalleryBtn"
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
                </div>
            </div>
        </div>

        <div id="readyListView" class="engine-list-view d-none">
            <c:forEach var="engine" items="${engines}">
                <c:set var="st" value="${engine.status}" />
                <c:set var="coverFilename" value="${coverImages[engine.id]}" />
                <a class="engine-list-row"
                   href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}"
                   data-search="${engine.engineCode} ${engine.engineRef} ${customerNames[engine.customerId]} ${st}">
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
                    <span class="badge-status
                        ${st == 'WAITING' ? 'status-stoccato' : ''}
                        ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                        ${st == 'READY' ? 'status-ready' : ''}
                        ${st == 'DELIVERED' ? 'status-consegnato' : ''}">
                        <c:choose>
                            <c:when test="${st == 'WAITING'}">In attesa</c:when>
                            <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                            <c:when test="${st == 'READY'}">Pronto</c:when>
                            <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                            <c:otherwise>${st}</c:otherwise>
                        </c:choose>
                    </span>
                </a>
            </c:forEach>
        </div>

        <!-- GALLERY -->
        <div class="row g-4" id="engineGalleryGrid">

            <c:forEach var="engine" items="${engines}">
                <c:set var="st" value="${engine.status}" />
                <c:set var="statusSearchLabel"
                       value="${st == 'WAITING' ? 'in attesa' : st == 'WORK_IN_PROGRESS' ? 'in lavorazione' : st == 'READY' ? 'pronto' : st == 'DELIVERED' ? 'consegnato' : st}" />
                <div class="col-xl-3 col-lg-4 col-md-6 engine-card-col"
                     data-search="${engine.engineCode} ${engine.engineRef} ${customerNames[engine.customerId]} ${st} ${statusSearchLabel}">

                    <a class="engine-card-link" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                    <div class="engine-gallery-card">

                        <!-- IMAGE -->
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

                        <!-- BODY -->
                        <div class="engine-body">

                            <div class="engine-code">
                                ${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" />
                            </div>

                            <div class="engine-client">
                                ${engine.engineRef}
                            </div>

                            <div class="engine-footer">

                                <!-- STATUS -->
                                <span class="badge-status
                                    ${st == 'WAITING' ? 'status-stoccato' : ''}
                                    ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                                    ${st == 'READY' ? 'status-ready' : ''}
                                    ${st == 'DELIVERED' ? 'status-consegnato' : ''}">

                                    <c:choose>
                                        <c:when test="${st == 'WAITING'}">In attesa</c:when>
                                        <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                                        <c:when test="${st == 'READY'}">Pronto</c:when>
                                        <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                                        <c:otherwise>${st}</c:otherwise>
                                    </c:choose>
                                </span>

                            </div>
                        </div>

                    </div>
                    </a>

                </div>
            </c:forEach>

        </div>

    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/live-search.js"></script>
    <script>
        const searchInput = document.getElementById('engineKeywordSearch');
        const engineCards = document.querySelectorAll('.engine-card-col');
        const listRows = document.querySelectorAll('#readyListView .engine-list-row');
        const emptyState = document.getElementById('engineKeywordEmptyState');
        const readyViewListBtn = document.getElementById('readyViewListBtn');
        const readyViewGalleryBtn = document.getElementById('readyViewGalleryBtn');
        const readyListView = document.getElementById('readyListView');
        const readyGalleryView = document.getElementById('engineGalleryGrid');
        const readyViewStorageKey = 'engineReady.viewMode';
        const readyFiltersToggle = document.getElementById('readyFiltersToggle');
        const readyFiltersModalEl = document.getElementById('readyFiltersModal');
        const readyFiltersModal = bootstrap.Modal.getOrCreateInstance(readyFiltersModalEl);
        const readyFiltersKeywordInput = document.getElementById('readyFiltersKeywordInput');
        const readyFiltersApplyBtn = document.getElementById('readyFiltersApplyBtn');
        const readyFiltersResetBtn = document.getElementById('readyFiltersResetBtn');

        const filterController = window.EngineGalleryLiveSearch && searchInput
            ? window.EngineGalleryLiveSearch.init({
                input: searchInput,
                groups: [
                    { elements: engineCards },
                    { elements: listRows }
                ],
                emptyState,
                debounceMs: 180
            })
            : null;

        readyFiltersToggle.addEventListener('click', () => {
            readyFiltersKeywordInput.value = searchInput.value;
            readyFiltersModal.show();
        });

        readyFiltersApplyBtn.addEventListener('click', () => {
            searchInput.value = readyFiltersKeywordInput.value || '';
            if (filterController) {
                filterController.apply();
            }
            readyFiltersModal.hide();
        });

        readyFiltersResetBtn.addEventListener('click', () => {
            readyFiltersKeywordInput.value = '';
            searchInput.value = '';
            if (filterController) {
                filterController.apply();
            }
            readyFiltersModal.hide();
        });

        const applyViewMode = (mode) => {
            const safeMode = mode === 'list' ? 'list' : 'gallery';
            readyListView.classList.toggle('d-none', safeMode !== 'list');
            readyGalleryView.classList.toggle('d-none', safeMode !== 'gallery');
            readyViewListBtn.classList.toggle('active', safeMode === 'list');
            readyViewGalleryBtn.classList.toggle('active', safeMode === 'gallery');
            sessionStorage.setItem(readyViewStorageKey, safeMode);
        };

        readyViewListBtn.addEventListener('click', () => applyViewMode('list'));
        readyViewGalleryBtn.addEventListener('click', () => applyViewMode('gallery'));
        applyViewMode(sessionStorage.getItem(readyViewStorageKey) || 'gallery');
        if (filterController) {
            filterController.apply();
        }
    </script>

</div>
</body>
</html>
