<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Statistiche</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=4">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page">

    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <div class="page-header">
            <h1>Statistiche</h1>
            <p>Andamento mensile dell'officina</p>
        </div>

        <div class="row g-4 mb-4">
             <div class="col-md-3">
                <div class="kpi-card">
                    <div class="kpi-title">Motori in lavorazione</div>
                    <div class="kpi-value">${motoriInLavorazione}</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="kpi-card">
                    <div class="kpi-title">Motori inseriti (mese di ${meseCorrenteLabel})</div>
                    <div class="kpi-value">${motoriInseritiMese}</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="kpi-card">
                    <div class="kpi-title">Motori consegnati (mese di ${meseCorrenteLabel})</div>
                    <div class="kpi-value">${motoriConsegnatiMese}</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="kpi-card">
                    <div class="kpi-title">Tempo medio lavorazione (mese di ${meseCorrenteLabel})</div>
                    <div class="kpi-value">${tempoMedioLavorazioneMese} gg</div>
                </div>
            </div>
        </div>

        <div class="table-container mb-4">
            <h5 class="mb-3 fw-semibold">Andamento mensile motori inseriti/consegnati</h5>
            <div class="w-100" style="height: 320px;">
                <canvas id="workshopTrendChart"></canvas>
            </div>
        </div>

        <div class="table-container">
            <h5 class="mb-3 fw-semibold">Storico KPI mensili</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0" id="monthlyKpiTable">
                    <thead>
                    <tr>
                        <th role="button" tabindex="0" data-sort-key="month" data-sort-type="text">Mese <span class="small text-muted" data-sort-indicator></span></th>
                        <th role="button" tabindex="0" data-sort-key="inserted" data-sort-type="number">Motori inseriti <span class="small text-muted" data-sort-indicator></span></th>
                        <th role="button" tabindex="0" data-sort-key="delivered" data-sort-type="number">Motori consegnati <span class="small text-muted" data-sort-indicator></span></th>
                        <th role="button" tabindex="0" data-sort-key="inProgress" data-sort-type="number">Motori in lavorazione <span class="small text-muted" data-sort-indicator></span></th>
                        <th role="button" tabindex="0" data-sort-key="avgDays" data-sort-type="number">Tempo medio lavorazione <span class="small text-muted" data-sort-indicator></span></th>
                    </tr>
                    </thead>
                    <tbody id="monthlyKpiTableBody">
                    <c:forEach var="row" items="${monthlyHistory}">
                        <tr data-month="${row.monthLabel}"
                            data-month-key="${row.monthKey}"
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
                    </tbody>
                </table>
            </div>
        </div>

        <div class="table-container mt-4">
            <h5 class="mb-3 fw-semibold">Azioni utenti recenti</h5>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
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
                                <tr>
                                    <td>${action.username}</td>
                                    <td>${action.action}</td>
                                    <td>${action.timestampLabel}</td>
                                    <td>${action.entity}</td>
                                    <td>${action.description}</td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                        <tr>
                            <td colspan="5" class="text-muted">Nessuna azione utente disponibile al momento.</td>
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

        const chartCtx = document.getElementById('workshopTrendChart');
        if (chartCtx && window.Chart) {
            new Chart(chartCtx, {
                type: 'line',
                data: {
                    labels: chartLabels,
                    datasets: [
                        {
                            label: 'Inseriti',
                            data: chartInserted,
                            borderColor: '#2563eb',
                            backgroundColor: 'rgba(37,99,235,0.12)',
                            tension: 0.3,
                            fill: true
                        },
                        {
                            label: 'Consegnati',
                            data: chartDelivered,
                            borderColor: '#0f766e',
                            backgroundColor: 'rgba(15,118,110,0.12)',
                            tension: 0.3,
                            fill: true
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'top'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: {
                                precision: 0
                            }
                        }
                    }
                }
            });
        }

        const sortableHeaders = document.querySelectorAll('#monthlyKpiTable thead th[data-sort-key]');
        const tableBody = document.getElementById('monthlyKpiTableBody');
        let currentSortKey = 'month';
        let currentSortDirection = 'asc';

        const getSortValue = (row, key, type) => {
            if (key === 'month') {
                return row.dataset.monthKey || '';
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
                if (header.dataset.sortKey === currentSortKey) {
                    indicator.textContent = currentSortDirection === 'asc' ? '▲' : '▼';
                } else {
                    indicator.textContent = '';
                }
            });
        };

        const sortTable = (key, type) => {
            const rows = Array.from(tableBody.querySelectorAll('tr'));
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
    </script>

</div>
</body>
</html>
