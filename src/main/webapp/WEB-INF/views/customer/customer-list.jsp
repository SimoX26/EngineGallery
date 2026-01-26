<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Engine Gallery • Clienti</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">
</head>


<body>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>



<!-- NAVBAR -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>


<div class="contair" style="text-align: center; display: flex; justify-content: center;  align-items: center;  font-family: Arial, Helvetica, sans-serif;   font-size: 1.6rem;">
    Funzionalità ancora non implementata
</div>

</body>
</html>