<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Modifica articolo magazzino</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body data-back-guard-form="1"
      data-back-guard-fallback="<%= request.getContextPath() %>/warehouse/detail?id=${itemId}">

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container-fluid d-flex align-items-center justify-content-center"
     style="min-height: calc(100vh - 70px);">

    <div class="card-base text-center" style="max-width: 560px; width: 100%;">

        <h2 class="mb-3">Modifica articolo</h2>

        <p class="text-muted mb-4">
            Aggiorna i dati dell'articolo selezionato
        </p>

        <c:if test="${not empty error}">
            <div class="alert alert-danger text-start" role="alert">
                ${error}
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/warehouse/edit?id=${itemId}"
              method="post"
              enctype="multipart/form-data"
              class="form-click-guides">

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Immagini articolo</label>
                <div class="engine-images-edit-list mb-3" data-image-list>
                    <c:choose>
                        <c:when test="${not empty itemImages}">
                            <c:forEach var="image" items="${itemImages}">
                                <div class="engine-image-edit-card" data-image-card>
                                    <button type="button"
                                            class="engine-image-delete-btn"
                                            data-delete-image
                                            data-filename="${fn:escapeXml(image.filename)}"
                                            title="Elimina immagine"
                                            aria-label="Elimina immagine">X</button>
                                    <img src="<%= request.getContextPath() %>/uploads/warehouse/${itemId}/${image.filename}"
                                         alt="Immagine articolo"
                                         class="engine-image-edit-preview">
                                    <div class="small text-muted mt-1 text-truncate">
                                        <c:out value="${image.filename}" />
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted small">Nessuna immagine presente.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div data-deletions-container></div>
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Aggiungi nuove immagini</label>
                <div class="file-input-wrap">
                    <input type="file"
                           id="newImagesInput"
                           name="newImages"
                           class="file-input-native"
                           accept="image/*"
                           multiple>
                    <label for="newImagesInput" class="file-input-visual file-input-visual-label mb-0">
                        Seleziona immagini
                    </label>
                </div>
                <div id="newImagesPreviewList" class="engine-images-edit-list mt-2 d-none"></div>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Nome articolo</label>
                <input type="text"
                       name="name"
                       class="form-control"
                       value="${name}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Codice articolo</label>
                <input type="text"
                       name="sku"
                       class="form-control"
                       value="${sku}">
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Quantita</label>
                <input type="number"
                       name="quantity"
                       min="0"
                       class="form-control"
                       value="${quantity}"
                       required>
            </div>

            <div class="mb-3 text-start">
                <label class="form-label fw-semibold">Ubicazione</label>
                <input type="text"
                       name="location"
                       class="form-control"
                       value="${location}">
            </div>

            <div class="mb-4 text-start">
                <label class="form-label fw-semibold">Note</label>
                <textarea name="notes"
                          class="form-control"
                          rows="3">${notes}</textarea>
            </div>

            <div class="d-flex gap-2">
                <a href="<%= request.getContextPath() %>/warehouse/detail?id=${itemId}" class="btn btn-outline-secondary w-50">
                    Annulla
                </a>
                <button type="submit" class="btn-engine w-50">
                    Salva modifiche
                </button>
            </div>

        </form>

    </div>
</div>

<script>
    (() => {
        const list = document.querySelector('[data-image-list]');
        const deletionsContainer = document.querySelector('[data-deletions-container]');
        if (!list || !deletionsContainer) {
            return;
        }

        const addDeletionInput = (filename) => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'deleteFilenames';
            input.value = filename;
            deletionsContainer.appendChild(input);
        };

        list.addEventListener('click', function (event) {
            const button = event.target.closest('[data-delete-image]');
            if (!button) {
                return;
            }

            const filename = button.getAttribute('data-filename');
            if (!filename) {
                return;
            }

            const confirmed = window.confirm('Sei sicuro di voler eliminare questa immagine?');
            if (!confirmed) {
                return;
            }

            addDeletionInput(filename);

            const card = button.closest('[data-image-card]');
            if (card) {
                card.remove();
            }
        });
    })();

    (() => {
        const input = document.getElementById('newImagesInput');
        const previewList = document.getElementById('newImagesPreviewList');
        if (!input || !previewList) {
            return;
        }

        const renderPreview = () => {
            previewList.innerHTML = '';
            const files = input.files ? Array.from(input.files) : [];
            const hasImages = files.some((file) => file.type && file.type.startsWith('image/'));
            if (!hasImages) {
                previewList.classList.add('d-none');
                return;
            }

            files.forEach((file, index) => {
                if (!file.type || !file.type.startsWith('image/')) {
                    return;
                }

                const card = document.createElement('div');
                card.className = 'engine-image-edit-card';

                const removeButton = document.createElement('button');
                removeButton.type = 'button';
                removeButton.className = 'engine-image-delete-btn';
                removeButton.textContent = 'X';
                removeButton.setAttribute('aria-label', 'Rimuovi immagine selezionata');
                removeButton.title = 'Rimuovi';
                removeButton.addEventListener('click', function () {
                    const currentFiles = input.files ? Array.from(input.files) : [];
                    const transfer = new DataTransfer();
                    currentFiles.forEach((currentFile, currentIndex) => {
                        if (currentIndex !== index) {
                            transfer.items.add(currentFile);
                        }
                    });
                    input.files = transfer.files;
                    renderPreview();
                });

                const img = document.createElement('img');
                img.className = 'engine-image-edit-preview';
                img.alt = file.name;
                const objectUrl = URL.createObjectURL(file);
                img.src = objectUrl;
                img.addEventListener('load', () => URL.revokeObjectURL(objectUrl));

                const filename = document.createElement('div');
                filename.className = 'small text-muted mt-1 text-truncate';
                filename.textContent = file.name;

                card.appendChild(removeButton);
                card.appendChild(img);
                card.appendChild(filename);
                previewList.appendChild(card);
            });

            previewList.classList.remove('d-none');
        };

        input.addEventListener('change', renderPreview);
    })();
</script>

</body>
</html>
