<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Storico manutenzioni • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=38">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <header class="page-header-with-search workshop-maintenance-header">
        <div class="page-header">
            <a class="text-decoration-none" href="${pageContext.request.contextPath}/workshop-maintenance">← Manutenzione macchinari</a>
            <h1 class="mt-2">Storico manutenzioni</h1>
        </div>
        <div class="workshop-maintenance-header__actions">
            <a class="btn btn-add-plus" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention">Aggiungi intervento +</a>
        </div>
    </header>

    <section aria-labelledby="history-title">
        <h2 class="h4 mb-3" id="history-title">Interventi effettuati</h2>
        <div class="workshop-intervention-list">
            <c:forEach var="item" items="${interventions}">
                <article class="card-base workshop-intervention-entry">
                    <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                        <div>
                            <time class="fw-semibold" datetime="${item.performedOn}"><c:out value="${item.performedOn}"/></time>
                            <span class="text-muted mx-1" aria-hidden="true">·</span>
                            <a href="${pageContext.request.contextPath}/workshop-maintenance?view=detail&amp;id=${item.machineId}"><c:out value="${item.machineName}"/></a>
                        </div>
                        <span class="badge ${item.type == 'ORDINARY' ? 'text-bg-secondary' : 'text-bg-warning'}">${item.type == 'ORDINARY' ? 'Ordinaria' : 'Straordinaria'}</span>
                    </div>
                    <p class="mb-2"><c:out value="${item.description}"/></p>
                    <c:if test="${not empty item.parts}"><p class="small mb-2"><strong>Ricambi e materiali:</strong> <c:out value="${item.parts}"/></p></c:if>
                    <div class="small text-muted">Operatore: <c:out value="${item.operator}"/></div>
                </article>
            </c:forEach>
            <c:if test="${empty interventions}"><div class="system-empty-state" role="status">Nessun intervento registrato.</div></c:if>
        </div>
    </section>
</main>
</body>
</html>
