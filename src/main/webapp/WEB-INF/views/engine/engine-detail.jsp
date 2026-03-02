<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>RML • Dettaglio Motore</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">

    <style>
        .clickable-image {
            cursor: pointer;
            transition: transform 0.2s ease, opacity 0.2s ease;
        }

        .clickable-image:hover {
            transform: scale(1.02);
            opacity: 0.9;
        }

        .modal-zoom-image {
            transform-origin: center center;
            transform: scale(1);
            transition: transform 0.15s ease-out;
            touch-action: none;
            user-select: none;
            -webkit-user-select: none;
            -webkit-user-drag: none;
            width: 100%;
            height: 90vh;
            object-fit: contain;
            object-position: center;
            display: block;
            background-color: #1f2933;
        }

        .zoom-debug-panel {
            position: absolute;
            top: 20px;
            left: 20px;
            z-index: 1200;
            background: rgba(0, 0, 0, 0.72);
            color: #fff;
            font-size: 12px;
            line-height: 1.35;
            padding: 10px 12px;
            border-radius: 8px;
            white-space: pre-line;
            pointer-events: none;
        }
    </style>
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-4">
    <c:if test="${updated}">
        <div class="alert alert-success" role="alert">
            Modifiche salvate correttamente.
        </div>
    </c:if>
    <div class="row g-4 card-base">

        <!-- =========================
             SINISTRA: DATI MOTORE
             ========================= -->
        <div class="col-lg-5">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Dati tecnici</h5>

                <dl class="engine-detail-list">
                    <dt>Riferimento:</dt>
                    <dd>${detail.engine.engineRef}</dd>

                    <dt>Codice motore:</dt>
                    <dd>${detail.engine.engineCode}</dd>

                    <dt>Cliente:</dt>
                    <dd>${detail.engine.customerName}</dd>

                    <dt>Stato:</dt>
                    <c:set var="st" value="${detail.engine.status}" />
                    <dd class="badge-status
                        ${st == 'WAITING' ? 'status-stoccato' : ''}
                        ${st == 'WORK_IN_PROGRESS' ? 'status-lavorazione' : ''}
                        ${st == 'READY' ? 'status-ready' : ''}
                        ${st == 'DELIVERED' ? 'status-consegnato' : ''}">
                        <c:choose>
                            <c:when test="${st == 'WAITING'}">In attesa</c:when>
                            <c:when test="${st == 'WORK_IN_PROGRESS'}">In lavorazione</c:when>
                            <c:when test="${st == 'READY'}">Pronto</c:when>
                            <c:when test="${st == 'DELIVERED'}">Consegnato</c:when>
                            <c:otherwise>${st}</c:otherwise>
                        </c:choose>
                    </dd>

                    <dt>Data ingresso:</dt>
                    <dd>${detail.engine.intakeDate}</dd>

                    <dt>Data consegna:</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${detail.engine.deliveryDate != null}">
                                ${detail.engine.deliveryDate}
                            </c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </dd>

                    <dt>Note:</dt>
                    <dd>${detail.engine.notes}</dd>
                </dl>
            </div>
        </div>

        <!-- =========================
             DESTRA: IMMAGINI
             ========================= -->
        <div class="col-lg-7">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Immagini</h5>

                <!-- Carousel principale -->
                <div id="engineCarousel" class="carousel slide" data-bs-ride="false">

                    <div class="carousel-inner">

                        <c:forEach var="image" items="${detail.images}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <div class="engine-image-lg clickable-image"
                                     data-index="${status.index}"
                                     data-image-url="<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}"
                                     data-filename="${image.filename}"
                                     data-bs-toggle="modal"
                                     data-bs-target="#imageModal"
                                     style="height: 420px;
                                            width: 100%;
                                            background-image: url('<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}');
                                            background-repeat: no-repeat;
                                            background-position: center;
                                            background-size: contain;
                                            background-color: #1f2933;">
                                </div>
                            </div>
                        </c:forEach>

                    </div>

                    <!-- PREV -->
                    <button class="carousel-control-prev"
                            type="button"
                            data-bs-target="#engineCarousel"
                            data-bs-slide="prev">
                        <span class="carousel-control-prev-icon"></span>
                    </button>

                    <!-- NEXT -->
                    <button class="carousel-control-next"
                            type="button"
                            data-bs-target="#engineCarousel"
                            data-bs-slide="next">
                        <span class="carousel-control-next-icon"></span>
                    </button>

                </div>
            </div>
        </div>

        <!-- =========================
             AZIONI
             ========================= -->
        <div class="row mt-4">
            <div class="col-12 d-flex justify-content-end gap-3">

                <a href="<%= request.getContextPath() %>/engine/edit?ref=${detail.engine.engineRef}" class="btn btn-primary px-4">
                    Modifica
                </a>

                <a href="<%= request.getContextPath() %>/engine/delete?engineRef=${detail.engine.engineRef}"
                   class="btn btn-danger px-4">
                    Elimina
                </a>

            </div>
        </div>

    </div>
