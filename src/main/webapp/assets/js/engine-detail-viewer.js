import PhotoSwipeLightbox from 'https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe-lightbox.esm.js';

const config = window.engineDetailViewerConfig || {};
const galleryRoot = document.getElementById('engineDetailGallery');
const navbar = document.querySelector('.navbar');
const clickableImages = Array.from(document.querySelectorAll('.clickable-image'));

const SHARE_LOG_PREFIX = '[engine-share-v2]';
let shareDelegationInitialized = false;
const SHARE_FILE_SHARING_ENABLED = false;
const SHARE_MULTIPLE_FILES_ENABLED = false;

function showShareMessage(message) {
    alert(message);
}

function normalizeImageUrls(imageUrls) {
    const normalized = [];
    const seen = new Set();

    (imageUrls || []).forEach((rawUrl) => {
        if (!rawUrl) {
            return;
        }
        try {
            const absolute = new URL(rawUrl, window.location.href).href;
            if (!seen.has(absolute)) {
                seen.add(absolute);
                normalized.push(absolute);
            }
        } catch (error) {
            console.warn(`${SHARE_LOG_PREFIX} URL immagine non valida`, rawUrl, error);
        }
    });

    return normalized;
}

function getExtensionFromMimeType(mimeType) {
    const normalized = (mimeType || '').toLowerCase();
    if (normalized.includes('png')) {
        return 'png';
    }
    if (normalized.includes('webp')) {
        return 'webp';
    }
    if (normalized.includes('gif')) {
        return 'gif';
    }
    if (normalized.includes('jpeg') || normalized.includes('jpg')) {
        return 'jpg';
    }
    return 'jpg';
}

function inferMimeTypeFromUrl(url) {
    const lower = (url || '').toLowerCase();
    if (lower.endsWith('.png')) {
        return 'image/png';
    }
    if (lower.endsWith('.webp')) {
        return 'image/webp';
    }
    if (lower.endsWith('.jpeg') || lower.endsWith('.jpg')) {
        return 'image/jpeg';
    }
    return '';
}

function getFileNameFromUrl(url, fallback) {
    try {
        const pathname = new URL(url).pathname;
        const rawName = pathname.substring(pathname.lastIndexOf('/') + 1);
        return rawName || fallback;
    } catch (_error) {
        return fallback;
    }
}

async function buildShareFiles(imageUrls) {
    const files = [];

    for (let i = 0; i < imageUrls.length; i += 1) {
        const imageUrl = imageUrls[i];
        try {
            console.info(`${SHARE_LOG_PREFIX} fetch immagine`, { index: i, imageUrl });
            const response = await fetch(imageUrl, { credentials: 'include' });
            const headerContentType = (response.headers.get('content-type') || '').toLowerCase();
            console.info(`${SHARE_LOG_PREFIX} fetch response`, {
                index: i,
                imageUrl,
                status: response.status,
                ok: response.ok,
                headerContentType
            });
            if (!response.ok) {
                console.warn(`${SHARE_LOG_PREFIX} fetch non ok`, { imageUrl, status: response.status });
                continue;
            }
            if (headerContentType && !headerContentType.startsWith('image/')) {
                console.warn(`${SHARE_LOG_PREFIX} content-type header non immagine`, { imageUrl, headerContentType });
                continue;
            }

            const blob = await response.blob();
            if (!blob || blob.size === 0) {
                console.warn(`${SHARE_LOG_PREFIX} blob vuoto`, { imageUrl });
                continue;
            }

            const blobType = (blob.type || '').toLowerCase();
            const inferredMimeType = inferMimeTypeFromUrl(imageUrl);
            const mimeType = (headerContentType && headerContentType.startsWith('image/'))
                ? headerContentType.split(';')[0].trim()
                : (blobType || inferredMimeType);
            console.info(`${SHARE_LOG_PREFIX} blob dettagli`, {
                index: i,
                imageUrl,
                blobType,
                blobSize: blob.size,
                inferredMimeType,
                resolvedMimeType: mimeType
            });
            if (!mimeType.startsWith('image/')) {
                console.warn(`${SHARE_LOG_PREFIX} mime non immagine`, { imageUrl, mimeType });
                continue;
            }

            const fallbackName = `immagine-motore-${i + 1}`;
            const baseName = getFileNameFromUrl(imageUrl, fallbackName);
            const hasExtension = /\.[a-zA-Z0-9]+$/.test(baseName);
            const filename = hasExtension ? baseName : `${baseName}.${getExtensionFromMimeType(mimeType)}`;
            const file = new File([blob], filename, { type: mimeType });

            console.info(`${SHARE_LOG_PREFIX} file creato`, {
                index: i,
                imageUrl,
                filename,
                mimeType,
                fileType: file.type,
                size: file.size
            });

            files.push(file);
        } catch (error) {
            console.error(`${SHARE_LOG_PREFIX} errore durante fetch/conversione file`, { imageUrl, error });
        }
    }

    return files;
}

