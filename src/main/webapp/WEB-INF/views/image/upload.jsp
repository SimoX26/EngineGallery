<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Carica immagine</title>

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

        <!-- Icona -->
        <div class="mb-4">
            <span style="font-size: 3rem;">📷</span>
        </div>

        <!-- Titolo -->
        <h2 class="mb-3">Carica immagine o scatta foto</h2>

        <!-- Sottotesto -->
        <p class="text-muted mb-4">
            Seleziona un’immagine dal tuo dispositivo oppure utilizza la fotocamera
        </p>

        <!-- Area upload -->
        <form action="<%= request.getContextPath() %>/upload" method="post" enctype="multipart/form-data">


            <div class="mb-4">
                <!-- <label class="form-label fw-semibold">Motore</label> -->

                <select class="form-select"
                        name="engineMode"
                        onchange="handleEngineMode(this)"
                        required>

                    <option value="new" ${engineMode == 'new' ? 'selected' : ''}>
                        ➕ Nuovo (riferimento: ${newEngineRef})
                    </option>

                    <option value="existing" ${engineMode == 'existing' ? 'selected' : ''}>
                        📂 Esistente (riferimento: ${existingEngineRef})
                    </option>
                </select>
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
        }
    }
</script>
</html>
