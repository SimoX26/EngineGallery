<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Manutenzione macchinari • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=12">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5">
    <div class="page-header"><h1>Manutenzione macchinari</h1><p>Attrezzature interne dell'officina</p></div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Dati salvati.</div></c:if>
    <c:if test="${param.imageError == '1'}"><div class="alert alert-warning" role="alert">Macchinario salvato, ma l'immagine non è stata caricata. Riprova da Modifica.</div></c:if>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-3"><div class="card-base"><div>Macchinari</div><strong class="fs-3">${machineCount}</strong></div></div>
        <div class="col-6 col-lg-3"><div class="card-base"><div>Fuori servizio</div><strong class="fs-3">${outOfServiceCount}</strong></div></div>
        <div class="col-6 col-lg-3"><div class="card-base"><div>Scadute</div><strong class="fs-3 text-danger">${overdueCount}</strong></div></div>
        <div class="col-6 col-lg-3"><div class="card-base"><div>Imminenti</div><strong class="fs-3 text-warning">${dueSoonCount}</strong></div></div>
    </div>

    <section class="card-base mb-4" id="scadenze">
        <h2 class="h4">Scadenzario</h2>
        <p class="text-muted">Imminenti entro 30 giorni o 50 ore. Una scadenza per ore diventa scaduta dopo il superamento della soglia.</p>
        <div class="table-responsive"><table class="table align-middle">
            <thead><tr><th>Macchinario</th><th>Intervento</th><th>Scadenza</th><th>Stato</th></tr></thead>
            <tbody>
            <c:forEach var="plan" items="${plans}">
                <tr>
                    <td><c:out value="${plan.machineName}"/></td>
                    <td><c:out value="${plan.description}"/></td>
                    <td><c:choose><c:when test="${not empty plan.dueDate}"><c:out value="${plan.dueDate}"/></c:when><c:otherwise><c:out value="${plan.dueHours}"/> ore (attuali: <c:out value="${plan.machineHours}"/>)</c:otherwise></c:choose></td>
                    <td><c:choose>
                        <c:when test="${plan.completed}"><span class="badge text-bg-success">Eseguita</span></c:when>
                        <c:when test="${plan.overdue}"><span class="badge text-bg-danger">Scaduta</span></c:when>
                        <c:when test="${plan.dueSoon}"><span class="badge text-bg-warning">Imminente</span></c:when>
                        <c:otherwise><span class="badge text-bg-secondary">Programmata</span></c:otherwise>
                    </c:choose></td>
                </tr>
            </c:forEach>
            <c:if test="${empty plans}"><tr><td colspan="4" class="text-muted">Nessun piano registrato.</td></tr></c:if>
            </tbody>
        </table></div>
    </section>

    <section class="card-base mb-4" id="macchinari">
        <div class="d-flex justify-content-between align-items-center gap-2 mb-3">
            <h2 class="h4 mb-0">Anagrafica macchinari</h2>
            <c:choose><c:when test="${not empty editMachine}"><a class="btn btn-primary" href="${pageContext.request.contextPath}/workshop-maintenance?newMachine=1#machine-form">Aggiungi macchinario</a></c:when><c:otherwise><button class="btn btn-primary" type="button" data-bs-toggle="collapse" data-bs-target="#machine-form" aria-controls="machine-form" aria-expanded="${showMachineForm or param.newMachine == '1'}">Aggiungi macchinario</button></c:otherwise></c:choose>
        </div>
        <div class="table-responsive"><table class="table align-middle">
            <thead><tr><th>Foto</th><th>Nome</th><th>Modello</th><th>Matricola</th><th>Posizione</th><th>Ore</th><th>Stato</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="machine" items="${machines}"><tr>
                <td><c:if test="${not empty machine.imageFilename}"><img src="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${machine.imageFilename}" alt="Foto di ${fn:escapeXml(machine.name)}" class="rounded" style="width:72px;height:72px;object-fit:cover" loading="lazy"></c:if></td>
                <td><c:out value="${machine.name}"/></td><td><c:out value="${machine.model}"/></td>
                <td><c:out value="${machine.serialNumber}"/></td><td><c:out value="${machine.location}"/></td>
                <td><c:out value="${machine.usageHours}"/></td>
                <td>${machine.status == 'ACTIVE' ? 'Attivo' : 'Fuori servizio'}</td>
                <td><a href="${pageContext.request.contextPath}/workshop-maintenance?edit=${machine.id}#machine-form">Modifica</a></td>
            </tr></c:forEach>
            <c:if test="${empty machines}"><tr><td colspan="8" class="text-muted">Nessun macchinario registrato.</td></tr></c:if>
            </tbody>
        </table></div>
    </section>

    <div class="row g-4 mb-4">
        <div class="col-lg-6"><div class="collapse ${not empty editMachine or showMachineForm or param.newMachine == '1' ? 'show' : ''}" id="machine-form"><section class="card-base h-100">
            <h2 class="h4">${empty editMachine ? 'Nuovo macchinario' : 'Modifica macchinario'}</h2>
            <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/workshop-maintenance">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                <input type="hidden" name="action" value="${empty editMachine ? 'createMachine' : 'updateMachine'}">
                <c:if test="${not empty editMachine}"><input type="hidden" name="machineId" value="${editMachine.id}"></c:if>
                <div class="mb-2"><label class="form-label" for="machine-name">Nome</label><input class="form-control" id="machine-name" name="name" maxlength="120" required value="<c:out value='${editMachine.name}'/>"></div>
                <div class="mb-2"><label class="form-label" for="machine-model">Modello</label><input class="form-control" id="machine-model" name="model" maxlength="120" required value="<c:out value='${editMachine.model}'/>"></div>
                <div class="mb-2"><label class="form-label" for="machine-serial">Matricola</label><input class="form-control" id="machine-serial" name="serialNumber" maxlength="120" required value="<c:out value='${editMachine.serialNumber}'/>"></div>
                <div class="mb-2"><label class="form-label" for="machine-location">Posizione</label><input class="form-control" id="machine-location" name="location" maxlength="120" required value="<c:out value='${editMachine.location}'/>"></div>
                <div class="mb-2"><label class="form-label" for="machine-hours">Ore di utilizzo</label><input class="form-control" type="number" min="0" id="machine-hours" name="usageHours" required value="${empty editMachine ? 0 : editMachine.usageHours}"></div>
                <div class="mb-3"><label class="form-label" for="machine-image">Immagine (facoltativa, massimo 10 MB)</label><input class="form-control" type="file" accept="image/*" id="machine-image" name="image"><c:if test="${not empty editMachine.imageFilename}"><small class="text-muted">Una nuova immagine sostituisce quella attuale.</small></c:if></div>
                <c:if test="${not empty editMachine}"><div class="mb-3"><label class="form-label" for="machine-status">Stato</label><select class="form-select" id="machine-status" name="status"><option value="ACTIVE" ${editMachine.status == 'ACTIVE' ? 'selected' : ''}>Attivo</option><option value="OUT_OF_SERVICE" ${editMachine.status == 'OUT_OF_SERVICE' ? 'selected' : ''}>Fuori servizio</option></select></div></c:if>
                <button class="btn btn-primary" type="submit">Salva macchinario</button>
                <c:if test="${not empty editMachine}"><a class="btn btn-link" href="${pageContext.request.contextPath}/workshop-maintenance#machine-form">Annulla</a></c:if>
            </form>
        </section></div></div>

        <div class="col-lg-6"><section class="card-base h-100" id="plan-form">
            <h2 class="h4">Nuovo piano</h2>
            <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}"><input type="hidden" name="action" value="createPlan">
                <div class="mb-2"><label class="form-label" for="plan-machine">Macchinario</label><select class="form-select" id="plan-machine" name="machineId" required><option value="">Seleziona</option><c:forEach var="machine" items="${machines}"><option value="${machine.id}"><c:out value="${machine.name}"/> — <c:out value="${machine.serialNumber}"/></option></c:forEach></select></div>
                <div class="mb-2"><label class="form-label" for="plan-description">Intervento previsto</label><input class="form-control" id="plan-description" name="description" maxlength="500" required></div>
                <p class="text-muted mb-2">Indica una sola scadenza.</p>
                <div class="mb-2"><label class="form-label" for="plan-date">Data</label><input class="form-control" type="date" id="plan-date" name="dueDate"></div>
                <div class="mb-3"><label class="form-label" for="plan-hours">Oppure ore di utilizzo</label><input class="form-control" type="number" min="0" id="plan-hours" name="dueHours"></div>
                <button class="btn btn-primary" type="submit" ${empty machines ? 'disabled' : ''}>Salva piano</button>
            </form>
        </section></div>
    </div>

    <section class="card-base mb-4" id="intervention-form">
        <h2 class="h4">Registra intervento</h2>
        <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}"><input type="hidden" name="action" value="recordIntervention">
            <div class="col-md-6"><label class="form-label" for="intervention-machine">Macchinario</label><select class="form-select" id="intervention-machine" name="machineId" required><option value="">Seleziona</option><c:forEach var="machine" items="${machines}"><option value="${machine.id}"><c:out value="${machine.name}"/> — <c:out value="${machine.serialNumber}"/></option></c:forEach></select></div>
            <div class="col-md-6"><label class="form-label" for="intervention-plan">Piano da chiudere (facoltativo)</label><select class="form-select" id="intervention-plan" name="planId"><option value="">Intervento senza piano</option><c:forEach var="plan" items="${plans}"><c:if test="${not plan.completed}"><option value="${plan.id}"><c:out value="${plan.machineName}"/> — <c:out value="${plan.description}"/></option></c:if></c:forEach></select></div>
            <div class="col-md-6"><label class="form-label" for="intervention-type">Tipo</label><select class="form-select" id="intervention-type" name="type"><option value="ORDINARY">Ordinaria</option><option value="EXTRAORDINARY">Straordinaria</option></select></div>
            <div class="col-md-6"><label class="form-label" for="intervention-date">Data</label><input class="form-control" type="date" id="intervention-date" name="performedOn" max="${today}" value="${today}" required></div>
            <div class="col-12"><label class="form-label" for="intervention-description">Descrizione</label><textarea class="form-control" id="intervention-description" name="description" maxlength="5000" required></textarea></div>
            <div class="col-12"><label class="form-label" for="intervention-parts">Ricambi utilizzati</label><textarea class="form-control" id="intervention-parts" name="parts" maxlength="5000"></textarea></div>
            <div class="col-12"><button class="btn btn-primary" type="submit" ${empty machines ? 'disabled' : ''}>Registra intervento</button></div>
        </form>
        <p class="text-muted mt-2 mb-0">Operatore: <c:out value="${sessionScope.loggedUser.username}"/></p>
    </section>

    <section class="card-base" id="storico"><h2 class="h4">Storico interventi</h2>
        <div class="table-responsive"><table class="table align-middle">
            <thead><tr><th>Data</th><th>Macchinario</th><th>Tipo</th><th>Descrizione</th><th>Ricambi</th><th>Operatore</th></tr></thead>
            <tbody><c:forEach var="item" items="${interventions}"><tr>
                <td><c:out value="${item.performedOn}"/></td><td><c:out value="${item.machineName}"/></td>
                <td>${item.type == 'ORDINARY' ? 'Ordinaria' : 'Straordinaria'}</td>
                <td><c:out value="${item.description}"/></td><td><c:out value="${item.parts}"/></td>
                <td><c:out value="${item.operator}"/></td>
            </tr></c:forEach><c:if test="${empty interventions}"><tr><td colspan="6" class="text-muted">Nessun intervento registrato.</td></tr></c:if></tbody>
        </table></div>
    </section>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
