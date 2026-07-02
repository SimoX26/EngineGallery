<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Home</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=12">
</head>
<body>

<c:set var="loggedRole" value="${sessionScope.loggedUser != null ? sessionScope.loggedUser.role : null}" />
<c:set var="canViewEngineArchive" value="${loggedRole == 'ADMIN'}" />

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page dashboard-crm-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header dashboard-crm-header">
            <p class="dashboard-crm-greeting mb-2 fw-bold">
                <c:out value="${dashboardGreeting}" />, <c:out value="${dashboardUserDisplayName}" />
            </p>
            <p>Panoramica di ${meseCorrenteLabel}.</p>
        </div>

        <div class="row g-3 g-lg-4 mb-4 dashboard-kpi-grid">
            <div class="col-12 col-lg-6">
                <div class="kpi-card dashboard-kpi-card dashboard-kpi-card--highlight">
                    <div class="kpi-title">Tempo medio lavorazione (mese corrente)</div>
                    <c:choose>
                        <c:when test="${tempoMedioDisponibile}">
                            <div class="kpi-value">${tempoMedioLavorazioneMese} gg</div>
                        </c:when>
                        <c:otherwise>
                            <div class="kpi-value">--</div>
                            <small class="text-muted">Tempo medio non disponibile: nessun motore consegnato nel mese.</small>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">Inseriti mese</div>
                        <div class="kpi-value">${motoriInseritiMese}</div>
                    </div>
                </a>
            </div>
            <div class="col-6 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list?status=DELIVERED" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">Consegnati mese</div>
                        <div class="kpi-value">${motoriConsegnatiMese}</div>
                    </div>
                </a>
            </div>
            <div class="col-12 col-lg-2">
                <a href="<%= request.getContextPath() %>/engine/list?status=WORK_IN_PROGRESS" class="kpi-card-link">
                    <div class="kpi-card dashboard-kpi-card">
                        <div class="kpi-title">In lavorazione</div>
                        <div class="kpi-value">${workInProgressEngines}</div>
                    </div>
                </a>
            </div>
        </div>

        <div class="home-nav-section mt-4 mb-4">
            <div class="dashboard-section-head mb-3">
                <h5 class="mb-0 fw-semibold">Funzioni rapide</h5>
            </div>
            <div class="row g-3">
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/engine/list"><div class="home-nav-card-title">Motori</div><p class="home-nav-card-desc mb-0">Gestione motori in lavorazione.</p></a></div>
                <c:if test="${canViewEngineArchive}">
                    <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/engine/archive"><div class="home-nav-card-title">Archivio motori</div><p class="home-nav-card-desc mb-0">Storico motori consegnati.</p></a></div>
                </c:if>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/customer/list"><div class="home-nav-card-title">Clienti</div><p class="home-nav-card-desc mb-0">Rubrica e anagrafiche clienti.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/hydraulic-test/list"><div class="home-nav-card-title">Prove idrauliche</div><p class="home-nav-card-desc mb-0">Video e schede test.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/warehouse/list"><div class="home-nav-card-title">Magazzino</div><p class="home-nav-card-desc mb-0">Articoli, quantità e giacenze.</p></a></div>
                <div class="col-lg-4 col-md-6"><a class="home-nav-card card-base" href="<%= request.getContextPath() %>/ready-delivery"><div class="home-nav-card-title">Pronta Consegna</div><p class="home-nav-card-desc mb-0">Sezione pronta consegna.</p></a></div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</div>

</body>
</html>
