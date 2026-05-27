<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Nuova prova idraulica</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css?v=11">
</head>
<body data-back-guard-form="1"
      data-back-guard-fallback="<%= request.getContextPath() %>/hydraulic-test/list">

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center" style="min-height: calc(100vh - 70px);">
    <div class="card-base" style="max-width: 620px; width: 100%;">

        <h2 class="mb-3">Nuova prova idraulica</h2>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form action="<%= request.getContextPath() %>/hydraulic-test/new"
              method="post"
              enctype="multipart/form-data"
              class="form-click-guides">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <div class="mb-3">
                <label class="form-label fw-semibold">Nome cliente</label>
                <input type="text" name="customerName" class="form-control" value="${customerName}" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Codice motore</label>
                <input type="text" name="engineCode" class="form-control" value="${engineCode}" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Video prova</label>
                <div class="file-input-wrap">
                    <input type="file"
                           id="videoFileInput"
                           name="videoFile"
                           class="file-input-native"
                           accept="video/*"
                           required>
                    <label for="videoFileInput" class="file-input-visual file-input-visual-label mb-0">
                        Seleziona video
                    </label>
                </div>
                <div class="small text-muted mt-1">Formati supportati: video/*</div>
                <div class="small text-muted mt-1">I video molto grandi vengono compressi automaticamente prima dell'invio.</div>
                <div id="videoUploadStatus" class="small mt-2 d-none" aria-live="polite"></div>
                <div id="videoPreviewWrap" class="selected-video-preview-wrap mt-2 d-none">
                    <video id="videoPreview" class="selected-video-preview" controls preload="metadata"></video>
                </div>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Data prova</label>
                <input type="date" name="testDate" class="form-control" value="${testDate}" required>
            </div>

            <div class="mb-4">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="notes" class="form-control" rows="3">${notes}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a class="btn btn-cancel-action" href="<%= request.getContextPath() %>/hydraulic-test/list">Annulla</a>
                <button type="submit" class="btn btn-save-action">Salva</button>
            </div>
        </form>
    </div>
</div>

<script>
    (() => {
        const DIRECT_UPLOAD_MAX_BYTES = 18 * 1024 * 1024; // 18 MB
        const TARGET_MAX_BYTES = 12 * 1024 * 1024; // 12 MB
        const MAX_WIDTH = 1280;
        const MAX_HEIGHT = 720;
        const MAX_DURATION_SECONDS = 10 * 60; // 10 min
        const AUDIO_BITRATE = 96000;
        const MIN_VIDEO_BITRATE = 450000;
        const MAX_VIDEO_BITRATE = 1400000;

        const input = document.getElementById('videoFileInput');
        const wrap = document.getElementById('videoPreviewWrap');
        const preview = document.getElementById('videoPreview');
        const status = document.getElementById('videoUploadStatus');
        const form = input ? input.closest('form') : null;
        const submitBtn = form ? form.querySelector('button[type="submit"]') : null;
        if (!input || !wrap || !preview || !status || !form || !submitBtn) {
            return;
        }

        let compressionInProgress = false;
        let compressionFailed = false;
        let currentObjectUrl = null;

        const setStatus = (message, type) => {
            status.classList.remove('d-none', 'text-muted', 'text-success', 'text-danger', 'text-warning');
            status.textContent = message || '';
            if (!message) {
                status.classList.add('d-none');
                return;
            }
            status.classList.add(type || 'text-muted');
        };

        const setSubmitEnabled = (enabled) => {
            submitBtn.disabled = !enabled;
            input.disabled = !enabled && compressionInProgress;
        };

        const cleanupPreviewObjectUrl = () => {
            if (currentObjectUrl) {
                URL.revokeObjectURL(currentObjectUrl);
                currentObjectUrl = null;
            }
        };

        const showPreviewFile = (file) => {
            if (!file || !file.type || !file.type.startsWith('video/')) {
                cleanupPreviewObjectUrl();
                preview.removeAttribute('src');
                preview.load();
                wrap.classList.add('d-none');
                return;
            }
            cleanupPreviewObjectUrl();
            currentObjectUrl = URL.createObjectURL(file);
            preview.src = currentObjectUrl;
            wrap.classList.remove('d-none');
        };

        const supportsClientCompression = () => {
            return !!(
                window.MediaRecorder &&
                window.DataTransfer &&
                window.File &&
                document.createElement('canvas').captureStream
            );
        };

        const pickRecorderMimeType = () => {
            const candidates = [
                'video/webm;codecs=vp9,opus',
                'video/webm;codecs=vp8,opus',
                'video/webm'
            ];
            for (const type of candidates) {
                if (MediaRecorder.isTypeSupported(type)) {
                    return type;
                }
            }
            return '';
        };

        const clamp = (value, min, max) => Math.min(max, Math.max(min, value));

        const replaceInputFile = (file) => {
            const transfer = new DataTransfer();
            transfer.items.add(file);
            input.files = transfer.files;
        };

        const compressVideoFile = async (file) => {
            const mimeType = pickRecorderMimeType();
            if (!mimeType) {
                throw new Error('Codec non supportato dal browser per la compressione locale.');
            }

            const sourceUrl = URL.createObjectURL(file);
            const video = document.createElement('video');
            video.preload = 'metadata';
            video.src = sourceUrl;
            video.muted = true;
            video.playsInline = true;

            await new Promise((resolve, reject) => {
                video.onloadedmetadata = resolve;
                video.onerror = () => reject(new Error('Impossibile leggere il video selezionato.'));
            });

            const duration = Number.isFinite(video.duration) ? video.duration : 0;
            if (duration <= 0 || duration > MAX_DURATION_SECONDS) {
                URL.revokeObjectURL(sourceUrl);
                throw new Error('Durata video non supportata per la compressione locale.');
            }

            const scale = Math.min(1, MAX_WIDTH / video.videoWidth, MAX_HEIGHT / video.videoHeight);
            const width = Math.max(2, Math.floor(video.videoWidth * scale / 2) * 2);
            const height = Math.max(2, Math.floor(video.videoHeight * scale / 2) * 2);

            const targetBits = Math.min(TARGET_MAX_BYTES * 8, Math.floor(file.size * 0.7 * 8));
            const estimatedVideoBitrate = Math.floor((targetBits / duration) - AUDIO_BITRATE);
            const videoBitrate = clamp(estimatedVideoBitrate, MIN_VIDEO_BITRATE, MAX_VIDEO_BITRATE);

            const canvas = document.createElement('canvas');
            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d', { alpha: false });
            if (!ctx) {
                URL.revokeObjectURL(sourceUrl);
                throw new Error('Canvas non disponibile per la compressione video.');
            }

            const canvasStream = canvas.captureStream(24);
            let combinedStream = canvasStream;
            let audioContext = null;

            try {
                if (window.AudioContext || window.webkitAudioContext) {
                    const AudioCtx = window.AudioContext || window.webkitAudioContext;
                    audioContext = new AudioCtx();
                    const sourceNode = audioContext.createMediaElementSource(video);
                    const destinationNode = audioContext.createMediaStreamDestination();
                    sourceNode.connect(destinationNode);
                    if (audioContext.state === 'suspended') {
                        await audioContext.resume();
                    }
                    const tracks = [
                        ...canvasStream.getVideoTracks(),
                        ...destinationNode.stream.getAudioTracks()
                    ];
                    combinedStream = new MediaStream(tracks);
                }
            } catch (error) {
                combinedStream = canvasStream;
            }

            const chunks = [];
            const recorder = new MediaRecorder(combinedStream, {
                mimeType,
                videoBitsPerSecond: videoBitrate,
                audioBitsPerSecond: AUDIO_BITRATE
            });
            recorder.ondataavailable = (event) => {
                if (event.data && event.data.size > 0) {
                    chunks.push(event.data);
                }
            };

            const renderFrame = () => {
                if (video.paused || video.ended) {
                    return;
                }
                ctx.drawImage(video, 0, 0, width, height);
                requestAnimationFrame(renderFrame);
            };

            await new Promise((resolve, reject) => {
                recorder.onerror = () => reject(new Error('Errore durante la registrazione compressa.'));
                recorder.onstop = resolve;
                video.onended = () => {
                    if (recorder.state !== 'inactive') {
                        recorder.stop();
                    }
                };
                video.onerror = () => reject(new Error('Errore durante la lettura del video.'));
                recorder.start(500);
                video.play()
                    .then(() => {
                        renderFrame();
                    })
                    .catch(() => reject(new Error('Riproduzione bloccata durante la compressione.')));
            });

            if (audioContext) {
                audioContext.close().catch(() => {});
            }
            URL.revokeObjectURL(sourceUrl);

            const blob = new Blob(chunks, { type: mimeType });
            if (!blob.size) {
                throw new Error('Compressione non riuscita: output vuoto.');
            }

            const ext = '.webm';
            const baseName = (file.name || 'video').replace(/\.[^/.]+$/, '');
            const outputName = `${baseName}_compressed${ext}`;
            return new File([blob], outputName, {
                type: blob.type || 'video/webm',
                lastModified: Date.now()
            });
        };

        input.addEventListener('change', async function () {
            compressionFailed = false;
            const file = input.files && input.files.length > 0 ? input.files[0] : null;
            showPreviewFile(file);

            if (!file || !file.type || !file.type.startsWith('video/')) {
                setStatus('', 'text-muted');
                return;
            }

            if (file.size <= DIRECT_UPLOAD_MAX_BYTES) {
                setStatus('Video pronto per upload diretto.', 'text-success');
                setSubmitEnabled(true);
                return;
            }

            if (!supportsClientCompression()) {
                compressionFailed = true;
                setSubmitEnabled(false);
                setStatus('Video troppo grande e browser non compatibile con compressione locale. Prova da Chrome/Android o riduci il video.', 'text-danger');
                return;
            }

            compressionInProgress = true;
            setSubmitEnabled(false);
            setStatus('Compressione video in corso... Attendi prima di inviare.', 'text-warning');

            try {
                const compressedFile = await compressVideoFile(file);
                if (compressedFile.size > DIRECT_UPLOAD_MAX_BYTES) {
                    compressionFailed = true;
                    setSubmitEnabled(false);
                    setStatus('Compressione completata ma file ancora troppo grande per il limite upload. Riduci ulteriormente il video.', 'text-danger');
                    return;
                }
                if (compressedFile.size >= file.size) {
                    setStatus('Compressione completata, ma il file non si è ridotto. Verrà usato comunque il file compresso.', 'text-warning');
                } else {
                    setStatus('Compressione completata. Upload pronto.', 'text-success');
                }
                replaceInputFile(compressedFile);
                showPreviewFile(compressedFile);
            } catch (error) {
                compressionFailed = true;
                setStatus(`Compressione fallita: ${error.message}`, 'text-danger');
            } finally {
                compressionInProgress = false;
                setSubmitEnabled(!compressionFailed);
            }
        });

        form.addEventListener('submit', (event) => {
            if (compressionInProgress) {
                event.preventDefault();
                setStatus('Attendi: compressione in corso.', 'text-warning');
                return;
            }
            if (compressionFailed) {
                event.preventDefault();
                setStatus('Impossibile inviare: risolvi l\'errore di compressione o scegli un video piu piccolo.', 'text-danger');
            }
        });
    })();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
