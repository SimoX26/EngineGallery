<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Impostazioni</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=12">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header">
            <h1>Impostazioni</h1>
        </div>

        <div class="row g-4">
            <div class="col-12 col-lg-6">
                <div class="card-base">
                    <h5 class="fw-semibold mb-3">Tema applicazione</h5>

                    <label class="form-label fw-semibold" for="themeSelect">Tema</label>
                    <select id="themeSelect" class="form-select">
                        <option value="auto">Automatico (sistema)</option>
                        <option value="light">Chiaro</option>
                        <option value="dark">Scuro</option>
                    </select>
                    <div id="themeStatus" class="small text-muted mt-2">Tema attuale: —</div>
                </div>
            </div>

            <div class="col-12 col-lg-6">
                <div class="card-base">
                    <h5 class="fw-semibold mb-3">Notifiche Android</h5>
                    <div class="small text-muted">Gli amministratori che usano l'app Android ricevono un avviso quando un motore viene consegnato. Il permesso si gestisce nelle impostazioni Android dell'app.</div>
                    <c:if test="${sessionScope.loggedUser.role == 'ADMIN'}">
                        <button id="enableAndroidNotifications" type="button" class="btn btn-primary mt-3 d-none">Attiva notifiche</button>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        (function () {
            const THEME_KEY = 'enginegallery.theme';
            const root = document.documentElement;

            const themeSelect = document.getElementById('themeSelect');
            const themeStatus = document.getElementById('themeStatus');
            const notificationsButton = document.getElementById('enableAndroidNotifications');

            if (notificationsButton && window.AndroidNotificationBridge
                    && typeof window.AndroidNotificationBridge.enableNotifications === 'function') {
                notificationsButton.classList.remove('d-none');
                notificationsButton.addEventListener('click', function () {
                    window.AndroidNotificationBridge.enableNotifications();
                });
            }

            function getSavedTheme() {
                const value = localStorage.getItem(THEME_KEY) || 'auto';
                if (value === 'light' || value === 'dark' || value === 'auto') {
                    return value;
                }
                return 'auto';
            }

            function getAppliedTheme(savedTheme) {
                if (savedTheme === 'auto') {
                    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                    return prefersDark ? 'dark' : 'light';
                }
                return savedTheme;
            }

            function applyTheme(savedTheme) {
                const appliedTheme = getAppliedTheme(savedTheme);
                root.setAttribute('data-theme', appliedTheme);
                root.setAttribute('data-theme-preference', savedTheme);
                if (themeStatus) {
                    const map = { auto: 'Automatico', light: 'Chiaro', dark: 'Scuro' };
                    themeStatus.textContent = 'Tema attuale: ' + map[savedTheme] + ' (' + (appliedTheme === 'dark' ? 'Scuro' : 'Chiaro') + ')';
                }
            }

            if (themeSelect) {
                const savedTheme = getSavedTheme();
                themeSelect.value = savedTheme;
                applyTheme(savedTheme);

                themeSelect.addEventListener('change', function () {
                    const selected = themeSelect.value;
                    localStorage.setItem(THEME_KEY, selected);
                    applyTheme(selected);
                });

                if (window.matchMedia) {
                    const media = window.matchMedia('(prefers-color-scheme: dark)');
                    media.addEventListener('change', function () {
                        const currentSaved = getSavedTheme();
                        if (currentSaved === 'auto') {
                            applyTheme(currentSaved);
                        }
                    });
                }
            }

        })();
    </script>
</div>

</body>
</html>
