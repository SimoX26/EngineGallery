<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Piani di manutenzione • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <h1>Piani di manutenzione</h1>
        <p>Seleziona un macchinario per consultare le sue scadenze.</p>
    </div>

    <nav class="workshop-plan-toolbar" aria-label="Navigazione piani di manutenzione">
        <a class="detail-round-btn" href="${pageContext.request.contextPath}/workshop-maintenance" aria-label="Torna ai macchinari" title="Torna ai macchinari">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14.5 5.5L8 12l6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </a>
        <c:if test="${not empty selectedMachine}">
            <c:choose>
                <c:when test="${not empty previousMachineId}">
                    <a class="detail-round-btn" href="${pageContext.request.contextPath}/workshop-maintenance?view=plans&amp;machineId=${previousMachineId}" aria-label="Macchinario precedente" title="Macchinario precedente">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14.5 5.5L8 12l6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </a>
                </c:when>
                <c:otherwise><span class="detail-round-btn workshop-plan-nav-disabled" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M14.5 5.5L8 12l6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg></span></c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${not empty nextMachineId}">
                    <a class="detail-round-btn" href="${pageContext.request.contextPath}/workshop-maintenance?view=plans&amp;machineId=${nextMachineId}" aria-label="Macchinario successivo" title="Macchinario successivo">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9.5 5.5L16 12l-6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    </a>
                </c:when>
                <c:otherwise><span class="detail-round-btn workshop-plan-nav-disabled" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M9.5 5.5L16 12l-6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg></span></c:otherwise>
            </c:choose>
            <span class="workshop-plan-toolbar__name"><c:out value="${selectedMachine.name}"/></span>
            <a class="detail-round-btn workshop-plan-toolbar__add" href="${pageContext.request.contextPath}/workshop-maintenance?view=new-plan&amp;machineId=${selectedMachine.id}" aria-label="Aggiungi piano di manutenzione" title="Aggiungi piano di manutenzione">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round"/></svg>
            </a>
        </c:if>
    </nav>

    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Piano salvato.</div></c:if>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-4"><div class="card-base h-100"><div class="kpi-title">Piani</div><div class="kpi-value"><c:out value="${planCount}"/></div></div></div>
        <div class="col-6 col-lg-4"><div class="card-base h-100"><div class="kpi-title">Scaduti</div><div class="kpi-value text-danger"><c:out value="${overdueCount}"/></div></div></div>
        <div class="col-6 col-lg-4"><div class="card-base h-100"><div class="kpi-title">Imminenti</div><div class="kpi-value text-warning"><c:out value="${dueSoonCount}"/></div></div></div>
    </div>

    <section aria-labelledby="workshop-plan-machines-title">
        <h2 class="h4 mb-3" id="workshop-plan-machines-title">Macchinari</h2>
        <div class="record-gallery-grid workshop-plan-grid">
            <c:forEach var="machine" items="${machines}">
                <a class="record-gallery-card workshop-plan-machine-card ${not empty selectedMachine && selectedMachine.id == machine.id ? 'workshop-plan-machine-card--selected' : ''}"
                   href="${pageContext.request.contextPath}/workshop-maintenance?view=plans&amp;machineId=${machine.id}"
                   aria-current="${not empty selectedMachine && selectedMachine.id == machine.id ? 'page' : 'false'}">
                    <c:choose>
                        <c:when test="${not empty machine.imageFilename}"><img class="workshop-plan-machine-card__image" src="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${fn:escapeXml(machine.imageFilename)}" alt="Foto di ${fn:escapeXml(machine.name)}" loading="lazy"></c:when>
                        <c:otherwise><div class="workshop-plan-machine-card__image workshop-machine-card__empty" aria-hidden="true">Nessuna foto</div></c:otherwise>
                    </c:choose>
                    <div class="record-card__body">
                        <span class="record-card__eyebrow">Matricola <c:out value="${machine.serialNumber}"/></span>
                        <span class="record-card__title"><c:out value="${machine.name}"/></span>
                        <span class="record-card__subtitle"><c:out value="${machine.model}"/></span>
                        <span class="record-card__facts">
                            <span class="record-card__fact"><strong>Posizione:</strong> <c:out value="${machine.location}"/></span>
                            <span class="record-card__fact"><strong>Acquistato:</strong> <c:out value="${machine.purchaseDate}" default="Non indicata"/></span>
                            <span class="record-card__fact"><strong>Ore:</strong> <c:out value="${machine.usageHours}"/></span>
                        </span>
                        <span class="badge ${machine.status == 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'} workshop-plan-machine-card__status">${machine.status == 'ACTIVE' ? 'Attivo' : 'Fuori servizio'}</span>
                    </div>
                </a>
            </c:forEach>
            <c:if test="${empty machines}"><div class="card-base text-muted">Nessun macchinario disponibile. Aggiungine uno dalla sezione manutenzione.</div></c:if>
        </div>
    </section>

    <c:if test="${not empty selectedMachine}">
        <section class="mt-4" aria-labelledby="workshop-selected-plans-title">
            <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap mb-3">
                <div><h2 class="h4 mb-1" id="workshop-selected-plans-title">Piani di <c:out value="${selectedMachine.name}"/></h2><p class="text-muted mb-0">Scadenze per data oppure per ore di utilizzo.</p></div>
                <a class="btn btn-add-plus" href="${pageContext.request.contextPath}/workshop-maintenance?view=new-plan&amp;machineId=${selectedMachine.id}">Nuovo piano +</a>
            </div>
            <div class="workshop-plan-list">
                <c:forEach var="plan" items="${selectedPlans}">
                    <article class="card-base workshop-plan-entry">
                        <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                            <h3 class="h6 mb-0"><c:out value="${plan.description}"/></h3>
                            <c:choose>
                                <c:when test="${plan.completed}"><span class="badge text-bg-success">Eseguito</span></c:when>
                                <c:when test="${plan.overdue}"><span class="badge text-bg-danger">Scaduto</span></c:when>
                                <c:when test="${plan.dueSoon}"><span class="badge text-bg-warning">Imminente</span></c:when>
                                <c:otherwise><span class="badge text-bg-secondary">Programmato</span></c:otherwise>
                            </c:choose>
                        </div>
                        <p class="small text-muted mb-0">
                            <c:choose>
                                <c:when test="${not empty plan.dueDate}">Entro il <c:out value="${plan.dueDate}"/></c:when>
                                <c:otherwise>Entro <c:out value="${plan.dueHours}"/> ore · attuali <c:out value="${plan.machineHours}"/> ore</c:otherwise>
                            </c:choose>
                        </p>
                    </article>
                </c:forEach>
                <c:if test="${empty selectedPlans}"><div class="card-base text-muted">Nessun piano registrato per questo macchinario.</div></c:if>
            </div>
        </section>
    </c:if>
</main>
</body>
</html>
