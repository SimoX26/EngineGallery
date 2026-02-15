<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Aggiunta motore</title>

    <!-- Bootstrap (se già usato nel progetto) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Stile globale Engine Gallery -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>

<!-- Navbar -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<!-- CONTENUTO PRINCIPALE -->
<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 520px; width: 100%;">

        <!-- Titolo -->
        <h2 class="mb-3">Aggiunta motore</h2>

        <!-- Sottotesto -->
        <p class="text-muted mb-4">
            Carica le immagini e completa i dati tecnici del motore
        </p>

        <!-- Area upload -->
        <form action="<%= request.getContextPath() %>/upload" method="post" enctype="multipart/form-data">


            <div class="mb-4 text-start">

                <select class="form-select"
                        name="engineMode"
                        onchange="handleEngineMode(this)"
                        required>

                    <option value="new" ${engineMode == 'new' ? 'selected' : ''}>
                        Nuovo motore
                    </option>

                    <option value="existing" ${engineMode == 'existing' ? 'selected' : ''}>
                        Motore esistente
                    </option>
                </select>

                <div class="small text-muted mt-2">
                    <c:choose>
                        <c:when test="${engineMode == 'existing'}">
                            Riferimento selezionato
                            <span class="d-block d-sm-inline">:
                                <span class="badge bg-light text-dark border">${existingEngineRef}</span>
                            </span>
                        </c:when>
                        <c:otherwise>
                            Riferimento proposto dal sistema
                            <span class="d-block d-sm-inline">:
                                <span class="badge bg-light text-dark border">${newEngineRef}</span>
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- ENGINE REF nascosto (serve solo se nuovo motore) -->
            <input type="hidden" name="engineRef" value="${engineRef}">

            <!-- UPLOAD -->
            <div class="mb-4">
                <label class="form-label fw-semibold">Immagini</label>
                <input type="file"
                       name="images"
                       class="form-control"
                       accept="image/*"
                       multiple
                       required>
            </div>

            <!-- CLIENTE -->
            <div class="mb-3">
                <label class="form-label fw-semibold">Nome cliente</label>
                <input type="text"
                       name="customer"
                       class="form-control"
                       value="${customer}"
                       ${engineMode == 'new' ? 'required' : ''}>
            </div>

            <!-- CODICE MOTORE -->
            <div class="mb-3">
                <label class="form-label fw-semibold">Codice motore</label>
                <input type="text"
                       name="engineCode"
                       class="form-control"
                       value="${engineCode}"
                       ${engineMode == 'new' ? 'required' : ''}>
            </div>

            <!-- STATO -->
            <div class="mb-3">
                <label class="form-label fw-semibold">Stato</label>
                <select class="form-select"
                        name="status"
                        ${engineMode == 'new' ? 'required' : 'disabled'}>
                    <option value="WAITING" ${status == 'WAITING' ? 'selected' : ''}>In attesa</option>
                    <option value="WORK_IN_PROGRESS" ${status == 'WORK_IN_PROGRESS' ? 'selected' : ''}>In lavorazione</option>
                    <option value="DISASSEMBLED" ${status == 'DISASSEMBLED' ? 'selected' : ''}>Smontato</option>
                    <option value="READY" ${status == 'READY' ? 'selected' : ''}>Pronto</option>
                    <option value="DELIVERED" ${status == 'DELIVERED' ? 'selected' : ''}>Consegnato</option>
                </select>
            </div>

            <!-- NOTE -->
            <div class="mb-4">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="note"
                          class="form-control"
                          rows="3">${note}</textarea>
            </div>

            <button type="submit" class="btn-engine w-100">
                Carica immagini
            </button>

        </form>

    </div>
</div>

</body>


<script>
    function handleEngineMode(select) {
        if (select.value === 'existing') {
            window.location.href = '<%= request.getContextPath() %>/engine/select';
            return;
        }
        if (select.value === 'new') {
            window.location.href = '<%= request.getContextPath() %>/upload';
        }
    }
</script>
</html>
