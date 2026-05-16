<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>RML • Dettaglio Motore</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe.css">

    <style>
        .clickable-image {
            cursor: pointer;
            transition: transform 0.2s ease, opacity 0.2s ease;
            outline: none;
        }

        .clickable-image:hover,
        .clickable-image:focus-visible {
            transform: scale(1.02);
            opacity: 0.9;
        }
    </style>
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container mt-5 mb-4" id="engineDetailGallery" data-engine-ref="${detail.engine.engineRef}">
    <c:if test="${updated}">
        <div class="alert alert-success" role="alert">
            Modifiche salvate correttamente.
        </div>
    </c:if>
    <div class="row g-4 card-base">

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
                    <dd>
                        <span class="badge-status
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
                        </span>
                    </dd>

                    <dt>Data ingresso:</dt>
                    <dd>
                        <fmt:parseDate value="${detail.engine.intakeDate}" pattern="yyyy-MM-dd" var="engineIntakeDateParsed" />
                        <fmt:formatDate value="${engineIntakeDateParsed}" pattern="dd / MM / yyyy" />
                    </dd>

                    <dt>Data consegna:</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${detail.engine.deliveryDate != null}">
                                <fmt:parseDate value="${detail.engine.deliveryDate}" pattern="yyyy-MM-dd" var="engineDeliveryDateParsed" />
                                <fmt:formatDate value="${engineDeliveryDateParsed}" pattern="dd / MM / yyyy" />
                            </c:when>
                            <c:otherwise>-</c:otherwise>
                        </c:choose>
                    </dd>

                    <dt>Note:</dt>
                    <dd>${detail.engine.notes}</dd>
                </dl>

                <c:set var="engineDeliveryDateShare" value="" />
                <c:if test="${detail.engine.deliveryDate != null}">
                    <fmt:parseDate value="${detail.engine.deliveryDate}" pattern="yyyy-MM-dd" var="engineDeliveryDateShareParsed" />
                    <fmt:formatDate value="${engineDeliveryDateShareParsed}" pattern="dd/MM/yyyy" var="engineDeliveryDateShare" />
                </c:if>

                <button
                        id="engineTechnicalShareBtn"
                        type="button"
                        class="share-icon-btn engine-technical-share-btn"
                        aria-label="Condividi scheda tecnica"
                        title="Condividi scheda tecnica"
                        data-engine-code="${detail.engine.engineCode}"
                        data-engine-status="${detail.engine.status}"
                        data-delivery-date="${engineDeliveryDateShare}">
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                        <circle cx="18" cy="5" r="3"></circle>
                        <circle cx="6" cy="12" r="3"></circle>
                        <circle cx="18" cy="19" r="3"></circle>
                        <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                        <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
                    </svg>
                </button>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Immagini</h5>

                <div id="engineCarousel" class="carousel slide" data-bs-ride="false">
                    <div class="carousel-inner">
                        <c:forEach var="image" items="${detail.images}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <div class="engine-image-lg clickable-image"
                                     role="button"
                                     tabindex="0"
                                     data-index="${status.index}"
                                     data-image-url="<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}"
                                     data-filename="${image.filename}"
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

                    <button class="carousel-control-prev" type="button" data-bs-target="#engineCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon"></span>
                    </button>

                    <button class="carousel-control-next" type="button" data-bs-target="#engineCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon"></span>
                    </button>
                </div>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-12 detail-actions d-flex flex-wrap justify-content-center gap-2 gap-md-3">
                <a href="<%= request.getContextPath() %>/engine/list" class="btn btn-outline-secondary px-4">
                    Indietro
                </a>

                <a href="<%= request.getContextPath() %>/engine/edit?ref=${detail.engine.engineRef}" class="btn btn-detail-edit px-4">
                    Modifica
                </a>

                <a href="<%= request.getContextPath() %>/engine/delete?engineRef=${detail.engine.engineRef}" class="btn btn-detail-delete px-4">
                    Elimina
                </a>
            </div>
        </div>

    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    window.engineDetailViewerConfig = {
        engineRef: '${detail.engine.engineRef}',
        contextPath: '<%= request.getContextPath() %>'
    };
</script>
<script type="module" src="<%= request.getContextPath() %>/assets/js/engine-detail-viewer.js"></script>

</body>
</html>
