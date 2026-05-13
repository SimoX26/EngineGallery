<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Catalogo</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=4">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header">
            <h1>Catalogo</h1>
            <p>Motori disponibili a catalogo</p>
        </div>

        <div class="table-container">
            <p class="mb-0">Funzionalità ancora non implementata</p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</div>

</body>
</html>
