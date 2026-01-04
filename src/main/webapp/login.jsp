<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Engine Gallery • Login</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <!-- Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap"
          rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">
</head>

<body class="login-page">

<div class="login-card">

    <!-- BRAND -->
    <div class="text-center mb-4">
        <h1 class="brand">Engine Gallery</h1>
        <p>Accesso al sistema di gestione motori</p>
    </div>

    <!-- ERROR MESSAGE -->
    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="alert alert-danger" role="alert">
        <%= error %>
    </div>
    <%
        }
    %>

    <!-- LOGIN FORM -->
    <form action="<%= request.getContextPath() %>/login" method="post">

        <div class="mb-3">
            <label for="username" class="form-label">Username</label>
            <input type="text"
                   class="form-control"
                   id="username"
                   name="username"
                   required
                   autofocus>
        </div>

        <div class="mb-4">
            <label for="password" class="form-label">Password</label>
            <input type="password"
                   class="form-control"
                   id="password"
                   name="password"
                   required>
        </div>

        <div class="d-grid">
            <button type="submit" class="btn btn-engine">
                Accedi
            </button>
        </div>

    </form>

    <!-- FOOTER -->
    <div class="login-footer">
        © Engine Gallery – Sistema di catalogazione motori
    </div>

</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
