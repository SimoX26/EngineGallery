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

                    <select id="themeSelect" class="form-select" aria-label="Tema applicazione">
                        <option value="auto">Automatico (sistema)</option>
                        <option value="light">Chiaro</option>
                        <option value="dark">Scuro</option>
                    </select>
                </div>
            </div>

            <div class="col-12 col-lg-6">
                <div class="card-base">
                    <h5 class="fw-semibold mb-3">Notifiche Android</h5>
                    <p class="small text-muted mb-1">Nell'app Android gli amministratori ricevono notifiche per:</p>
                    <ul class="small text-muted mb-0"><li>la consegna di un motore.</li></ul>
                    <c:if test="${sessionScope.loggedUser.role == 'ADMIN'}">
                        <div class="form-check form-switch mt-3">
                            <input class="form-check-input" type="checkbox" role="switch" id="androidNotificationsToggle" disabled>
                            <label class="form-check-label" for="androidNotificationsToggle">Attiva notifiche</label>
                        </div>
                        <div id="androidNotificationStatus" class="small text-muted mt-2" role="status" aria-live="polite">Verifica in corso…</div>
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
            const notificationsToggle = document.getElementById('androidNotificationsToggle');
            const notificationsStatus = document.getElementById('androidNotificationStatus');

            window.setAndroidNotificationStatus = function (state) {
                if (!notificationsStatus) return;
                const messages = {
                    checking: 'Verifica in corso…',
                    inactive: 'Notifiche disattivate su questo dispositivo.',
                    registering: 'Attivazione in corso…',
                    disabling: 'Disattivazione in corso…',
                    active: 'Notifiche attive su questo dispositivo.',
                    error: 'Attivazione non riuscita. Riprova.',
                    disable_error: 'Disattivazione non riuscita. Riprova.',
                    unavailable: 'Disponibile nell’app Android aggiornata.',
                    verification_failed: 'Impossibile verificare l’accesso amministratore. Riprova.'
                };
                notificationsStatus.textContent = messages[state] || messages.checking;
                notificationsStatus.classList.toggle('text-success', state === 'active');
                if (notificationsToggle) {
                    notificationsToggle.checked = state === 'active' || state === 'disable_error';
                    notificationsToggle.disabled = state === 'checking' || state === 'registering'
                        || state === 'disabling' || state === 'unavailable';
                }
            };

            if (notificationsToggle && window.AndroidNotificationBridge
                    && typeof window.AndroidNotificationBridge.enableNotifications === 'function'
                    && typeof window.AndroidNotificationBridge.disableNotifications === 'function'
                    && typeof window.AndroidNotificationBridge.refreshNotificationStatus === 'function') {
                notificationsToggle.addEventListener('change', function () {
                    const enabled = notificationsToggle.checked;
                    window.setAndroidNotificationStatus(enabled ? 'registering' : 'disabling');
                    if (enabled) window.AndroidNotificationBridge.enableNotifications();
                    else window.AndroidNotificationBridge.disableNotifications();
                });
                window.AndroidNotificationBridge.refreshNotificationStatus();
            } else {
                window.setAndroidNotificationStatus('unavailable');
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
