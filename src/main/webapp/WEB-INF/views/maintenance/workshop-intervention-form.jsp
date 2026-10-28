<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Nuovo intervento • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=39">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/workshop-maintenance">← Manutenzione macchinari</a>
        <h1 class="mt-3">Nuovo intervento</h1>
        <p>Registra un intervento di manutenzione effettuato.</p>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${empty machines}"><div class="alert alert-info">Aggiungi prima un macchinario. <a href="${pageContext.request.contextPath}/workshop-maintenance?view=new">Aggiungi macchinario</a></div></c:if>
    <section class="card-base workshop-form-panel">
        <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3"
              data-draft-key="workshop-intervention-new"
              data-draft-user="${sessionScope.loggedUser.id}"
              data-draft-success="${pageContext.request.contextPath}/workshop-maintenance"
              data-draft-success-param="saved">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="recordIntervention">
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="intervention-machine">Macchinario</label>
                <select class="form-select" id="intervention-machine" name="machineId" required>
                    <option value="">Seleziona un macchinario</option>
                    <c:set var="chosenMachineId" value="${not empty error ? param.machineId : selectedMachineId}"/>
                    <c:forEach var="machine" items="${machines}">
                        <option value="${machine.id}" ${chosenMachineId == machine.id ? 'selected' : ''}><c:out value="${machine.name}"/> — <c:out value="${machine.serialNumber}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="intervention-type">Tipo</label>
                <c:set var="chosenType" value="${not empty error ? param.type : 'ORDINARY'}"/>
                <select class="form-select" id="intervention-type" name="type" required>
                    <option value="ORDINARY" ${chosenType == 'ORDINARY' ? 'selected' : ''}>Ordinaria</option>
                    <option value="EXTRAORDINARY" ${chosenType == 'EXTRAORDINARY' ? 'selected' : ''}>Straordinaria</option>
                </select>
            </div>
            <div class="col-12" data-draft-dynamic>
                <label class="form-label fw-semibold" for="intervention-plan">Manutenzione programmata (facoltativa)</label>
                <select class="form-select" id="intervention-plan" name="planId">
                    <option value="">Nessuna manutenzione programmata</option>
                    <c:forEach var="plan" items="${plans}">
                        <option value="${plan.id}" data-machine-id="${plan.machineId}" ${param.planId == plan.id ? 'selected' : ''}>
                            <c:out value="${plan.machineName}"/> — <c:out value="${plan.description}"/>
                            (<c:out value="${not empty plan.dueDate ? plan.dueDate : plan.dueHours}"/>${empty plan.dueDate ? ' ore' : ''})
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="intervention-date">Data dell'intervento</label>
                <input class="form-control" type="date" id="intervention-date" name="performedOn" max="${today}" required value="<c:out value='${not empty error ? param.performedOn : today}'/>">
            </div>
            <div class="col-12">
                <label class="form-label fw-semibold" for="intervention-description">Descrizione</label>
                <textarea class="form-control" id="intervention-description" name="description" rows="4" maxlength="5000" required><c:out value="${param.description}"/></textarea>
            </div>
            <div class="col-12">
                <label class="form-label fw-semibold" for="intervention-parts">Note su ricambi o materiali non a magazzino</label>
                <textarea class="form-control" id="intervention-parts" name="parts" rows="3" maxlength="5000"><c:out value="${param.parts}"/></textarea>
            </div>
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap mb-2">
                    <label class="form-label fw-semibold mb-0">Materiali prelevati dal magazzino</label>
                    <a class="small" href="${pageContext.request.contextPath}/workshop-maintenance?view=materials">Gestisci magazzino</a>
                </div>
                <p class="small text-muted mb-2">Seleziona uno o più materiali e indica la quantità usata. La giacenza viene scalata al salvataggio.</p>
                <input type="text" name="materialUsageDraft" id="material-usage-draft" hidden
                       value="${fn:escapeXml(param.materialUsageDraft)}">
                <div id="material-usage-rows" data-draft-dynamic></div>
                <button id="add-material-row" class="btn btn-outline-secondary btn-sm mt-2" type="button"
                        ${empty materials ? 'disabled' : ''}>Aggiungi materiale</button>
                <c:if test="${empty materials}"><div class="small text-muted mt-2">Nessun materiale in magazzino. Puoi registrare l'intervento senza prelievi.</div></c:if>
            </div>
            <div class="col-12 small text-muted">Operatore: <c:out value="${sessionScope.loggedUser.username}"/></div>
            <div class="col-12 d-flex flex-wrap gap-2 align-items-center">
                <button class="btn btn-primary" type="submit" ${empty machines ? 'disabled' : ''}>Registra intervento</button>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance">Annulla</a>
            </div>
        </form>
        <template id="material-usage-template">
            <div class="row g-2 align-items-end mb-2" data-material-row>
                <div class="col-md-7">
                    <label class="form-label small">Materiale</label>
                    <select name="materialId" class="form-select" required>
                        <option value="">Seleziona materiale</option>
                        <c:forEach var="material" items="${materials}">
                            <option value="${material.id}" data-unit="${material.unit}" data-available="${material.quantity}">
                                <c:out value="${material.name}"/> · <c:out value="${material.categoryLabel}"/>
                                (disponibili: <c:out value="${material.quantity}"/> <c:out value="${material.unit}"/>)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-8 col-md-3">
                    <label class="form-label small">Quantità usata</label>
                    <input name="materialQuantity" type="number" min="0.001" step="0.001" class="form-control" required>
                </div>
                <div class="col-4 col-md-2"><button type="button" class="btn btn-outline-danger w-100" data-remove-material>Rimuovi</button></div>
            </div>
        </template>
    </section>
