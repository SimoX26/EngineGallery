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
                    <h5 class="mb-3 fw-semibold">Storico KPI mensili</h5>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0" id="monthlyKpiTable">
                            <thead>
                            <tr>
                                <th>Mese</th>
                                <th>Inseriti</th>
                                <th>Consegnati</th>
                                <th>In lavorazione</th>
                                <th>Tempo medio</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="row" items="${monthlyHistory}">
                                <tr>
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
    </script>
</div>
</body>
</html>
