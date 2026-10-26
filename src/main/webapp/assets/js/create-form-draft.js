(() => {
    const form = document.querySelector('form[data-draft-key]');
    if (!form || !form.dataset.draftKey) return;

    const key = ['create-form-v1', form.dataset.draftUser, form.dataset.draftKey].join(':');
    const fields = Array.from(form.elements).filter((element) =>
        element.name && !element.closest('[data-draft-dynamic]')
        && !['hidden', 'file', 'submit', 'button'].includes(element.type));
    const fileInputs = Array.from(form.querySelectorAll('input[type="file"][name]'));
    const submitButton = form.querySelector('[type="submit"]');
    const notice = document.createElement('div');
    notice.className = 'alert d-none text-start';
    notice.setAttribute('role', 'status');
    notice.setAttribute('aria-live', 'polite');
    form.prepend(notice);

    function show(message, type, canDiscard = false) {
        notice.className = 'alert alert-' + type + ' text-start';
        notice.textContent = message;
        if (canDiscard) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'btn btn-sm btn-outline-secondary ms-2';
            button.textContent = 'Elimina bozza';
            button.addEventListener('click', async () => {
                try {
                    clearTimeout(fieldTimer);
                    await pending;
                    await Promise.all([remove('fields'), remove('files')]);
                    window.location.reload();
                } catch (error) {
                    show('Impossibile eliminare la bozza da questo dispositivo.', 'warning');
                }
            });
            notice.appendChild(button);
        }
    }

    function database() {
        return new Promise((resolve, reject) => {
            if (!window.indexedDB) return reject(new Error('IndexedDB unavailable'));
            const request = indexedDB.open('engine-gallery-drafts', 1);
            request.onupgradeneeded = () => {
                const db = request.result;
                db.createObjectStore('fields');
                db.createObjectStore('files');
            };
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    let dbPromise = database();
    function operation(store, method, value) {
        return dbPromise.then((db) => new Promise((resolve, reject) => {
            const transaction = db.transaction(store, 'readwrite');
            const request = method === 'put'
                ? transaction.objectStore(store).put(value, key)
                : transaction.objectStore(store)[method](key);
            transaction.oncomplete = () => resolve(request.result);
            transaction.onabort = () => reject(transaction.error);
            transaction.onerror = () => reject(transaction.error);
        }));
    }
    const read = (store) => operation(store, 'get');
    const remove = (store) => operation(store, 'delete');
    const put = (store, value) => operation(store, 'put', value);

    function fieldValues() {
        return fields.map((element) => element.type === 'checkbox' || element.type === 'radio'
            ? element.checked : element.value);
    }

    let submitAttempted = false;
    function saveFields() {
        return put('fields', {values: fieldValues(), attempted: submitAttempted, savedAt: Date.now()});
    }

    function saveFiles() {
        const entries = fileInputs.map((input) => ({
            files: Array.from(input.files || []).map((file) => ({
                blob: file, name: file.name, type: file.type, lastModified: file.lastModified
            }))
        }));
        return put('files', entries);
    }

    let fieldTimer;
    let pending = Promise.resolve();
    let draftSaveFailed = false;
    function queue(work) {
        pending = pending.then(work).then(() => {
            draftSaveFailed = false;
        }).catch(() => {
            draftSaveFailed = true;
            show('La bozza non è stata salvata sul dispositivo. Controlla lo spazio disponibile prima di uscire dal modulo.', 'warning');
        });
        return pending;
    }

    function scheduleFields() {
        clearTimeout(fieldTimer);
        fieldTimer = setTimeout(() => queue(() => saveFields()), 350);
    }

    function queueFiles() {
        queue(async () => {
            await saveFiles();
            show('File salvati nella bozza su questo dispositivo.', 'success', true);
        });
    }

    function offerDownloads(savedFiles) {
        (savedFiles || []).forEach((entry) => (entry.files || []).forEach((file) => {
            const link = document.createElement('a');
            link.className = 'd-block mt-2';
            link.href = URL.createObjectURL(file.blob);
            link.download = file.name;
            link.textContent = 'Scarica ' + file.name;
            notice.appendChild(link);
        }));
    }

    async function restore() {
        let savedFiles;
        try {
            const savedFields = await read('fields');
            savedFiles = await read('files');
            if (!savedFields && !savedFiles) return;
            submitAttempted = !!savedFields?.attempted;
            if (savedFields && savedFields.values.length === fields.length) {
                fields.forEach((element, index) => {
                    if (element.type === 'checkbox' || element.type === 'radio') {
                        element.checked = savedFields.values[index];
                    } else {
                        element.value = savedFields.values[index];
                    }
                });
                fields.forEach((element) => {
                    if (element.tagName === 'SELECT') element.dispatchEvent(new Event('change'));
                });
                form.dispatchEvent(new Event('draftrestored'));
            }
            if (savedFiles && typeof DataTransfer !== 'undefined') {
                fileInputs.forEach((input, index) => {
                    const transfer = new DataTransfer();
                    (savedFiles[index]?.files || []).forEach((entry) => {
                        transfer.items.add(new File([entry.blob], entry.name, {
                            type: entry.type, lastModified: entry.lastModified
                        }));
                    });
                    input.files = transfer.files;
                    input.dispatchEvent(new Event('change'));
                });
            } else if (savedFiles && savedFiles.some((entry) => entry.files.length)) {
                show('Il browser non può ripristinare automaticamente i file. Scaricali e selezionali di nuovo prima di salvare.', 'warning');
                offerDownloads(savedFiles);
                return;
            }
            show(savedFields?.attempted
                ? 'Bozza ripristinata. Se avevi già premuto Salva, verifica che il record non sia stato creato prima di riprovare.'
                : 'Bozza ripristinata da questo dispositivo, inclusi i file selezionati.', 'info', true);
        } catch (error) {
            show('Il ripristino della bozza non è disponibile su questo dispositivo.', 'warning');
            offerDownloads(savedFiles);
        }
    }

    let submitting = false;
    restore().then(() => {
        form.addEventListener('input', (event) => {
            if (event.target.type !== 'file') scheduleFields();
        });
        form.addEventListener('change', (event) => {
            if (event.target.type === 'file') queueFiles();
            else scheduleFields();
        });
        form.addEventListener('draftchange', queueFiles);
        form.addEventListener('submit', async (event) => {
            if (event.defaultPrevented) return;
            event.preventDefault();
            if (submitting) return;
            submitting = true;
            let navigating = false;
            const enabledControls = Array.from(form.elements).filter((element) => !element.disabled);
            clearTimeout(fieldTimer);
            if (submitButton) submitButton.disabled = true;
            let backedUp = true;
            try {
                await pending;
                if (draftSaveFailed) throw new Error('Draft save failed');
                submitAttempted = true;
                await saveFields();
                await saveFiles();
            } catch (error) {
                backedUp = false;
            }
            show(backedUp
                ? 'Salvataggio in corso. Mantieni aperta questa pagina.'
                : 'Bozza non disponibile su questo dispositivo. Mantieni aperta questa pagina fino alla fine del salvataggio.',
            backedUp ? 'info' : 'warning');
            try {
                const body = new FormData(form);
                enabledControls.forEach((element) => { element.disabled = true; });
                const response = await fetch(form.action, {
                    method: 'POST', body, credentials: 'same-origin'
                });
                const target = new URL(response.url);
                const success = new URL(form.dataset.draftSuccess, window.location.href);
                const successfulRedirect = response.ok && response.redirected
                    && target.origin === success.origin && target.pathname === success.pathname
                    && (!form.dataset.draftSuccessParam
                        || target.searchParams.get(form.dataset.draftSuccessParam) === '1');
                if (successfulRedirect) {
                    try {
                        await Promise.all([remove('fields'), remove('files')]);
                    } catch (error) {
                        // Il record è stato salvato: prosegui anche se il browser non cancella la bozza.
                    }
                    navigating = true;
                    window.location.assign(response.url);
                    return;
                }
                const html = await response.text();
                const returnedPage = new DOMParser().parseFromString(html, 'text/html');
                const errorMessage = returnedPage.querySelector('.alert-danger')?.textContent?.trim();
                show((errorMessage || 'Salvataggio non riuscito.')
                    + ' Verifica che il record non sia stato creato prima di riprovare. La bozza resta disponibile su questo dispositivo.', 'danger', true);
            } catch (error) {
                show('Connessione interrotta o esito incerto. '
                    + (backedUp ? 'La bozza è salvata sul dispositivo. ' : 'La bozza non è disponibile sul dispositivo. ')
                    + 'Verifica che il record non sia già stato creato prima di riprovare.', 'warning', backedUp);
            } finally {
                if (!navigating) {
                    submitting = false;
                    enabledControls.forEach((element) => { element.disabled = false; });
                }
            }
        });
    });
})();
