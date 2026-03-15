<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Dettaglio Articolo</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-4">
    <c:if test="${updated}">
        <div class="alert alert-success" role="alert">
            Modifiche salvate correttamente.
        </div>
    </c:if>

    <div class="card-base">
        <div class="engine-detail-section">
            <h5 class="fw-semibold mb-3">Dettaglio articolo magazzino</h5>

            <dl class="engine-detail-list">
                <dt>Nome:</dt>
                <dd>${item.name}</dd>

                <dt>Codice:</dt>
                <dd><c:out value="${item.sku}" default="—" /></dd>

                <dt>Disponibilita:</dt>
                <dd>${item.quantity}</dd>

                <dt>Ubicazione:</dt>
                <dd><c:out value="${item.location}" default="—" /></dd>

                <dt>Note:</dt>
                <dd><c:out value="${item.notes}" default="—" /></dd>
            </dl>
        </div>

        <div class="row mt-4">
            <div class="col-12 d-flex justify-content-end gap-3">
                <a href="<%= request.getContextPath() %>/warehouse/list" class="btn btn-outline-secondary px-4">
                    Indietro
                </a>
                <a href="<%= request.getContextPath() %>/warehouse/edit?id=${item.id}" class="btn btn-detail-edit px-4">
                    Modifica
                </a>
                <a href="<%= request.getContextPath() %>/warehouse/delete?id=${item.id}" class="btn btn-detail-delete px-4">
                    Elimina
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
