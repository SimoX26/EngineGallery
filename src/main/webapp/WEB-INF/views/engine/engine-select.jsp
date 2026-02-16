<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Selezione motore</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"  rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

    <!-- NAVBAR -->
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">

        <!-- HEADER -->
        <div class="page-header">
            <h1>Seleziona motore esistente</h1>
            <p>Visualizzazione completa dei motori presenti nel sistema</p>
        </div>

        <!-- (opzionale) errore -->
        <c:if test="${not empty error}">
            <div class="alert alert-warning">${error}</div>
        </c:if>

        <!-- GALLERY -->
        <div class="row g-4">

            <c:forEach var="engine" items="${engines}">
                <div class="col-xl-3 col-lg-4 col-md-6">

                    <div class="engine-gallery-card">

                        <!-- IMAGE -->
                        <div class="engine-image"
                             style="background-image: url('<%= request.getContextPath() %>/uploads/engines/${engine.engineRef}/${coverImages[engine.id]}');">
                        </div>

                        <!-- BODY -->
                        <div class="engine-body">

                            <div class="engine-code">
                                ${engine.engineCode} • ${engine.engineRef}
                            </div>

                            <div class="engine-client">
                                Cliente ID: ${engine.customerId}
                            </div>

                            <div class="engine-footer">

                                <!-- STATUS -->
                                <c:set var="st" value="${engine.status}" />

                                <span class="badge-status
                                    ${st == 'WAITING' ? 'status-stoccato' : ''}
                                    ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                                    ${st == 'DISASSEMBLED' ? 'status-smontato' : ''}
                                    ${st == 'READY' ? 'status-ready' : ''}
                                    ${st == 'DELIVERED' ? 'status-consegnato' : ''}">

                                    <c:choose>
                                        <c:when test="${st == 'WAITING'}">In attesa</c:when>
                                        <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                                        <c:when test="${st == 'DISASSEMBLED'}">Smontato</c:when>
                                        <c:when test="${st == 'READY'}">Pronto</c:when>
                                        <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                                        <c:otherwise>${st}</c:otherwise>
                                    </c:choose>
                                </span>

                                <!-- SELECTED ENGINE -->
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/upload?ref=${engine.engineRef}">
                                    Seleziona
                                </a>

                            </div>
                        </div>

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