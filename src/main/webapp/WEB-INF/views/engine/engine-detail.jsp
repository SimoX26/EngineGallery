<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
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
    </style>
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-4">
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
                    <dd class="badge-status status-${detail.engine.status}">
                        ${detail.engine.status}
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

                <a href="" class="btn btn-primary px-4">
                    Modifica
                </a>

                <form action="<%= request.getContextPath() %>/engine/delete"
                      method="post"
                      onsubmit="return confirm('Sei sicuro di voler eliminare questo motore?');">
                    <input type="hidden" name="engineRef" value="${detail.engine.engineRef}">
                    <button type="submit" class="btn btn-danger px-4">
                        Elimina
                    </button>
                </form>

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

            <div class="modal-body d-flex align-items-center p-0">

                <div id="modalCarousel"
                     class="carousel slide w-100"
                     data-bs-ride="false">

                    <div class="carousel-inner">

                        <c:forEach var="image" items="${detail.images}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <div style="height: 90vh;
                                            background-image: url('<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}');
                                            background-repeat: no-repeat;
                                            background-position: center;
                                            background-size: contain;">
                                </div>
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
    const modalCarousel = new bootstrap.Carousel(modalCarouselElement);

    const navbar = document.querySelector('.navbar');
    const shareBtn = document.getElementById('shareBtn');

    modal.addEventListener('show.bs.modal', function (event) {
        const clickedImage = event.relatedTarget;
        const index = clickedImage.getAttribute('data-index');
        modalCarousel.to(index);

        // Nascondi navbar
        navbar.style.display = 'none';
    });

    modal.addEventListener('hidden.bs.modal', function () {
        // Mostra navbar
        navbar.style.display = '';
    });

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

            // Calcola l'indice dell'immagine attualmente visualizzata
            const allItems = Array.from(modalCarouselElement.querySelectorAll('.carousel-item'));
            const imageIndex = allItems.indexOf(activeItem);

            if (imageIndex === -1) {
                alert('Errore nel recupero dell\'indice dell\'immagine');
                return;
            }

            // Costanti
            const contextPath = '<%= request.getContextPath() %>';
            const engineRef = '${detail.engine.engineRef}';
            const shareUrl = `${contextPath}/image/share?engineRef=${engineRef}&imageIndex=${imageIndex}`;

            console.log('shareUrl:', shareUrl);
            console.log('imageIndex:', imageIndex);

            // Scarica l'immagine dal server
            const response = await fetch(shareUrl);
            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Errore server:', errorText);
                throw new Error('Errore nel download dell\'immagine: ' + response.status + ' - ' + errorText);
            }

            // Converti la risposta in blob
            const blob = await response.blob();
            console.log('Blob size:', blob.size);
            console.log('Blob type:', blob.type);

            // Estrai il filename dall'header Content-Disposition
            const contentDisposition = response.headers.get('content-disposition');
            let filename = 'immagine.jpg';
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename[^;=\n]*=(?:UTF-8'')?([^;\n]*)/);
                if (filenameMatch && filenameMatch[1]) {
                    filename = decodeURIComponent(filenameMatch[1]);
                }
            }

            console.log('Filename:', filename);

            // Crea un File object dal blob
            const file = new File([blob], filename, { type: blob.type });

            // Dati da condividere
            const shareData = {
                title: 'RML • Engine Gallery',
                text: `Guarda questa immagine del motore: ${engineRef}`,
                files: [file]
            };

            console.log('ShareData:', shareData);

            // Condivisione
            if (navigator.canShare && navigator.canShare(shareData)) {
                console.log('Usando navigator.share con file');
                // Se il browser supporta Web Share API con file
                await navigator.share(shareData);
            } else if (navigator.share) {
                console.log('Usando navigator.share senza file');
                // Fallback: condividi senza file
                await navigator.share({
                    title: 'RML • Engine Gallery',
                    text: `Guarda questa immagine del motore: ${engineRef}`,
                    url: window.location.href
                });
            } else {
                alert('La condivisione non è supportata da questo browser');
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