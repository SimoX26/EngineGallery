<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Pronta Consegna • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=38">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container section-add-page">
    <div class="page-header-with-search">
        <div class="page-header"><h1>Pronta consegna</h1></div>
        <div class="search-panel-compact">
            <label for="readyDeliverySearch" class="form-label">Cerca motore</label>
            <input type="search" id="readyDeliverySearch" class="form-control" placeholder="cerca...">
        </div>
        <div class="page-header-actions page-header-actions--view">
            <div class="btn-group btn-group-sm page-header-view-switch" role="group" aria-label="Cambia vista pronta consegna">
                <button type="button" id="readyDeliveryViewListBtn" class="btn btn-outline-secondary engine-view-toggle-btn" title="Vista lista" aria-label="Vista lista">
                    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><line x1="5" y1="7" x2="19" y2="7"></line><line x1="5" y1="12" x2="19" y2="12"></line><line x1="5" y1="17" x2="19" y2="17"></line></svg>
                    <span class="visually-hidden">Lista</span>
                </button>
                <button type="button" id="readyDeliveryViewGalleryBtn" class="btn btn-outline-secondary active engine-view-toggle-btn" title="Vista galleria" aria-label="Vista galleria">
                    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><rect x="4" y="4" width="7" height="7" rx="1"></rect><rect x="13" y="4" width="7" height="7" rx="1"></rect><rect x="4" y="13" width="7" height="7" rx="1"></rect><rect x="13" y="13" width="7" height="7" rx="1"></rect></svg>
                    <span class="visually-hidden">Galleria</span>
                </button>
            </div>
        </div>
    </div>
    <div id="readyDeliveryEmptyState" class="system-empty-state mb-3 d-none" role="status">Nessun motore corrisponde alla ricerca.</div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Motore pronta consegna aggiunto.</div></c:if>

    <c:choose>
        <c:when test="${not empty engines}">
            <div id="readyDeliveryListView" class="engine-list-view d-none">
                <c:forEach var="engine" items="${engines}">
                    <article class="engine-list-row record-list-card ready-delivery-item--list"
                             data-search="${fn:escapeXml(engine.engineCode)} ${fn:escapeXml(engine.model)} ${fn:escapeXml(engine.notes)}">
                        <c:choose>
                            <c:when test="${not empty engine.imageFilename}">
                                <img class="record-list-card__media warehouse-preview-image warehouse-preview-image--list"
                                     src="${pageContext.request.contextPath}/ready-delivery/image?id=${engine.id}"
                                     alt="Foto di ${fn:escapeXml(engine.engineCode)}" loading="lazy">
                            </c:when>
                            <c:otherwise><div class="engine-image engine-image-empty record-list-card__media d-flex align-items-center justify-content-center"><span class="engine-image-empty-label">MOTORE</span></div></c:otherwise>
                        </c:choose>
                        <div class="engine-list-row__body record-list-card__content">
                            <div class="record-card__eyebrow">Motore pronta consegna</div>
                            <div class="engine-list-row__main record-card__title"><c:out value="${engine.engineCode}"/></div>
                            <div class="engine-list-row__sub record-card__subtitle"><c:out value="${engine.model}"/></div>
                            <c:if test="${not empty engine.notes}"><div class="record-card__summary"><c:out value="${engine.notes}"/></div></c:if>
                        </div>
                        <span class="badge-status ${engine.quantity > 0 ? 'status-ready' : 'status-stoccato'}">${engine.quantity > 0 ? 'Disponibili:' : 'Esaurito ·'} <c:out value="${engine.quantity}"/></span>
                    </article>
                </c:forEach>
            </div>
            <div id="readyDeliveryGalleryView" class="record-gallery-grid">
                <c:forEach var="engine" items="${engines}">
                    <article class="record-gallery-card ready-delivery-item--gallery"
                             data-search="${fn:escapeXml(engine.engineCode)} ${fn:escapeXml(engine.model)} ${fn:escapeXml(engine.notes)}">
                        <c:choose>
                            <c:when test="${not empty engine.imageFilename}">
                                <img class="warehouse-preview-image warehouse-preview-image--gallery"
                                     src="${pageContext.request.contextPath}/ready-delivery/image?id=${engine.id}"
                                     alt="Foto di ${fn:escapeXml(engine.engineCode)}" loading="lazy">
                            </c:when>
                            <c:otherwise><div class="workshop-machine-card__image workshop-machine-card__empty" aria-hidden="true">Nessuna foto</div></c:otherwise>
                        </c:choose>
                        <div class="record-card__body">
                            <span class="record-card__eyebrow">Motore pronta consegna</span>
                            <span class="record-card__title"><c:out value="${engine.engineCode}"/></span>
                            <span class="record-card__subtitle"><c:out value="${engine.model}"/></span>
                            <c:if test="${not empty engine.notes}"><span class="record-card__summary"><c:out value="${engine.notes}"/></span></c:if>
                        </div>
                        <div class="record-card__footer">
                            <span class="small text-muted">Inserito da <c:out value="${engine.createdBy}" default="—"/></span>
                            <span class="badge-status ${engine.quantity > 0 ? 'status-ready' : 'status-stoccato'}">${engine.quantity > 0 ? 'Disponibili:' : 'Esaurito ·'} <c:out value="${engine.quantity}"/></span>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise><div class="system-empty-state" role="status">Nessun motore pronta consegna registrato.</div></c:otherwise>
    </c:choose>

    <a class="section-add-fab" href="${pageContext.request.contextPath}/ready-delivery?view=new">
        <span aria-hidden="true">+</span> Aggiungi motore
    </a>
</main>
<script src="${pageContext.request.contextPath}/assets/js/live-search.js"></script>
<script>
    const readyDeliveryViewListBtn = document.getElementById('readyDeliveryViewListBtn');
    const readyDeliveryViewGalleryBtn = document.getElementById('readyDeliveryViewGalleryBtn');
    const readyDeliveryListView = document.getElementById('readyDeliveryListView');
    const readyDeliveryGalleryView = document.getElementById('readyDeliveryGalleryView');
    if (readyDeliveryListView && readyDeliveryGalleryView) {
        const applyViewMode = (mode) => {
            const list = mode === 'list';
            readyDeliveryListView.classList.toggle('d-none', !list);
            readyDeliveryGalleryView.classList.toggle('d-none', list);
            readyDeliveryViewListBtn.classList.toggle('active', list);
            readyDeliveryViewGalleryBtn.classList.toggle('active', !list);
            sessionStorage.setItem('readyDelivery.viewMode', list ? 'list' : 'gallery');
        };
        readyDeliveryViewListBtn.addEventListener('click', () => applyViewMode('list'));
        readyDeliveryViewGalleryBtn.addEventListener('click', () => applyViewMode('gallery'));
        applyViewMode(sessionStorage.getItem('readyDelivery.viewMode'));
        window.EngineGalleryLiveSearch.init({
            input: document.getElementById('readyDeliverySearch'),
            groups: [
                {elements: document.querySelectorAll('.ready-delivery-item--list')},
                {elements: document.querySelectorAll('.ready-delivery-item--gallery')}
            ],
            emptyState: document.getElementById('readyDeliveryEmptyState'),
            debounceMs: 180
        });
    }
</script>
</body>
</html>
