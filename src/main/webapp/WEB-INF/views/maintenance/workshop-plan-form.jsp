<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Nuovo piano di manutenzione • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <h1>Nuovo piano di manutenzione</h1>
        <p>Macchinario: <strong><c:out value="${selectedMachine.name}"/></strong></p>
    </div>
    <nav class="workshop-plan-toolbar" aria-label="Navigazione piano di manutenzione">
        <a class="detail-round-btn" href="${pageContext.request.contextPath}/workshop-maintenance?view=plans&amp;machineId=${selectedMachine.id}" aria-label="Torna ai piani del macchinario" title="Torna ai piani del macchinario">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14.5 5.5L8 12l6.5 6.5" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </a>
    </nav>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <section class="card-base workshop-form-panel">
        <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="createPlan">
            <input type="hidden" name="machineId" value="${selectedMachine.id}">
            <div class="col-12">
                <label class="form-label fw-semibold" for="plan-description">Intervento previsto</label>
                <input class="form-control" id="plan-description" name="description" maxlength="500" required value="<c:out value='${param.description}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="plan-date">Scadenza per data</label>
                <input class="form-control" type="date" id="plan-date" name="dueDate" value="<c:out value='${param.dueDate}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="plan-hours">Oppure per ore di utilizzo</label>
                <input class="form-control" type="number" min="0" id="plan-hours" name="dueHours" value="<c:out value='${param.dueHours}'/>">
                <div class="form-text">Ore attuali: <c:out value="${selectedMachine.usageHours}"/></div>
            </div>
            <div class="col-12 small text-muted">Indica una sola scadenza: per data oppure per ore.</div>
            <div class="col-12 d-flex flex-wrap gap-2">
                <button class="btn btn-primary" type="submit">Salva piano</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=plans&amp;machineId=${selectedMachine.id}">Annulla</a>
            </div>
        </form>
    </section>
</main>
<script>
    const planDate = document.getElementById('plan-date');
    const planHours = document.getElementById('plan-hours');
    planDate.addEventListener('change', function () { if (planDate.value) planHours.value = ''; });
    planHours.addEventListener('input', function () { if (planHours.value) planDate.value = ''; });
</script>
</body>
</html>
