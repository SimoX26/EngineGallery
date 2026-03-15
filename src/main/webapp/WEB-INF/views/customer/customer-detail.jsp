<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Dettaglio Cliente</title>

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

    <c:if test="${deletedError == '1'}">
        <div class="alert alert-danger" role="alert">
            Eliminazione non consentita: il cliente è associato a motori esistenti.
        </div>
    </c:if>

    <div class="card-base">
        <div class="engine-detail-section">
            <h5 class="fw-semibold mb-3">Dettaglio cliente</h5>

            <dl class="engine-detail-list">
                <dt>Nome:</dt>
                <dd>${customer.name}</dd>

                <dt>Azienda:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.companyName}">${customer.companyName}</c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Telefono:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.phone}">${customer.phone}</c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Email:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.email}">${customer.email}</c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Note:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.notes}">${customer.notes}</c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>
            </dl>
        </div>

        <div class="row mt-4">
            <div class="col-12 d-flex justify-content-end gap-3">
                <a href="<%= request.getContextPath() %>/customer/edit?id=${customer.id}" class="btn btn-primary px-4">
                    Modifica
                </a>

                <c:choose>
                    <c:when test="${canDelete}">
                        <a href="<%= request.getContextPath() %>/customer/delete?id=${customer.id}" class="btn btn-danger px-4">
                            Elimina
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-danger px-4" disabled
                                title="Cliente associato a motori esistenti">
                            Elimina
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
