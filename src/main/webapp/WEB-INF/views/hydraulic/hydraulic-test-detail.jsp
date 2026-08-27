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
    <title>Engine Gallery • Dettaglio prova idraulica</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=14">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<c:url var="hydraulicEditUrl" value="/hydraulic-test/edit"><c:param name="id" value="${hydraulicTest.id}"/></c:url>
<c:url var="hydraulicDeleteUrl" value="/hydraulic-test/delete"><c:param name="id" value="${hydraulicTest.id}"/></c:url>

<div class="container mt-5 mb-4 detail-has-mobile-actions">
    <div class="row g-4 card-base">
        <div class="col-12 detail-mobile-actions"><div class="detail-mobile-toolbar">
            <a href="<%= request.getContextPath() %>/hydraulic-test/list" class="share-icon-btn detail-round-btn" aria-label="Torna alla lista" title="Torna alla lista"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14.5 5.5L8 12l6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg></a>
            <a href="<c:out value='${hydraulicEditUrl}'/>" class="share-icon-btn detail-round-btn" aria-label="Modifica prova" title="Modifica prova"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20l4.2-1 9.8-9.8-3.2-3.2L5 15.8 4 20z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M13.8 6.2l3.2 3.2" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></a>
            <a href="<c:out value='${hydraulicDeleteUrl}'/>" class="share-icon-btn detail-round-btn detail-round-btn--delete" aria-label="Elimina prova" title="Elimina prova"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 7h14M9 7V5h6v2M8 7l1 12h6l1-12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></a>
            <button type="button" class="share-icon-btn detail-round-btn detail-round-btn--share js-media-share-btn" data-share-source="#hydraulicShareMedia" aria-label="Condividi video" title="Condividi video"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="18" cy="5" r="3" fill="none" stroke="currentColor" stroke-width="2.25"/><circle cx="6" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="2.25"/><circle cx="18" cy="19" r="3" fill="none" stroke="currentColor" stroke-width="2.25"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4" fill="none" stroke="currentColor" stroke-width="2.25"/></svg></button>
        </div></div>
        <div class="col-lg-5">
            <h5 class="fw-semibold mb-3">Dettaglio prova</h5>
            <dl class="engine-detail-list mb-0">
                <dt>ID prova:</dt>
                <dd>${hydraulicTest.id}</dd>

                <dt>Cliente:</dt>
                <dd><c:out value="${hydraulicTest.customerName}" default="—"/></dd>

                <dt>Codice motore:</dt>
                <dd><c:out value="${hydraulicTest.engineCode}" default="—"/></dd>

                <dt>Data prova:</dt>
                <dd>
                    <fmt:parseDate value="${hydraulicTest.testDate}" pattern="yyyy-MM-dd" var="hydraulicTestDateParsed" />
                    <fmt:formatDate value="${hydraulicTestDateParsed}" pattern="dd / MM / yyyy" />
                </dd>

                <dt>Note:</dt>
                <dd><c:out value="${hydraulicTest.notes}" default="—"/></dd>
            </dl>
        </div>
        <div id="hydraulicShareMedia" class="d-none"><c:choose><c:when test="${fn:startsWith(hydraulicTest.videoUrl, 'http://') || fn:startsWith(hydraulicTest.videoUrl, 'https://')}"><span data-media-url="${fn:escapeXml(hydraulicTest.videoUrl)}"></span></c:when><c:otherwise><span data-media-url="${pageContext.request.contextPath}/uploads/hydraulic/${fn:escapeXml(hydraulicTest.videoUrl)}"></span></c:otherwise></c:choose></div>

        <div class="col-lg-7">
            <h5 class="fw-semibold mb-3">Video prova</h5>
            <div class="hydraulic-video-wrap rounded">
                <video class="hydraulic-video" controls playsinline preload="metadata">
                    <c:choose>
                        <c:when test="${fn:startsWith(hydraulicTest.videoUrl, 'http://') || fn:startsWith(hydraulicTest.videoUrl, 'https://')}">
                            <source src="${fn:escapeXml(hydraulicTest.videoUrl)}">
                        </c:when>
                        <c:otherwise>
                            <source src="${pageContext.request.contextPath}/uploads/hydraulic/${fn:escapeXml(hydraulicTest.videoUrl)}">
                        </c:otherwise>
                    </c:choose>
                    Il browser non supporta la riproduzione video.
                </video>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-12 detail-actions engine-detail-actions-toolbar d-flex flex-wrap justify-content-center">
                <a href="<%= request.getContextPath() %>/hydraulic-test/list" class="btn engine-detail-action-btn engine-detail-action-btn--back">
                    Indietro
                </a>
                <a href="<c:out value='${hydraulicEditUrl}'/>" class="btn engine-detail-action-btn engine-detail-action-btn--edit">Modifica</a>
                <a href="<c:out value='${hydraulicDeleteUrl}'/>" class="btn engine-detail-action-btn engine-detail-action-btn--delete">Elimina</a>
                <button type="button" class="btn engine-detail-action-btn engine-detail-action-btn--share js-media-share-btn" data-share-source="#hydraulicShareMedia">Condividi</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/media-share.js"></script>
</body>
</html>
