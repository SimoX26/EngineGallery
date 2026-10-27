<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>${empty vehicle ? 'Nuovo veicolo' : 'Modifica veicolo'} • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=34">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>
<main class="container-fluid d-flex align-items-center justify-content-center py-4" style="min-height: calc(100vh - 70px);">
    <section class="card-base text-center" style="max-width: 520px; width: 100%;">
        <h1 class="h2 mb-3">${empty vehicle ? 'Nuovo veicolo' : 'Modifica veicolo'}</h1>
        <c:if test="${not empty error}"><div class="alert alert-danger text-start" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/vehicles" class="form-click-guides">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="${empty vehicle ? 'createVehicle' : 'updateVehicle'}">
            <c:if test="${not empty vehicle}"><input type="hidden" name="id" value="${vehicle.id}"></c:if>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="plate">Targa</label>
                <input class="form-control" id="plate" name="plate" maxlength="20" required value="<c:out value='${not empty error ? param.plate : vehicle.plate}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="make">Marca</label>
                <input class="form-control" id="make" name="make" maxlength="100" required value="<c:out value='${not empty error ? param.make : vehicle.make}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="model">Modello</label>
                <input class="form-control" id="model" name="model" maxlength="100" required value="<c:out value='${not empty error ? param.model : vehicle.model}'/>">
            </div>
            <div class="mb-4 text-start">
                <label class="form-label fw-semibold" for="notes">Note</label>
                <textarea class="form-control" id="notes" name="notes" rows="3" maxlength="2000"><c:out value="${not empty error ? param.notes : vehicle.notes}"/></textarea>
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="vehiclePhoto">Foto del veicolo (facoltativa)</label>
                <div class="file-input-wrap">
                    <input class="file-input-native" type="file" accept="image/*" id="vehiclePhoto" name="vehiclePhoto">
                    <label for="vehiclePhoto" class="file-input-visual file-input-visual-label media-action-button mb-0">Seleziona immagine</label>
                </div>
                <div class="form-text">Massimo 10 MB.</div>
                <div id="vehiclePhotoName" class="small text-muted mt-2 d-none"></div>
                <img id="vehiclePhotoPreview" class="workshop-form-preview mt-2 d-none" alt="Anteprima foto del veicolo">
                <c:if test="${not empty vehicle.vehiclePhotoFilename}"><a class="small" href="${pageContext.request.contextPath}/vehicles?view=photo&amp;id=${vehicle.id}&amp;kind=vehicle" target="_blank" rel="noopener">Vedi foto attuale</a></c:if>
            </div>
            <div class="mb-4 text-start">
                <label class="form-label fw-semibold" for="registrationPhoto">Foto del libretto (facoltativa)</label>
                <div class="file-input-wrap">
                    <input class="file-input-native" type="file" accept="image/*" id="registrationPhoto" name="registrationPhoto">
                    <label for="registrationPhoto" class="file-input-visual file-input-visual-label media-action-button mb-0">Seleziona immagine</label>
                </div>
                <div class="form-text">Massimo 10 MB.</div>
                <div id="registrationPhotoName" class="small text-muted mt-2 d-none"></div>
                <img id="registrationPhotoPreview" class="workshop-form-preview mt-2 d-none" alt="Anteprima foto del libretto">
                <c:if test="${not empty vehicle.registrationPhotoFilename}"><a class="small" href="${pageContext.request.contextPath}/vehicles?view=photo&amp;id=${vehicle.id}&amp;kind=registration" target="_blank" rel="noopener">Vedi foto attuale</a></c:if>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-cancel-action w-50" href="${pageContext.request.contextPath}/vehicles">Annulla</a>
                <button class="btn btn-save-action w-50" type="submit">Salva</button>
            </div>
        </form>
    </section>
</main>
<script>
    ['vehiclePhoto', 'registrationPhoto'].forEach(function (id) {
        const input = document.getElementById(id);
        const name = document.getElementById(id + 'Name');
        const preview = document.getElementById(id + 'Preview');
        let objectUrl;
        input.addEventListener('change', function () {
            if (objectUrl) URL.revokeObjectURL(objectUrl);
            const file = input.files && input.files[0];
            name.textContent = file ? file.name : '';
            name.classList.toggle('d-none', !file);
            objectUrl = file && file.type.startsWith('image/') ? URL.createObjectURL(file) : null;
            preview.src = objectUrl || '';
            preview.classList.toggle('d-none', !objectUrl);
        });
    });
</script>
</body>
</html>
