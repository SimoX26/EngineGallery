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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=11">
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
        </div>

        <div class="page-header-actions">
            <a href="<%= request.getContextPath() %>/warehouse/new" class="btn btn-sm btn-add-plus">
                Aggiungi +
            </a>
        </div>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <div class="customer-list" id="warehouseListGrid">
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
<script>
    const searchInput = document.getElementById('warehouseKeywordSearch');
    const warehouseCards = document.querySelectorAll('.warehouse-card-item');
    const emptyState = document.getElementById('warehouseKeywordEmptyState');

    const normalizeText = (value) => (value || '')
        .toString()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();

    const applyWarehouseFilter = () => {
        const keyword = normalizeText(searchInput.value);
        let visibleCount = 0;

        warehouseCards.forEach((card) => {
            const haystack = normalizeText(card.dataset.search);
            const isVisible = keyword.length === 0 || haystack.includes(keyword);
            card.classList.toggle('d-none', !isVisible);
            if (isVisible) {
                visibleCount += 1;
            }
        });

        emptyState.classList.toggle('d-none', visibleCount > 0);
    };

    searchInput.addEventListener('input', applyWarehouseFilter);
</script>

</body>
</html>
