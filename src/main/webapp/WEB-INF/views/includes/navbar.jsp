<%@ page contentType="text/html; charset=UTF-8" %>

<nav class="navbar navbar-expand-lg navbar-dark sticky-top"
     style="background-color: #1f2933;">
    <div class="container-fluid">

        <!-- BRAND -->
        <a class="navbar-brand fw-bold"
           href="<%= request.getContextPath() %>/dashboard">
            RML • Engine Gallery
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
                           ${pageContext.request.requestURI.contains("engine") ? "active" : ""}"
                           href="<%= request.getContextPath() %>/engine/list">
                            Motori
                        </a>
                    </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${pageContext.request.requestURI.contains("customer") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/customer/list">
                        Clienti
                    </a>
                </li>


            </ul>

            <!-- LOGOUT -->
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <form action="<%= request.getContextPath() %>/logout"
                          method="post"
                          class="d-inline">

                        <button type="submit"
                                class="nav-link btn btn-link text-danger fw-semibold p-0">
                            Logout
                        </button>
                    </form>
                </li>
            </ul>
        </div>

    </div>
</nav>
