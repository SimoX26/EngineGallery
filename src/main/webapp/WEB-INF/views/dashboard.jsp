<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Engine Gallery • Dashboard</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body class="dashboard-page">

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container">

    <!-- HEADER -->
    <div class="page-header">
        <h1>Dashboard</h1>
        <p>
            Panoramica generale
        </p>
    </div>

    <!-- KPI -->
    <div class="row g-4 mb-5">

        <!-- Clienti con motori in officina -->
        <div class="col-md-3">
            <div class="kpi-card">
                <div class="kpi-title">
                    Clienti con motori in officina
                </div>
                <div class="kpi-value">
                    ${clientiConMotoriAttivi}
                </div>
            </div>
        </div>

        <!-- Motori totali in officina -->
        <div class="col-md-3">
            <div class="kpi-card">
                <div class="kpi-title">
                    Motori totali in officina
                </div>
                <div class="kpi-value">
                    ${motoriInOfficina}
                </div>
            </div>
        </div>

        <!-- Motori in lavorazione -->
        <div class="col-md-3">
            <div class="kpi-card">
                <div class="kpi-title">
                    Motori in lavorazione
                </div>
                <div class="kpi-value">
                    ${motoriInLavorazione}
                </div>
            </div>
        </div>

        <!-- Motori consegnati ultima settimana -->
        <div class="col-md-3">
            <div class="kpi-card">
                <div class="kpi-title">
                    Consegnati (ultimi 7 giorni)
                </div>
                <div class="kpi-value">
                    ${motoriConsegnatiUltimaSettimana}
                </div>
            </div>
        </div>

    </div>


    <!-- ULTIMI MOTORI -->
    <div class="table-container">

        <h5 class="mb-4 fw-semibold">Ultimi motori inseriti</h5>

        <table class="table align-middle">
            <thead>
            <tr>
                <th>Codice Motore</th>
                <th>Cliente</th>
                <th>Stato</th>
                <th>Ultima modifica</th>
            </tr>
            </thead>
            <tbody>

            <c:forEach var="motore" items="${ultimiMotori}">
                <tr>
                    <td>
                        <a href="<%= request.getContextPath() %>/motori/dettaglio?id=${motore.id}">
                            ${motore.codice}
                        </a>
                    </td>
                    <td>${motore.clienteNome}</td>
                    <td>
                        <span class="badge-status
                            ${motore.stato == 'STOCCATO' ? 'status-stoccato' : ''}
                            ${motore.stato == 'IN_LAVORAZIONE' ? 'status-lavorazione' : ''}
                            ${motore.stato == 'CONSEGNATO' ? 'status-consegnato' : ''}">
                            ${motore.stato}
                        </span>
                    </td>
                    <td>${motore.ultimaModifica}</td>
                </tr>
            </c:forEach>

            </tbody>
        </table>

    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
