<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Prove idrauliche</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=8">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <div class="page-header-with-search">
            <div class="page-header d-flex align-items-center justify-content-between gap-2 flex-wrap">
                <h1 class="mb-0">Prove idrauliche</h1>
                <a class="btn-engine" href="<%= request.getContextPath() %>/hydraulic-test/new">
                    Aggiungi prova idraulica
                </a>
            </div>

            <div class="card-base search-panel-compact">
                <label for="hydraulicKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
                <input type="search"
                       id="hydraulicKeywordSearch"
                       class="form-control"
                       value="${fn:escapeXml(keyword)}"
                       placeholder="Cerca per cliente, codice motore o note...">
                <div id="hydraulicKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                    Nessuna prova idraulica corrisponde alla ricerca.
                </div>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-warning">${error}</div>
        </c:if>

        <c:choose>
            <c:when test="${not empty hydraulicTests}">
                <div class="row g-4" id="hydraulicGalleryGrid">
                    <c:forEach var="test" items="${hydraulicTests}">
                        <div class="col-xl-3 col-lg-4 col-md-6 hydraulic-card-col"
                             data-search="${fn:escapeXml(test.customerName)} ${fn:escapeXml(test.engineCode)} ${fn:escapeXml(test.notes)} ${test.testDate}">
                            <a class="engine-card-link" href="${pageContext.request.contextPath}/hydraulic-test/detail?id=${test.id}">
                            <div class="engine-gallery-card h-100">
                                <div class="hydraulic-video-wrap">
                                    <video class="hydraulic-video" muted preload="metadata">
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

                                <div class="engine-body">
                                    <div class="engine-code">
                                        <c:out value="${test.engineCode}" default="—" /> -
                                        <c:out value="${test.customerName}" default="Cliente non disponibile" />
                                    </div>
                                    <div class="engine-client">
                                        Data prova: <c:out value="${test.testDate}" default="—" />
                                    </div>
                                    <c:if test="${not empty test.notes}">
                                        <div class="hydraulic-notes mt-2">
                                            <c:out value="${test.notes}" />
                                        </div>
                                    </c:if>
                                </div>
                            </div>
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
    <script>
        const searchInput = document.getElementById('hydraulicKeywordSearch');
        const hydraulicCards = document.querySelectorAll('.hydraulic-card-col');
        const emptyState = document.getElementById('hydraulicKeywordEmptyState');

        const normalizeText = (value) => (value || '')
            .toString()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();

        const applyHydraulicFilter = () => {
            const keyword = normalizeText(searchInput.value);
            let visibleCount = 0;

            hydraulicCards.forEach((card) => {
                const haystack = normalizeText(card.dataset.search);
                const isVisible = keyword.length === 0 || haystack.includes(keyword);
                card.classList.toggle('d-none', !isVisible);
                if (isVisible) {
                    visibleCount += 1;
                }
            });

            const showEmptyState = keyword.length > 0 && visibleCount === 0;
            emptyState.classList.toggle('d-none', !showEmptyState);
        };

        searchInput.addEventListener('input', applyHydraulicFilter);
        applyHydraulicFilter();
    </script>

</div>
</body>
</html>
