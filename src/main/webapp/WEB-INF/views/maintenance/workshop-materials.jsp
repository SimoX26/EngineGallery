<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Magazzino materiali manutenzione • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=39">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>
<main class="container pb-5 workshop-maintenance-page">
    <div class="page-header">
        <a class="text-decoration-none" href="${pageContext.request.contextPath}/workshop-maintenance">← Manutenzione macchinari</a>
        <h1 class="mt-3">Magazzino materiali</h1>
        <p>Filtri, detergenti, liquidi e ricambi per la manutenzione dei macchinari.</p>
    </div>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${param.saved == '1'}"><div class="alert alert-success" role="status">Giacenza aggiornata.</div></c:if>

    <section class="card-base mb-4" aria-labelledby="new-material-title">
        <h2 class="h5 mb-3" id="new-material-title">Aggiungi materiale</h2>
        <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-3"
              data-draft-key="workshop-material-new"
              data-draft-user="${sessionScope.loggedUser.id}"
              data-draft-success="${pageContext.request.contextPath}/workshop-maintenance"
              data-draft-success-param="saved">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="createMaterial">
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="material-name">Nome</label>
                <input id="material-name" name="name" class="form-control" maxlength="150" required
                       value="<c:out value='${param.action == "createMaterial" ? param.name : ""}'/>">
            </div>
            <div class="col-md-3">
                <label class="form-label fw-semibold" for="material-category">Categoria</label>
                <select id="material-category" name="category" class="form-select" required>
                    <option value="">Seleziona</option>
                    <option value="FILTRO" ${param.category == 'FILTRO' ? 'selected' : ''}>Filtri</option>
                    <option value="DETERGENTE" ${param.category == 'DETERGENTE' ? 'selected' : ''}>Detergenti</option>
                    <option value="LUBRIFICANTE" ${param.category == 'LUBRIFICANTE' ? 'selected' : ''}>Lubrificanti</option>
                    <option value="LIQUIDO" ${param.category == 'LIQUIDO' ? 'selected' : ''}>Liquidi</option>
                    <option value="RICAMBIO" ${param.category == 'RICAMBIO' ? 'selected' : ''}>Ricambi</option>
                    <option value="ALTRO" ${param.category == 'ALTRO' ? 'selected' : ''}>Altro</option>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label fw-semibold" for="material-unit">Unità</label>
                <select id="material-unit" name="unit" class="form-select" required>
                    <option value="">Seleziona</option>
                    <option value="PZ" ${param.unit == 'PZ' ? 'selected' : ''}>Pezzi</option>
                    <option value="L" ${param.unit == 'L' ? 'selected' : ''}>Litri</option>
                    <option value="KG" ${param.unit == 'KG' ? 'selected' : ''}>Chilogrammi</option>
                    <option value="ML" ${param.unit == 'ML' ? 'selected' : ''}>Millilitri</option>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label fw-semibold" for="material-quantity">Giacenza iniziale</label>
                <input id="material-quantity" name="quantity" type="number" min="0" step="0.001" class="form-control" required
                       value="<c:out value='${param.action == "createMaterial" ? param.quantity : "0"}'/>">
            </div>
            <div class="col-md-3">
                <label class="form-label fw-semibold" for="material-minimum">Scorta minima</label>
                <input id="material-minimum" name="minimumQuantity" type="number" min="0" step="0.001" class="form-control" required
                       value="<c:out value='${param.action == "createMaterial" ? param.minimumQuantity : "0"}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label fw-semibold" for="material-location">Ubicazione</label>
                <input id="material-location" name="location" class="form-control" maxlength="120"
                       value="<c:out value='${param.action == "createMaterial" ? param.location : ""}'/>">
            </div>
            <div class="col-12"><button class="btn btn-primary" type="submit">Aggiungi al magazzino</button></div>
        </form>
    </section>

    <section aria-labelledby="stock-title">
        <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap mb-3">
            <h2 class="h5 mb-0" id="stock-title">Materiali in stoccaggio</h2>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/workshop-maintenance?view=intervention">Registra intervento</a>
        </div>
        <div class="row g-2 mb-3">
            <div class="col-md-5"><input id="material-search" type="search" class="form-control" placeholder="Cerca materiale o ubicazione" aria-label="Cerca materiale"></div>
            <div class="col-md-4">
                <select id="material-filter-category" class="form-select" aria-label="Filtra per categoria">
                    <option value="">Tutte le categorie</option>
                    <option value="FILTRO">Filtri</option><option value="DETERGENTE">Detergenti</option>
                    <option value="LUBRIFICANTE">Lubrificanti</option><option value="LIQUIDO">Liquidi</option>
                    <option value="RICAMBIO">Ricambi</option><option value="ALTRO">Altro</option>
                </select>
            </div>
            <div class="col-md-3 d-flex align-items-center"><label class="form-check-label"><input id="material-filter-low" type="checkbox" class="form-check-input me-2">Solo scorte basse</label></div>
        </div>
        <div class="row g-3" id="material-list">
            <c:forEach var="material" items="${materials}">
                <div class="col-12 material-entry" data-category="${material.category}" data-low="${material.lowStock}"
                     data-search="${fn:escapeXml(material.name)} ${fn:escapeXml(material.location)}">
                    <div class="card-base">
                        <div class="d-flex justify-content-between align-items-start gap-2 flex-wrap mb-2">
                            <div><span class="badge text-bg-secondary me-2"><c:out value="${material.categoryLabel}"/></span><strong><c:out value="${material.name}"/></strong></div>
                            <span class="badge ${material.lowStock ? 'text-bg-warning' : 'text-bg-success'}">${material.lowStock ? 'Scorta bassa' : 'Disponibile'}</span>
                        </div>
                        <div class="small mb-3">Giacenza: <strong><c:out value="${material.quantity}"/> <c:out value="${material.unit}"/></strong>
                            · Minimo: <c:out value="${material.minimumQuantity}"/> <c:out value="${material.unit}"/>
                            <c:if test="${not empty material.location}"> · Ubicazione: <c:out value="${material.location}"/></c:if>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/workshop-maintenance" class="row g-2 align-items-end">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                            <input type="hidden" name="action" value="adjustMaterial">
                            <input type="hidden" name="materialId" value="${material.id}">
                            <div class="col-sm-4 col-md-3"><label class="form-label small" for="direction-${material.id}">Movimento</label>
                                <select id="direction-${material.id}" name="direction" class="form-select form-select-sm">
                                    <option value="IN">Carico +</option><option value="OUT">Rettifica -</option>
                                </select>
                            </div>
                            <div class="col-sm-4 col-md-3"><label class="form-label small" for="adjust-${material.id}">Quantità (<c:out value="${material.unit}"/>)</label>
                                <input id="adjust-${material.id}" name="quantity" type="number"
                                       min="${material.unit == 'PZ' ? '1' : '0.001'}"
                                       step="${material.unit == 'PZ' ? '1' : '0.001'}"
                                       class="form-control form-control-sm" required>
                            </div>
                            <div class="col-sm-4 col-md-3"><button class="btn btn-outline-primary btn-sm" type="submit">Aggiorna giacenza</button></div>
                        </form>
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty materials}"><div class="col-12"><div class="system-empty-state" role="status">Nessun materiale registrato. Aggiungi filtri, detergenti o altri materiali per iniziare.</div></div></c:if>
        </div>
        <div id="materialsFilteredEmpty" class="system-empty-state mt-3 d-none" role="status">Nessun materiale corrisponde ai filtri.</div>
    </section>