</div>


<!-- =========================
     MODAL FULLSCREEN CON CAROUSEL
     ========================= -->
<div class="modal fade" id="imageModal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen">
        <div class="modal-content bg-dark border-0 position-relative">

            <!-- CONDIVIDI -->
            <button type="button"
                    id="shareBtn"
                    class="modal-action-btn"
                    aria-label="Condividi"
                    title="Condividi immagine">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="18" cy="5" r="3"></circle>
                    <circle cx="6" cy="12" r="3"></circle>
                    <circle cx="18" cy="19" r="3"></circle>
                    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
                </svg>
            </button>

            <!-- CHIUDI (X) -->
            <button type="button"
                    class="modal-close-btn"
                    data-bs-dismiss="modal"
                    aria-label="Chiudi">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
            </button>

            <div id="zoomDebugPanel" class="zoom-debug-panel">debug init</div>

            <div class="modal-body d-flex align-items-center p-0">

                <div id="modalCarousel"
                     class="carousel slide w-100"
                     data-bs-ride="false"
                     data-bs-touch="false">

                    <div class="carousel-inner">

                        <c:forEach var="image" items="${detail.images}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <img data-image-url="<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}"
                                     data-filename="${image.filename}"
                                     class="modal-zoom-image"
                                     src="<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}"
                                     alt="Immagine motore ${status.index + 1}"
                                     draggable="false">
                            </div>
                        </c:forEach>

                    </div>

                    <button class="carousel-control-prev"
                            type="button"
                            data-bs-target="#modalCarousel"
                            data-bs-slide="prev">
                        <span class="carousel-control-prev-icon"></span>
                    </button>

                    <button class="carousel-control-next"
                            type="button"
                            data-bs-target="#modalCarousel"
                            data-bs-slide="next">
                        <span class="carousel-control-next-icon"></span>
                    </button>

                </div>

            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- Script sincronizzazione -->