</main>
<script>
    (() => {
        const machineSelect = document.getElementById('intervention-machine');
        const planSelect = document.getElementById('intervention-plan');
        const syncPlans = () => {
            const machineId = machineSelect.value;
            planSelect.disabled = !machineId;
            planSelect.querySelectorAll('option[data-machine-id]').forEach((option) => {
                option.disabled = option.dataset.machineId !== machineId;
                option.hidden = option.disabled;
            });
            if (planSelect.selectedOptions[0]?.disabled) planSelect.value = '';
        };
        machineSelect.addEventListener('change', syncPlans);
        syncPlans();

        const form = document.querySelector('form[data-draft-key="workshop-intervention-new"]');
        const rows = document.getElementById('material-usage-rows');
        const draft = document.getElementById('material-usage-draft');
        const template = document.getElementById('material-usage-template');
        const addButton = document.getElementById('add-material-row');

        function selections() {
            return Array.from(rows.querySelectorAll('[data-material-row]')).map((row) => ({
                id: row.querySelector('[name="materialId"]').value,
                quantity: row.querySelector('[name="materialQuantity"]').value
            }));
        }

        function saveSelection() {
            draft.value = JSON.stringify(selections());
            draft.dispatchEvent(new Event('input', {bubbles: true}));
            addButton.disabled = rows.children.length >= 30;
        }

        function addRow(selection = {}) {
            if (rows.children.length >= 30) return;
            const row = template.content.firstElementChild.cloneNode(true);
            const select = row.querySelector('[name="materialId"]');
            const quantity = row.querySelector('[name="materialQuantity"]');
            select.value = selection.id || '';
            quantity.value = selection.quantity || '';
            const updateUnit = () => {
                const option = select.selectedOptions[0];
                const pieces = option && option.dataset.unit === 'PZ';
                quantity.step = pieces ? '1' : '0.001';
                quantity.min = pieces ? '1' : '0.001';
                quantity.max = option && option.dataset.available ? option.dataset.available : '';
            };
            updateUnit();
            select.addEventListener('change', () => { updateUnit(); saveSelection(); });
            quantity.addEventListener('input', saveSelection);
            row.querySelector('[data-remove-material]').addEventListener('click', () => {
                row.remove();
                saveSelection();
            });
            rows.appendChild(row);
            saveSelection();
        }

        function restoreRows() {
            let values = [];
            try { values = JSON.parse(draft.value || '[]'); } catch (ignored) { }
            rows.replaceChildren();
            if (Array.isArray(values)) values.slice(0, 30).forEach(addRow);
            addButton.disabled = rows.children.length >= 30 || !template.content.querySelector('option[value]:not([value=""])');
        }

        addButton.addEventListener('click', () => addRow());
        form.addEventListener('draftrestored', restoreRows);
        restoreRows();
    })();
</script>
<script src="${pageContext.request.contextPath}/assets/js/create-form-draft.js?v=5"></script>
</body>
</html>
