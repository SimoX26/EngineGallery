<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Nuova prova idraulica</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=9">
</head>
<body data-back-guard-form="1"
      data-back-guard-fallback="<%= request.getContextPath() %>/hydraulic-test/list">

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center" style="min-height: calc(100vh - 70px);">
    <div class="card-base" style="max-width: 620px; width: 100%;">

        <h2 class="mb-3">Nuova prova idraulica</h2>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form action="<%= request.getContextPath() %>/hydraulic-test/new"
              method="post"
              enctype="multipart/form-data"
              class="form-click-guides">
            <div class="mb-3">
                <label class="form-label fw-semibold">Nome cliente</label>
                <input type="text" name="customerName" class="form-control" value="${customerName}" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Codice motore</label>
                <input type="text" name="engineCode" class="form-control" value="${engineCode}" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Video prova</label>
                <div class="file-input-wrap">
                    <input type="file"
                           id="videoFileInput"
                           name="videoFile"
                           class="file-input-native"
                           accept="video/*"
                           required>
                    <label for="videoFileInput" class="file-input-visual file-input-visual-label mb-0">
                        Seleziona video
                    </label>
                </div>
                <div class="small text-muted mt-1">Formati supportati: video/*</div>
                <div id="videoPreviewWrap" class="selected-video-preview-wrap mt-2 d-none">
                    <video id="videoPreview" class="selected-video-preview" controls preload="metadata"></video>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Data prova</label>
                <input type="date" name="testDate" class="form-control" value="${testDate}" required>
            </div>

            <div class="mb-4">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="notes" class="form-control" rows="3">${notes}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/hydraulic-test/list">Annulla</a>
                <button type="submit" class="btn-engine">Salva prova idraulica</button>
            </div>
        </form>
    </div>
</div>

<script>
    (() => {
        const input = document.getElementById('videoFileInput');
        const wrap = document.getElementById('videoPreviewWrap');
        const preview = document.getElementById('videoPreview');
        if (!input || !wrap || !preview) {
            return;
        }

        input.addEventListener('change', function () {
            const file = input.files && input.files.length > 0 ? input.files[0] : null;
            if (!file || !file.type || !file.type.startsWith('video/')) {
                preview.removeAttribute('src');
                preview.load();
                wrap.classList.add('d-none');
                return;
            }

            const objectUrl = URL.createObjectURL(file);
            preview.src = objectUrl;
            preview.onloadeddata = function () {
                URL.revokeObjectURL(objectUrl);
            };
            wrap.classList.remove('d-none');
        });
    })();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
