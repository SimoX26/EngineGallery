import PhotoSwipeLightbox from 'https://cdn.jsdelivr.net/npm/photoswipe@5.4.4/dist/photoswipe-lightbox.esm.js';

const config = window.engineDetailViewerConfig || {};
const galleryRoot = document.getElementById('engineDetailGallery');
const navbar = document.querySelector('.navbar');
const fab = document.querySelector('.fab-mobile-center');
const clickableImages = Array.from(document.querySelectorAll('.clickable-image'));

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
                    alert('Immagine non disponibile');
                    return;
                }

                const resolvedImageUrl = new URL(imageUrl, window.location.origin).href;
                const shareData = {
                    title: 'RML • Engine Gallery',
                    text: 'Guarda questa immagine del motore: ' + (config.engineRef || ''),
                    url: resolvedImageUrl
                };

                try {
                    if (navigator.share) {
                        await navigator.share(shareData);
                        return;
                    }

                    if (navigator.clipboard && window.isSecureContext) {
                        await navigator.clipboard.writeText(resolvedImageUrl);
                        alert('Link immagine copiato negli appunti');
                        return;
                    }

                    alert('Condivisione non supportata su questo dispositivo o contesto.');
                } catch (error) {
                    if (error && error.name !== 'AbortError') {
                        alert('Errore durante la condivisione: ' + error.message);
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
