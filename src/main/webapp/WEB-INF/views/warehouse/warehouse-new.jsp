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

    <div class="card-base" style="max-width: 560px; width: 100%;">

        <h2 class="mb-3 text-center">Aggiungi articolo</h2>

        <p class="text-muted mb-4 text-center">
            Inserisci i dati del nuovo articolo di magazzino
        </p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                ${error}
            </div>
        </c:if>

        <div class="alert alert-light border text-start py-2 px-3 mb-4 clickable-fields-hint" role="note">
            Compila i campi e premi <strong>Salva articolo</strong>.
        </div>

        <form action="<%= request.getContextPath() %>/warehouse/new"
              method="post"
              class="form-click-guides">

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Nome articolo</label>
                <div class="position-relative field-icon-wrap">
                    <span class="field-icon" aria-hidden="true">✎</span>
                    <input type="text"
                           name="name"
                           class="form-control has-field-icon"
                           value="${name}"
                           required>
                </div>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Codice articolo</label>
                <div class="position-relative field-icon-wrap">
                    <span class="field-icon" aria-hidden="true">#</span>
                    <input type="text"
                           name="sku"
                           class="form-control has-field-icon"
                           value="${sku}">
                </div>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Quantita</label>
                <div class="position-relative field-icon-wrap">
                    <span class="field-icon" aria-hidden="true">#</span>
                    <input type="number"
                           name="quantity"
                           min="0"
                           class="form-control has-field-icon"
                           value="${quantity}"
                           required>
                </div>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Ubicazione</label>
                <div class="position-relative field-icon-wrap">
                    <span class="field-icon" aria-hidden="true">⌂</span>
                    <input type="text"
                           name="location"
                           class="form-control has-field-icon"
                           value="${location}">
                </div>
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Note</label>
                <div class="position-relative field-icon-wrap">
                    <span class="field-icon field-icon-textarea" aria-hidden="true">✎</span>
                    <textarea name="notes"
                              class="form-control has-field-icon"
                              rows="3">${notes}</textarea>
                </div>
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
