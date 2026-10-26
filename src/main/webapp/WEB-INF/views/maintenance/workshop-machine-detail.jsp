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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=26">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page workshop-has-fab">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/workshop-maintenance">← Tutti i macchinari</a>
        <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap mt-3">
            <div><h1 class="mb-1"><c:out value="${machine.name}"/></h1><p class="mb-0">Anagrafica macchinario</p></div>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=edit&amp;id=${machine.id}">Modifica macchinario</a>
        </div>
    </div>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Dati salvati.</div></c:if>

    <section class="card-base mb-4" aria-label="Anagrafica del macchinario">
        <div class="row g-4 align-items-start">
            <div class="col-md-5 col-lg-4">
                <c:choose>
                    <c:when test="${not empty machine.imageFilename}">
                        <img class="workshop-detail-image" src="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${fn:escapeXml(machine.imageFilename)}" alt="Foto di ${fn:escapeXml(machine.name)}">
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
            <c:if test="${empty recentInterventions}"><div class="card-base text-muted">Nessun intervento registrato nell'ultimo anno.</div></c:if>
        </div>
    </section>

    <a class="workshop-intervention-fab" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention&amp;machineId=${machine.id}">
        <span aria-hidden="true">+</span> Aggiungi intervento
    </a>
</main>
</body>
</html>
