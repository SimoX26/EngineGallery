<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>RML • Conferma eliminazione</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=12">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm border-danger">
                <div class="card-body p-4 p-md-5">
                    <h1 class="h4 text-danger mb-3">Conferma eliminazione</h1>
                    <p class="mb-1">Sei veramente sicuro di voler eliminare questo motore?</p>
                    <p class="mb-4 fw-semibold">${detail.engine.engineCode} - ${detail.engine.engineRef}</p>
                    <p class="text-muted small mb-4">Questa azione non puo essere annullata.</p>

                    <div class="d-flex flex-wrap gap-2">
                        <a href="<%= request.getContextPath() %>/engine/detail?ref=${detail.engine.engineRef}"
                           class="btn btn-cancel-action px-4">
                            Annulla
                        </a>

                        <form action="<%= request.getContextPath() %>/engine/delete" method="post" class="m-0">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                            <input type="hidden" name="engineRef" value="${detail.engine.engineRef}">
                            <input type="hidden" name="confirmDelete" value="true">
                            <button type="submit" class="btn btn-danger px-4">
                                Si, elimina
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
