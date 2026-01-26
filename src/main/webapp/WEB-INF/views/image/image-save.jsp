<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Dettagli immagine</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Font Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Stile globale -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<!-- Navbar -->
<jsp:include page="/WEB-INF/views/partials/navbar.jsp"/>

<div class="container my-5">

    <div class="row justify-content-center">
        <div class="col-lg-8">

            <div class="card-base">

                <!-- Titolo -->
                <div class="page-header">
                    <h1>Dettagli immagine</h1>
                    <p>Completa le informazioni prima di salvare l’immagine nella galleria</p>
                </div>

                <div class="row g-4">

                    <!-- PREVIEW IMMAGINE -->
                    <div class="col-md-5 text-center">
                        <div class="mb-3">
                            <img src="<%= request.getContextPath() %>/temp_uploads/${tempImageName}"
                                 alt="Anteprima immagine"
                                 class="img-fluid rounded shadow-sm">
                        </div>
                        <p class="text-muted small">
                            File originale: <strong>${originalFileName}</strong>
                        </p>
                    </div>

                    <!-- FORM DETTAGLI -->
                    <div class="col-md-7">

                        <form action="<%= request.getContextPath() %>/image/save" method="post">

                            <!-- Hidden fields -->
                            <input type="hidden" name="tempImageName" value="${tempImageName}">
                            <input type="hidden" name="originalFileName" value="${originalFileName}">

                            <!-- Codice motore -->
                            <div class="mb-3">
                                <label class="form-label">Codice motore</label>
                                <input type="text"
                                       name="engineCode"
                                       class="form-control"
                                       placeholder="Es. ENG-00123"
                                       required>
                            </div>

                            <!-- Cartella -->
                            <div class="mb-3">
                                <label class="form-label">Cartella di destinazione</label>
                                <select name="folderId" class="form-select" required>
                                    <option value="">Seleziona cartella</option>
                                    <!-- popolata dinamicamente -->
                                    <option value="1">Motori / Ingresso</option>
                                    <option value="2">Motori / Lavorazione</option>
                                    <option value="3">Motori / Consegnati</option>
                                </select>
                            </div>

                            <!-- Stato lavorazione -->
                            <div class="mb-3">
                                <label class="form-label">Stato motore</label>
                                <select name="engineStatus" class="form-select">
                                    <option value="STOCCATO">Stoccato</option>
                                    <option value="LAVORAZIONE">In lavorazione</option>
                                    <option value="CONSEGNATO">Consegnato</option>
                                </select>
                            </div>

                            <!-- Descrizione -->
                            <div class="mb-4">
                                <label class="form-label">Descrizione (opzionale)</label>
                                <textarea name="description"
                                          class="form-control"
                                          rows="3"
                                          placeholder="Note o dettagli sull'immagine"></textarea>
                            </div>

                            <!-- Azioni -->
                            <div class="d-flex justify-content-between">
                                <a href="<%= request.getContextPath() %>/image/cancel?file=${tempImageName}"
                                   class="btn btn-outline-secondary">
                                    Annulla
                                </a>

                                <button type="submit" class="btn btn-engine">
                                    Salva immagine
                                </button>
                            </div>

                        </form>

                    </div>
                </div>

            </div>

        </div>
    </div>

</div>

</body>
</html>
