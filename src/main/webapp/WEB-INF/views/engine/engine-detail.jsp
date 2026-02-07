<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>RML • Dettaglio Motore</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-4">

    <div class="row g-4 card-base">

        <!-- SINISTRA: DATI MOTORE -->
        <div class="col-lg-5">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Dati tecnici</h5>

                <dl class="engine-detail-list">
                    <dt>Riferimento:</dt>
                    <dd>${detail.engine.engineRef}</dd>

                    <dt>Codice motore:</dt>
                    <dd>${detail.engine.engineCode}</dd>

                    <dt>ID Cliente:</dt>
                    <dd>${detail.engine.customerId}</dd>

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

        <!-- DESTRA: IMMAGINI -->
        <div class="col-lg-7">
            <div class="engine-detail-section">
                <h5 class="fw-semibold mb-3">Immagini</h5>

                <div id="engineCarousel" class="carousel slide" data-bs-ride="false">

                    <div class="carousel-inner">

                        <c:forEach var="image" items="${detail.images}" varStatus="status">
                            <div class="carousel-item ${status.first ? 'active' : ''}">
                                <div class="engine-image-lg"
                                     style=" height: 420px; background-image: url('<%= request.getContextPath() %>/uploads/engines/${detail.engine.engineRef}/${image.filename}');">
                                </div>
                            </div>
                        </c:forEach>

                    </div>

                    <!-- PULSANTE PRECEDENTE -->
                    <button class="carousel-control-prev" type="button"
                            data-bs-target="#engineCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon"></span>
                    </button>

                    <!-- PULSANTE SUCCESSIVO -->
                    <button class="carousel-control-next" type="button"
                            data-bs-target="#engineCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon"></span>
                    </button>

                </div>
            </div>
        </div>

    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>