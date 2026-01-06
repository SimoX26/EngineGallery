<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Engine Gallery • Motori</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body class="engine-gallery-page">

<!-- NAVBAR -->
<jsp:include page="/WEB-INF/jsp/includes/navbar.jsp"/>

<div class="container">

    <!-- HEADER -->
    <div class="page-header">
        <h1>Motori</h1>
        <p>
            Visualizzazione completa dei motori presenti nel sistema
        </p>
    </div>

    <!-- GALLERY -->
    <div class="row g-4">

        <c:forEach var="motore" items="${motori}">
            <div class="col-xl-3 col-lg-4 col-md-6">

                <div class="engine-gallery-card">

                    <!-- IMAGE -->
                    <div class="engine-image"
                         style="background-image: url('<%= request.getContextPath() %>/assets/img/engine-placeholder.jpg');">
                    </div>

                    <!-- BODY -->
                    <div class="engine-body">

                        <div class="engine-code">
                            ${motore.codice}
                        </div>

                        <div class="engine-client">
                            Cliente: ${motore.clienteNome}
                        </div>

                        <div class="engine-footer">

                            <!-- STATUS -->
                            <span class="badge-status
                                ${motore.stato == 'STOCCATO' ? 'status-stoccato' : ''}
                                ${motore.stato == 'IN_LAVORAZIONE' ? 'status-lavorazione' : ''}
                                ${motore.stato == 'CONSEGNATO' ? 'status-consegnato' : ''}">
                                ${motore.statoLabel}
                            </span>

                            <!-- DETAIL -->
                            <a class="btn btn-sm btn-outline-primary"
                               href="<%= request.getContextPath() %>/motori/dettaglio?id=${motore.id}">
                                Dettaglio
                            </a>

                        </div>
                    </div>

                </div>

            </div>
        </c:forEach>

    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
