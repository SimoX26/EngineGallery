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
    <title>Engine Gallery • Magazzino</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=12">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="container">

    <div class="page-header-with-search">
        <div class="page-header">
            <h1>Magazzino</h1>
        </div>

        <div class="search-panel-compact">
            <label for="warehouseKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
            <input type="search"
                   id="warehouseKeywordSearch"
                   class="form-control"
                   placeholder="cerca...">
            <div id="warehouseKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                Nessun articolo corrisponde alla ricerca.
            </div>

            <div class="modal fade" id="warehouseFiltersModal" tabindex="-1" aria-labelledby="warehouseFiltersModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title fw-semibold" id="warehouseFiltersModalLabel">Filtri magazzino</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                        </div>
                        <div class="modal-body">
                            <label for="warehouseFiltersKeywordInput" class="form-label small mb-1">Parola chiave</label>
                            <input type="search" id="warehouseFiltersKeywordInput" class="form-control" placeholder="cerca...">
                        </div>
                        <div class="modal-footer filters-modal-footer">
                            <button type="button" id="warehouseFiltersResetBtn" class="btn btn-sm btn-outline-secondary filters-btn-secondary">Reimposta filtri</button>
                            <button type="button" id="warehouseFiltersApplyBtn" class="btn btn-sm btn-engine filters-btn-primary">Applica filtri</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="page-header-action-group">
            <div class="page-header-filter">
                <button class="btn btn-outline-secondary btn-sm"
                        type="button"
                        aria-expanded="false"
                        aria-controls="warehouseFiltersModal"
                        id="warehouseFiltersToggle">
                    Filtri
                </button>
            </div>

            <div class="page-header-actions page-header-actions--view">
                <div class="btn-group btn-group-sm page-header-view-switch" role="group" aria-label="Cambia vista magazzino">
                    <button type="button"
                            id="warehouseViewListBtn"
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
                            id="warehouseViewGalleryBtn"
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

            <div class="page-header-actions page-header-actions--add">
                <a href="<%= request.getContextPath() %>/warehouse/new" class="btn btn-sm btn-add-plus">
                    Aggiungi +
                </a>
            </div>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <div id="warehouseListView" class="engine-list-view d-none">
        <c:forEach var="item" items="${items}">
            <a href="<%= request.getContextPath() %>/warehouse/detail?id=${item.id}"
               class="engine-list-row warehouse-list-row-item"
               data-search="${fn:escapeXml(item.name)} ${fn:escapeXml(item.sku)} ${fn:escapeXml(item.location)} ${item.quantity} ${fn:escapeXml(item.notes)}">
                <div class="engine-image engine-image-empty d-flex align-items-center justify-content-center">
                    <span class="engine-image-empty-label">MAG</span>
                </div>
                <div class="engine-list-row__body">
                    <div class="engine-list-row__main">${item.name}</div>
                    <div class="engine-list-row__sub">Codice: <c:out value="${item.sku}" default="—" /></div>
                    <div class="engine-list-row__meta">
                        <span class="engine-list-row__intake-label">Qta:</span>
                        <span>${item.quantity}</span>
                        <span class="mx-1">•</span>
                        <span><c:out value="${item.location}" default="—" /></span>
                    </div>
                </div>
                <span class="badge-status ${item.quantity <= 0 ? 'status-stoccato' : 'status-ready'}">
                    ${item.quantity <= 0 ? 'Esaurito' : 'Disponibile'}
                </span>
            </a>
        </c:forEach>
    </div>

    <div class="customer-list" id="warehouseGalleryView">
        <c:forEach var="item" items="${items}">
            <a href="<%= request.getContextPath() %>/warehouse/detail?id=${item.id}"
               class="customer-card-link warehouse-card-item"
               data-search="${fn:escapeXml(item.name)} ${fn:escapeXml(item.sku)} ${fn:escapeXml(item.location)} ${item.quantity} ${fn:escapeXml(item.notes)}">
                <div class="card-base customer-card">
                    <div class="customer-row">
                        <div class="customer-field">
                            <div class="customer-main">${item.name}</div>
                            <div class="customer-meta">
                                Codice: <c:out value="${item.sku}" default="—" />
                            </div>
                        </div>

                        <div class="customer-field">
                            <div class="customer-meta">Disponibilita</div>
                            <div>${item.quantity}</div>
                        </div>

                        <div class="customer-field">
                            <div class="customer-meta">Ubicazione</div>
                            <div><c:out value="${item.location}" default="—" /></div>
                        </div>
                    </div>
                </div>
            </a>
        </c:forEach>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/live-search.js"></script>