<script>
    const modal = document.getElementById('imageModal');
    const modalCarouselElement = document.getElementById('modalCarousel');
    const modalCarousel = new bootstrap.Carousel(modalCarouselElement, {
        interval: false,
        touch: false,
        keyboard: false
    });

    const navbar = document.querySelector('.navbar');
    const shareBtn = document.getElementById('shareBtn');
    const zoomDebugPanel = document.getElementById('zoomDebugPanel');
    const doubleTapDelayMs = 550;
    const doubleTapMaxDistancePx = 48;
    const doubleTapZoomScale = 1.5;

    let currentScale = 1;
    let lastTapTimestamp = 0;
    let lastTapSlideIndex = -1;
    let lastTapX = 0;
    let lastTapY = 0;
    let debugTapCount = 0;

    function getActiveZoomImage() {
        const activeItem = modalCarouselElement.querySelector('.carousel-item.active');
        return activeItem ? activeItem.querySelector('.modal-zoom-image') : null;
    }

    function applyScale(imageElement, scale) {
        const clampedScale = Math.min(Math.max(scale, 1), 4);
        imageElement.style.transform = `scale(${clampedScale})`;
        currentScale = clampedScale;
        updateDebugPanel('applyScale -> ' + clampedScale.toFixed(2));
    }

    function resetZoomOnAllImages() {
        modalCarouselElement.querySelectorAll('.modal-zoom-image').forEach((imageElement) => {
            imageElement.style.transform = 'scale(1)';
        });
        currentScale = 1;
    }

    function isOnActiveImage(target) {
        const activeImage = getActiveZoomImage();
        return !!activeImage && activeImage.contains(target);
    }

    function isOnBlockedControl(target) {
        return !!target.closest('.modal-close-btn, .modal-action-btn, .carousel-control-prev, .carousel-control-next');
    }

    function getActiveSlide() {
        return modalCarouselElement.querySelector('.carousel-item.active');
    }

    function getActiveSlideIndex() {
        const slides = modalCarouselElement.querySelectorAll('.carousel-item');
        const active = getActiveSlide();
        return active ? Array.from(slides).indexOf(active) : -1;
    }

    function updateDebugPanel(message) {
        if (!zoomDebugPanel) {
            return;
        }
        zoomDebugPanel.textContent =
                'event: ' + message + '\n' +
                'scale: ' + currentScale.toFixed(2) + '\n' +
                'slide: ' + getActiveSlideIndex() + '\n' +
                'tapCount: ' + debugTapCount;
    }

    function toggleDoubleTapZoom() {
        const activeImage = getActiveZoomImage();
        if (!activeImage) {
            return;
        }
        const nextScale = currentScale > 1.01 ? 1 : doubleTapZoomScale;
        applyScale(activeImage, nextScale);
    }

    modal.addEventListener('show.bs.modal', function (event) {
        const clickedImage = event.relatedTarget;
        const index = clickedImage ? clickedImage.getAttribute('data-index') : 0;
        modalCarousel.to(index);
        debugTapCount = 0;
        updateDebugPanel('modal show index=' + index);

        // Nascondi navbar
        navbar.style.display = 'none';
    });

    modal.addEventListener('hidden.bs.modal', function () {
        // Mostra navbar
        navbar.style.display = '';
        resetZoomOnAllImages();
        updateDebugPanel('modal hidden');
    });

    modalCarouselElement.addEventListener('touchmove', function (event) {
        if (isOnActiveImage(event.target)) {
            event.preventDefault();
            updateDebugPanel('touchmove blocked');
        }
    }, { passive: false });

    modalCarouselElement.addEventListener('wheel', function (event) {
        if (isOnActiveImage(event.target)) {
            event.preventDefault();
            updateDebugPanel('wheel blocked');
        }
    }, { passive: false });

    modalCarouselElement.addEventListener('slid.bs.carousel', function () {
        resetZoomOnAllImages();
        lastTapTimestamp = 0;
        lastTapSlideIndex = -1;
        debugTapCount = 0;
        updateDebugPanel('slide changed');
    });

    function handleTapCandidate(clientX, clientY, target, preventDefaultFn) {
        debugTapCount += 1;
        if (!target || isOnBlockedControl(target)) {
            updateDebugPanel('tap blocked by control');
            return;
        }

        const activeSlideIndex = getActiveSlideIndex();
        if (activeSlideIndex < 0) {
            return;
        }

        const now = Date.now();
        const dt = now - lastTapTimestamp;
        const dx = clientX - lastTapX;
        const dy = clientY - lastTapY;
        const distance = Math.hypot(dx, dy);
        const isDoubleTap = dt <= doubleTapDelayMs
                && distance <= doubleTapMaxDistancePx
                && lastTapSlideIndex === activeSlideIndex;
        updateDebugPanel('tap dt=' + dt + 'ms dist=' + distance.toFixed(1) + ' double=' + isDoubleTap);

        if (isDoubleTap) {
            if (preventDefaultFn) {
                preventDefaultFn();
            }
            toggleDoubleTapZoom();
            lastTapTimestamp = 0;
            lastTapSlideIndex = -1;
            return;
        }

        lastTapTimestamp = now;
        lastTapSlideIndex = activeSlideIndex;
        lastTapX = clientX;
        lastTapY = clientY;
    }

    function handleMouseLikeTap(event) {
        handleTapCandidate(
                event.clientX,
                event.clientY,
                event.target,
                () => event.preventDefault()
        );
    }

    if (window.PointerEvent) {
        modalCarouselElement.addEventListener('pointerup', function (event) {
            if (event.pointerType === 'mouse' && event.button !== 0) {
                return;
            }
            handleMouseLikeTap(event);
        }, { capture: true });
    } else {
        modalCarouselElement.addEventListener('touchend', function (event) {
            if (!event.changedTouches || event.changedTouches.length === 0) {
                return;
            }
            const touch = event.changedTouches[0];
            handleTapCandidate(
                    touch.clientX,
                    touch.clientY,
                    event.target,
                    () => event.preventDefault()
            );
        }, { capture: true, passive: false });
    }

    // Fallback robusto per browser/webview che non gestiscono bene pointer/touch.
    modalCarouselElement.addEventListener('click', function (event) {
        handleMouseLikeTap(event);
    }, { capture: true });

    updateDebugPanel('script loaded');

    /* =========================
       Logica Condividi
       ========================= */
    shareBtn.addEventListener('click', async function () {
        try {
            // Recupera l'elemento del carousel attivo
            const activeItem = modalCarouselElement.querySelector('.carousel-item.active');
            if (!activeItem) {
                alert('Nessuna immagine selezionata');
                return;
            }

            const activeImage = activeItem.querySelector('[data-image-url]');
            if (!activeImage) {
                alert('Immagine non disponibile');
                return;
            }

            const imageUrl = activeImage.getAttribute('data-image-url');
            const resolvedImageUrl = new URL(imageUrl, window.location.origin).href;
            const engineRef = '${detail.engine.engineRef}';

            // Dati base da condividere
            const shareData = {
                title: 'RML • Engine Gallery',
                text: 'Guarda questa immagine del motore: ' + engineRef,
                url: resolvedImageUrl
            };

            if (navigator.share) {
                await navigator.share(shareData);
            } else {
                alert('Condivisione non supportata su questo dispositivo o contesto.');
            }

        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('Errore nella condivisione:', error);
                alert('Errore durante la condivisione: ' + error.message);
            }
        }
    });
</script>

</body>
</html>
