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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=34">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/vehicles">← Veicoli</a>
        <h1 class="mt-3">${empty vehicle ? 'Nuovo veicolo' : 'Modifica veicolo'}</h1>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <section class="card-base workshop-form-panel">
        <form method="post" action="${pageContext.request.contextPath}/vehicles" class="row g-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="${empty vehicle ? 'createVehicle' : 'updateVehicle'}">
            <c:if test="${not empty vehicle}"><input type="hidden" name="id" value="${vehicle.id}"></c:if>
            <div class="col-md-4">
                <label class="form-label fw-semibold" for="plate">Targa</label>
                <input class="form-control" id="plate" name="plate" maxlength="20" required value="<c:out value='${not empty error ? param.plate : vehicle.plate}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label fw-semibold" for="make">Marca</label>
                <input class="form-control" id="make" name="make" maxlength="100" required value="<c:out value='${not empty error ? param.make : vehicle.make}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label fw-semibold" for="model">Modello</label>
                <input class="form-control" id="model" name="model" maxlength="100" required value="<c:out value='${not empty error ? param.model : vehicle.model}'/>">
            </div>
            <div class="col-12">
                <label class="form-label fw-semibold" for="notes">Note</label>
                <textarea class="form-control" id="notes" name="notes" rows="3" maxlength="2000"><c:out value="${not empty error ? param.notes : vehicle.notes}"/></textarea>
            </div>
            <div class="col-12 d-flex gap-2 mt-4">
                <button class="btn btn-primary" type="submit">Salva veicolo</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/vehicles">Annulla</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
