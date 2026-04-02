<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="servletPath" value="${pageContext.request.servletPath}" />

<nav class="navbar navbar-expand-lg navbar-dark sticky-top"
     style="background-color: #1f2933;">
    <div class="container-fluid">

        <!-- BRAND -->
        <a class="navbar-brand fw-bold"
           href="<%= request.getContextPath() %>/dashboard">
            RML
        </a>

        <!-- TOGGLER (mobile) -->
        <button id="engineNavbarToggler" class="navbar-toggler navbar-action-btn" type="button"
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
                       ${fn:startsWith(servletPath, "/dashboard") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/dashboard">
                        Home
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/engine")
                        && !fn:startsWith(servletPath, "/engine/ready") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/engine/list">
                        Motori
                    </a>
                </li>



                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/customer") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/customer/list">
                        Clienti
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/hydraulic-test") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/hydraulic-test/list">
                        Prove idrauliche
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/warehouse") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/warehouse/list">
                        Magazzino
                    </a>
                </li>

                 <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/engine/ready") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/engine/ready">
                        Pronta consegna
                    </a>
                </li>


            </ul>

            <!-- LOGOUT -->
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a href="<%= request.getContextPath() %>/logout"
                       class="nav-link text-danger fw-semibold">
                        Logout
                    </a>
                </li>
            </ul>
        </div>

    </div>
</nav>

<script>
    (function () {
        function initNavbarToggle() {
            const toggler = document.getElementById('engineNavbarToggler');
            const menu = document.getElementById('engineNavbar');
            if (!toggler || !menu || !window.bootstrap || !window.bootstrap.Collapse) {
                return;
            }

            if (toggler.dataset.initialized === 'true') {
                return;
            }

            toggler.dataset.initialized = 'true';
            const collapse = window.bootstrap.Collapse.getOrCreateInstance(menu, {toggle: false});

            toggler.addEventListener('click', function (event) {
                event.preventDefault();
                collapse.toggle();
            });

            menu.addEventListener('shown.bs.collapse', function () {
                toggler.setAttribute('aria-expanded', 'true');
            });

            menu.addEventListener('hidden.bs.collapse', function () {
                toggler.setAttribute('aria-expanded', 'false');
            });
        }

        if (!window.bootstrap) {
            const bootstrapScript = document.createElement('script');
            bootstrapScript.src = 'https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js';
            bootstrapScript.defer = true;
            bootstrapScript.addEventListener('load', initNavbarToggle);
            document.head.appendChild(bootstrapScript);
            return;
        }

        initNavbarToggle();
    })();
</script>
