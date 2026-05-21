<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Manutenzione</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header">
            <h1>Manutenzione</h1>
        </div>

        <div class="card-base">
            <h5 class="fw-semibold mb-2">Strumenti di manutenzione del sistema</h5>
            <p class="text-muted mb-0">Area riservata agli amministratori.</p>
        </div>

        <div class="card-base mt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-semibold mb-0">Audit Log Utenti</h5>
                <span class="badge text-bg-secondary">Ultime 100 azioni</span>
            </div>

            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead>
                    <tr>
                        <th scope="col">Data/Ora</th>
                        <th scope="col">Utente</th>
                        <th scope="col">Ruolo</th>
                        <th scope="col">Azione</th>
                        <th scope="col">Entità</th>
                        <th scope="col">ID Entità</th>
                        <th scope="col">Descrizione</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${recentActivityLogs}" var="log">
                        <tr>
                            <td>${log.createdAt}</td>
                            <td>${log.username}</td>
                            <td>${log.userRole}</td>
                            <td>${log.actionType}</td>
                            <td>${log.entityType}</td>
                            <td>${empty log.entityId ? "-" : log.entityId}</td>
                            <td>${log.description}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty recentActivityLogs}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-3">Nessuna attività registrata.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
