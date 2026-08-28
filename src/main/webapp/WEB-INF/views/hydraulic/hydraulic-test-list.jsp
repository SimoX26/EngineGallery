<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Prove idrauliche</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=15">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <div class="page-header-with-search">
            <div class="page-header">
                <h1>Prove idrauliche</h1>
            </div>

            <div class="search-panel-compact">
                <label for="hydraulicKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
                <input type="search"
                       id="hydraulicKeywordSearch"
                       class="form-control"
                       value="${fn:escapeXml(keyword)}"
                       placeholder="cerca...">
                <div id="hydraulicKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                    Nessuna prova idraulica corrisponde alla ricerca.
                </div>

                <div class="modal fade" id="hydraulicFiltersModal" tabindex="-1" aria-labelledby="hydraulicFiltersModalLabel" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title fw-semibold" id="hydraulicFiltersModalLabel">Filtri prove idrauliche</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
                            </div>
                            <div class="modal-body">
                                <label for="hydraulicFiltersKeywordInput" class="form-label small mb-1">Parola chiave</label>
                                <input type="search" id="hydraulicFiltersKeywordInput" class="form-control" placeholder="cerca...">
                            </div>
                            <div class="modal-footer filters-modal-footer">
                                <button type="button" id="hydraulicFiltersResetBtn" class="btn btn-sm btn-outline-secondary filters-btn-secondary">Reimposta filtri</button>
                                <button type="button" id="hydraulicFiltersApplyBtn" class="btn btn-sm btn-engine filters-btn-primary">Applica filtri</button>
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
                            aria-controls="hydraulicFiltersModal"
                            id="hydraulicFiltersToggle">
                        Filtri
                    </button>
                </div>

                <div class="page-header-actions page-header-actions--view">
                    <div class="btn-group btn-group-sm page-header-view-switch" role="group" aria-label="Cambia vista prove idrauliche">
                        <button type="button"
                                id="hydraulicViewListBtn"
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
                                id="hydraulicViewGalleryBtn"
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
                    <a class="btn btn-sm btn-add-plus" href="<%= request.getContextPath() %>/hydraulic-test/new">
                        Aggiungi +
                    </a>
                </div>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-warning">${error}</div>
        </c:if>

        <c:choose>
            <c:when test="${not empty hydraulicTests}">
                <div id="hydraulicListView" class="engine-list-view d-none">
                    <c:forEach var="test" items="${hydraulicTests}">
                        <a class="engine-list-row record-list-card hydraulic-list-row-item"
                           href="${pageContext.request.contextPath}/hydraulic-test/detail?id=${test.id}"
                           data-search="${fn:escapeXml(test.customerName)} ${fn:escapeXml(test.engineCode)} ${fn:escapeXml(test.notes)} ${fn:escapeXml(test.createdBy)} ${fn:escapeXml(test.createdAtLabel)} ${test.testDate}">
                            <div class="engine-image engine-image-empty record-list-card__media d-flex align-items-center justify-content-center">
                                <span class="engine-image-empty-label">VIDEO</span>
                            </div>
                            <div class="engine-list-row__body record-list-card__content">
                                <div class="record-card__eyebrow">Prova idraulica</div>
                                <div class="engine-list-row__main record-card__title"><c:out value="${test.engineCode}" default="Codice non disponibile" /></div>
                                <div class="engine-list-row__sub record-card__subtitle"><c:out value="${test.customerName}" default="Cliente non disponibile" /></div>
                                <div class="record-card__facts">
                                    <fmt:parseDate value="${test.testDate}" pattern="yyyy-MM-dd" var="hydraulicTestDateListParsed" />
                                    <span class="record-card__fact"><strong>Data prova</strong> <fmt:formatDate value="${hydraulicTestDateListParsed}" pattern="dd / MM / yyyy" /></span>
                                </div>
                                <c:if test="${not empty test.notes}">
                                    <div class="record-card__summary"><c:out value="${test.notes}" /></div>
                                </c:if>
                                <div class="record-card__audit">
                                    <span class="record-card__audit-dot" aria-hidden="true"></span>
                                    <span class="record-card__audit-copy">
                                        <span>Inserito da <strong><c:out value="${test.createdBy}" default="Utente non disponibile" /></strong></span>
                                        <span><c:out value="${test.createdAtLabel}" default="Data e ora non disponibili" /></span>
                                    </span>
                                </div>
                            </div>
                            <span class="badge-status status-stoccato">Test</span>
                        </a>
                    </c:forEach>
                </div>

                <div class="record-gallery-grid" id="hydraulicGalleryGrid">
                    <c:forEach var="test" items="${hydraulicTests}">
                        <div class="hydraulic-card-col"
                             data-search="${fn:escapeXml(test.customerName)} ${fn:escapeXml(test.engineCode)} ${fn:escapeXml(test.notes)} ${fn:escapeXml(test.createdBy)} ${fn:escapeXml(test.createdAtLabel)} ${test.testDate}">
                            <a class="engine-card-link" href="${pageContext.request.contextPath}/hydraulic-test/detail?id=${test.id}">
                            <article class="engine-gallery-card record-gallery-card">
                                <div class="hydraulic-video-wrap">
                                    <video class="hydraulic-video" muted playsinline preload="metadata">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(test.videoUrl, 'http://') || fn:startsWith(test.videoUrl, 'https://')}">
                                                <source src="${fn:escapeXml(test.videoUrl)}">
                                            </c:when>
                                            <c:otherwise>
                                                <source src="${pageContext.request.contextPath}/uploads/hydraulic/${fn:escapeXml(test.videoUrl)}">
                                            </c:otherwise>
                                        </c:choose>
                                        Il browser non supporta la riproduzione video.
                                    </video>
                                </div>

                                <div class="engine-body record-card__body">
                                    <div class="record-card__eyebrow">Prova idraulica</div>
                                    <div class="engine-code record-card__title"><c:out value="${test.engineCode}" default="Codice non disponibile" /></div>
                                    <div class="engine-client record-card__subtitle"><c:out value="${test.customerName}" default="Cliente non disponibile" /></div>
                                    <div class="record-card__facts">
                                        <fmt:parseDate value="${test.testDate}" pattern="yyyy-MM-dd" var="hydraulicTestDateParsed" />
                                        <span class="record-card__fact"><strong>Data prova</strong> <fmt:formatDate value="${hydraulicTestDateParsed}" pattern="dd / MM / yyyy" /></span>
                                    </div>
                                    <c:if test="${not empty test.notes}">
                                        <div class="hydraulic-notes record-card__summary"><c:out value="${test.notes}" /></div>
                                    </c:if>
                                </div>
                                <div class="record-card__footer">
                                    <div class="record-card__audit">
                                        <span class="record-card__audit-dot" aria-hidden="true"></span>
                                        <span class="record-card__audit-copy">
                                            <span>Inserito da <strong><c:out value="${test.createdBy}" default="Utente non disponibile" /></strong></span>
                                            <span><c:out value="${test.createdAtLabel}" default="Data e ora non disponibili" /></span>
                                        </span>
                                    </div>
                                    <span class="badge-status status-stoccato">Test</span>
                                </div>
                            </article>
                            </a>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-light border">Nessuna prova idraulica disponibile.</div>
            </c:otherwise>
        </c:choose>

    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/live-search.js"></script>
    <script>
        const searchInput = document.getElementById('hydraulicKeywordSearch');
        const hydraulicCards = document.querySelectorAll('.hydraulic-card-col');
        const hydraulicListRows = document.querySelectorAll('.hydraulic-list-row-item');
        const emptyState = document.getElementById('hydraulicKeywordEmptyState');
        const hydraulicViewListBtn = document.getElementById('hydraulicViewListBtn');
        const hydraulicViewGalleryBtn = document.getElementById('hydraulicViewGalleryBtn');
        const hydraulicListView = document.getElementById('hydraulicListView');
        const hydraulicGalleryView = document.getElementById('hydraulicGalleryGrid');
        const hydraulicViewStorageKey = 'hydraulic.viewMode';
        const hydraulicFiltersToggle = document.getElementById('hydraulicFiltersToggle');
        const hydraulicFiltersModalEl = document.getElementById('hydraulicFiltersModal');
        const hydraulicFiltersModal = bootstrap.Modal.getOrCreateInstance(hydraulicFiltersModalEl);
        const hydraulicFiltersKeywordInput = document.getElementById('hydraulicFiltersKeywordInput');
        const hydraulicFiltersApplyBtn = document.getElementById('hydraulicFiltersApplyBtn');
        const hydraulicFiltersResetBtn = document.getElementById('hydraulicFiltersResetBtn');

        const filterController = window.EngineGalleryLiveSearch && searchInput
            ? window.EngineGalleryLiveSearch.init({
                input: searchInput,
                groups: [
                    { elements: hydraulicCards },
                    { elements: hydraulicListRows }
                ],
                emptyState,
                debounceMs: 180
            })
            : null;

        hydraulicFiltersToggle.addEventListener('click', () => {
            hydraulicFiltersKeywordInput.value = searchInput.value;
            hydraulicFiltersModal.show();
        });

        hydraulicFiltersApplyBtn.addEventListener('click', () => {
            searchInput.value = hydraulicFiltersKeywordInput.value || '';
            if (filterController) {
                filterController.apply();
            }
            hydraulicFiltersModal.hide();
        });

        hydraulicFiltersResetBtn.addEventListener('click', () => {
            hydraulicFiltersKeywordInput.value = '';
            searchInput.value = '';
            if (filterController) {
                filterController.apply();
            }
            hydraulicFiltersModal.hide();
        });
        const applyHydraulicViewMode = (mode) => {
            const safeMode = mode === 'list' ? 'list' : 'gallery';
            hydraulicListView.classList.toggle('d-none', safeMode !== 'list');
            hydraulicGalleryView.classList.toggle('d-none', safeMode !== 'gallery');
            hydraulicViewListBtn.classList.toggle('active', safeMode === 'list');
            hydraulicViewGalleryBtn.classList.toggle('active', safeMode === 'gallery');
            sessionStorage.setItem(hydraulicViewStorageKey, safeMode);
        };

        hydraulicViewListBtn.addEventListener('click', () => applyHydraulicViewMode('list'));
        hydraulicViewGalleryBtn.addEventListener('click', () => applyHydraulicViewMode('gallery'));
        applyHydraulicViewMode(sessionStorage.getItem(hydraulicViewStorageKey) || 'gallery');
        if (filterController) {
            filterController.apply();
        }
    </script>

</div>
</body>
</html>
