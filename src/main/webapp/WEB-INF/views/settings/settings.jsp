<%@ page contentType="text/html; charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Impostazioni</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="dashboard-page">
    <jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

    <div class="container">
        <div class="page-header">
            <h1>Impostazioni</h1>
            <p>Preferenze di tema e notifiche del dispositivo</p>
        </div>

        <div class="row g-4">
            <div class="col-12 col-lg-6">
                <div class="card-base">
                    <h5 class="fw-semibold mb-2">Tema applicazione</h5>
                    <p class="text-muted mb-3">Scegli il tema grafico dell'interfaccia.</p>

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
                    <h5 class="fw-semibold mb-2">Notifiche Android</h5>
                    <p class="text-muted mb-3">Gestione permessi notifiche del browser/app installata.</p>

                    <div class="form-check form-switch mb-3">
                        <input class="form-check-input" type="checkbox" role="switch" id="notificationsToggle">
                        <label class="form-check-label fw-semibold" for="notificationsToggle">Abilita notifiche</label>
                    </div>

                    <div id="notificationsStatus" class="small text-muted">Stato notifiche: —</div>
                    <div id="notificationsHint" class="small text-muted mt-2"></div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        (function () {
            const THEME_KEY = 'enginegallery.theme';
            const NOTIFY_OPT_OUT_KEY = 'enginegallery.notifications.optOut';
            const root = document.documentElement;

            const themeSelect = document.getElementById('themeSelect');
            const themeStatus = document.getElementById('themeStatus');
            const notificationsToggle = document.getElementById('notificationsToggle');
            const notificationsStatus = document.getElementById('notificationsStatus');
            const notificationsHint = document.getElementById('notificationsHint');

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

            function updateNotificationsUi() {
                const supported = 'Notification' in window;
                if (!supported) {
                    notificationsToggle.checked = false;
                    notificationsToggle.disabled = true;
                    notificationsStatus.textContent = 'Stato notifiche: non supportate';
                    notificationsHint.textContent = 'Questo browser/dispositivo non supporta l\'API Notification.';
                    return;
                }

                const permission = Notification.permission;
                const userOptOut = localStorage.getItem(NOTIFY_OPT_OUT_KEY) === '1';

                if (permission === 'granted' && !userOptOut) {
                    notificationsToggle.checked = true;
                    notificationsStatus.textContent = 'Stato notifiche: abilitate';
                    notificationsHint.textContent = 'Permesso browser concesso.';
                    return;
                }

                if (permission === 'denied') {
                    notificationsToggle.checked = false;
                    notificationsStatus.textContent = 'Stato notifiche: permesso negato';
                    notificationsHint.textContent = 'Riabilita manualmente dalle impostazioni del browser/dispositivo.';
                    return;
                }

                notificationsToggle.checked = false;
                notificationsStatus.textContent = 'Stato notifiche: disabilitate';
                notificationsHint.textContent = 'Attivando lo switch verrà richiesto il permesso del browser.';
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

            if (notificationsToggle) {
                updateNotificationsUi();

                notificationsToggle.addEventListener('change', async function () {
                    const supported = 'Notification' in window;
                    if (!supported) {
                        updateNotificationsUi();
                        return;
                    }

                    if (notificationsToggle.checked) {
                        if (Notification.permission === 'granted') {
                            localStorage.removeItem(NOTIFY_OPT_OUT_KEY);
                            updateNotificationsUi();
                            return;
                        }

                        if (Notification.permission === 'denied') {
                            notificationsToggle.checked = false;
                            updateNotificationsUi();
                            return;
                        }

                        const result = await Notification.requestPermission();
                        if (result === 'granted') {
                            localStorage.removeItem(NOTIFY_OPT_OUT_KEY);
                        } else {
                            notificationsToggle.checked = false;
                        }
                        updateNotificationsUi();
                        return;
                    }

                    localStorage.setItem(NOTIFY_OPT_OUT_KEY, '1');
                    updateNotificationsUi();
                });
            }
        })();
    </script>
</div>

</body>
</html>
