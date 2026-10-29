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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=40">
</head>

<body>

<div class="engine-gallery-page">

    <jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>

    <div class="container section-add-page">

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
        </div>

        <c:if test="${not empty hydraulicTests}">
            <div id="hydraulicKeywordEmptyState" class="system-empty-state mb-3 d-none" role="status">
                Nessuna prova idraulica corrisponde alla ricerca.
            </div>
        </c:if>

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
                            <div class="record-list-card__media hydraulic-list-preview">
                                <video class="hydraulic-video-preview" muted playsinline preload="none">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(test.videoUrl, 'http://') || fn:startsWith(test.videoUrl, 'https://')}">
                                            <source src="${fn:escapeXml(test.videoUrl)}">
                                        </c:when>
                                        <c:otherwise>
                                            <source src="${pageContext.request.contextPath}/uploads/hydraulic/${fn:escapeXml(test.videoUrl)}">
                                        </c:otherwise>
                                    </c:choose>
                                </video>
                                <span class="hydraulic-video-badge" aria-hidden="true">Video</span>
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
                                    <video class="hydraulic-video hydraulic-video-preview" muted playsinline preload="none">
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
                                    <span class="hydraulic-video-badge" aria-hidden="true">Video</span>
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
                <div class="system-empty-state" role="status">Nessuna prova idraulica disponibile.</div>
            </c:otherwise>
        </c:choose>

    </div>

    <a class="section-add-fab" href="<%= request.getContextPath() %>/hydraulic-test/new">
        <span aria-hidden="true">+</span> Aggiungi prova idraulica
    </a>

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

        const videoPreviews = document.querySelectorAll('.hydraulic-video-preview');
        const loadVideoPreview = (video) => {
            if (video.dataset.previewLoaded) return;
            video.dataset.previewLoaded = '1';
            video.addEventListener('loadedmetadata', () => {
                const seekTo = Number.isFinite(video.duration) && video.duration > 0
                    ? Math.min(1, video.duration / 2) : 1;
                try { video.currentTime = seekTo; } catch (ignored) { }
            }, {once: true});
            video.preload = 'metadata';
            video.load();
        };
        if ('IntersectionObserver' in window) {
            const previewObserver = new IntersectionObserver((entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) {
                        loadVideoPreview(entry.target);
                        previewObserver.unobserve(entry.target);
                    }
                });
            });
            videoPreviews.forEach((video) => previewObserver.observe(video));
        } else {
            videoPreviews.forEach(loadVideoPreview);
        }
    </script>

</div>
</body>
</html>
