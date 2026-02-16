<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Rubrica Clienti</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>

<body>

<!-- NAVBAR (sempre fuori dal container) -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="container">

    <!-- HEADER -->
    <div class="page-header">
        <h1>Rubrica Clienti</h1>
        <p>Elenco completo dei clienti</p>
    </div>

    <!-- ERROR -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
            ${error}
        </div>
    </c:if>

    <!-- LISTA CLIENTI -->
    <div class="customer-list">

        <c:forEach var="customer" items="${customers}">

            <div class="card-base customer-card">

                <div class="customer-row">

                    <div class="customer-field">
                        <div class="customer-main">
                            ${customer.name}
                        </div>
                        <div class="customer-meta">
                            ID ${customer.id}
                        </div>
                    </div>

                    <c:if test="${not empty customer.companyName}">
                        <div class="customer-field">
                            <div class="customer-meta">Azienda</div>
                            <div>${customer.companyName}</div>
                        </div>
                    </c:if>

                    <c:if test="${not empty customer.phone}">
                        <div class="customer-field">
                            <div class="customer-meta">Telefono</div>
                            <div>${customer.phone}</div>
                        </div>
                    </c:if>

                    <c:if test="${not empty customer.email}">
                        <div class="customer-field">
                            <div class="customer-meta">Email</div>
                            <div>${customer.email}</div>
                        </div>
                    </c:if>

                    <div class="customer-field">
                        <div class="customer-meta">Creato</div>
                        <div>${customer.createdAt}</div>
                    </div>

                </div>

                <c:if test="${not empty customer.notes}">
                    <div class="customer-notes">
                        ${customer.notes}
                    </div>
                </c:if>

            </div>

        </c:forEach>

    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>