<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Pronta consegna</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"  rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

    <!-- NAVBAR -->
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <!-- HEADER -->
        <div class="page-header">
            <h1>Pronta consegna</h1>
            <p>Motori pronti alla consegna</p>
        </div>

        <div class="card-base mb-4">
            <label for="engineKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
            <input type="search"
                   id="engineKeywordSearch"
                   class="form-control"
                   placeholder="Cerca per codice, riferimento, cliente o stato...">
            <div id="engineKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                Nessun motore corrisponde alla ricerca.
            </div>
        </div>

        <!-- GALLERY -->
        <div class="row g-4" id="engineGalleryGrid">

            <c:forEach var="engine" items="${engines}">
                <c:set var="st" value="${engine.status}" />
                <c:set var="statusSearchLabel"
                       value="${st == 'WAITING' ? 'in attesa' : st == 'WORK_IN_PROGRESS' ? 'in lavorazione' : st == 'READY' ? 'pronto' : st == 'DELIVERED' ? 'consegnato' : st}" />
                <div class="col-xl-3 col-lg-4 col-md-6 engine-card-col"
                     data-search="${engine.engineCode} ${engine.engineRef} ${customerNames[engine.customerId]} ${st} ${statusSearchLabel}">

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
                                ${engine.engineCode} • ${engine.engineRef}
                            </div>

                            <div class="engine-client">
                                Cliente: <c:out value="${customerNames[engine.customerId]}" default="—" />
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

                                <!-- DETAIL -->
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
                                    Dettaglio
                                </a>

                            </div>
                        </div>

                    </div>

                </div>
            </c:forEach>

        </div>

    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const searchInput = document.getElementById('engineKeywordSearch');
        const engineCards = document.querySelectorAll('.engine-card-col');
        const emptyState = document.getElementById('engineKeywordEmptyState');

        const normalizeText = (value) => (value || '')
            .toString()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();

        const applyEngineFilter = () => {
            const keyword = normalizeText(searchInput.value);
            let visibleCount = 0;

            engineCards.forEach((card) => {
                const haystack = normalizeText(card.dataset.search);
                const isVisible = keyword.length === 0 || haystack.includes(keyword);
                card.classList.toggle('d-none', !isVisible);
                if (isVisible) {
                    visibleCount += 1;
                }
            });

            emptyState.classList.toggle('d-none', visibleCount > 0);
        };

        searchInput.addEventListener('input', applyEngineFilter);
    </script>

</div>
</body>
</html>
