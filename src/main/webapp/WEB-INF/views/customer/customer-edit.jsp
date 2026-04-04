<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Modifica cliente</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body data-back-guard-form="1"
      data-back-guard-fallback="<%= request.getContextPath() %>/customer/detail?id=${customerId}">

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 560px; width: 100%;">

        <h2 class="mb-3">Modifica cliente</h2>

        <p class="text-muted mb-4">
            Aggiorna i dati anagrafici del cliente selezionato
        </p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                ${error}
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/customer/edit?id=${customerId}" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Nome</label>
                <input type="text"
                       name="name"
                       class="form-control"
                       value="${name}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Azienda</label>
                <input type="text"
                       name="companyName"
                       class="form-control"
                       value="${companyName}">
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Telefono</label>
                <input type="text"
                       name="phone"
                       class="form-control"
                       value="${phone}">
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Email</label>
                <input type="email"
                       name="email"
                       class="form-control"
                       value="${email}">
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="notes"
                          class="form-control"
                          rows="3">${notes}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a href="<%= request.getContextPath() %>/customer/detail?id=${customerId}" class="btn btn-outline-secondary w-50">
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
