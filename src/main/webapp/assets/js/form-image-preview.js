(() => {
    const previewSelector = 'form img.engine-image-edit-preview, form img.workshop-form-preview, '
        + '#iosCollectorPreviewList img.engine-image-edit-preview';

    function init() {
        if (!document.querySelector('form input[type="file"][accept*="image"]')) return;

        const modal = document.createElement('div');
        modal.className = 'form-image-modal';
        modal.hidden = true;
        modal.innerHTML = `
            <div class="form-image-modal__panel" role="dialog" aria-modal="true" aria-labelledby="formImageModalTitle">
                <div class="form-image-modal__header">
                    <strong id="formImageModalTitle">Anteprima</strong>
                    <button type="button" class="form-image-modal__close" aria-label="Chiudi anteprima" data-preview-close>×</button>
                </div>
                <div class="form-image-modal__body">
                    <img class="form-image-modal__image" alt="">
                </div>
                <div class="form-image-modal__footer">
                    <button type="button" class="form-image-modal__delete" data-preview-delete>Elimina</button>
                </div>
            </div>`;
        document.body.appendChild(modal);

        const largeImage = modal.querySelector('.form-image-modal__image');
        const closeButton = modal.querySelector('[data-preview-close]');
        const deleteButton = modal.querySelector('[data-preview-delete]');
        let activeImage = null;
        let deleteAction = null;

        function prepareImage(image) {
            if (!image.matches(previewSelector)) return;
            image.tabIndex = 0;
            image.setAttribute('role', 'button');
            image.setAttribute('aria-label', 'Apri anteprima: ' + (image.alt || 'immagine'));
        }

        function visitImages(node, callback) {
            if (node.nodeType !== Node.ELEMENT_NODE) return;
            if (node.matches('img')) callback(node);
            node.querySelectorAll('img').forEach(callback);
        }

        function removeActionFor(image) {
            const button = image.closest('.engine-image-edit-card')?.querySelector('.engine-image-delete-btn');
            if (button) return () => button.click();
            const removeField = image.dataset.previewRemoveField
                ? image.closest('form')?.elements.namedItem(image.dataset.previewRemoveField) : null;
            if (removeField) return () => {
                removeField.value = '1';
                image.classList.add('d-none');
                const message = image.dataset.previewRemovedMessage
                    ? document.getElementById(image.dataset.previewRemovedMessage) : null;
                if (message) message.classList.remove('d-none');
            };
            const input = image.dataset.previewInput
                ? document.getElementById(image.dataset.previewInput) : null;
            if (!input) return null;
            return () => {
                input.value = '';
                input.dispatchEvent(new Event('change', {bubbles: true}));
            };
        }

        function close() {
            modal.hidden = true;
            largeImage.removeAttribute('src');
            document.documentElement.classList.remove('form-image-preview-open');
            if (activeImage?.isConnected) activeImage.focus({preventScroll: true});
            activeImage = null;
            deleteAction = null;
        }

        function open(image) {
            if (!image.src || image.closest('.d-none')) return;
            activeImage = image;
            deleteAction = removeActionFor(image);
            largeImage.src = image.currentSrc || image.src;
            largeImage.alt = image.alt || 'Immagine selezionata';
            deleteButton.hidden = !deleteAction;
            modal.hidden = false;
            document.documentElement.classList.add('form-image-preview-open');
            closeButton.focus();
        }

        document.querySelectorAll(previewSelector).forEach(prepareImage);
        document.documentElement.classList.add('form-image-preview-enabled');
        document.addEventListener('click', (event) => {
            const image = event.target.closest(previewSelector);
            if (image) open(image);
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && !modal.hidden) {
                close();
                return;
            }
            if ((event.key === 'Enter' || event.key === ' ') && event.target.matches(previewSelector)) {
                event.preventDefault();
                open(event.target);
            }
        });
        modal.addEventListener('click', (event) => {
            if (event.target === modal || event.target.closest('[data-preview-close]')) {
                close();
            } else if (event.target.closest('[data-preview-delete]') && deleteAction) {
                const action = deleteAction;
                close();
                action();
            }
        });

        const observer = new MutationObserver((changes) => {
            changes.forEach((change) => {
                change.removedNodes.forEach((node) => visitImages(node, (image) => {
                    if (image.dataset.previewObjectUrl) URL.revokeObjectURL(image.dataset.previewObjectUrl);
                }));
                change.addedNodes.forEach((node) => visitImages(node, prepareImage));
            });
        });
        observer.observe(document.body, {childList: true, subtree: true});
        window.addEventListener('pagehide', () => {
            document.querySelectorAll('img[data-preview-object-url]').forEach((image) => {
                URL.revokeObjectURL(image.dataset.previewObjectUrl);
            });
        });
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init, {once: true});
    else init();
})();
