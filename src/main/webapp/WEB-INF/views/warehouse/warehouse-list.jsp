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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=41">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>

<div class="container section-add-page">
    <c:url var="warehouseFuelUrl" value="/warehouse/list"><c:param name="department" value="Alimentazione"/></c:url>
    <c:url var="warehouseEngineUrl" value="/warehouse/list"><c:param name="department" value="Motori"/></c:url>
    <c:url var="warehouseTimingUrl" value="/warehouse/list"><c:param name="department" value="Distribuzione"/></c:url>
    <c:url var="warehouseOtherUrl" value="/warehouse/list"><c:param name="department" value="Altri ricambi"/></c:url>

    <div class="page-header-with-search warehouse-list-header" id="warehouse-items">
        <div class="page-header"><h2 class="mb-0">Magazzino</h2></div>

        <div class="search-panel-compact">
            <label for="warehouseKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
            <input type="search"
                   id="warehouseKeywordSearch"
                   class="form-control"
                   placeholder="cerca..."
                   value="<c:out value='${param.keyword}'/>">
            <div class="modal fade filters-modal" id="warehouseFiltersModal" tabindex="-1" aria-labelledby="warehouseFiltersModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
                    <form class="modal-content" method="get" action="${pageContext.request.contextPath}/warehouse/list">
                        <input type="hidden" id="warehouseFiltersKeywordInput" name="keyword" value="<c:out value='${param.keyword}'/>">
                        <div class="modal-header">
                            <h5 class="modal-title fw-semibold" id="warehouseFiltersModalLabel">Filtri magazzino</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                        </div>
                        <div class="modal-body">
                            <label for="warehouseFiltersComponentInput" class="form-label small mb-1">Tipologia componente</label>
                            <select id="warehouseFiltersComponentInput" name="componentType" class="form-select">
                                <option value="" ${empty selectedComponentType ? 'selected' : ''}>Tutte le tipologie</option>
                                <c:forEach var="type" items="${componentTypes}">
                                    <option value="${type.code}" ${selectedComponentType == type.code ? 'selected' : ''}><c:out value="${type.label}" /></option>
                                </c:forEach>
                            </select>
                            <div class="row g-2 mt-1">
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterDepartment">Reparto</label>
                                    <select id="warehouseFilterDepartment" name="department" class="form-select">
                                        <option value="">Tutti i reparti</option>
                                        <option value="Alimentazione" ${param.department == 'Alimentazione' ? 'selected' : ''}>Alimentazione</option>
                                        <option value="Motori" ${param.department == 'Motori' ? 'selected' : ''}>Motori</option>
                                        <option value="Distribuzione" ${param.department == 'Distribuzione' ? 'selected' : ''}>Distribuzione</option>
                                        <option value="Altri ricambi" ${param.department == 'Altri ricambi' ? 'selected' : ''}>Altri ricambi</option>
                                    </select></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterCategory">Categoria</label>
                                    <select id="warehouseFilterCategory" name="categoryId" class="form-select">
                                        <option value="">Tutte le categorie</option>
                                        <c:forEach var="category" items="${categories}"><option value="${category.id}" ${param.categoryId == category.id ? 'selected' : ''}><c:out value="${category.department}"/> · <c:out value="${category.name}"/></option></c:forEach>
                                    </select></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterManufacturer">Produttore</label><input id="warehouseFilterManufacturer" class="form-control" name="manufacturer" value="<c:out value='${param.manufacturer}'/>"></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterSku">Codice ricambio</label><input id="warehouseFilterSku" class="form-control" name="sku" value="<c:out value='${param.sku}'/>"></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterEngine">Codice motore</label><input id="warehouseFilterEngine" class="form-control" name="engineCode" value="<c:out value='${param.engineCode}'/>"></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterCondition">Condizione</label><input id="warehouseFilterCondition" class="form-control" name="condition" value="<c:out value='${param.condition}'/>"></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterLocation">Ubicazione</label><input id="warehouseFilterLocation" class="form-control" name="location" value="<c:out value='${param.location}'/>"></div>
                                <div class="col-12 col-sm-6"><label class="form-label small mb-1" for="warehouseFilterLot">Lotto</label><input id="warehouseFilterLot" class="form-control" name="lotCode" value="<c:out value='${param.lotCode}'/>"></div>
                            </div>
                        </div>
                        <div class="modal-footer filters-modal-footer">
                            <button type="button" id="warehouseFiltersResetBtn" class="btn btn-sm filters-btn-secondary">Rimuovi filtri</button>
                            <button type="submit" class="btn btn-sm filters-btn-primary">Applica filtri</button>
                        </div>
                    </form>
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

        </div>
    </div>

    <section class="warehouse-overview" aria-label="Reparti del magazzino">
        <nav class="warehouse-overview__grid" aria-label="Reparti del magazzino">
            <a class="warehouse-overview__card ${param.department == 'Alimentazione' ? 'is-active' : ''}" href="<c:out value='${warehouseFuelUrl}'/>#warehouse-items" aria-current="${param.department == 'Alimentazione' ? 'page' : 'false'}">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 21V4a1 1 0 0 1 1-1h8a1 1 0 0 1 1 1v17M4 9h10M3 21h13M14 7l3 2v7a2 2 0 0 0 4 0v-5l-3-3"/></svg>
                <span class="warehouse-overview__card-title">Alimentazione</span>
                <span class="warehouse-overview__description">Iniettori, rail, pompe</span>
            </a>
            <a class="warehouse-overview__card ${param.department == 'Motori' ? 'is-active' : ''}" href="<c:out value='${warehouseEngineUrl}'/>#warehouse-items" aria-current="${param.department == 'Motori' ? 'page' : 'false'}">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14 5a5 5 0 0 0-6 6L3 16a2 2 0 0 0 3 3l5-5a5 5 0 0 0 6-6l-3 3-3-3 3-3Z"/></svg>
                <span class="warehouse-overview__card-title">Motori</span>
                <span class="warehouse-overview__description">Testate, alberi motore, pistoni</span>
            </a>
            <a class="warehouse-overview__card ${param.department == 'Distribuzione' ? 'is-active' : ''}" href="<c:out value='${warehouseTimingUrl}'/>#warehouse-items" aria-current="${param.department == 'Distribuzione' ? 'page' : 'false'}">
                <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="3"/><path d="m10 2-.5 2.2-2.1 1.2-2.1-.7-2 3.4 1.6 1.6v2.5L3.3 14l2 3.4 2.1-.7 2.1 1.2L10 20h4l.5-2.1 2.1-1.2 2.1.7 2-3.4-1.6-1.8V9.7L20.7 8l-2-3.4-2.1.7-2.1-1.2L14 2h-4Z"/></svg>
                <span class="warehouse-overview__card-title">Distribuzione</span>
                <span class="warehouse-overview__description">Componenti della distribuzione</span>
            </a>
            <a class="warehouse-overview__card ${param.department == 'Altri ricambi' ? 'is-active' : ''}" href="<c:out value='${warehouseOtherUrl}'/>#warehouse-items" aria-current="${param.department == 'Altri ricambi' ? 'page' : 'false'}">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 2 9 5v10l-9 5-9-5V7l9-5ZM3 7l9 5 9-5M12 12v10"/></svg>
                <span class="warehouse-overview__card-title">Altri ricambi</span>
                <span class="warehouse-overview__description">Turbocompressori e componenti vari</span>
            </a>
        </nav>
    </section>

    <div id="warehouseKeywordEmptyState" class="system-empty-state mb-3 ${empty items ? '' : 'd-none'}" role="status">
        Nessun articolo da mostrare.
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <div id="warehouseListView" class="engine-list-view d-none">
        <c:forEach var="item" items="${items}">
            <c:set var="previewFilename" value="${previewImages[item.id]}"/>
            <a href="<%= request.getContextPath() %>/warehouse/detail?id=${item.id}"
               class="engine-list-row record-list-card warehouse-list-row-item"
               data-search="${fn:escapeXml(item.componentType.label)} ${fn:escapeXml(item.details.searchText)} ${fn:escapeXml(item.name)} ${fn:escapeXml(item.sku)} ${fn:escapeXml(item.location)} ${item.quantity} ${fn:escapeXml(item.notes)} ${fn:escapeXml(item.createdBy)} ${fn:escapeXml(item.createdAtLabel)}">
                <c:choose>
                    <c:when test="${not empty previewFilename}">
                        <img class="record-list-card__media warehouse-preview-image warehouse-preview-image--list"
                             src="${pageContext.request.contextPath}/uploads/warehouse/${item.id}/${fn:escapeXml(previewFilename)}"
                             alt="Anteprima di ${fn:escapeXml(item.name)}" loading="lazy">
                    </c:when>
                    <c:otherwise>
                        <div class="engine-image engine-image-empty record-list-card__media d-flex align-items-center justify-content-center">
                            <span class="engine-image-empty-label">MAG</span>
                        </div>
                    </c:otherwise>
                </c:choose>
                <div class="engine-list-row__body record-list-card__content">
                    <div class="record-card__eyebrow"><c:out value="${item.details.department}" default="Magazzino"/> · <c:out value="${item.details.categoryName}" default="${item.componentType.label}"/> · Codice <c:out value="${item.sku}" default="non disponibile" /></div>
                    <div class="engine-list-row__main record-card__title"><c:out value="${item.name}" /></div>
                    <div class="record-card__facts">
                        <span class="record-card__fact"><strong>Quantità</strong> <c:out value="${item.quantity}" /></span>
                        <span class="record-card__fact"><strong>Ubicazione</strong> <c:out value="${item.location}" default="Non disponibile" /></span>
                    </div>
                    <div class="record-card__audit">
                        <span class="record-card__audit-dot" aria-hidden="true"></span>
                        <span class="record-card__audit-copy">
                            <span>Inserito da <strong><c:out value="${item.createdBy}" default="Utente non disponibile" /></strong></span>
                            <span><c:out value="${item.createdAtLabel}" default="Data e ora non disponibili" /></span>
                        </span>
                    </div>
                </div>
                <span class="badge-status ${item.quantity <= 0 ? 'status-stoccato' : 'status-ready'}">
                    ${item.quantity <= 0 ? 'Esaurito' : 'Disponibile'}
                </span>
            </a>
        </c:forEach>
    </div>

    <div class="record-gallery-grid" id="warehouseGalleryView">
        <c:forEach var="item" items="${items}">
            <c:set var="previewFilename" value="${previewImages[item.id]}"/>
            <a href="<%= request.getContextPath() %>/warehouse/detail?id=${item.id}"
               class="customer-card-link warehouse-card-item"
               data-search="${fn:escapeXml(item.componentType.label)} ${fn:escapeXml(item.details.searchText)} ${fn:escapeXml(item.name)} ${fn:escapeXml(item.sku)} ${fn:escapeXml(item.location)} ${item.quantity} ${fn:escapeXml(item.notes)} ${fn:escapeXml(item.createdBy)} ${fn:escapeXml(item.createdAtLabel)}">
                <article class="record-gallery-card">
                    <c:choose>
                        <c:when test="${not empty previewFilename}">
                            <img class="warehouse-preview-image warehouse-preview-image--gallery"
                                 src="${pageContext.request.contextPath}/uploads/warehouse/${item.id}/${fn:escapeXml(previewFilename)}"
                                 alt="Anteprima di ${fn:escapeXml(item.name)}" loading="lazy">
                        </c:when>
                        <c:otherwise>
                            <div class="record-card__visual record-card__visual--warehouse" aria-hidden="true">
                                <span>MAG</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <div class="record-card__body">
                        <div class="record-card__eyebrow"><c:out value="${item.details.department}" default="Magazzino"/> · <c:out value="${item.details.categoryName}" default="${item.componentType.label}"/> · Codice <c:out value="${item.sku}" default="non disponibile" /></div>
                        <div class="record-card__title"><c:out value="${item.name}" /></div>
                        <div class="record-card__facts">
                            <span class="record-card__fact"><strong>Quantità</strong> <c:out value="${item.quantity}" /></span>
                            <span class="record-card__fact"><strong>Ubicazione</strong> <c:out value="${item.location}" default="Non disponibile" /></span>
                        </div>
                    </div>
                    <div class="record-card__footer">
                        <div class="record-card__audit">
                            <span class="record-card__audit-dot" aria-hidden="true"></span>
                            <span class="record-card__audit-copy">
                                <span>Inserito da <strong><c:out value="${item.createdBy}" default="Utente non disponibile" /></strong></span>
                                <span><c:out value="${item.createdAtLabel}" default="Data e ora non disponibili" /></span>
                            </span>
                        </div>
                        <span class="badge-status ${item.quantity <= 0 ? 'status-stoccato' : 'status-ready'}">
                            ${item.quantity <= 0 ? 'Esaurito' : 'Disponibile'}
                        </span>
                    </div>
                </article>
            </a>
        </c:forEach>
    </div>
</div>

<a class="section-add-fab" href="<%= request.getContextPath() %>/warehouse/new">
    <span aria-hidden="true">+</span> Aggiungi articolo
</a>

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
    const warehouseFiltersComponentInput = document.getElementById('warehouseFiltersComponentInput');
    const warehouseFiltersKeywordInput = document.getElementById('warehouseFiltersKeywordInput');
    const warehouseFiltersResetBtn = document.getElementById('warehouseFiltersResetBtn');

    const filterController = window.EngineGalleryLiveSearch && searchInput
        ? window.EngineGalleryLiveSearch.init({
            input: searchInput,
            groups: [
                { elements: warehouseCards },
                { elements: warehouseListRows }
            ],
            emptyState,
            showEmptyWhenBlank: true,
            debounceMs: 180
        })
        : null;

    warehouseFiltersToggle.addEventListener('click', () => {
        warehouseFiltersComponentInput.value = new URLSearchParams(window.location.search).get('componentType') || '';
        warehouseFiltersKeywordInput.value = searchInput.value;
        warehouseFiltersModal.show();
    });

    warehouseFiltersResetBtn.addEventListener('click', () => {
        window.location.assign(window.location.pathname);
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
