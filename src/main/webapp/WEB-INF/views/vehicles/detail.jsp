<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title><c:out value="${vehicle.plate}"/> • Veicoli • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=34">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <header class="page-header-with-search workshop-maintenance-header">
        <div class="page-header">
            <a class="text-decoration-none" href="${pageContext.request.contextPath}/vehicles">← Veicoli</a>
            <h1 class="mt-3"><c:out value="${vehicle.make}"/> <c:out value="${vehicle.model}"/></h1>
            <p>Targa <strong><c:out value="${vehicle.plate}"/></strong></p>
        </div>
        <div class="workshop-maintenance-header__actions">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/vehicles?view=edit&amp;id=${vehicle.id}">Modifica veicolo</a>
            <a class="btn btn-add-plus" href="${pageContext.request.contextPath}/vehicles?view=newEvent&amp;id=${vehicle.id}">Aggiungi scadenza o manutenzione +</a>
        </div>
    </header>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Modifiche salvate.</div></c:if>
    <c:if test="${not empty vehicle.notes}"><div class="card-base mb-4"><strong>Note sul veicolo</strong><p class="mb-0 mt-2"><c:out value="${vehicle.notes}"/></p></div></c:if>
    <c:if test="${not empty vehicle.vehiclePhotoFilename or not empty vehicle.registrationPhotoFilename}">
        <section class="card-base mb-4" aria-label="Foto del veicolo">
            <div class="row g-3">
                <c:if test="${not empty vehicle.vehiclePhotoFilename}"><div class="col-12 col-md-6"><h2 class="h6">Foto del veicolo</h2><a href="${pageContext.request.contextPath}/vehicles?view=photo&amp;id=${vehicle.id}&amp;kind=vehicle" target="_blank" rel="noopener"><img class="img-fluid rounded" src="${pageContext.request.contextPath}/vehicles?view=photo&amp;id=${vehicle.id}&amp;kind=vehicle" alt="Foto del veicolo"></a></div></c:if>
                <c:if test="${not empty vehicle.registrationPhotoFilename}"><div class="col-12 col-md-6"><h2 class="h6">Foto del libretto</h2><a href="${pageContext.request.contextPath}/vehicles?view=photo&amp;id=${vehicle.id}&amp;kind=registration" target="_blank" rel="noopener"><img class="img-fluid rounded" src="${pageContext.request.contextPath}/vehicles?view=photo&amp;id=${vehicle.id}&amp;kind=registration" alt="Foto del libretto"></a></div></c:if>
            </div>
        </section>
    </c:if>
    <section aria-labelledby="vehicle-events-title">
        <h2 class="h4 mb-3" id="vehicle-events-title">Scadenze e manutenzioni</h2>
        <c:if test="${empty events}"><div class="card-base text-muted">Nessuna voce registrata per questo veicolo.</div></c:if>
        <div class="d-grid gap-3">
            <c:forEach var="event" items="${events}">
                <article class="card-base">
                    <div class="d-flex flex-wrap justify-content-between align-items-start gap-2">
                        <div>
                            <span class="small text-muted">${event.type == 'DEADLINE' ? 'Scadenza' : 'Manutenzione'}</span>
                            <h3 class="h5 mb-1"><c:out value="${event.title}"/></h3>
                            <div class="small">Data: <c:out value="${event.dueDate}"/></div>
                            <c:if test="${not empty event.completedOn}"><div class="small">Completata il <c:out value="${event.completedOn}"/></div></c:if>
                        </div>
                        <span class="badge ${not empty event.completedOn ? 'text-bg-success' : event.overdue ? 'text-bg-danger' : event.dueSoon ? 'text-bg-warning' : 'text-bg-secondary'}">
                            ${not empty event.completedOn ? 'Completata' : event.overdue ? 'Scaduta' : event.dueSoon ? 'Imminente' : 'Da fare'}
                        </span>
                    </div>
                    <c:if test="${not empty event.notes}"><p class="mb-2 mt-3"><c:out value="${event.notes}"/></p></c:if>
                    <div class="d-flex flex-wrap gap-2 mt-3">
                        <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/vehicles?view=editEvent&amp;id=${vehicle.id}&amp;eventId=${event.id}">Modifica</a>
                        <form method="post" action="${pageContext.request.contextPath}/vehicles">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                            <input type="hidden" name="id" value="${vehicle.id}">
                            <input type="hidden" name="eventId" value="${event.id}">
                            <input type="hidden" name="action" value="${empty event.completedOn ? 'completeEvent' : 'reopenEvent'}">
                            <button class="btn btn-sm btn-outline-primary" type="submit">${empty event.completedOn ? 'Segna completata' : 'Riapri'}</button>
                        </form>
                        <form method="post" action="${pageContext.request.contextPath}/vehicles" onsubmit="return confirm('Eliminare questa voce?');">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                            <input type="hidden" name="action" value="deleteEvent">
                            <input type="hidden" name="id" value="${vehicle.id}">
                            <input type="hidden" name="eventId" value="${event.id}">
                            <button class="btn btn-sm btn-outline-danger" type="submit">Elimina</button>
                        </form>
                    </div>
                </article>
            </c:forEach>
        </div>
    </section>
    <form method="post" action="${pageContext.request.contextPath}/vehicles" class="mt-5" onsubmit="return confirm('Eliminare il veicolo e tutte le sue scadenze e manutenzioni?');">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
        <input type="hidden" name="action" value="deleteVehicle">
        <input type="hidden" name="id" value="${vehicle.id}">
        <button class="btn btn-outline-danger" type="submit">Elimina veicolo</button>
    </form>
</main>
</body>
</html>
