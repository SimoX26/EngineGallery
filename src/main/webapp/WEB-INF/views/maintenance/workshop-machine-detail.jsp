<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title><c:out value="${machine.name}"/> • Manutenzione macchinari</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=38">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pt-4 pb-5 workshop-maintenance-page workshop-has-fab">
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Dati salvati.</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>

    <section class="card-base mb-4" aria-label="Anagrafica del macchinario">
        <div class="workshop-detail-toolbar mb-4" aria-label="Azioni macchinario">
            <a class="share-icon-btn workshop-detail-round-btn" href="${pageContext.request.contextPath}/workshop-maintenance" aria-label="Torna ai macchinari" title="Torna ai macchinari">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14.5 5.5L8 12l6.5 6.5"></path></svg>
            </a>
            <a class="share-icon-btn workshop-detail-round-btn" href="${pageContext.request.contextPath}/workshop-maintenance?view=edit&amp;id=${machine.id}" aria-label="Modifica macchinario" title="Modifica macchinario">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20l4.2-1 9.8-9.8-3.2-3.2L5 15.8 4 20z"></path><path d="M13.8 6.2l3.2 3.2"></path></svg>
            </a>
            <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" onsubmit="return confirm('Eliminare questo macchinario? L’operazione non può essere annullata.');">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                <input type="hidden" name="action" value="deleteMachine">
                <input type="hidden" name="machineId" value="${machine.id}">
                <button class="share-icon-btn workshop-detail-round-btn workshop-detail-round-btn--delete" type="submit" aria-label="Elimina macchinario" title="Elimina macchinario">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 7h14"></path><path d="M9 7V5h6v2"></path><path d="M8 7l1 12h6l1-12"></path></svg>
                </button>
            </form>
            <button class="share-icon-btn workshop-detail-round-btn workshop-detail-round-btn--share js-media-share-btn" type="button"
                    data-share-source="#workshop-machine-image" data-share-kind="images"
                    aria-label="Condividi foto del macchinario" title="${empty machine.imageFilename ? 'Nessuna foto da condividere' : 'Condividi foto del macchinario'}"
                    ${empty machine.imageFilename ? 'disabled' : ''}>
                <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"></path></svg>
            </button>
        </div>
        <div class="mb-4">
            <h1 class="mb-1"><c:out value="${machine.name}"/></h1>
            <p class="mb-0 text-muted">Anagrafica macchinario</p>
        </div>
        <div class="row g-4 align-items-start">
            <div class="col-md-5 col-lg-4" id="workshop-machine-image">
                <c:choose>
                    <c:when test="${not empty machine.imageFilename}">
                        <img class="workshop-detail-image" src="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${fn:escapeXml(machine.imageFilename)}" data-media-url="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${fn:escapeXml(machine.imageFilename)}" alt="Foto di ${fn:escapeXml(machine.name)}">
                    </c:when>
                    <c:otherwise><div class="workshop-detail-image workshop-machine-card__empty" aria-hidden="true">Nessuna foto</div></c:otherwise>
                </c:choose>
            </div>
            <div class="col-md-7 col-lg-8">
                <div class="d-flex align-items-center gap-2 flex-wrap mb-3">
                    <span class="record-card__eyebrow">Matricola <c:out value="${machine.serialNumber}"/></span>
                    <span class="badge ${machine.status == 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'}">${machine.status == 'ACTIVE' ? 'Attivo' : 'Fuori servizio'}</span>
                </div>
                <dl class="workshop-detail-facts mb-0">
                    <div><dt>Modello</dt><dd><c:out value="${machine.model}"/></dd></div>
                    <div><dt>Posizione</dt><dd><c:out value="${machine.location}"/></dd></div>
                    <div><dt>Data di acquisto</dt><dd><c:out value="${machine.purchaseDate}" default="Non indicata"/></dd></div>
                    <div><dt>Ore di utilizzo</dt><dd><c:out value="${machine.usageHours}"/></dd></div>
                </dl>
            </div>
        </div>
    </section>

    <section aria-labelledby="workshop-interventions-title">
        <div class="d-flex justify-content-between align-items-end gap-2 flex-wrap mb-3">
            <div>
                <h2 class="h4 mb-1" id="workshop-interventions-title">Interventi dell'ultimo anno</h2>
                <p class="text-muted small mb-0">Dal <c:out value="${since}"/> a oggi</p>
            </div>
        </div>
        <div class="workshop-intervention-list">
            <c:forEach var="item" items="${recentInterventions}">
                <article class="card-base workshop-intervention-entry">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                        <time class="fw-semibold" datetime="${item.performedOn}"><c:out value="${item.performedOn}"/></time>
                        <span class="badge ${item.type == 'ORDINARY' ? 'text-bg-secondary' : 'text-bg-warning'}">${item.type == 'ORDINARY' ? 'Ordinaria' : 'Straordinaria'}</span>
                    </div>
                    <p class="mb-2"><c:out value="${item.description}"/></p>
                    <c:if test="${not empty item.parts}"><p class="small mb-2"><strong>Note su ricambi/materiali:</strong> <c:out value="${item.parts}"/></p></c:if>
                    <c:if test="${not empty materialUses[item.id]}">
                        <div class="small mb-2"><strong>Materiali prelevati:</strong>
                            <ul class="mb-0"><c:forEach var="use" items="${materialUses[item.id]}">
                                <li><c:out value="${use.materialName}"/> — <c:out value="${use.quantity}"/> <c:out value="${use.unit}"/></li>
                            </c:forEach></ul>
                        </div>
                    </c:if>
                    <div class="small text-muted">Operatore: <c:out value="${item.operator}"/></div>
                </article>
            </c:forEach>
            <c:if test="${empty recentInterventions}"><div class="system-empty-state" role="status">Nessun intervento registrato nell'ultimo anno.</div></c:if>
        </div>
    </section>

    <a class="workshop-intervention-fab" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention&amp;machineId=${machine.id}">
        <span aria-hidden="true">+</span> Aggiungi intervento
    </a>
</main>
<script src="${pageContext.request.contextPath}/assets/js/media-share.js?v=2"></script>
</body>
</html>
