<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Calendario manutenzioni • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=39">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>
<main class="container pb-5 workshop-maintenance-page workshop-calendar-page">
    <header class="page-header-with-search workshop-maintenance-header">
        <div class="page-header">
            <a class="share-icon-btn workshop-detail-round-btn mb-3" href="${pageContext.request.contextPath}/workshop-maintenance" aria-label="Torna ai macchinari" title="Torna ai macchinari">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14.5 5.5L8 12l6.5 6.5"></path></svg>
            </a>
            <h1>Calendario manutenzioni</h1>
        </div>
    </header>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Manutenzione programmata.</div></c:if>

    <section class="card-base workshop-calendar mb-4" aria-labelledby="calendar-month-title">
        <div class="workshop-calendar__toolbar">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=calendar&amp;month=${previousMonth}" aria-label="Mese precedente">‹</a>
            <h2 class="h4 mb-0" id="calendar-month-title"><c:out value="${calendarMonthTitle}"/></h2>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=calendar&amp;month=${nextMonth}" aria-label="Mese successivo">›</a>
        </div>
        <div class="workshop-calendar__grid" aria-label="Giorni del mese">
            <span class="workshop-calendar__weekday">Lun</span>
            <span class="workshop-calendar__weekday">Mar</span>
            <span class="workshop-calendar__weekday">Mer</span>
            <span class="workshop-calendar__weekday">Gio</span>
            <span class="workshop-calendar__weekday">Ven</span>
            <span class="workshop-calendar__weekday">Sab</span>
            <span class="workshop-calendar__weekday">Dom</span>
            <c:forEach var="day" items="${calendarDays}">
                <a class="workshop-calendar__day${day.inMonth ? '' : ' is-outside'}${day.selected ? ' is-selected' : ''}${day.today ? ' is-today' : ''}${day.count > 0 ? ' has-events' : ''}${day.overdue ? ' is-overdue' : ''}"
                   href="${pageContext.request.contextPath}/workshop-maintenance?view=calendar&amp;month=${day.month}&amp;day=${day.date}"
                   aria-label="${day.date}: ${day.count} manutenzioni"
                   aria-current="${day.selected ? 'date' : 'false'}">
                    <c:out value="${day.number}"/>
                </a>
            </c:forEach>
        </div>
    </section>

    <section class="workshop-calendar-section mb-4" aria-labelledby="calendar-agenda-title">
        <h2 class="h4 mb-3" id="calendar-agenda-title">Agenda del <time datetime="${selectedDate}"><c:out value="${selectedDayTitle}"/></time></h2>
        <div class="workshop-intervention-list">
            <c:forEach var="plan" items="${selectedPlans}">
                <article class="card-base workshop-intervention-entry">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                        <a class="fw-semibold" href="${pageContext.request.contextPath}/workshop-maintenance?view=detail&amp;id=${plan.machineId}"><c:out value="${plan.machineName}"/></a>
                        <span class="badge ${plan.completed ? 'text-bg-success' : plan.overdue ? 'text-bg-danger' : 'text-bg-secondary'}">${plan.completed ? 'Completata' : plan.overdue ? 'Scaduta' : 'Programmata'}</span>
                    </div>
                    <p class="mb-2"><c:out value="${plan.description}"/></p>
                    <c:if test="${not plan.completed}">
                        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention&amp;machineId=${plan.machineId}&amp;planId=${plan.id}">Registra intervento</a>
                    </c:if>
                </article>
            </c:forEach>
            <c:forEach var="item" items="${selectedInterventions}">
                <article class="card-base workshop-intervention-entry">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                        <a class="fw-semibold" href="${pageContext.request.contextPath}/workshop-maintenance?view=detail&amp;id=${item.machineId}"><c:out value="${item.machineName}"/></a>
                        <span class="badge text-bg-success">Effettuata · ${item.type == 'ORDINARY' ? 'Ordinaria' : 'Straordinaria'}</span>
                    </div>
                    <p class="mb-2"><c:out value="${item.description}"/></p>
                    <div class="small text-muted">Operatore: <c:out value="${item.operator}"/></div>
                </article>
            </c:forEach>
            <c:if test="${empty selectedPlans && empty selectedInterventions}"><div class="system-empty-state" role="status">Nessuna manutenzione per questo giorno.</div></c:if>
        </div>
    </section>

    <c:if test="${not empty hourPlans}">
        <section class="workshop-calendar-section mb-4" aria-labelledby="hour-plans-title">
            <h2 class="h4 mb-3" id="hour-plans-title">Manutenzioni a ore</h2>
            <div class="workshop-intervention-list">
                <c:forEach var="plan" items="${hourPlans}">
                    <article class="card-base workshop-intervention-entry">
                        <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                            <a class="fw-semibold" href="${pageContext.request.contextPath}/workshop-maintenance?view=detail&amp;id=${plan.machineId}"><c:out value="${plan.machineName}"/></a>
                            <span class="badge ${plan.overdue ? 'text-bg-danger' : 'text-bg-secondary'}">${plan.overdue ? 'Scaduta' : 'Programmata'}</span>
                        </div>
                        <p class="mb-1"><c:out value="${plan.description}"/></p>
                        <p class="small text-muted mb-2">Scadenza: <c:out value="${plan.dueHours}"/> ore · Utilizzo attuale: <c:out value="${plan.machineHours}"/> ore</p>
                        <a class="btn btn-outline-secondary btn-sm" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention&amp;machineId=${plan.machineId}&amp;planId=${plan.id}">Registra intervento</a>
                    </article>
                </c:forEach>
            </div>
        </section>
    </c:if>

    <button class="workshop-intervention-fab workshop-calendar-fab" type="button" data-selected-date="${selectedDate}"
            data-bs-toggle="modal" data-bs-target="#programPlanModal" aria-controls="programPlanModal">
        <span aria-hidden="true">+</span> Programma manutenzione
    </button>
