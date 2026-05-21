<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="servletPath" value="${pageContext.request.servletPath}" />
<c:set var="loggedRole" value="${sessionScope.loggedUser != null ? sessionScope.loggedUser.role : null}" />
<c:set var="canViewStatistics" value="${loggedRole == 'ADMIN' || loggedRole == 'INSPECTOR'}" />
<c:set var="canViewMaintenance" value="${loggedRole == 'ADMIN'}" />

<style>
    body:not(.login-page) {
        padding-top: 72px;
    }

    @media (max-width: 768px) {
        body:not(.login-page) {
            padding-top: 66px;
        }
    }
</style>

<nav class="navbar navbar-expand-lg navbar-dark fixed-top"
     style="background-color: #1f2933; position: fixed; top: 0; left: 0; right: 0; width: 100%; z-index: 3000;">
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
            <span class="navbar-toggler-label">MENU</span>
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

                <li class="nav-item nav-group-start">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/customer") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/customer/list">
                        Clienti
                    </a>
                </li>

                <li class="nav-item nav-group-start">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/engine")
                        && !fn:startsWith(servletPath, "/engine/ready")
                        && !fn:startsWith(servletPath, "/engine/archive") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/engine/list">
                        Motori
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/engine/archive") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/engine/archive">
                        Archivio motori
                    </a>
                </li>

                <li class="nav-item nav-group-start">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/hydraulic-test") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/hydraulic-test/list">
                        Prove idrauliche
                    </a>
                </li>

                <li class="nav-item nav-group-start">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/warehouse") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/warehouse/list">
                        Magazzino
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/ready-delivery") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/ready-delivery">
                        Pronta Consegna
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link
                       ${fn:startsWith(servletPath, "/catalog") ? "active" : ""}"
                       href="<%= request.getContextPath() %>/catalog">
                        Catalogo
                    </a>
                </li>

                <c:if test="${canViewStatistics}">
                    <li class="nav-item nav-group-start">
                        <a class="nav-link
                           ${fn:startsWith(servletPath, "/statistics") ? "active" : ""}"
                           href="<%= request.getContextPath() %>/statistics">
                            Statistiche
                        </a>
                    </li>
                </c:if>

                <c:if test="${canViewMaintenance}">
                    <li class="nav-item">
                        <a class="nav-link
                           ${fn:startsWith(servletPath, "/maintenance") ? "active" : ""}"
                           href="<%= request.getContextPath() %>/maintenance">
                            Manutenzione
                        </a>
                    </li>
                </c:if>


            </ul>

            <!-- LOGOUT -->
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a href="<%= request.getContextPath() %>/settings"
                       class="nav-link ${fn:startsWith(servletPath, '/settings') ? 'active' : ''}">
                        Impostazioni
                    </a>
                </li>
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
        const THEME_KEY = 'enginegallery.theme';
        const root = document.documentElement;

        function getSavedTheme() {
            const value = localStorage.getItem(THEME_KEY) || 'auto';
            if (value === 'light' || value === 'dark' || value === 'auto') {
                return value;
            }
            return 'auto';
        }

        function applyThemeFromPreference(themePreference) {
            let applied = themePreference;
            if (themePreference === 'auto') {
                const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                applied = prefersDark ? 'dark' : 'light';
            }
            root.setAttribute('data-theme', applied);
            root.setAttribute('data-theme-preference', themePreference);
        }

        applyThemeFromPreference(getSavedTheme());

        if (window.matchMedia) {
            const media = window.matchMedia('(prefers-color-scheme: dark)');
            media.addEventListener('change', function () {
                const current = getSavedTheme();
                if (current === 'auto') {
                    applyThemeFromPreference(current);
                }
            });
        }
    })();

    (function () {
        const BACK_LOCK_KEY = 'postSubmitBackLock.currentFallback';
        const FINAL_HOME_WALL_KEY = 'postSubmitBackLock.finalHomeWall';
        const HOME_PATH = '<%= request.getContextPath() %>/dashboard';

        function cleanUrlWithoutLockBack() {
            const params = new URLSearchParams(window.location.search);
            params.delete('lockBack');
            const query = params.toString();
            return window.location.pathname + (query ? ('?' + query) : '');
        }

        function initPostSubmitBackGuard() {
            const params = new URLSearchParams(window.location.search);
            const hasLockBackParam = params.get('lockBack') === '1';

            if (hasLockBackParam) {
                const cleanUrl = cleanUrlWithoutLockBack();
                sessionStorage.setItem(BACK_LOCK_KEY, cleanUrl);
                history.replaceState({postSubmitBackLock: true}, '', cleanUrl + window.location.hash);
            }

            const lockedFallback = sessionStorage.getItem(BACK_LOCK_KEY);
            const currentUrl = window.location.pathname + window.location.search;
            const isLockedFallbackPage = lockedFallback && lockedFallback === currentUrl;
            if (isLockedFallbackPage) {
                history.pushState({postSubmitBackLock: true}, '', window.location.href);
                window.addEventListener('popstate', function () {
                    sessionStorage.removeItem(BACK_LOCK_KEY);
                    sessionStorage.setItem(FINAL_HOME_WALL_KEY, '1');
                    window.location.replace(HOME_PATH);
                });
            }

            const isHomePage = window.location.pathname === HOME_PATH;
            const hasFinalWall = sessionStorage.getItem(FINAL_HOME_WALL_KEY) === '1';
            if (isHomePage && hasFinalWall) {
                history.pushState({finalHomeBackWall: true}, '', window.location.href);
                window.addEventListener('popstate', function () {
                    history.pushState({finalHomeBackWall: true}, '', window.location.href);
                });
            }

            const body = document.body;
            if (!body || body.dataset.backGuardForm !== '1') {
                return;
            }

            const fallback = body.dataset.backGuardFallback;
            if (!fallback) {
                return;
            }

            window.addEventListener('pageshow', function (event) {
                const navigationEntries = performance.getEntriesByType
                        ? performance.getEntriesByType('navigation')
                        : [];
                const navigationType = navigationEntries.length > 0 ? navigationEntries[0].type : '';
                const isBackForwardNavigation = event.persisted || navigationType === 'back_forward';
                const hasSubmitLock = !!sessionStorage.getItem(BACK_LOCK_KEY);

                if (hasSubmitLock && isBackForwardNavigation) {
                    window.location.replace(fallback);
                }
            });
        }

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

        function initLoadingOverlay() {
            if (window.__appLoadingOverlayInitialized) {
                return;
            }
            window.__appLoadingOverlayInitialized = true;

            const overlay = document.createElement('div');
            overlay.className = 'app-loading-overlay';
            overlay.id = 'appLoadingOverlay';
            overlay.setAttribute('aria-hidden', 'true');
            overlay.innerHTML = ''
                + '<div class="app-loading-overlay__panel" role="status" aria-live="polite">'
                + '  <div class="spinner-border app-loading-overlay__spinner" aria-hidden="true"></div>'
                + '  <span>Caricamento in corso...</span>'
                + '</div>';
            document.body.appendChild(overlay);

            const showOverlay = function () {
                overlay.classList.add('is-visible');
                overlay.setAttribute('aria-hidden', 'false');
            };

            const isSameOrigin = function (url) {
                return url.origin === window.location.origin;
            };

            document.addEventListener('submit', function (event) {
                const form = event.target;
                if (!(form instanceof HTMLFormElement)) {
                    return;
                }
                if (form.dataset.noLoadingOverlay === 'true') {
                    return;
                }
                const method = (form.method || '').toLowerCase();
                if (method !== 'get' && method !== 'post') {
                    return;
                }
                showOverlay();
            }, true);

            document.addEventListener('click', function (event) {
                if (event.defaultPrevented || event.button !== 0) {
                    return;
                }
                if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
                    return;
                }

                // Quick-status trigger lives inside clickable cards in some views:
                // ignore it here to avoid showing loader without real navigation.
                if (event.target.closest('[data-quick-status-trigger]')) {
                    return;
                }

                const link = event.target.closest('a[href]');
                if (!link) {
                    return;
                }

                const href = link.getAttribute('href');
                if (!href || href.startsWith('#') || href.startsWith('javascript:')) {
                    return;
                }
                if (link.hasAttribute('download') || link.target === '_blank') {
                    return;
                }

                let destination;
                try {
                    destination = new URL(link.href, window.location.href);
                } catch (err) {
                    return;
                }

                if (!isSameOrigin(destination)) {
                    return;
                }

                const current = window.location.pathname + window.location.search + window.location.hash;
                const next = destination.pathname + destination.search + destination.hash;
                if (current === next) {
                    return;
                }

                showOverlay();
            }, true);

            window.addEventListener('beforeunload', function () {
                showOverlay();
            });
        }

        initPostSubmitBackGuard();
        initLoadingOverlay();

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