<script>
    const searchInput = document.getElementById('warehouseKeywordSearch');
    const warehouseCards = document.querySelectorAll('.warehouse-card-item');
    const warehouseListRows = document.querySelectorAll('.warehouse-list-row-item');
    const emptyState = document.getElementById('warehouseKeywordEmptyState');
    const warehouseViewListBtn = document.getElementById('warehouseViewListBtn');
    const warehouseViewGalleryBtn = document.getElementById('warehouseViewGalleryBtn');
    const warehouseListView = document.getElementById('warehouseListView');
    const warehouseGalleryView = document.getElementById('warehouseGalleryView');
    const warehouseViewStorageKey = 'warehouse.viewMode';
    const warehouseFiltersToggle = document.getElementById('warehouseFiltersToggle');
    const warehouseFiltersModalEl = document.getElementById('warehouseFiltersModal');
    const warehouseFiltersModal = bootstrap.Modal.getOrCreateInstance(warehouseFiltersModalEl);
    const warehouseFiltersKeywordInput = document.getElementById('warehouseFiltersKeywordInput');
    const warehouseFiltersApplyBtn = document.getElementById('warehouseFiltersApplyBtn');
    const warehouseFiltersResetBtn = document.getElementById('warehouseFiltersResetBtn');

    const filterController = window.EngineGalleryLiveSearch && searchInput
        ? window.EngineGalleryLiveSearch.init({
            input: searchInput,
            groups: [
                { elements: warehouseCards },
                { elements: warehouseListRows }
            ],
            emptyState,
            debounceMs: 180
        })
        : null;

    warehouseFiltersToggle.addEventListener('click', () => {
        warehouseFiltersKeywordInput.value = searchInput.value;
        warehouseFiltersModal.show();
    });

    warehouseFiltersApplyBtn.addEventListener('click', () => {
        searchInput.value = warehouseFiltersKeywordInput.value || '';
        if (filterController) {
            filterController.apply();
        }
        warehouseFiltersModal.hide();
    });

    warehouseFiltersResetBtn.addEventListener('click', () => {
        warehouseFiltersKeywordInput.value = '';
        searchInput.value = '';
        if (filterController) {
            filterController.apply();
        }
        warehouseFiltersModal.hide();
    });

    const applyWarehouseViewMode = (mode) => {
        const safeMode = mode === 'list' ? 'list' : 'gallery';
        warehouseListView.classList.toggle('d-none', safeMode !== 'list');
        warehouseGalleryView.classList.toggle('d-none', safeMode !== 'gallery');
        warehouseViewListBtn.classList.toggle('active', safeMode === 'list');
        warehouseViewGalleryBtn.classList.toggle('active', safeMode === 'gallery');
        sessionStorage.setItem(warehouseViewStorageKey, safeMode);
    };

    warehouseViewListBtn.addEventListener('click', () => applyWarehouseViewMode('list'));
    warehouseViewGalleryBtn.addEventListener('click', () => applyWarehouseViewMode('gallery'));
    applyWarehouseViewMode(sessionStorage.getItem(warehouseViewStorageKey) || 'gallery');
    if (filterController) {
        filterController.apply();
    }
</script>

</body>
</html>
