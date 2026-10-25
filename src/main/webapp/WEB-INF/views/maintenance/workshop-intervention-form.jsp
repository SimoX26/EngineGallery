<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Nuovo intervento • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=22">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/workshop-maintenance">← Manutenzione macchinari</a>
        <h1 class="mt-3">Nuovo intervento</h1>
        <p>Registra un intervento di manutenzione effettuato.</p>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${empty machines}"><div class="alert alert-info">Aggiungi prima un macchinario. <a href="${pageContext.request.contextPath}/workshop-maintenance?view=new">Aggiungi macchinario</a></div></c:if>
    <section class="card-base workshop-form-panel">
        <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="recordIntervention">
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="intervention-machine">Macchinario</label>
                <select class="form-select" id="intervention-machine" name="machineId" required>
                    <option value="">Seleziona un macchinario</option>
                    <c:set var="chosenMachineId" value="${not empty error ? param.machineId : selectedMachineId}"/>
                    <c:forEach var="machine" items="${machines}">
                        <option value="${machine.id}" ${chosenMachineId == machine.id ? 'selected' : ''}><c:out value="${machine.name}"/> — <c:out value="${machine.serialNumber}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="intervention-type">Tipo</label>
                <c:set var="chosenType" value="${not empty error ? param.type : 'ORDINARY'}"/>
                <select class="form-select" id="intervention-type" name="type" required>
                    <option value="ORDINARY" ${chosenType == 'ORDINARY' ? 'selected' : ''}>Ordinaria</option>
                    <option value="EXTRAORDINARY" ${chosenType == 'EXTRAORDINARY' ? 'selected' : ''}>Straordinaria</option>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="intervention-date">Data dell'intervento</label>
                <input class="form-control" type="date" id="intervention-date" name="performedOn" max="${today}" required value="<c:out value='${not empty error ? param.performedOn : today}'/>">
            </div>
            <div class="col-12">
                <label class="form-label fw-semibold" for="intervention-description">Descrizione</label>
                <textarea class="form-control" id="intervention-description" name="description" rows="4" maxlength="5000" required><c:out value="${param.description}"/></textarea>
            </div>
            <div class="col-12">
                <label class="form-label fw-semibold" for="intervention-parts">Ricambi utilizzati</label>
                <textarea class="form-control" id="intervention-parts" name="parts" rows="3" maxlength="5000"><c:out value="${param.parts}"/></textarea>
            </div>
            <div class="col-12 small text-muted">Operatore: <c:out value="${sessionScope.loggedUser.username}"/></div>
            <div class="col-12 d-flex flex-wrap gap-2 align-items-center">
                <button class="btn btn-primary" type="submit" ${empty machines ? 'disabled' : ''}>Registra intervento</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance">Annulla</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
