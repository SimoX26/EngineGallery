<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Engine Gallery • Dettaglio Motore</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body>


<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>



<div class="engine-detail-page">

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container">

    <!-- HEADER -->
    <div class="engine-detail-header">
        <div>
            <div class="engine-detail-code">
                ${motore.codice}
            </div>
            <div class="text-muted">
                Cliente: ${motore.clienteNome}
            </div>
        </div>

        <span class="badge-status
            ${motore.stato == 'STOCCATO' ? 'status-stoccato' : ''}
            ${motore.stato == 'IN_LAVORAZIONE' ? 'status-lavorazione' : ''}
            ${motore.stato == 'CONSEGNATO' ? 'status-consegnato' : ''}">
            ${motore.statoLabel}
        </span>
    </div>

    <!-- IMAGES -->
    <div class="engine-detail-section">
        <h5 class="fw-semibold mb-3">Galleria immagini</h5>

        <div class="engine-detail-images">
            <c:forEach var="img" items="${motore.immagini}">
                <div class="engine-detail-image"
                     style="background-image: url('<%= request.getContextPath() %>/${img}');">
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- TECHNICAL DATA -->
    <div class="engine-detail-section card-base">
        <h5 class="fw-semibold mb-3">Dati tecnici</h5>

        <dl class="engine-detail-list">
            <dt>Codice motore</dt>
            <dd>${motore.codice}</dd>

            <dt>Cliente</dt>
            <dd>${motore.clienteNome}</dd>

            <dt>Stato</dt>
            <dd>${motore.statoLabel}</dd>

            <dt>Data inserimento</dt>
            <dd>${motore.dataInserimento}</dd>

            <dt>Ultima modifica</dt>
            <dd>${motore.ultimaModifica}</dd>

            <dt>Note tecniche</dt>
            <dd>${motore.note}</dd>
        </dl>
    </div>

    <!-- HISTORY -->
    <div class="engine-detail-section card-base">
        <h5 class="fw-semibold mb-3">Storico operazioni</h5>

        <c:forEach var="evento" items="${motore.storico}">
            <div class="engine-history-item">
                <div class="fw-semibold">${evento.titolo}</div>
                <div class="text-muted">
                    ${evento.data} – ${evento.descrizione}
                </div>
            </div>
        </c:forEach>
    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</div>
</body>
</html>
