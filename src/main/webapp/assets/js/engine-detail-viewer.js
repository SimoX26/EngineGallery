import PhotoSwipeLightbox from 'https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe-lightbox.esm.js';

const galleryRoot = document.getElementById('engineDetailGallery');
const navbar = document.querySelector('.navbar');
const clickableImages = Array.from(document.querySelectorAll('.clickable-image'));

const SHARE_LOG_PREFIX = '[engine-share-v2]';
let shareDelegationInitialized = false;
const SHARE_MAX_SINGLE_FILE_BYTES = 8 * 1024 * 1024;
const SHARE_MODE_CURRENT = 'current-image-only';
const SHARE_MODE_ALL = 'all-motor-images';
let activeLightboxIndex = 0;

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
            if (blob.size > SHARE_MAX_SINGLE_FILE_BYTES) {
                console.warn(`${SHARE_LOG_PREFIX} file troppo grande, salto immagine`, {
                    imageUrl,
                    size: blob.size,
                    maxSize: SHARE_MAX_SINGLE_FILE_BYTES
                });
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

function isFileShareSupported(files) {
    if (typeof navigator.share !== 'function') {
        return false;
    }
    if (typeof navigator.canShare !== 'function') {
        return true;
    }
    try {
        return navigator.canShare({ files });
    } catch (_error) {
        return false;
    }
}

async function shareFilesOnly(files, errorMessage) {
    if (!files || files.length === 0) {
        showShareMessage(errorMessage);
        return;
    }
    if (!isFileShareSupported(files)) {
        showShareMessage('Condivisione immagini non supportata su questo browser/dispositivo.');
        return;
    }
    try {
        await navigator.share({ files });
    } catch (error) {
        if (error && error.name === 'AbortError') {
            console.info(`${SHARE_LOG_PREFIX} condivisione annullata dall'utente`);
            return;
        }
        console.warn(`${SHARE_LOG_PREFIX} errore condivisione file`, {
            name: error?.name,
            message: error?.message,
            stack: error?.stack
        });
        showShareMessage(errorMessage);
    }
}

function readCurrentLightboxImageUrl() {
    if (!galleryRoot || !clickableImages.length) {
        return '';
    }
    const safeIndex = Math.max(0, Math.min(activeLightboxIndex, clickableImages.length - 1));
    const node = clickableImages[safeIndex];
    return (node?.getAttribute('data-image-url') || node?.getAttribute('src') || '').trim();
}

async function shareCurrentImageOnly() {
    const currentUrl = readCurrentLightboxImageUrl();
    const normalizedUrls = normalizeImageUrls(currentUrl ? [currentUrl] : []);
    const files = await buildShareFiles(normalizedUrls);
    await shareFilesOnly(files, 'Immagine corrente non condivisibile.');
}

async function shareAllMotorImages(imageUrls) {
    const normalizedUrls = normalizeImageUrls(imageUrls);

    if (window.AndroidShareBridge && typeof window.AndroidShareBridge.shareTechnicalSheet === 'function') {
        try {
            window.AndroidShareBridge.shareTechnicalSheet(JSON.stringify(normalizedUrls), '');
            console.info(`${SHARE_LOG_PREFIX} share all images via Android bridge`);
            return;
        } catch (error) {
            console.warn(`${SHARE_LOG_PREFIX} Android bridge shareTechnicalSheet errore`, error);
        }
    }

    const files = await buildShareFiles(normalizedUrls);
    await shareFilesOnly(files, 'Nessuna immagine condivisibile disponibile.');
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
        const shareMode = trigger.getAttribute('data-share-mode') || SHARE_MODE_ALL;
        const imageUrls = readImageUrlsFromButton(trigger);

        console.info(`${SHARE_LOG_PREFIX} share button cliccato`, {
            triggerClass: trigger.className,
            shareMode,
            imagesFound: imageUrls.length,
            imageUrlsRaw: imageUrls
        });

        if (shareMode === SHARE_MODE_CURRENT) {
            await shareCurrentImageOnly();
            return;
        }
        await shareAllMotorImages(imageUrls);
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
        activeLightboxIndex = Number(lightbox.pswp?.currIndex || 0);
    });

    lightbox.on('close', () => {
        if (navbar) {
            navbar.classList.remove('navbar-hidden');
        }
    });

    lightbox.on('change', () => {
        activeLightboxIndex = Number(lightbox.pswp?.currIndex || 0);
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
                element.setAttribute('data-share-mode', SHARE_MODE_CURRENT);
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
