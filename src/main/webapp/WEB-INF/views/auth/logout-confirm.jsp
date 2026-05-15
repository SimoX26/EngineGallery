<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Conferma logout</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="card shadow-sm border-warning">
                <div class="card-body p-4 p-md-5">
                    <c:choose>
                        <c:when test="${logoutStep == 2}">
                            <h1 class="h4 text-danger mb-3">Conferma logout</h1>
                            <p class="mb-4">Sei veramente sicuro?</p>

                            <div class="d-flex flex-wrap gap-2">
                                <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-outline-secondary px-4">
                                    Annulla
                                </a>

                                <a href="<%= request.getContextPath() %>/logout" class="btn btn-outline-dark px-4">
                                    Torna indietro
                                </a>

                                <form action="<%= request.getContextPath() %>/logout" method="post" class="m-0">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                    <input type="hidden" name="action" value="confirm">
                                    <button type="submit" class="btn btn-danger px-4">
                                        Si, esci
                                    </button>
                                </form>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <h1 class="h4 text-warning mb-3">Conferma logout</h1>
                            <p class="mb-4">Sei sicuro di fare logout?</p>

                            <div class="d-flex flex-wrap gap-2">
                                <a href="<%= request.getContextPath() %>/dashboard" class="btn btn-outline-secondary px-4">
                                    Annulla
                                </a>

                                <form action="<%= request.getContextPath() %>/logout" method="post" class="m-0">
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                    <input type="hidden" name="action" value="continue">
                                    <button type="submit" class="btn btn-warning px-4">
                                        Continua
                                    </button>
                                </form>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
