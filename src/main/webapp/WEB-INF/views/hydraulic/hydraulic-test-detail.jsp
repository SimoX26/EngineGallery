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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=12">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container mt-5 mb-4">
    <div class="row g-4 card-base">
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

        <div class="row mt-3">
            <div class="col-12 d-flex justify-content-center">
                <a href="<%= request.getContextPath() %>/hydraulic-test/list" class="btn btn-outline-secondary px-4">
                    Indietro
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
