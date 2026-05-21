<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Dashboard</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body>

<div class="dashboard-page dashboard-crm-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <c:set var="loggedRole" value="${sessionScope.loggedUser != null ? sessionScope.loggedUser.role : null}" />
        <c:set var="canViewStatistics" value="${loggedRole == 'ADMIN' || loggedRole == 'INSPECTOR'}" />
        <c:set var="canViewMaintenance" value="${loggedRole == 'ADMIN'}" />

        <div class="page-header dashboard-crm-header">
            <h1>Dashboard</h1>
            <p>Panoramica operativa rapida per officina, test e magazzino.</p>
        </div>

        <div class="row g-3 g-lg-4 mb-4 dashboard-kpi-grid">
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">Motori totali</div>
                        <div class="kpi-value">${motoriTotali}</div>
                    </div>
                </a>
            </div>
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list?status=WAITING" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">In attesa</div>
                        <div class="kpi-value">${motoriInAttesa}</div>
                    </div>
                </a>
            </div>
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list?status=WORK_IN_PROGRESS" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">In lavorazione</div>
                        <div class="kpi-value">${workInProgressEngines}</div>
                    </div>
                </a>
            </div>
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list?status=READY" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">Pronti</div>
                        <div class="kpi-value">${motoriPronti}</div>
                    </div>
                </a>
            </div>
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list?status=DELIVERED" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">Consegnati</div>
                        <div class="kpi-value">${motoriConsegnatiTotali}</div>
                    </div>
                </a>
            </div>
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/hydraulic-test/list" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">Prove idrauliche</div>
                        <div class="kpi-value">${proveIdraulicheTotali}</div>
                    </div>
                </a>
            </div>
        </div>

        <div class="row g-3 g-lg-4 mb-4">
            <div class="col-12 col-lg-7">
                <div class="card-base dashboard-status-card h-100">
                    <div class="dashboard-section-head">
                        <h5 class="mb-0 fw-semibold">Stato lavorazioni</h5>
                    </div>

                    <c:set var="totaleLavorazioni" value="${motoriInAttesa + workInProgressEngines + motoriPronti + motoriConsegnatiTotali}" />
                    <div class="dashboard-status-grid mt-3">
                        <div class="dashboard-status-item">
                            <div class="dashboard-status-top"><span class="status-color-dot status-dot-waiting"></span>In attesa</div>
                            <div class="dashboard-status-value">${motoriInAttesa}</div>
                        </div>
                        <div class="dashboard-status-item">
                            <div class="dashboard-status-top"><span class="status-color-dot status-dot-work"></span>In lavorazione</div>
                            <div class="dashboard-status-value">${workInProgressEngines}</div>
                        </div>
                        <div class="dashboard-status-item">
                            <div class="dashboard-status-top"><span class="status-color-dot status-dot-ready"></span>Pronti</div>
                            <div class="dashboard-status-value">${motoriPronti}</div>
                        </div>
                        <div class="dashboard-status-item">
                            <div class="dashboard-status-top"><span class="status-color-dot status-dot-delivered"></span>Consegnati</div>
                            <div class="dashboard-status-value">${motoriConsegnatiTotali}</div>
                        </div>
                    </div>

                    <div class="dashboard-mini-progress mt-3">
                        <c:set var="safeTot" value="${totaleLavorazioni == 0 ? 1 : totaleLavorazioni}"/>
                        <div class="dashboard-progress-track">
                            <div class="dashboard-progress-segment dashboard-progress-segment--waiting" style="width:${(motoriInAttesa * 100.0) / safeTot}%;"></div>
                            <div class="dashboard-progress-segment dashboard-progress-segment--working" style="width:${(workInProgressEngines * 100.0) / safeTot}%;"></div>
                            <div class="dashboard-progress-segment dashboard-progress-segment--ready" style="width:${(motoriPronti * 100.0) / safeTot}%;"></div>
                            <div class="dashboard-progress-segment dashboard-progress-segment--delivered" style="width:${(motoriConsegnatiTotali * 100.0) / safeTot}%;"></div>
                        </div>
                        <small class="text-muted">Totale motori monitorati: ${totaleLavorazioni}</small>
                    </div>
                </div>
            </div>

            <div class="col-12 col-lg-5">
                <div class="card-base dashboard-warehouse-card h-100">
                    <div class="dashboard-section-head">
                        <h5 class="mb-0 fw-semibold">Magazzino</h5>
                    </div>
                    <div class="dashboard-warehouse-metrics mt-3">
                        <div class="dashboard-warehouse-metric">
                            <span class="dashboard-warehouse-label">Articoli</span>
                            <strong>${articoliMagazzinoTotali}</strong>
                        </div>
                        <div class="dashboard-warehouse-metric">
                            <span class="dashboard-warehouse-label">Quantità totale</span>
                            <strong>${quantitaMagazzinoTotale}</strong>
                        </div>
                        <div class="dashboard-warehouse-metric">
                            <span class="dashboard-warehouse-label">Esauriti</span>
                            <strong>${articoliEsauriti}</strong>
                        </div>
                        <div class="dashboard-warehouse-metric">
                            <span class="dashboard-warehouse-label">Clienti</span>
                            <strong>${clientiTotali}</strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 g-lg-4 mb-4">
            <div class="col-12 col-xl-6">
                <div class="table-container h-100">
                    <div class="dashboard-section-head mb-3">
                        <h5 class="mb-0 fw-semibold">Motori recenti</h5>
                    </div>
                    <c:choose>
                        <c:when test="${not empty ultimiMotori}">
                            <div class="list-group dashboard-recent-list">
                                <c:forEach var="motore" items="${ultimiMotori}">
                                    <c:set var="st" value="${motore.status}" />
                                    <c:set var="coverFilename" value="${coverImages[motore.id]}" />
                                    <a href="<%= request.getContextPath() %>/engine/detail?ref=${motore.engineRef}" class="list-group-item list-group-item-action">
                                        <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap">
                                            <div class="d-flex align-items-center gap-3">
                                                <c:choose>
                                                    <c:when test="${not empty coverFilename}">
                                                        <div class="dashboard-engine-thumb" style="background-image: url('${pageContext.request.contextPath}/uploads/engines/${motore.engineRef}/${coverFilename}');"></div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="dashboard-engine-thumb engine-image-empty"></div>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div>
                                                    <div class="fw-semibold">${motore.engineCode}</div>
                                                    <small class="text-muted"><c:out value="${customerNames[motore.customerId]}" default="—" /> • ${motore.engineRef}</small>
                                                </div>
                                            </div>
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
                                    </a>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted mb-0">Nessun motore recente disponibile.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-12 col-md-6 col-xl-3">
                <div class="table-container h-100">
                    <div class="dashboard-section-head mb-3">
                        <h5 class="mb-0 fw-semibold">Clienti recenti</h5>
                    </div>
                    <c:choose>
                        <c:when test="${not empty clientiRecenti}">
                            <div class="list-group dashboard-recent-list">
                                <c:forEach var="cliente" items="${clientiRecenti}">
                                    <a href="<%= request.getContextPath() %>/customer/list" class="list-group-item list-group-item-action">
                                        <div class="fw-semibold"><c:out value="${cliente.name}" default="—" /></div>
                                        <small class="text-muted"><c:out value="${cliente.companyName}" default="Nessuna azienda" /></small>
                                    </a>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted mb-0">Nessun cliente disponibile.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-12 col-md-6 col-xl-3">
                <div class="table-container h-100">
                    <div class="dashboard-section-head mb-3">
                        <h5 class="mb-0 fw-semibold">Prove idrauliche recenti</h5>
                    </div>
                    <c:choose>
                        <c:when test="${not empty proveIdraulicheRecenti}">
                            <div class="list-group dashboard-recent-list">
                                <c:forEach var="test" items="${proveIdraulicheRecenti}">
                                    <a href="<%= request.getContextPath() %>/hydraulic-test/detail?id=${test.id}" class="list-group-item list-group-item-action">
                                        <div class="fw-semibold"><c:out value="${test.engineCode}" default="—" /></div>
                                        <small class="text-muted"><c:out value="${test.customerName}" default="Cliente non disponibile" /></small>
                                    </a>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-muted mb-0">Nessuna prova idraulica disponibile.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="home-nav-section mt-4 mb-4">
            <div class="dashboard-section-head mb-3">
                <h5 class="mb-0 fw-semibold">Operatività rapida</h5>
            </div>
            <div class="row g-3">
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/engine/list"><div class="home-nav-card-title">Motori</div><p class="home-nav-card-desc mb-0">Gestione motori in lavorazione.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/engine/archive"><div class="home-nav-card-title">Archivio motori</div><p class="home-nav-card-desc mb-0">Storico motori consegnati.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/customer/list"><div class="home-nav-card-title">Clienti</div><p class="home-nav-card-desc mb-0">Rubrica e anagrafiche clienti.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/hydraulic-test/list"><div class="home-nav-card-title">Prove idrauliche</div><p class="home-nav-card-desc mb-0">Video e schede test.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/warehouse/list"><div class="home-nav-card-title">Magazzino</div><p class="home-nav-card-desc mb-0">Articoli, quantità e giacenze.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/ready-delivery"><div class="home-nav-card-title">Pronta Consegna</div><p class="home-nav-card-desc mb-0">Sezione pronta consegna.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/catalog"><div class="home-nav-card-title">Catalogo</div><p class="home-nav-card-desc mb-0">Catalogo motori vendibili.</p></a></div>
                <c:if test="${canViewStatistics}">
                    <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/statistics"><div class="home-nav-card-title">Statistiche</div><p class="home-nav-card-desc mb-0">KPI e storico andamento.</p></a></div>
                </c:if>
                <c:if test="${canViewMaintenance}">
                    <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/settings"><div class="home-nav-card-title">Manutenzione</div><p class="home-nav-card-desc mb-0">Impostazioni e strumenti admin.</p></a></div>
                </c:if>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</div>

</body>
</html>
