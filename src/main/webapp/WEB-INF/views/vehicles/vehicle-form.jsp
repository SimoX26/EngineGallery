<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>${empty vehicle ? 'Nuovo veicolo' : 'Modifica veicolo'} • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=34">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container-fluid d-flex align-items-center justify-content-center py-4" style="min-height: calc(100vh - 70px);">
    <section class="card-base text-center" style="max-width: 520px; width: 100%;">
        <h1 class="h2 mb-3">${empty vehicle ? 'Nuovo veicolo' : 'Modifica veicolo'}</h1>
        <c:if test="${not empty error}"><div class="alert alert-danger text-start" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/vehicles" class="form-click-guides">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="${empty vehicle ? 'createVehicle' : 'updateVehicle'}">
            <c:if test="${not empty vehicle}"><input type="hidden" name="id" value="${vehicle.id}"></c:if>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="plate">Targa</label>
                <input class="form-control" id="plate" name="plate" maxlength="20" required value="<c:out value='${not empty error ? param.plate : vehicle.plate}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="make">Marca</label>
                <input class="form-control" id="make" name="make" maxlength="100" required value="<c:out value='${not empty error ? param.make : vehicle.make}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="model">Modello</label>
                <input class="form-control" id="model" name="model" maxlength="100" required value="<c:out value='${not empty error ? param.model : vehicle.model}'/>">
            </div>
            <div class="mb-4 text-start">
                <label class="form-label fw-semibold" for="notes">Note</label>
                <textarea class="form-control" id="notes" name="notes" rows="3" maxlength="2000"><c:out value="${not empty error ? param.notes : vehicle.notes}"/></textarea>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-cancel-action w-50" href="${pageContext.request.contextPath}/vehicles">Annulla</a>
                <button class="btn btn-save-action w-50" type="submit">Salva</button>
            </div>
        </form>
    </section>
</main>
</body>
</html>
