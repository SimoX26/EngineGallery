<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Statistiche</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page statistics-analytics-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header dashboard-crm-header">
            <h1>Statistiche</h1>
            <p>Analisi avanzata officina • periodo corrente ${meseCorrenteLabel}</p>
        </div>

        <div class="table-container mb-4">
            <form method="get" action="<%= request.getContextPath() %>/statistics" class="row g-2 align-items-end">
                <div class="col-12 col-md-3">
                    <label for="months" class="form-label mb-1">Storico ultimi mesi</label>
                    <select class="form-select" id="months" name="months">
                        <option value="6" ${selectedMonths == 6 ? 'selected' : ''}>6 mesi</option>
                        <option value="12" ${selectedMonths == 12 ? 'selected' : ''}>12 mesi</option>
                        <option value="24" ${selectedMonths == 24 ? 'selected' : ''}>24 mesi</option>
                    </select>
                </div>
                <div class="col-6 col-md-3">
                    <label for="fromMonth" class="form-label mb-1">Da (YYYY-MM)</label>
                    <input type="month" class="form-control" id="fromMonth" name="fromMonth" value="${fromMonth}">
                </div>
                <div class="col-6 col-md-3">
                    <label for="toMonth" class="form-label mb-1">A (YYYY-MM)</label>
                    <input type="month" class="form-control" id="toMonth" name="toMonth" value="${toMonth}">
                </div>
                <div class="col-12 col-md-3 d-grid">
                    <button type="submit" class="btn btn-engine">Applica filtro</button>
                </div>
            </form>
        </div>

        <div class="row g-3 g-lg-4 mb-4">
            <div class="col-6 col-xl-2">
                <div class="kpi-card dashboard-kpi-card">
                    <div class="kpi-title">Motori totali</div>
                    <div class="kpi-value">${motoriTotali}</div>
                </div>
            </div>
            <div class="col-6 col-xl-2">
                <div class="kpi-card dashboard-kpi-card">
                    <div class="kpi-title">Inseriti mese</div>
                    <div class="kpi-value">${motoriInseritiMese}</div>
                </div>
            </div>
            <div class="col-6 col-xl-2">
                <div class="kpi-card dashboard-kpi-card">
                    <div class="kpi-title">Consegnati mese</div>
                    <div class="kpi-value">${motoriConsegnatiMese}</div>
                </div>
            </div>
            <div class="col-6 col-xl-2">
                <div class="kpi-card dashboard-kpi-card">
                    <div class="kpi-title">In lavorazione</div>
                    <div class="kpi-value">${motoriInLavorazione}</div>
                </div>
            </div>
            <div class="col-6 col-xl-2">
                <div class="kpi-card dashboard-kpi-card">
                    <div class="kpi-title">Pronti</div>
                    <div class="kpi-value">${motoriPronti}</div>
                </div>
            </div>
            <div class="col-6 col-xl-2">
                <div class="kpi-card dashboard-kpi-card">
                    <div class="kpi-title">Tempo medio mese</div>
                    <div class="kpi-value">${tempoMedioLavorazioneMese} gg</div>
                </div>
            </div>
        </div>

        <div class="row g-3 g-lg-4 mb-4">
            <div class="col-12 col-lg-8">
                <div class="table-container h-100">
                    <h5 class="mb-3 fw-semibold">Trend mensile officina</h5>
                    <div class="w-100" style="height: 340px;">
                        <canvas id="workshopTrendChart"></canvas>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-4">
                <div class="table-container h-100 mb-3 mb-lg-0">
                    <h5 class="mb-3 fw-semibold">Distribuzione stati motore</h5>
                    <div class="w-100" style="height: 220px;">
                        <canvas id="statusDistributionChart"></canvas>
                    </div>
                    <hr>
                    <h6 class="mb-2 fw-semibold">Consegnati / Non consegnati</h6>
                    <div class="w-100" style="height: 180px;">
                        <canvas id="deliveryDistributionChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 g-lg-4 mb-4">
            <div class="col-12 col-lg-7">
                <div class="table-container h-100">
                    <h5 class="mb-3 fw-semibold">Storico Dati Mensili</h5>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0" id="monthlyKpiTable">
                            <thead>
                            <tr>
                                <th role="button" tabindex="0" data-sort-key="month" data-sort-type="text" aria-sort="none">Mese <span class="small text-muted" data-sort-indicator></span></th>
                                <th role="button" tabindex="0" data-sort-key="inserted" data-sort-type="number" aria-sort="none">Inseriti <span class="small text-muted" data-sort-indicator></span></th>
                                <th role="button" tabindex="0" data-sort-key="delivered" data-sort-type="number" aria-sort="none">Consegnati <span class="small text-muted" data-sort-indicator></span></th>
                                <th role="button" tabindex="0" data-sort-key="inProgress" data-sort-type="number" aria-sort="none">In lavorazione <span class="small text-muted" data-sort-indicator></span></th>
                                <th role="button" tabindex="0" data-sort-key="avgDays" data-sort-type="number" aria-sort="none">Tempo medio <span class="small text-muted" data-sort-indicator></span></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${monthlyHistory}">
                                <tr data-month-key="${row.monthKey}"
                                    data-month-label="${row.monthLabel}"
                                    data-inserted="${row.inserted}"
                                    data-delivered="${row.delivered}"
                                    data-in-progress="${row.inProgress}"
                                    data-avg-days="${row.avgDays}">
                                    <td>${row.monthLabel}</td>
                                    <td>${row.inserted}</td>
                                    <td>${row.delivered}</td>
                                    <td>${row.inProgress}</td>
                                    <td>${row.avgDays} gg</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty monthlyHistory}">
                                <tr>
                                    <td colspan="5" class="text-muted">Nessun dato storico disponibile per il filtro selezionato.</td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="col-12 col-lg-5">
                <div class="table-container h-100">
                    <h5 class="mb-3 fw-semibold">Previsione prossimo mese (stima)</h5>
                    <p class="text-muted mb-3">Metodo: media mobile semplice sugli ultimi 3 mesi del filtro attuale.</p>
                    <c:choose>
                        <c:when test="${forecastHasEnoughData}">
                            <div class="dashboard-warehouse-metrics">
                                <div class="dashboard-warehouse-metric">
                                    <span class="dashboard-warehouse-label">Inseriti stimati</span>
                                    <strong>${forecastInsertedNextMonth}</strong>
                                </div>
                                <div class="dashboard-warehouse-metric">
                                    <span class="dashboard-warehouse-label">Consegnati stimati</span>
                                    <strong>${forecastDeliveredNextMonth}</strong>
                                </div>
                                <div class="dashboard-warehouse-metric">
                                    <span class="dashboard-warehouse-label">Tempo medio stimato</span>
                                    <strong>${forecastAvgDaysNextMonth} gg</strong>
                                </div>
                                <div class="dashboard-warehouse-metric">
                                    <span class="dashboard-warehouse-label">Tempo medio complessivo</span>
                                    <strong>${tempoMedioLavorazione} gg</strong>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-light border mb-0">
                                Dati insufficienti per la previsione: servono almeno 3 mesi con storico disponibile.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="table-container mt-4 user-actions-panel">
            <div class="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-3">
                <div>
                    <h5 class="mb-1 fw-semibold">Azioni utenti</h5>
                    <p class="text-muted mb-0">Controllo operativo delle attività registrate negli ultimi 10 giorni.</p>
                </div>
                <div class="user-actions-quick-check">
                    <span class="user-actions-quick-label">maurizio oggi</span>
                    <strong>${maurizioEngineCreatesToday}</strong>
                    <span class="text-muted">aggiunte motore</span>
                </div>
            </div>

            <form method="get" action="<%= request.getContextPath() %>/statistics" class="row g-2 align-items-end mb-3">
                <input type="hidden" name="months" value="${selectedMonths}">
                <input type="hidden" name="fromMonth" value="${fromMonth}">
                <input type="hidden" name="toMonth" value="${toMonth}">
                <div class="col-12 col-md-5">
                    <label for="activityUser" class="form-label mb-1">Utente</label>
                    <input type="search"
                           class="form-control"
                           id="activityUser"
                           name="activityUser"
                           value="${activityUser}"
                           placeholder="es. maurizio">
                </div>
                <div class="col-12 col-md-4">
                    <label for="activityScope" class="form-label mb-1">Periodo</label>
                    <select class="form-select" id="activityScope" name="activityScope">
                        <option value="all" ${activityScope == 'all' ? 'selected' : ''}>Ultimi 10 giorni</option>
                        <option value="today" ${activityScope == 'today' ? 'selected' : ''}>Oggi</option>
                    </select>
                </div>
                <div class="col-12 col-md-3 d-grid">
                    <button type="submit" class="btn btn-engine">Filtra azioni</button>
                </div>
            </form>

            <div class="d-flex flex-wrap gap-2 mb-3">
                <a class="btn btn-sm btn-outline-secondary"
                   href="<%= request.getContextPath() %>/statistics?months=${selectedMonths}&fromMonth=${fromMonth}&toMonth=${toMonth}&activityUser=maurizio&activityScope=today">
                    maurizio oggi
                </a>
                <a class="btn btn-sm btn-outline-secondary"
                   href="<%= request.getContextPath() %>/statistics?months=${selectedMonths}&fromMonth=${fromMonth}&toMonth=${toMonth}&activityScope=today">
                    tutte oggi
                </a>
            </div>

            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 user-actions-table">
                    <thead>
                    <tr>
                        <th>Utente</th>
                        <th>Azione</th>
                        <th>Data e ora</th>
                        <th>Entità coinvolta</th>
                        <th>Descrizione</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty userActions}">
                            <c:forEach var="action" items="${userActions}">
                                <tr class="${action.engineCreate ? 'user-action-row--engine-create' : ''}">
                                    <td class="fw-semibold"><c:out value="${action.username}" /></td>
                                    <td>
                                        <span class="user-action-badge ${action.engineCreate ? 'user-action-badge--engine-create' : ''}">
                                            <c:out value="${action.actionLabel}" />
                                        </span>
                                    </td>
                                    <td><c:out value="${action.createdAtLabel}" /></td>
                                    <td>
                                        <c:out value="${action.entityLabel}" />
                                        <c:if test="${not empty action.entityId}">
                                            <span class="text-muted">#<c:out value="${action.entityId}" /></span>
                                        </c:if>
                                    </td>
                                    <td><c:out value="${action.description}" /></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="5" class="text-muted">Nessuna azione registrata negli ultimi 10 giorni.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
    <script>
        const chartLabels = [
            <c:forEach var="row" items="${monthlyHistory}" varStatus="status">"${row.monthLabel}"${status.last ? '' : ','}</c:forEach>
        ];
        const chartInserted = [
            <c:forEach var="row" items="${monthlyHistory}" varStatus="status">${row.inserted}${status.last ? '' : ','}</c:forEach>
        ];
        const chartDelivered = [
            <c:forEach var="row" items="${monthlyHistory}" varStatus="status">${row.delivered}${status.last ? '' : ','}</c:forEach>
        ];
        const chartAvgDays = [
            <c:forEach var="row" items="${monthlyHistory}" varStatus="status">${row.avgDays}${status.last ? '' : ','}</c:forEach>
        ];

        const statusData = {
            waiting: Number('${motoriInAttesa}'),
            inProgress: Number('${motoriInLavorazione}'),
            ready: Number('${motoriPronti}'),
            delivered: Number('${motoriConsegnatiTotali}')
        };

        const rootStyles = getComputedStyle(document.documentElement);
        const textColor = rootStyles.getPropertyValue('--text-main').trim() || '#e6ebf1';
        const mutedColor = rootStyles.getPropertyValue('--text-muted').trim() || '#9ca3af';
        const borderColor = rootStyles.getPropertyValue('--border-soft').trim() || '#374151';
        const waitingColor = rootStyles.getPropertyValue('--status-waiting-border').trim() || '#9eb0c7';
        const workColor = rootStyles.getPropertyValue('--status-working-border').trim() || '#d9a979';
        const readyColor = rootStyles.getPropertyValue('--status-ready-border').trim() || '#7bc3a7';
        const deliveredColor = rootStyles.getPropertyValue('--status-delivered-border').trim() || '#8fb9df';

        const defaultChartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: textColor
                    }
                }
            },
            scales: {
                x: {
                    ticks: { color: mutedColor },
                    grid: { color: borderColor }
                },
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0, color: mutedColor },
                    grid: { color: borderColor }
                }
            }
        };

        if (window.Chart) {
            const trendCtx = document.getElementById('workshopTrendChart');
            if (trendCtx) {
                new Chart(trendCtx, {
                    type: 'line',
                    data: {
                        labels: chartLabels,
                        datasets: [
                            {
                                label: 'Inseriti',
                                data: chartInserted,
                                borderColor: '#3b82f6',
                                backgroundColor: 'rgba(59,130,246,0.16)',
                                tension: 0.3,
                                fill: true
                            },
                            {
                                label: 'Consegnati',
                                data: chartDelivered,
                                borderColor: '#10b981',
                                backgroundColor: 'rgba(16,185,129,0.14)',
                                tension: 0.3,
                                fill: true
                            },
                            {
                                label: 'Tempo medio (gg)',
                                data: chartAvgDays,
                                borderColor: '#f59e0b',
                                backgroundColor: 'rgba(245,158,11,0.12)',
                                tension: 0.3,
                                fill: false,
                                yAxisID: 'y1'
                            }
                        ]
                    },
                    options: {
                        ...defaultChartOptions,
                        scales: {
                            ...defaultChartOptions.scales,
                            y1: {
                                position: 'right',
                                beginAtZero: true,
                                ticks: { precision: 0, color: mutedColor },
                                grid: { drawOnChartArea: false }
                            }
                        }
                    }
                });
            }

            const statusCtx = document.getElementById('statusDistributionChart');
            if (statusCtx) {
                new Chart(statusCtx, {
                    type: 'pie',
                    data: {
                        labels: ['In attesa', 'In lavorazione', 'Pronti', 'Consegnati'],
                        datasets: [{
                            data: [statusData.waiting, statusData.inProgress, statusData.ready, statusData.delivered],
                            backgroundColor: [waitingColor, workColor, readyColor, deliveredColor],
                            borderColor,
                            borderWidth: 1
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: { legend: { labels: { color: textColor } } }
                    }
                });
            }

            const deliveryCtx = document.getElementById('deliveryDistributionChart');
            if (deliveryCtx) {
                const delivered = statusData.delivered;
                const notDelivered = Math.max(0, Number('${motoriTotali}') - delivered);
                new Chart(deliveryCtx, {
                    type: 'doughnut',
                    data: {
                        labels: ['Consegnati', 'Non consegnati'],
                        datasets: [{
                            data: [delivered, notDelivered],
                            backgroundColor: [deliveredColor, mutedColor],
                            borderColor,
                            borderWidth: 1
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: { legend: { labels: { color: textColor } } }
                    }
                });
            }
        }

        const monthlyKpiTable = document.getElementById('monthlyKpiTable');
        if (monthlyKpiTable) {
            const sortableHeaders = monthlyKpiTable.querySelectorAll('thead th[data-sort-key]');
            const tableBody = monthlyKpiTable.querySelector('tbody');
            let currentSortKey = 'month';
            let currentSortDirection = 'asc';

            const getSortValue = (row, key, type) => {
                if (key === 'month') {
                    return row.dataset.monthKey || row.dataset.monthLabel || '';
                }
                if (key === 'inserted') {
                    return Number(row.dataset.inserted || 0);
                }
                if (key === 'delivered') {
                    return Number(row.dataset.delivered || 0);
                }
                if (key === 'inProgress') {
                    return Number(row.dataset.inProgress || 0);
                }
                if (key === 'avgDays') {
                    return Number(row.dataset.avgDays || 0);
                }
                return type === 'number' ? 0 : '';
            };

            const updateIndicators = () => {
                sortableHeaders.forEach((header) => {
                    const indicator = header.querySelector('[data-sort-indicator]');
                    if (!indicator) {
                        return;
                    }
                    const isActive = header.dataset.sortKey === currentSortKey;
                    indicator.textContent = isActive ? (currentSortDirection === 'asc' ? '▲' : '▼') : '';
                    header.setAttribute('aria-sort', isActive ? (currentSortDirection === 'asc' ? 'ascending' : 'descending') : 'none');
                });
            };

            const sortTable = (key, type) => {
                const rows = Array.from(tableBody.querySelectorAll('tr'))
                    .filter((row) => row.dataset.monthKey || row.dataset.monthLabel);
                rows.sort((a, b) => {
                    const aVal = getSortValue(a, key, type);
                    const bVal = getSortValue(b, key, type);
                    if (aVal < bVal) {
                        return currentSortDirection === 'asc' ? -1 : 1;
                    }
                    if (aVal > bVal) {
                        return currentSortDirection === 'asc' ? 1 : -1;
                    }
                    return 0;
                });
                rows.forEach((row) => tableBody.appendChild(row));
                updateIndicators();
            };

            sortableHeaders.forEach((header) => {
                const handleSort = () => {
                    const key = header.dataset.sortKey;
                    const type = header.dataset.sortType || 'text';
                    if (currentSortKey === key) {
                        currentSortDirection = currentSortDirection === 'asc' ? 'desc' : 'asc';
                    } else {
                        currentSortKey = key;
                        currentSortDirection = 'asc';
                    }
                    sortTable(key, type);
                };
                header.addEventListener('click', handleSort);
                header.addEventListener('keydown', (event) => {
                    if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault();
                        handleSort();
                    }
                });
            });

            updateIndicators();
        }
    </script>
</div>
</body>
</html>
