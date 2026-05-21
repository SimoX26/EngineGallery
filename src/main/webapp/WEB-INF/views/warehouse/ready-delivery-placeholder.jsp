<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Pronta Consegna</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header-with-search">
            <div class="page-header">
                <h1>Pronta Consegna</h1>
            </div>

            <div class="search-panel-compact">
                <label for="readyDeliverySearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
                <input type="search" id="readyDeliverySearch" class="form-control" placeholder="cerca..." disabled>
            </div>

            <div class="page-header-actions page-header-actions--view">
                <div class="btn-group btn-group-sm page-header-view-switch" role="group" aria-label="Cambia vista pronta consegna">
                    <button type="button"
                            id="readyDeliveryViewListBtn"
                            class="btn btn-outline-secondary engine-view-toggle-btn"
                            title="Vista lista"
                            aria-label="Vista lista">
                        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <line x1="5" y1="7" x2="19" y2="7"></line>
                            <line x1="5" y1="12" x2="19" y2="12"></line>
                            <line x1="5" y1="17" x2="19" y2="17"></line>
                        </svg>
                        <span class="visually-hidden">Lista</span>
                    </button>
                    <button type="button"
                            id="readyDeliveryViewGalleryBtn"
                            class="btn btn-outline-secondary active engine-view-toggle-btn"
                            title="Vista galleria"
                            aria-label="Vista galleria">
                        <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <rect x="4" y="4" width="7" height="7" rx="1"></rect>
                            <rect x="13" y="4" width="7" height="7" rx="1"></rect>
                            <rect x="4" y="13" width="7" height="7" rx="1"></rect>
                            <rect x="13" y="13" width="7" height="7" rx="1"></rect>
                        </svg>
                        <span class="visually-hidden">Galleria</span>
                    </button>
                </div>
            </div>
        </div>

        <div id="readyDeliveryListView" class="table-container d-none">
            <p class="mb-0">Vista Lista disponibile quando la sezione sarà popolata.</p>
        </div>

        <div id="readyDeliveryGalleryView" class="table-container">
            <p class="mb-0">Vista Galleria disponibile quando la sezione sarà popolata.</p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const readyDeliveryViewListBtn = document.getElementById('readyDeliveryViewListBtn');
        const readyDeliveryViewGalleryBtn = document.getElementById('readyDeliveryViewGalleryBtn');
        const readyDeliveryListView = document.getElementById('readyDeliveryListView');
        const readyDeliveryGalleryView = document.getElementById('readyDeliveryGalleryView');
        const readyDeliveryViewStorageKey = 'readyDelivery.viewMode';

        const applyReadyDeliveryViewMode = (mode) => {
            const safeMode = mode === 'list' ? 'list' : 'gallery';
            readyDeliveryListView.classList.toggle('d-none', safeMode !== 'list');
            readyDeliveryGalleryView.classList.toggle('d-none', safeMode !== 'gallery');
            readyDeliveryViewListBtn.classList.toggle('active', safeMode === 'list');
            readyDeliveryViewGalleryBtn.classList.toggle('active', safeMode === 'gallery');
            sessionStorage.setItem(readyDeliveryViewStorageKey, safeMode);
        };

        readyDeliveryViewListBtn.addEventListener('click', () => applyReadyDeliveryViewMode('list'));
        readyDeliveryViewGalleryBtn.addEventListener('click', () => applyReadyDeliveryViewMode('gallery'));
        applyReadyDeliveryViewMode(sessionStorage.getItem(readyDeliveryViewStorageKey) || 'gallery');
    </script>
</div>

</body>
</html>
