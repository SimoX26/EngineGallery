<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Modifica motore</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 560px; width: 100%;">

        <h2 class="mb-3">Modifica motore</h2>

        <p class="text-muted mb-4">
            Aggiorna i dati tecnici del motore selezionato
        </p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                ${error}
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/engine/edit?ref=${engineRef}" method="post">

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Riferimento</label>
                <input type="text" class="form-control" value="${engineRef}" readonly>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Nome cliente</label>
                <input type="text"
                       name="customer"
                       class="form-control"
                       value="${customer}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Codice motore</label>
                <input type="text"
                       name="engineCode"
                       class="form-control"
                       value="${engineCode}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Stato</label>
                <select class="form-select" name="status" required>
                    <option value="WAITING" ${status == 'WAITING' ? 'selected' : ''}>In attesa</option>
                    <option value="WORK_IN_PROGRESS" ${status == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                    <option value="READY" ${status == 'READY' ? 'selected' : ''}>Pronto</option>
                    <option value="DELIVERED" ${status == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                </select>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Data ingresso</label>
                <input type="date"
                       name="intakeDate"
                       class="form-control"
                       value="${intakeDate}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Data consegna</label>
                <input type="date"
                       name="deliveryDate"
                       class="form-control"
                       value="${deliveryDate}">
                <div class="small text-muted mt-1">
                    Usata solo se lo stato è "Consegnato".
                </div>
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="note"
                          class="form-control"
                          rows="3">${note}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a href="<%= request.getContextPath() %>/engine/detail?ref=${engineRef}" class="btn btn-outline-secondary w-50">
                    Annulla
                </a>
                <button type="submit" class="btn-engine w-50">
                    Salva modifiche
                </button>
            </div>

        </form>

    </div>
</div>

</body>
</html>
