<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Dettaglio Cliente</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<c:url var="customerEditUrl" value="/customer/edit">
    <c:param name="id" value="${customer.id}" />
</c:url>
<c:url var="customerDeleteUrl" value="/customer/delete">
    <c:param name="id" value="${customer.id}" />
</c:url>

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
                <dd><c:out value="${customer.name}" default="—" /></dd>

                <dt>Azienda:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.companyName}"><c:out value="${customer.companyName}" /></c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Telefono:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.phone}"><c:out value="${customer.phone}" /></c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Email:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.email}"><c:out value="${customer.email}" /></c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>

                <dt>Note:</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty customer.notes}"><c:out value="${customer.notes}" /></c:when>
                        <c:otherwise>—</c:otherwise>
                    </c:choose>
                </dd>
            </dl>
        </div>

        <div class="row mt-4">
            <div class="col-12 detail-actions d-flex flex-wrap justify-content-center gap-2 gap-md-3">
                <a href="<%= request.getContextPath() %>/customer/list" class="btn btn-outline-secondary px-4">
                    Indietro
                </a>

                <a href="<c:out value='${customerEditUrl}'/>" class="btn btn-detail-edit px-4">
                    Modifica
                </a>

                <c:choose>
                    <c:when test="${canDelete}">
                        <a href="<c:out value='${customerDeleteUrl}'/>" class="btn btn-detail-delete px-4">
                            Elimina
                        </a>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-detail-delete px-4" disabled
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
