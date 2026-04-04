<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Motori</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"  rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=3">
</head>

<body>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

    <!-- NAVBAR -->
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <div class="page-header-with-search">
            <!-- HEADER -->
            <div class="page-header">
                <h1>Lista motori</h1>
            </div>

            <div class="card-base search-panel-compact">
                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
                    <label for="engineKeywordSearch" class="form-label fw-semibold mb-0">Ricerca per parola chiave</label>
                    <button class="btn btn-outline-secondary btn-sm"
                            type="button"
                            aria-expanded="false"
                            aria-controls="engineFiltersPanel"
                            id="engineFiltersToggle">
                        Filtri
                    </button>
                </div>
                <input type="search"
                       id="engineKeywordSearch"
                       class="form-control"
                       placeholder="Cerca per codice, riferimento, cliente o stato...">
                <div class="collapse mt-3" id="engineFiltersPanel">
                    <div class="row g-2">
                        <div class="col-md-6">
                            <label for="engineStatusFilter" class="form-label small mb-1">Stato</label>
                            <select id="engineStatusFilter" class="form-select">
                                <option value="">Tutti gli stati</option>
                                <option value="WAITING">In attesa</option>
                                <option value="WORK_IN_PROGRESS">In lavorazione</option>
                                <option value="READY">Pronto</option>
                                <option value="DELIVERED">Consegnato</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label for="engineCustomerFilter" class="form-label small mb-1">Cliente</label>
                            <select id="engineCustomerFilter" class="form-select">
                                <option value="">Tutti i clienti</option>
                            </select>
                        </div>
                        <div class="col-12 d-flex justify-content-end">
                            <button type="button" id="engineFiltersReset" class="btn btn-link btn-sm text-decoration-none px-0">
                                Reimposta filtri
                            </button>
                        </div>
                    </div>
                </div>
                <div id="engineKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                    Nessun motore corrisponde alla ricerca.
                </div>
            </div>
        </div>

        <!-- (opzionale) errore -->
        <c:if test="${not empty error}">
            <div class="alert alert-warning">${error}</div>
        </c:if>

        <!-- GALLERY -->
        <div class="row g-4" id="engineGalleryGrid">

            <c:forEach var="engine" items="${engines}">
                <c:set var="st" value="${engine.status}" />
                <c:set var="statusSearchLabel"
                       value="${st == 'WAITING' ? 'in attesa' : st == 'WORK_IN_PROGRESS' ? 'in lavorazione' : st == 'READY' ? 'pronto' : st == 'DELIVERED' ? 'consegnato' : st}" />
                <div class="col-xl-3 col-lg-4 col-md-6 engine-card-col"
                     data-search="${engine.engineCode} ${engine.engineRef} ${customerNames[engine.customerId]} ${st} ${statusSearchLabel}"
                     data-status="${st}"
                     data-customer="<c:out value='${customerNames[engine.customerId]}' default='—'/>">

                    <a class="engine-card-link" href="${pageContext.request.contextPath}/engine/detail?ref=${engine.engineRef}">
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
                                ${engine.engineCode} - <c:out value="${customerNames[engine.customerId]}" default="—" />
                            </div>

                            <div class="engine-client">
                                ${engine.engineRef}
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

                            </div>
                        </div>

                    </div>
                    </a>

                </div>
            </c:forEach>

        </div>

    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const searchInput = document.getElementById('engineKeywordSearch');
        const statusFilter = document.getElementById('engineStatusFilter');
        const customerFilter = document.getElementById('engineCustomerFilter');
        const filtersToggle = document.getElementById('engineFiltersToggle');
        const filtersPanel = document.getElementById('engineFiltersPanel');
        const resetFiltersButton = document.getElementById('engineFiltersReset');
        const engineCards = document.querySelectorAll('.engine-card-col');
        const emptyState = document.getElementById('engineKeywordEmptyState');
        const filtersCollapse = bootstrap.Collapse.getOrCreateInstance(filtersPanel, { toggle: false });
        const queryParams = new URLSearchParams(window.location.search);

        const normalizeText = (value) => (value || '')
            .toString()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();

        const populateCustomerFilter = () => {
            const customers = new Map();
            engineCards.forEach((card) => {
                const customerName = (card.dataset.customer || '').trim();
                if (customerName.length > 0 && customerName !== '—') {
                    const key = normalizeText(customerName);
                    if (!customers.has(key)) {
                        customers.set(key, customerName);
                    }
                }
            });

            [...customers.values()]
                .sort((a, b) => a.localeCompare(b, 'it', { sensitivity: 'base' }))
                .forEach((name) => {
                    const option = document.createElement('option');
                    option.value = normalizeText(name);
                    option.textContent = name;
                    customerFilter.appendChild(option);
                });
        };

        const applyInitialFiltersFromQuery = () => {
            const requestedStatus = (queryParams.get('status') || '').trim();
            const requestedCustomer = normalizeText(queryParams.get('customer'));

            if (requestedStatus.length > 0 && [...statusFilter.options].some((opt) => opt.value === requestedStatus)) {
                statusFilter.value = requestedStatus;
            }

            if (requestedCustomer.length > 0) {
                const customerOption = [...customerFilter.options]
                    .find((opt) => normalizeText(opt.value) === requestedCustomer);
                if (customerOption) {
                    customerFilter.value = customerOption.value;
                }
            }

            if ((statusFilter.value || '').length > 0 || (customerFilter.value || '').length > 0) {
                filtersCollapse.show();
            }
        };

        const applyEngineFilter = () => {
            const keyword = normalizeText(searchInput.value);
            const selectedStatus = (statusFilter.value || '').trim();
            const selectedCustomer = normalizeText(customerFilter.value);
            let visibleCount = 0;

            engineCards.forEach((card) => {
                const haystack = normalizeText(card.dataset.search);
                const statusMatches = selectedStatus.length === 0 || card.dataset.status === selectedStatus;
                const customerMatches = selectedCustomer.length === 0
                    || normalizeText(card.dataset.customer) === selectedCustomer;
                const keywordMatches = keyword.length === 0 || haystack.includes(keyword);
                const isVisible = statusMatches && customerMatches && keywordMatches;
                card.classList.toggle('d-none', !isVisible);
                if (isVisible) {
                    visibleCount += 1;
                }
            });

            emptyState.classList.toggle('d-none', visibleCount > 0);
            const activeFilters = selectedStatus.length > 0 || selectedCustomer.length > 0;
            filtersToggle.classList.toggle('btn-outline-secondary', !activeFilters);
            filtersToggle.classList.toggle('btn-secondary', activeFilters);
        };

        filtersPanel.addEventListener('shown.bs.collapse', () => {
            filtersToggle.setAttribute('aria-expanded', 'true');
        });
        filtersPanel.addEventListener('hidden.bs.collapse', () => {
            filtersToggle.setAttribute('aria-expanded', 'false');
        });

        populateCustomerFilter();
        applyInitialFiltersFromQuery();
        searchInput.addEventListener('input', applyEngineFilter);
        statusFilter.addEventListener('change', applyEngineFilter);
        customerFilter.addEventListener('change', applyEngineFilter);
        filtersToggle.addEventListener('click', () => {
            filtersCollapse.toggle();
        });
        resetFiltersButton.addEventListener('click', () => {
            statusFilter.value = '';
            customerFilter.value = '';
            applyEngineFilter();
        });
        applyEngineFilter();
    </script>

</div>
</body>
</html>