</main>
<div class="modal fade workshop-calendar-modal" id="programPlanModal" tabindex="-1" aria-labelledby="program-plan-title" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title h5" id="program-plan-title">Programma manutenzione</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
            </div>
            <div class="modal-body">
                <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3"
                      data-draft-key="workshop-plan-new"
                      data-draft-user="${sessionScope.loggedUser.id}"
                      data-draft-success="${pageContext.request.contextPath}/workshop-maintenance"
                      data-draft-success-param="saved">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                    <input type="hidden" name="submissionToken" value="<%= java.util.UUID.randomUUID() %>">
                    <input type="hidden" name="action" value="createPlan">
                    <input type="hidden" name="month" value="${calendarMonth}">
                    <input type="hidden" name="day" value="${selectedDate}">
                    <div class="col-12">
                        <label class="form-label fw-semibold" for="plan-machine">Macchinario</label>
                        <select class="form-select" id="plan-machine" name="machineId" required>
                            <option value="">Seleziona un macchinario</option>
                            <c:forEach var="machine" items="${machines}">
                                <option value="${machine.id}" ${param.machineId == machine.id ? 'selected' : ''}><c:out value="${machine.name}"/> — <c:out value="${machine.serialNumber}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold" for="plan-date">Data prevista</label>
                        <input class="form-control" type="date" id="plan-date" name="dueDate" required value="<c:out value='${not empty error ? param.dueDate : selectedDate}'/>">
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold" for="plan-description">Descrizione</label>
                        <textarea class="form-control" id="plan-description" name="description" maxlength="500" rows="3" required><c:out value="${param.description}"/></textarea>
                    </div>
                    <div class="col-12 d-flex flex-wrap gap-2">
                        <button class="btn btn-primary" type="submit" ${empty machines ? 'disabled' : ''}>Salva manutenzione</button>
                        <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">Annulla</button>
                    </div>
                    <c:if test="${empty machines}"><p class="small text-muted mb-0">Aggiungi prima un macchinario per programmare una manutenzione.</p></c:if>
                </form>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<c:if test="${not empty error}">
    <script>bootstrap.Modal.getOrCreateInstance(document.getElementById('programPlanModal')).show();</script>
</c:if>
<script src="${pageContext.request.contextPath}/assets/js/create-form-draft.js?v=7"></script>
<script>
    document.querySelector('.workshop-calendar-fab').addEventListener('click', (event) => {
        const dateInput = document.getElementById('plan-date');
        dateInput.value = event.currentTarget.dataset.selectedDate;
        dateInput.dispatchEvent(new Event('input', { bubbles: true }));
    });
</script>
</body>
</html>
