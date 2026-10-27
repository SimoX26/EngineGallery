<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Veicoli • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=34">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page workshop-has-fab">
    <header class="page-header-with-search workshop-maintenance-header">
        <div class="page-header"><h1>Veicoli</h1></div>
    </header>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.deleted == '1'}"><div class="alert alert-success" role="status">Veicolo eliminato.</div></c:if>
    <section class="mb-4" aria-labelledby="vehicle-reminders-title">
        <h2 class="h4 mb-3" id="vehicle-reminders-title">Da fare</h2>
        <c:if test="${empty reminders}"><div class="card-base text-muted">Nessuna scadenza o manutenzione aperta.</div></c:if>
        <c:if test="${not empty reminders}">
            <div class="card-base">
                <div class="d-grid gap-3">
                    <c:forEach var="reminder" items="${reminders}">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 border-bottom pb-2">
                            <div>
                                <a href="${pageContext.request.contextPath}/vehicles?view=detail&amp;id=${reminder.event.vehicleId}"><c:out value="${reminder.plate}"/></a>
                                · <c:out value="${reminder.event.title}"/>
                                <span class="small text-muted">(${reminder.event.type == 'DEADLINE' ? 'Scadenza' : 'Manutenzione'})</span>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <time datetime="${reminder.event.dueDate}"><c:out value="${reminder.event.dueDate}"/></time>
                                <span class="badge ${reminder.event.overdue ? 'text-bg-danger' : reminder.event.dueSoon ? 'text-bg-warning' : 'text-bg-secondary'}">
                                    ${reminder.event.overdue ? 'Scaduta' : reminder.event.dueSoon ? 'Imminente' : 'Da fare'}
                                </span>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>
    </section>
    <div class="record-gallery-grid">
        <c:forEach var="vehicle" items="${vehicles}">
            <a class="record-gallery-card" href="${pageContext.request.contextPath}/vehicles?view=detail&amp;id=${vehicle.id}">
                <div class="record-card__body">
                    <span class="record-card__eyebrow">Targa <c:out value="${vehicle.plate}"/></span>
                    <span class="record-card__title"><c:out value="${vehicle.make}"/> <c:out value="${vehicle.model}"/></span>
                </div>
            </a>
        </c:forEach>
        <c:if test="${empty vehicles}"><div class="card-base text-muted">Nessun veicolo registrato. Aggiungi il primo veicolo per iniziare.</div></c:if>
    </div>
    <a class="section-add-fab" href="${pageContext.request.contextPath}/vehicles?view=new">
        <span aria-hidden="true">+</span> Aggiungi veicolo
    </a>
</main>
</body>
</html>
