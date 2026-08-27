<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Catalogo</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=13">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header-with-search">
            <div class="page-header">
                <h1>Catalogo</h1>
            </div>

            <div class="search-panel-compact">
                <label for="catalogKeywordSearch" class="form-label fw-semibold mb-2">
                    Ricerca per parola chiave
                </label>
                <input type="search"
                       id="catalogKeywordSearch"
                       class="form-control"
                       placeholder="cerca...">
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">
                <c:out value="${error}" />
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty catalogItems}">
                <div class="table-container">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 catalog-table" id="catalogTable">
                            <thead>
                            <tr>
                                <th scope="col"
                                    class="catalog-sortable-header"
                                    role="button"
                                    tabindex="0"
                                    data-sort-key="cylinderDiameter"
                                    data-sort-type="number"
                                    aria-sort="none">
                                    Diametro cilindro <span data-sort-indicator aria-hidden="true"></span>
                                </th>
                                <th scope="col"
                                    class="catalog-sortable-header"
                                    role="button"
                                    tabindex="0"
                                    data-sort-key="engineModel"
                                    data-sort-type="text"
                                    aria-sort="none">
                                    Marca / modello motore <span data-sort-indicator aria-hidden="true"></span>
                                </th>
                                <th scope="col"
                                    class="catalog-sortable-header"
                                    role="button"
                                    tabindex="0"
                                    data-sort-key="displacement"
                                    data-sort-type="number"
                                    aria-sort="none">
                                    Cilindrata <span data-sort-indicator aria-hidden="true"></span>
                                </th>
                                <th scope="col"
                                    class="catalog-sortable-header"
                                    role="button"
                                    tabindex="0"
                                    data-sort-key="valveCount"
                                    data-sort-type="number"
                                    aria-sort="none">
                                    Numero valvole <span data-sort-indicator aria-hidden="true"></span>
                                </th>
                                <th scope="col"
                                    class="catalog-sortable-header"
                                    role="button"
                                    tabindex="0"
                                    data-sort-key="engineCode"
                                    data-sort-type="text"
                                    aria-sort="none">
                                    Codice motore <span data-sort-indicator aria-hidden="true"></span>
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="item" items="${catalogItems}">
                                <c:url var="catalogDetailUrl" value="/catalog/detail">
                                    <c:param name="id" value="${item.id}" />
                                </c:url>
                                <tr class="catalog-table-row"
                                    tabindex="0"
                                    role="link"
                                    aria-label="Apri dettaglio catalogo"
                                    data-detail-url="${fn:escapeXml(catalogDetailUrl)}"
                                    data-search="${fn:escapeXml(item.cylinderDiameterMm)} ${fn:escapeXml(item.engineModel)} ${fn:escapeXml(item.displacementCc)} ${fn:escapeXml(item.valveCount)} ${fn:escapeXml(item.engineCode)}"
                                    data-cylinder-diameter="${fn:escapeXml(item.cylinderDiameterMm)}"
                                    data-engine-model="${fn:escapeXml(item.engineModel)}"
                                    data-displacement="${fn:escapeXml(item.displacementCc)}"
                                    data-valve-count="${fn:escapeXml(item.valveCount)}"
                                    data-engine-code="${fn:escapeXml(item.engineCode)}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.cylinderDiameterMm}">
                                                <c:out value="${item.cylinderDiameterMm}" /> mm
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${item.engineModel}" default="—" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.displacementCc}">
                                                <c:out value="${item.displacementCc}" /> cm³
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${item.valveCount}" default="—" /></td>
                                    <td>
                                        <a class="catalog-detail-link"
                                           href="${fn:escapeXml(catalogDetailUrl)}"
                                           title="Apri dettaglio catalogo">
                                            <c:out value="${item.engineCode}" default="—" />
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div id="catalogKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                    Nessuna voce del catalogo corrisponde alla ricerca.
                </div>
            </c:when>
            <c:otherwise>
                <c:if test="${empty error}">
                    <div class="alert alert-light border">Nessuna voce disponibile nel catalogo.</div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/live-search.js"></script>
    <script>
        (function () {
            const catalogTable = document.getElementById('catalogTable');
            const searchInput = document.getElementById('catalogKeywordSearch');
            if (!catalogTable || !searchInput) {
                return;
            }

            const tableBody = catalogTable.querySelector('tbody');
            const rows = Array.from(tableBody.querySelectorAll('.catalog-table-row'));
            const emptyState = document.getElementById('catalogKeywordEmptyState');
            const sortableHeaders = catalogTable.querySelectorAll('thead th[data-sort-key]');
            let currentSortKey = null;
            let currentSortDirection = 'asc';

            const filterController = window.EngineGalleryLiveSearch
                ? window.EngineGalleryLiveSearch.init({
                    input: searchInput,
                    groups: [{ elements: rows }],
                    emptyState,
                    debounceMs: 180
                })
                : null;

            const openDetail = (row) => {
                const detailUrl = row.dataset.detailUrl;
                if (detailUrl) {
                    window.location.href = detailUrl;
                }
            };

            rows.forEach((row) => {
                row.addEventListener('click', (event) => {
                    if (event.target.closest('a, button, input, select, textarea')) {
                        return;
                    }
                    openDetail(row);
                });
                row.addEventListener('keydown', (event) => {
                    if (event.target !== row || (event.key !== 'Enter' && event.key !== ' ')) {
                        return;
                    }
                    event.preventDefault();
                    openDetail(row);
                });
            });

            const getSortValue = (row, key, type) => {
                const rawValue = (row.dataset[key] || '').trim();
                if (rawValue.length === 0) {
                    return null;
                }
                if (type === 'number') {
                    const numericValue = Number(rawValue);
                    return Number.isFinite(numericValue) ? numericValue : null;
                }
                return rawValue;
            };

            const updateSortIndicators = () => {
                sortableHeaders.forEach((header) => {
                    const isActive = header.dataset.sortKey === currentSortKey;
                    const indicator = header.querySelector('[data-sort-indicator]');
                    header.setAttribute(
                        'aria-sort',
                        isActive ? (currentSortDirection === 'asc' ? 'ascending' : 'descending') : 'none'
                    );
                    if (indicator) {
                        indicator.textContent = isActive
                            ? (currentSortDirection === 'asc' ? '▲' : '▼')
                            : '';
                    }
                });
            };

            const sortRows = (key, type) => {
                rows.sort((firstRow, secondRow) => {
                    const firstValue = getSortValue(firstRow, key, type);
                    const secondValue = getSortValue(secondRow, key, type);
                    if (firstValue === null && secondValue === null) {
                        return 0;
                    }
                    if (firstValue === null) {
                        return 1;
                    }
                    if (secondValue === null) {
                        return -1;
                    }

                    const comparison = type === 'number'
                        ? firstValue - secondValue
                        : firstValue.localeCompare(secondValue, 'it', { sensitivity: 'base', numeric: true });
                    return currentSortDirection === 'asc' ? comparison : -comparison;
                });
                rows.forEach((row) => tableBody.appendChild(row));
                updateSortIndicators();
            };

            sortableHeaders.forEach((header) => {
                const handleSort = () => {
                    const sortKey = header.dataset.sortKey;
                    const sortType = header.dataset.sortType || 'text';
                    if (currentSortKey === sortKey) {
                        currentSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
                    } else {
                        currentSortKey = sortKey;
                        currentSortDirection = 'asc';
                    }
                    sortRows(sortKey, sortType);
                };

                header.addEventListener('click', handleSort);
                header.addEventListener('keydown', (event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault();
                        handleSort();
                    }
                });
            });

            updateSortIndicators();
            if (filterController) {
                filterController.apply();
            }
        })();
    </script>
</div>

</body>
</html>
