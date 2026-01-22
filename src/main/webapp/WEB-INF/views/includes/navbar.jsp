<%@ page contentType="text/html; charset=UTF-8" %>

<nav class="navbar navbar-expand-lg navbar-dark sticky-top"
     style="background-color: #1f2933;">
    <div class="container-fluid">

        <!-- BRAND -->
        <a class="navbar-brand fw-bold"
           href="<%= request.getContextPath() %>/dashboard">
            Engine Gallery
        </a>

        <!-- TOGGLER (mobile) -->
        <button class="navbar-toggler" type="button"
                data-bs-toggle="collapse"
                data-bs-target="#engineNavbar"
                aria-controls="engineNavbar"
                aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- LINKS -->
        <div class="collapse navbar-collapse" id="engineNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link
                       ${pageContext.request.requestURI.contains("dashboard") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/dashboard">
                        Dashboard
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${pageContext.request.requestURI.contains("clienti") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/customer">
                        Clienti
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${pageContext.request.requestURI.contains("motori") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/engine">
                        Motori
                    </a>
                </li>
            </ul>

            <!-- LOGOUT -->
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link text-danger fw-semibold"
                       href="<%= request.getContextPath() %>/logout">
                        Logout
                    </a>
                </li>
            </ul>
        </div>

    </div>
</nav>