</main>
<script>
    (() => {
        const search = document.getElementById('material-search');
        const category = document.getElementById('material-filter-category');
        const low = document.getElementById('material-filter-low');
        const unit = document.getElementById('material-unit');
        const quantity = document.getElementById('material-quantity');
        const minimum = document.getElementById('material-minimum');
        const entries = Array.from(document.querySelectorAll('.material-entry'));
        const filteredEmpty = document.getElementById('materialsFilteredEmpty');
        unit.addEventListener('change', () => {
            const step = unit.value === 'PZ' ? '1' : '0.001';
            quantity.step = step;
            minimum.step = step;
        });
        unit.dispatchEvent(new Event('change'));
        const applyFilters = () => {
            const text = search.value.trim().toLocaleLowerCase('it');
            let visibleCount = 0;
            entries.forEach((entry) => {
                const matches = (!text || entry.dataset.search.toLocaleLowerCase('it').includes(text))
                    && (!category.value || entry.dataset.category === category.value)
                    && (!low.checked || entry.dataset.low === 'true');
                entry.classList.toggle('d-none', !matches);
                if (matches) visibleCount++;
            });
            filteredEmpty.classList.toggle('d-none', entries.length === 0 || visibleCount > 0);
        };
        [search, category, low].forEach((input) => input.addEventListener('input', applyFilters));
        applyFilters();
    })();
</script>
<script src="${pageContext.request.contextPath}/assets/js/create-form-draft.js?v=5"></script>
</body>
</html>
