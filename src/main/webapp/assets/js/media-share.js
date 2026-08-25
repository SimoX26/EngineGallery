(() => {
    const maxFileSize = 100 * 1024 * 1024;

    const urlsFor = (button) => {
        const root = document.querySelector(button.dataset.shareSource || '');
        return root ? Array.from(root.querySelectorAll('[data-media-url]')).map((node) => node.dataset.mediaUrl).filter(Boolean) : [];
    };

    document.addEventListener('click', async (event) => {
        const button = event.target.closest('.js-media-share-btn');
        if (!button) return;
        event.preventDefault();
        const files = [];
        for (const url of urlsFor(button)) {
            try {
                const response = await fetch(url, {credentials: 'include'});
                const blob = await response.blob();
                if (response.ok && blob.size > 0 && blob.size <= maxFileSize) {
                    files.push(new File([blob], url.split('/').pop() || 'media', {type: blob.type}));
                }
            } catch (_error) {
                // Prosegue con gli altri media disponibili.
            }
        }
        if (!files.length || !navigator.share || (navigator.canShare && !navigator.canShare({files}))) {
            alert('Nessun media condivisibile disponibile su questo browser/dispositivo.');
            return;
        }
        try {
            await navigator.share({files});
        } catch (error) {
            if (error?.name !== 'AbortError') alert('Impossibile condividere il media selezionato.');
        }
    });
})();
