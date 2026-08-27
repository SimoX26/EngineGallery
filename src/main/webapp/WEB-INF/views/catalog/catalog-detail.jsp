<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Dettaglio Catalogo</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=13">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-4">
    <div class="card-base">
        <div class="engine-detail-section">
            <h5 class="fw-semibold mb-3">Dettaglio catalogo</h5>

            <dl class="engine-detail-list">
                <dt>Diametro cilindro:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty catalogItem.cylinderDiameterMm}">
                            <c:out value="${catalogItem.cylinderDiameterMm}" /> mm
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Marca / modello motore:</dt>
                <dd><c:out value="${catalogItem.engineModel}" default="—" /></dd>

                <dt>Cilindrata:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty catalogItem.displacementCc}">
                            <c:out value="${catalogItem.displacementCc}" /> cm³
                        </c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Numero valvole:</dt>
                <dd><c:out value="${catalogItem.valveCount}" default="—" /></dd>

                <dt>Codice motore:</dt>
                <dd><c:out value="${catalogItem.engineCode}" default="—" /></dd>
            </dl>
        </div>

        <div class="row mt-4">
            <div class="col-12 detail-actions d-flex justify-content-center">
                <a href="<%= request.getContextPath() %>/catalog"
                   class="btn engine-detail-action-btn engine-detail-action-btn--back">
                    Indietro
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
