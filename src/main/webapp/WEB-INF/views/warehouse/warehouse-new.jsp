<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Nuovo articolo magazzino</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 560px; width: 100%;">

        <h2 class="mb-3">Nuovo articolo</h2>

        <p class="text-muted mb-4">
            Inserisci i dati del nuovo articolo di magazzino
        </p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                ${error}
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/warehouse/new" method="post">

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Nome articolo</label>
                <input type="text"
                       name="name"
                       class="form-control"
                       value="${name}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Codice articolo</label>
                <input type="text"
                       name="sku"
                       class="form-control"
                       value="${sku}">
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Quantita</label>
                <input type="number"
                       name="quantity"
                       min="0"
                       class="form-control"
                       value="${quantity}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Ubicazione</label>
                <input type="text"
                       name="location"
                       class="form-control"
                       value="${location}">
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="notes"
                          class="form-control"
                          rows="3">${notes}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a href="<%= request.getContextPath() %>/warehouse/list" class="btn btn-outline-secondary w-50">
                    Annulla
                </a>
                <button type="submit" class="btn-engine w-50">
                    Salva articolo
                </button>
            </div>

        </form>

    </div>
</div>

</body>
</html>
