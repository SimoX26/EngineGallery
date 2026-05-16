import PhotoSwipeLightbox from 'https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe-lightbox.esm.js';

const config = window.engineDetailViewerConfig || {};
const galleryRoot = document.getElementById('engineDetailGallery');
const navbar = document.querySelector('.navbar');
const fab = document.querySelector('.fab-mobile-center');
const clickableImages = Array.from(document.querySelectorAll('.clickable-image'));
const technicalShareButton = document.getElementById('engineTechnicalShareBtn');
const engineRefLabel = document.querySelector('.engine-ref-value');
const shareFileCache = new Map();
const SHARE_LOG_PREFIX = '[engine-share]';

function showShareMessage(message) {
    alert(message);
}

function buildShareFallbackUrl(shareText) {
    const subject = encodeURIComponent('Condivisione motore');
    const body = encodeURIComponent(`${shareText}\n${window.location.href}`);
    return `mailto:?subject=${subject}&body=${body}`;
}

function getFileNameFromUrl(url, fallback = 'immagine-motore.jpg') {
    try {
        const pathname = new URL(url).pathname;
        const name = pathname.substring(pathname.lastIndexOf('/') + 1);
        return name || fallback;
    } catch (_error) {
        return fallback;
    }
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
    if (normalized.includes('jpg') || normalized.includes('jpeg')) {
        return 'jpg';
    }
    return 'jpg';
}

async function tryBuildShareFile(imageUrl) {
    if (!imageUrl) {
        return null;
    }
    if (shareFileCache.has(imageUrl)) {
        return shareFileCache.get(imageUrl);
    }

    const promise = (async () => {
    try {
        console.info(`${SHARE_LOG_PREFIX} fetch immagine`, imageUrl);
        const response = await fetch(imageUrl, { credentials: 'include' });
        if (!response.ok) {
            console.warn(`${SHARE_LOG_PREFIX} fetch non ok`, { url: imageUrl, status: response.status });
            return null;
        }
        const blob = await response.blob();
        if (!blob || blob.size === 0) {
            console.warn(`${SHARE_LOG_PREFIX} blob vuoto`, imageUrl);
            return null;
        }
        const mimeType = blob.type || 'image/jpeg';
        const baseName = getFileNameFromUrl(imageUrl, 'immagine-motore');
        const hasExtension = /\.[a-zA-Z0-9]+$/.test(baseName);
        const fileName = hasExtension ? baseName : `${baseName}.${getExtensionFromMimeType(mimeType)}`;
        console.info(`${SHARE_LOG_PREFIX} file pronto`, { url: imageUrl, mimeType, fileName, size: blob.size });
        return new File([blob], fileName, { type: mimeType });
    } catch (error) {
        console.error(`${SHARE_LOG_PREFIX} errore fetch immagine`, imageUrl, error);
        return null;
    }
    })();

    shareFileCache.set(imageUrl, promise);
    return promise;
}

function buildEngineShareText() {
    const engineCode = (technicalShareButton && technicalShareButton.getAttribute('data-engine-code') || '').trim();
    const engineStatus = (technicalShareButton && technicalShareButton.getAttribute('data-engine-status') || '').trim();
    const deliveryDate = (technicalShareButton && technicalShareButton.getAttribute('data-delivery-date') || '').trim();
    const engineReference = engineCode || (engineRefLabel && engineRefLabel.textContent || '').trim() || '-';

    const textLines = [`Codice motore: ${engineReference}`];
    if (engineStatus === 'DELIVERED' && deliveryDate) {
        textLines.push('', `Consegnato il: ${deliveryDate}`);
    }
    return textLines.join('\n');
}

function getEngineImageUrls() {
    return clickableImages
        .map((item) => item.getAttribute('data-image-url'))
        .filter((url) => !!url)
        .map((url) => new URL(url, window.location.origin).href);
}

function canShareFiles(shareData) {
    if (!navigator.share || !navigator.canShare) {
        return false;
    }
    try {
        const result = navigator.canShare(shareData);
        console.info(`${SHARE_LOG_PREFIX} navigator.canShare(files)`, result);
        return result;
    } catch (error) {
        console.warn(`${SHARE_LOG_PREFIX} navigator.canShare ha lanciato errore`, error);
        return false;
    }
}

async function shareTextAndLink(shareText) {
    if (!navigator.share) {
        console.warn(`${SHARE_LOG_PREFIX} navigator.share non disponibile, uso fallback mailto`);
        window.location.href = buildShareFallbackUrl(shareText);
        return;
    }
    const shareData = {
        text: shareText,
        url: window.location.href
    };
    console.info(`${SHARE_LOG_PREFIX} fallback share text+url`);
    await navigator.share(shareData);
}

async function shareCurrentEngine() {
    const imageUrls = getEngineImageUrls();
    const shareText = buildEngineShareText();
    console.info(`${SHARE_LOG_PREFIX} click share ricevuto`, {
        imagesFound: imageUrls.length,
        imageUrls,
        secureContext: window.isSecureContext,
        hasNavigatorShare: !!navigator.share,
        hasNavigatorCanShare: !!navigator.canShare
    });

    try {
        const androidBridge = window.AndroidShareBridge;
        if (androidBridge && androidBridge.shareTechnicalSheet) {
            console.info(`${SHARE_LOG_PREFIX} uso AndroidShareBridge`);
            androidBridge.shareTechnicalSheet(JSON.stringify(imageUrls), shareText);
            return;
        }

        const files = await Promise.all(imageUrls.map((url) => tryBuildShareFile(url)));
        const validFiles = files.filter((file) => !!file);
        console.info(`${SHARE_LOG_PREFIX} file validi`, { validFiles: validFiles.length, requested: imageUrls.length });
        if (imageUrls.length === 0) {
            console.warn(`${SHARE_LOG_PREFIX} nessuna immagine associata: uso fallback testo+url`);
        } else if (validFiles.length !== imageUrls.length) {
            console.warn(`${SHARE_LOG_PREFIX} alcune immagini non valide: uso fallback testo+url`);
        }
        const shareDataWithFiles = {
            files: validFiles,
            text: shareText
        };
        const canUseFileShare = validFiles.length === imageUrls.length
            && validFiles.length > 0
            && canShareFiles(shareDataWithFiles);

        if (canUseFileShare) {
            console.info(`${SHARE_LOG_PREFIX} avvio share con file multipli`, { files: validFiles.length });
            await navigator.share(shareDataWithFiles);
            return;
        }

        await shareTextAndLink(shareText);
    } catch (error) {
        if (error && error.name === 'AbortError') {
            console.info(`${SHARE_LOG_PREFIX} condivisione annullata dall'utente`);
            return;
        }
        if (error) {
            console.error(`${SHARE_LOG_PREFIX} errore durante la condivisione del motore`, error);
            showShareMessage('Impossibile condividere il motore');
        }
    }
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
            className: 'pswp__button pswp__button--custom-share',
            html: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>',
            title: 'Condividi immagine',
            onClick: async () => {
                await shareCurrentEngine();
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

if (technicalShareButton) {
    technicalShareButton.addEventListener('click', async () => {
        await shareCurrentEngine();
    });
}

if (galleryRoot && clickableImages.length > 0) {
    const urls = getEngineImageUrls();
    console.info(`${SHARE_LOG_PREFIX} preload cache share avviato`, { images: urls.length });
    void Promise.allSettled(urls.map((url) => tryBuildShareFile(url)));
}