function readImageUrlsFromButton(button) {
    const rawFromData = (button.getAttribute('data-image-urls') || '').trim();
    if (rawFromData) {
        return rawFromData
            .split(',')
            .map((item) => item.trim())
            .filter((item) => item.length > 0);
    }

    const sourceSelector = button.getAttribute('data-share-source') || '#engineDetailGallery';
    const imageSelector = button.getAttribute('data-image-selector') || '.clickable-image';
    const sourceRoot = document.querySelector(sourceSelector);
    if (!sourceRoot) {
        console.warn(`${SHARE_LOG_PREFIX} contenitore immagini non trovato`, { sourceSelector });
        return [];
    }

    return Array.from(sourceRoot.querySelectorAll(imageSelector))
        .map((node) => node.getAttribute('data-image-url') || node.getAttribute('src') || '')
        .map((url) => url.trim())
        .filter((url) => url.length > 0);
}

async function copyFallbackText(text) {
    if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        console.info(`${SHARE_LOG_PREFIX} fallback clipboard usato`);
        showShareMessage('Condivisione file non disponibile: testo copiato negli appunti.');
        return;
    }

    console.warn(`${SHARE_LOG_PREFIX} clipboard non disponibile, fallback solo console`);
    showShareMessage('Condivisione non disponibile su questo browser.');
}

async function shareEngine({ engineCode, imageUrls }) {
    const safeCode = (engineCode || '').trim() || '-';
    const shareTitle = 'Condivisione motore';
    const shareText = `Motore: ${safeCode}`;
    const pageUrl = window.location.href;

    console.info(`${SHARE_LOG_PREFIX} share avviato`, {
        engineCode: safeCode,
        rawImages: imageUrls,
        hasNavigatorShare: typeof navigator.share === 'function',
        hasNavigatorCanShare: typeof navigator.canShare === 'function',
        isSecureContext: window.isSecureContext
    });

    const normalizedUrls = normalizeImageUrls(imageUrls);
    console.info(`${SHARE_LOG_PREFIX} URL normalizzati`, { count: normalizedUrls.length, normalizedUrls });

    if (typeof navigator.share !== 'function') {
        console.warn(`${SHARE_LOG_PREFIX} navigator.share non disponibile, uso fallback clipboard`);
        await copyFallbackText(`${shareText}\nLink: ${pageUrl}`);
        return;
    }

    const files = await buildShareFiles(normalizedUrls);
    console.info(`${SHARE_LOG_PREFIX} file validi pronti`, { count: files.length });

    if (!SHARE_FILE_SHARING_ENABLED) {
        console.warn(`${SHARE_LOG_PREFIX} file sharing disabilitato in configurazione: uso fallback text+url`);
    } else if (files.length > 0 && typeof navigator.canShare === 'function') {
        let canShareSingleFile = false;
        let canShareMultipleFiles = false;
        try {
            canShareSingleFile = navigator.canShare({ files: [files[0]] });
        } catch (error) {
            console.warn(`${SHARE_LOG_PREFIX} navigator.canShare({files:[single]}) errore`, error);
        }
        try {
            canShareMultipleFiles = files.length > 1 ? navigator.canShare({ files }) : canShareSingleFile;
        } catch (error) {
            console.warn(`${SHARE_LOG_PREFIX} navigator.canShare({files:[multi]}) errore`, error);
        }
        console.info(`${SHARE_LOG_PREFIX} compatibilità canShare`, {
            canShareSingleFile,
            canShareMultipleFiles,
            filesCount: files.length
        });

        let filesForShare = files;
        if (!SHARE_MULTIPLE_FILES_ENABLED && files.length > 1) {
            filesForShare = [files[0]];
            console.warn(`${SHARE_LOG_PREFIX} Fallback: condivisione singola immagine per compatibilità dispositivo`, {
                requestedFiles: files.length,
                sharedFiles: filesForShare.length
            });
        }

        let canShareMultiFiles = false;
        try {
            canShareMultiFiles = navigator.canShare({ files: filesForShare });
            console.info(`${SHARE_LOG_PREFIX} navigator.canShare({files})`, {
                result: canShareMultiFiles,
                filesCount: filesForShare.length
            });
        } catch (error) {
            console.warn(`${SHARE_LOG_PREFIX} navigator.canShare ha lanciato errore`, error);
        }

        if (canShareMultiFiles) {
            try {
                const filesPayload = {
                    title: shareTitle,
                    text: shareText,
                    files: filesForShare
                };
                console.info(`${SHARE_LOG_PREFIX} payload navigator.share files`, {
                    title: filesPayload.title,
                    text: filesPayload.text,
                    filesCount: filesPayload.files.length,
                    files: filesPayload.files.map((f) => ({ name: f.name, type: f.type, size: f.size }))
                });
                await navigator.share(filesPayload);
                console.info(`${SHARE_LOG_PREFIX} share con file completata`, { files: filesForShare.length });
                return;
            } catch (error) {
                if (error && error.name === 'AbortError') {
                    console.info(`${SHARE_LOG_PREFIX} condivisione annullata dall'utente (file)`);
                    return;
                }
                console.error(`${SHARE_LOG_PREFIX} errore share con file`, {
                    name: error?.name,
                    message: error?.message,
                    stack: error?.stack
                });
            }
        } else {
            console.warn(`${SHARE_LOG_PREFIX} file share non supportato, uso fallback text+url`);
        }
    } else if (SHARE_FILE_SHARING_ENABLED && files.length > 0) {
        console.warn(`${SHARE_LOG_PREFIX} navigator.canShare non disponibile, uso fallback text+url`);
    } else if (SHARE_FILE_SHARING_ENABLED) {
        console.warn(`${SHARE_LOG_PREFIX} nessun file immagine valido, uso fallback text+url`);
    }

    try {
        const fallbackPayload = {
            title: shareTitle,
            text: shareText,
            url: pageUrl
        };
        console.info(`${SHARE_LOG_PREFIX} payload navigator.share fallback`, fallbackPayload);
        await navigator.share(fallbackPayload);
        console.info(`${SHARE_LOG_PREFIX} fallback share text+url completata`);
    } catch (error) {
        if (error && error.name === 'AbortError') {
            console.info(`${SHARE_LOG_PREFIX} condivisione annullata dall'utente (fallback)`);
            return;
        }
        console.error(`${SHARE_LOG_PREFIX} errore fallback text+url`, {
            name: error?.name,
            message: error?.message,
            stack: error?.stack
        });
        await copyFallbackText(`${shareText}\nLink: ${pageUrl}`);
    }
}

