<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/ico/ICONA.png">
    <title>Nuovo motore pronta consegna • Engine Gallery</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=39">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/nav-menu.jsp"/>
<main class="container-fluid d-flex align-items-center justify-content-center py-4" style="min-height: calc(100vh - 70px);">
    <section class="card-base text-center" style="max-width: 560px; width: 100%;">
        <h1 class="h2 mb-3">Nuovo motore pronta consegna</h1>
        <c:if test="${not empty error}"><div class="alert alert-danger text-start" role="alert"><c:out value="${error}"/></div></c:if>
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/ready-delivery"
              class="form-click-guides" data-draft-key="ready-delivery-new"
              data-draft-user="${sessionScope.loggedUser.id}"
              data-draft-success="${pageContext.request.contextPath}/ready-delivery"
              data-draft-success-param="saved">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="action" value="create">
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="ready-engine-code">Codice motore</label>
                <input class="form-control" id="ready-engine-code" name="engineCode" maxlength="80" required
                       value="<c:out value='${param.engineCode}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="ready-engine-model">Modello</label>
                <input class="form-control" id="ready-engine-model" name="model" maxlength="120" required
                       value="<c:out value='${param.model}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="ready-engine-quantity">Quantità disponibile</label>
                <input class="form-control" type="number" id="ready-engine-quantity" name="quantity" min="0" step="1" required
                       value="<c:out value='${not empty error ? param.quantity : 1}'/>">
            </div>
            <div class="mb-3 text-start">
                <label class="form-label fw-semibold" for="ready-engine-image">Foto del motore (facoltativa)</label>
                <div class="file-input-wrap">
                    <input class="file-input-native" type="file" accept="image/*" id="ready-engine-image" name="image">
                    <label for="ready-engine-image" class="file-input-visual file-input-visual-label media-action-button mb-0">Seleziona immagine</label>
                </div>
                <div class="form-text">Massimo 10 MB.</div>
                <div id="ready-engine-image-name" class="small text-muted mt-2 d-none"></div>
                <img id="ready-engine-preview" class="workshop-form-preview mt-2 d-none" alt="Anteprima foto del motore">
            </div>
            <div class="mb-4 text-start">
                <label class="form-label fw-semibold" for="ready-engine-notes">Note</label>
                <textarea class="form-control" id="ready-engine-notes" name="notes" rows="3" maxlength="2000"><c:out value="${param.notes}"/></textarea>
            </div>
            <div class="d-flex gap-2">
                <a class="btn btn-cancel-action w-50" href="${pageContext.request.contextPath}/ready-delivery">Annulla</a>
                <button class="btn btn-save-action w-50" type="submit">Salva</button>
            </div>
        </form>
    </section>
</main>
<script>
    (() => {
        const form = document.querySelector('form[data-draft-key="ready-delivery-new"]');
        const input = document.getElementById('ready-engine-image');
        const name = document.getElementById('ready-engine-image-name');
        const preview = document.getElementById('ready-engine-preview');
        let objectUrl;
        const updatePreview = () => {
            if (objectUrl) URL.revokeObjectURL(objectUrl);
            const file = input.files && input.files[0];
            name.textContent = file ? file.name : '';
            name.classList.toggle('d-none', !file);
            objectUrl = file && file.type.startsWith('image/') ? URL.createObjectURL(file) : null;
            preview.classList.toggle('d-none', !objectUrl);
            if (objectUrl) preview.src = objectUrl;
            else preview.removeAttribute('src');
        };
        input.addEventListener('change', updatePreview);
        form.addEventListener('draftrestored', updatePreview);
    })();
</script>
<script src="${pageContext.request.contextPath}/assets/js/create-form-draft.js?v=5"></script>
</body>
</html>
