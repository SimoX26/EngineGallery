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

                <form action=""
                      method="post"
                      onsubmit="return confirm('Sei sicuro di voler eliminare questo motore?');">
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

            <!-- X dentro la modal -->
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
</script>

</body>
</html>