function initializeShareDelegation() {
    if (shareDelegationInitialized) {
        return;
    }
    shareDelegationInitialized = true;

    document.addEventListener('click', async (event) => {
        const trigger = event.target.closest('.js-engine-share-btn');
        if (!trigger) {
            return;
        }

        event.preventDefault();
        const engineCode = trigger.getAttribute('data-engine-code') || (config.engineRef || '');
        const imageUrls = readImageUrlsFromButton(trigger);

        console.info(`${SHARE_LOG_PREFIX} share button cliccato`, {
            triggerClass: trigger.className,
            engineCode,
            imagesFound: imageUrls.length,
            imageUrlsRaw: imageUrls
        });

        await shareEngine({ engineCode, imageUrls });
    });
}

if (!galleryRoot || clickableImages.length === 0) {
    // Nothing to initialize on pages without images.
} else {
    const dataSource = clickableImages.map((item) => {
        const src = item.getAttribute('data-image-url');
        const filename = item.getAttribute('data-filename') || 'Immagine motore';
        return {
            src,
            width: 1,
            height: 1,
            alt: filename,
            element: item
        };
    });
    const dimensionCache = new Map();

    function loadSlideDimensions(index) {
        if (index < 0 || index >= dataSource.length) {
            return Promise.resolve();
        }
        if (dimensionCache.has(index)) {
            return dimensionCache.get(index);
        }

        const item = dataSource[index];
        const promise = new Promise((resolve) => {
            const probe = new Image();
            probe.onload = () => {
                const width = probe.naturalWidth || item.width || 1;
                const height = probe.naturalHeight || item.height || 1;
                item.width = width;
                item.height = height;
                resolve();
            };
            probe.onerror = () => {
                resolve();
            };
            probe.src = item.src;
        });

        dimensionCache.set(index, promise);
        return promise;
    }

    const lightbox = new PhotoSwipeLightbox({
        dataSource,
        pswpModule: () => import('https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe.esm.js'),
        bgOpacity: 0.95,
        showHideAnimationType: 'zoom',
        wheelToZoom: true,
        pinchToClose: false,
        zoom: false,
        arrowPrev: true,
        arrowNext: true,
        maxZoomLevel: 4,
        secondaryZoomLevel: 2
    });

    lightbox.on('open', () => {
        if (navbar) {
            navbar.classList.add('navbar-hidden');
        }
    });

    lightbox.on('close', () => {
        if (navbar) {
            navbar.classList.remove('navbar-hidden');
        }
    });

    lightbox.on('uiRegister', () => {
        lightbox.pswp.ui.registerElement({
            name: 'custom-share',
            order: 8,
            isButton: true,
            tagName: 'button',
            className: 'pswp__button pswp__button--custom-share js-engine-share-btn',
            html: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>',
            title: 'Condividi immagine',
            onInit: (element) => {
                element.setAttribute('data-engine-code', config.engineRef || '');
                element.setAttribute('data-share-source', '#engineDetailGallery');
                element.setAttribute('data-image-selector', '.clickable-image');
            }
        });
    });

    lightbox.init();
    void Promise.allSettled(dataSource.map((_item, index) => loadSlideDimensions(index)));

    clickableImages.forEach((item) => {
        const openCurrent = async () => {
            const index = Number(item.getAttribute('data-index') || '0');
            await loadSlideDimensions(index);
            lightbox.loadAndOpen(index);
            void loadSlideDimensions(index - 1);
            void loadSlideDimensions(index + 1);
        };

        item.addEventListener('click', openCurrent);
        item.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                openCurrent();
            }
        });
    });
}

initializeShareDelegation();
