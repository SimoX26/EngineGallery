<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Engine Gallery • Rubrica Clienti</title>

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
            <h1>Rubrica Clienti</h1>
            <p>Elenco completo dei clienti registrati nel sistema</p>
        </div>

        <!-- ERRORE -->
        <c:if test="${not empty error}">
            <div class="alert alert-warning">${error}</div>
        </c:if>

        <!-- TABELLA -->
        <div class="table-responsive">

            <table class="table table-hover align-middle custom-table">

                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Nome</th>
                        <th>Email</th>
                        <th>Telefono</th>
                        <th class="text-end">Azioni</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="customer" items="${customers}">
                        <tr>

                            <td>${customer.id}</td>

                            <td>
                                <strong>${customer.firstName} ${customer.lastName}</strong>
                            </td>

                            <td>${customer.email}</td>

                            <td>${customer.phone}</td>

                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${pageContext.request.contextPath}/customer/detail?id=${customer.id}">
                                    Dettaglio
                                </a>
                            </td>

                        </tr>
                    </c:forEach>

                    <!-- Se non ci sono clienti -->
                    <c:if test="${empty customers}">
                        <tr>
                            <td colspan="5" class="text-center text-muted py-4">
                                Nessun cliente presente nel sistema
                            </td>
                        </tr>
                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>