<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>${empty event ? 'Nuova voce' : 'Modifica voce'} • Veicoli • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=39">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/vehicles?view=detail&amp;id=${vehicle.id}">← <c:out value="${vehicle.plate}"/></a>
        <h1 class="mt-3">${empty event ? 'Nuova scadenza o manutenzione' : 'Modifica scadenza o manutenzione'}</h1>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <section class="card-base workshop-form-panel">
        <form method="post" action="${pageContext.request.contextPath}/vehicles" class="row g-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="${empty event ? 'createEvent' : 'updateEvent'}">
            <input type="hidden" name="id" value="${vehicle.id}">
            <c:if test="${not empty event}"><input type="hidden" name="eventId" value="${event.id}"></c:if>
            <c:set var="currentType" value="${not empty error ? param.type : event.type}"/>
            <div class="col-md-4">
                <label class="form-label fw-semibold" for="type">Tipo</label>
                <select class="form-select" id="type" name="type" required>
                    <option value="DEADLINE" ${currentType == 'DEADLINE' ? 'selected' : ''}>Scadenza</option>
                    <option value="MAINTENANCE" ${currentType == 'MAINTENANCE' ? 'selected' : ''}>Manutenzione</option>
                </select>
            </div>
            <div class="col-md-8">
                <label class="form-label fw-semibold" for="title">Titolo</label>
                <input class="form-control" id="title" name="title" maxlength="150" required placeholder="Es. Revisione, bollo, tagliando" value="<c:out value='${not empty error ? param.title : event.title}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="due-date">Data di scadenza o dell'intervento</label>
                <input class="form-control" type="date" id="due-date" name="dueDate" required value="<c:out value='${not empty error ? param.dueDate : event.dueDate}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="completed-on">Data di completamento</label>
                <input class="form-control" type="date" id="completed-on" name="completedOn" value="<c:out value='${not empty error ? param.completedOn : event.completedOn}'/>">
                <div class="form-text">Lascia vuoto se ancora da fare.</div>
            </div>
            <div class="col-12">
                <label class="form-label fw-semibold" for="notes">Note</label>
                <textarea class="form-control" id="notes" name="notes" rows="3" maxlength="2000"><c:out value="${not empty error ? param.notes : event.notes}"/></textarea>
            </div>
            <div class="col-12 d-flex gap-2 mt-4">
                <button class="btn btn-primary" type="submit">Salva voce</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/vehicles?view=detail&amp;id=${vehicle.id}">Annulla</a>
            </div>
        </form>
    </section>
</main>
</body>
</html>
