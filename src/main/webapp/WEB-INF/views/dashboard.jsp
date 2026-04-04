<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Home</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=4">
</head>

<body>


<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>


<div class="dashboard-page">

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>



<div class="container">

    <!-- HEADER -->
    <div class="page-header">
        <h1>Home</h1>
        <p>
            Panoramica generale
        </p>
    </div>

    <!-- KPI -->
    <div class="row g-4 mb-5">

        <!-- Clienti serviti mese corrente -->
        <div class="col-md-3">
            <a href="<%= request.getContextPath() %>/customer/list" class="kpi-card-link">
                <div class="kpi-card">
                    <div class="kpi-title">
                        Clienti serviti (mese di ${meseCorrenteLabel})
                    </div>
                    <div class="kpi-value">
                        ${clientiServitiMese}
                    </div>
                </div>
            </a>
        </div>

        <!-- Motori in lavorazione -->
        <div class="col-md-3">
            <a href="<%= request.getContextPath() %>/engine/list?status=WORK_IN_PROGRESS" class="kpi-card-link">
                <div class="kpi-card">
                    <div class="kpi-title">
                        Motori in lavorazione
                    </div>
                    <div class="kpi-value">
                        ${workInProgressEngines}
                    </div>
                </div>
            </a>
        </div>

        <!-- Motori consegnati mese corrente -->
        <div class="col-md-3">
            <div class="kpi-card">
                <div class="kpi-title">
                    Motori consegnati (mese di ${meseCorrenteLabel})
                </div>
                <div class="kpi-value">
                    ${motoriConsegnatiMese}
                </div>
            </div>
        </div>

        <!-- Tempo medio lavorazione mese corrente -->
        <div class="col-md-3">
            <div class="kpi-card">
                <div class="kpi-title">
                    Tempo medio lavorazione (mese di ${meseCorrenteLabel})
                </div>
                <div class="kpi-value">
                    ${tempoMedioLavorazioneMese} gg
                </div>
            </div>
        </div>

    </div>


    <!-- ULTIMI MOTORI -->
    <div class="table-container">

        <h5 class="mb-4 fw-semibold">Motori recenti</h5>

        <c:choose>
            <c:when test="${not empty ultimiMotori}">
                <div class="list-group">
                    <c:forEach var="motore" items="${ultimiMotori}">
                        <c:set var="st" value="${motore.status}" />
                        <c:set var="coverFilename" value="${coverImages[motore.id]}" />
                        <a href="<%= request.getContextPath() %>/engine/detail?ref=${motore.engineRef}"
                           class="list-group-item list-group-item-action">
                            <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap">
                                <div class="d-flex align-items-center gap-3">
                                    <c:choose>
                                        <c:when test="${not empty coverFilename}">
                                            <div class="dashboard-engine-thumb"
                                                 style="background-image: url('${pageContext.request.contextPath}/uploads/engines/${motore.engineRef}/${coverFilename}');">
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="dashboard-engine-thumb engine-image-empty"></div>
                                        </c:otherwise>
                                    </c:choose>
                                    <div>
                                        <div class="fw-semibold">${motore.engineCode}</div>
                                        <small class="text-muted">
                                            <c:out value="${customerNames[motore.customerId]}" default="—" /> • ${motore.engineRef}
                                        </small>
                                    </div>
                                </div>
                                <div class="d-flex align-items-center gap-2">
                                    <small class="text-muted">${motore.intakeDate}</small>
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
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="text-muted mb-0">Nessun motore recente disponibile.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- ULTIMI ARTICOLI MAGAZZINO -->
    <div class="table-container mt-4">

        <h5 class="mb-4 fw-semibold">Articoli magazzino recenti</h5>

        <c:choose>
            <c:when test="${not empty ultimiArticoliMagazzino}">
                <div class="list-group">
                    <c:forEach var="item" items="${ultimiArticoliMagazzino}">
                        <a href="<%= request.getContextPath() %>/warehouse/detail?id=${item.id}"
                           class="list-group-item list-group-item-action">
                            <div class="d-flex align-items-center justify-content-between gap-3 flex-wrap">
                                <div>
                                    <div class="fw-semibold">${item.name}</div>
                                    <small class="text-muted">
                                        Codice: <c:out value="${item.sku}" default="—" /> •
                                        Ubicazione: <c:out value="${item.location}" default="—" />
                                    </small>
                                </div>
                                <div class="d-flex align-items-center gap-2">
                                    <small class="text-muted">Disponibilita</small>
                                    <span class="badge-status ${item.quantity <= 0 ? 'status-stoccato' : 'status-ready'}">
                                        ${item.quantity}
                                    </span>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="text-muted mb-0">Nessun articolo di magazzino disponibile.</p>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</div>

</body>
</html>
