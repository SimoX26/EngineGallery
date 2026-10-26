<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Manutenzione macchinari • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=33">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page workshop-has-fab">
    <header class="page-header-with-search workshop-maintenance-header">
        <div class="page-header">
            <h1>Manutenzione macchinari</h1>
            <p>Attrezzature interne dell'officina</p>
        </div>
        <div class="workshop-maintenance-header__actions">
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=materials">Magazzino materiali</a>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=calendar">Calendario manutenzioni</a>
            <a class="btn btn-add-plus" href="${pageContext.request.contextPath}/workshop-maintenance?view=new">Aggiungi macchinario +</a>
        </div>
    </header>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.deleted == '1'}"><div class="alert alert-success" role="status">Macchinario eliminato.</div></c:if>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-3"><div class="card-base h-100"><div class="kpi-title">Macchinari</div><div class="kpi-value"><c:out value="${machineCount}" default="—"/></div></div></div>
        <div class="col-6 col-lg-3"><div class="card-base h-100"><div class="kpi-title">Fuori servizio</div><div class="kpi-value"><c:out value="${outOfServiceCount}" default="—"/></div></div></div>
        <div class="col-6 col-lg-3"><div class="card-base h-100"><div class="kpi-title">Scadute</div><div class="kpi-value text-danger"><c:out value="${overdueCount}" default="—"/></div></div></div>
        <div class="col-6 col-lg-3"><div class="card-base h-100"><div class="kpi-title">Imminenti</div><div class="kpi-value text-warning"><c:out value="${dueSoonCount}" default="—"/></div></div></div>
    </div>

    <section aria-labelledby="workshop-machines-title">
        <div class="mb-3">
            <h2 class="h4 mb-0" id="workshop-machines-title">Macchinari</h2>
        </div>
        <div class="record-gallery-grid">
            <c:forEach var="machine" items="${machines}">
                <a class="record-gallery-card workshop-machine-card"
                   href="${pageContext.request.contextPath}/workshop-maintenance?view=detail&amp;id=${machine.id}">
                    <c:choose>
                        <c:when test="${not empty machine.imageFilename}">
                            <img class="workshop-machine-card__image"
                                 src="${pageContext.request.contextPath}/uploads/workshop-machines/${machine.id}/${fn:escapeXml(machine.imageFilename)}"
                                 alt="Foto di ${fn:escapeXml(machine.name)}" loading="lazy">
                        </c:when>
                        <c:otherwise><div class="workshop-machine-card__image workshop-machine-card__empty" aria-hidden="true">Nessuna foto</div></c:otherwise>
                    </c:choose>
                    <div class="record-card__body">
                        <span class="record-card__eyebrow">Matricola <c:out value="${machine.serialNumber}"/></span>
                        <span class="record-card__title"><c:out value="${machine.name}"/></span>
                        <span class="record-card__subtitle"><c:out value="${machine.model}"/></span>
                        <span class="record-card__facts">
                            <span class="record-card__fact"><strong>Posizione:</strong> <c:out value="${machine.location}"/></span>
                            <span class="record-card__fact"><strong>Acquistato:</strong> <c:out value="${machine.purchaseDate}" default="Non indicata"/></span>
                        </span>
                    </div>
                    <div class="record-card__footer">
                        <span class="small text-muted">Ore di utilizzo: <strong><c:out value="${machine.usageHours}"/></strong></span>
                        <span class="badge badge-status ${machine.status == 'ACTIVE' ? 'text-bg-success' : 'text-bg-secondary'}">
                            ${machine.status == 'ACTIVE' ? 'Attivo' : 'Fuori servizio'}
                        </span>
                    </div>
                </a>
            </c:forEach>
            <c:if test="${empty machines && empty error}">
                <div class="card-base text-muted">Nessun macchinario registrato. Usa “Aggiungi macchinario” per iniziare.</div>
            </c:if>
        </div>
    </section>

    <a class="workshop-intervention-fab" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention">
        <span aria-hidden="true">+</span> Aggiungi intervento
    </a>
</main>
</body>
</html>
