import PhotoSwipeLightbox from 'https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe-lightbox.esm.js';

const config = window.engineDetailViewerConfig || {};
const galleryRoot = document.getElementById('engineDetailGallery');
const navbar = document.querySelector('.navbar');
const fab = document.querySelector('.fab-mobile-center');
const clickableImages = Array.from(document.querySelectorAll('.clickable-image'));
const technicalShareButton = document.getElementById('engineTechnicalShareBtn');

function showShareMessage(message) {
    alert(message);
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

function toHttpsUrl(rawUrl) {
    const url = new URL(rawUrl, window.location.origin);
    if (url.protocol === 'http:') {
        url.protocol = 'https:';
    }
    return url.href;
}

async function tryBuildShareFile(imageUrl) {
    try {
        const response = await fetch(imageUrl, { credentials: 'include' });
        if (!response.ok) {
            return null;
        }
        const blob = await response.blob();
        if (!blob || blob.size === 0) {
            return null;
        }
        const fileName = getFileNameFromUrl(imageUrl);
        return new File([blob], fileName, { type: blob.type || 'image/jpeg' });
    } catch (_error) {
        return null;
    }
}

async function shareFilesWithWebApi(files, text, unsupportedMessage, unsupportedPayloadMessage) {
    if (!navigator.share || !navigator.canShare) {
        showShareMessage(unsupportedMessage);
        return false;
    }
    const shareData = text ? { files, text } : { files };
    if (!navigator.canShare(shareData)) {
        showShareMessage(unsupportedPayloadMessage);
        return false;
    }
    await navigator.share(shareData);
    return true;
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
            onClick: async (_event, _el, pswp) => {
                const current = pswp.currSlide;
                const imageUrl = current && current.data ? current.data.src : null;
                if (!imageUrl) {
                    showShareMessage('Immagine non disponibile');
                    return;
                }

                const resolvedImageUrl = new URL(imageUrl, window.location.origin).href;
                try {
                    const imageFile = await tryBuildShareFile(resolvedImageUrl);
                    if (!imageFile) {
                        showShareMessage('Impossibile condividere l\'immagine');
                        return;
                    }
                    await shareFilesWithWebApi(
                        [imageFile],
                        '',
                        'La condivisione non è supportata su questo dispositivo',
                        'Impossibile condividere l\'immagine'
                    );
                } catch (error) {
                    if (error && error.name !== 'AbortError') {
                        showShareMessage('Impossibile condividere l\'immagine');
                    }
                }
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
        const engineCode = (technicalShareButton.getAttribute('data-engine-code') || '').trim();
        const engineStatus = (technicalShareButton.getAttribute('data-engine-status') || '').trim();
        const deliveryDate = (technicalShareButton.getAttribute('data-delivery-date') || '').trim();
        const imageUrls = clickableImages
            .map((item) => item.getAttribute('data-image-url'))
            .filter((url) => !!url)
            .map((url) => toHttpsUrl(url));

        if (imageUrls.length === 0) {
            showShareMessage('Impossibile condividere: nessuna immagine disponibile');
            return;
        }

        const textLines = [`Codice motore: ${engineCode || '-'}`];
        if (engineStatus === 'DELIVERED' && deliveryDate) {
            textLines.push('', `Consegnato il: ${deliveryDate}`);
        }
        const shareText = textLines.join('\n');

        try {
            const androidBridge = window.AndroidShareBridge;
            if (androidBridge && androidBridge.shareTechnicalSheet) {
                androidBridge.shareTechnicalSheet(JSON.stringify(imageUrls), shareText);
                return;
            }

            if (!navigator.share || !navigator.canShare) {
                showShareMessage('La condivisione non è supportata su questo dispositivo');
                return;
            }

            const files = await Promise.all(imageUrls.map((url) => tryBuildShareFile(url)));
            if (files.some((file) => !file)) {
                showShareMessage('Impossibile condividere l\'immagine');
                return;
            }
            await shareFilesWithWebApi(
                files.filter((file) => !!file),
                shareText,
                'La condivisione non è supportata su questo dispositivo',
                'La condivisione multipla non è supportata su questo dispositivo'
            );
        } catch (error) {
            if (error && error.name !== 'AbortError') {
                showShareMessage('Impossibile condividere l\'immagine');
            }
        }
    });
}
