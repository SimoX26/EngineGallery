<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>${empty machine ? 'Nuovo macchinario' : 'Modifica macchinario'} • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=22">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/workshop-maintenance">← Manutenzione macchinari</a>
        <h1 class="mt-3">${empty machine ? 'Nuovo macchinario' : 'Modifica macchinario'}</h1>
        <p>${empty machine ? 'Inserisci i dati dell’attrezzatura.' : 'Aggiorna l’anagrafica e la foto del macchinario.'}</p>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.imageError == '1'}"><div class="alert alert-warning" role="alert">Macchinario salvato, ma l'immagine non è stata caricata. Riprova.</div></c:if>
    <section class="card-base workshop-form-panel">
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="${empty machine ? 'createMachine' : 'updateMachine'}">
            <c:if test="${not empty machine}"><input type="hidden" name="machineId" value="${machine.id}"></c:if>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="machine-name">Nome</label>
                <input class="form-control" id="machine-name" name="name" maxlength="120" required value="<c:out value='${not empty error ? param.name : machine.name}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="machine-model">Modello</label>
                <input class="form-control" id="machine-model" name="model" maxlength="120" required value="<c:out value='${not empty error ? param.model : machine.model}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="machine-serial">Matricola</label>
                <input class="form-control" id="machine-serial" name="serialNumber" maxlength="120" required value="<c:out value='${not empty error ? param.serialNumber : machine.serialNumber}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="machine-location">Posizione</label>
                <input class="form-control" id="machine-location" name="location" maxlength="120" required value="<c:out value='${not empty error ? param.location : machine.location}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="machine-purchase-date">Data di acquisto</label>
                <input class="form-control" type="date" id="machine-purchase-date" name="purchaseDate" max="${today}" value="<c:out value='${not empty error ? param.purchaseDate : machine.purchaseDate}'/>">
                <div class="form-text">Facoltativa per i macchinari già presenti.</div>
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="machine-hours">Ore di utilizzo</label>
                <c:set var="hoursValue" value="${not empty error ? param.usageHours : (empty machine ? 0 : machine.usageHours)}"/>
                <input class="form-control" type="number" min="0" id="machine-hours" name="usageHours" required value="<c:out value='${hoursValue}'/>">
            </div>
            <c:if test="${not empty machine}">
                <div class="col-md-6">
                    <label class="form-label fw-semibold" for="machine-status">Stato</label>
                    <c:set var="currentStatus" value="${not empty error ? param.status : machine.status}"/>
                    <select class="form-select" id="machine-status" name="status">
                        <option value="ACTIVE" ${currentStatus == 'ACTIVE' ? 'selected' : ''}>Attivo</option>
                        <option value="OUT_OF_SERVICE" ${currentStatus == 'OUT_OF_SERVICE' ? 'selected' : ''}>Fuori servizio</option>
                    </select>
                </div>
            </c:if>
            <div class="col-12">
                <label class="form-label fw-semibold" for="machine-image">Foto del macchinario</label>
                <input class="form-control" type="file" accept="image/*" id="machine-image" name="image">
                <div class="form-text">Facoltativa, massimo 10 MB. Una nuova foto sostituisce quella attuale.</div>
                <c:if test="${not empty machine.imageFilename}">
                    <img class="workshop-form-preview mt-3" src="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${fn:escapeXml(machine.imageFilename)}" alt="Foto attuale di ${fn:escapeXml(machine.name)}">
                </c:if>
            </div>
            <div class="col-12 d-flex flex-wrap gap-2 align-items-center mt-4">
                <button class="btn btn-primary" type="submit">Salva macchinario</button>
                <c:choose>
                    <c:when test="${empty machine}"><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance">Annulla</a></c:when>
                    <c:otherwise><a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=detail&amp;id=${machine.id}">Annulla</a></c:otherwise>
                </c:choose>
            </div>
        </form>
    </section>
</main>
</body>
</html>
