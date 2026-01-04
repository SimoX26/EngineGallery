<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Engine Gallery • Gestione Motori</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <!-- Google Font -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap"
          rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f5f6f7;
            color: #212529;
        }

        /* =====================
           HERO
           ===================== */
        .hero {
            background: linear-gradient(
                    rgba(0, 0, 0, 0.6),
                    rgba(0, 0, 0, 0.6)
            ),
            url("<%= request.getContextPath() %>/assets/img/engine-hero.jpg");
            background-size: cover;
            background-position: center;
            min-height: 90vh;
            display: flex;
            align-items: center;
        }

        .hero h1 {
            font-size: 3rem;
            font-weight: 700;
        }

        .hero p {
            font-size: 1.2rem;
            max-width: 600px;
        }

        /* =====================
           SECTIONS
           ===================== */
        section {
            padding: 80px 0;
        }

        .section-title {
            font-weight: 700;
            margin-bottom: 40px;
        }

        /* =====================
           FEATURES
           ===================== */
        .feature-card {
            background: #ffffff;
            border-radius: 10px;
            padding: 30px;
            height: 100%;
            box-shadow: 0 10px 30px rgba(0,0,0,0.08);
        }

        .feature-card h5 {
            font-weight: 600;
        }

        /* =====================
           CTA
           ===================== */
        .cta {
            background-color: #1f2933;
            color: #ffffff;
        }

        .btn-engine {
            background-color: #2563eb;
            border: none;
        }

        .btn-engine:hover {
            background-color: #1d4ed8;
        }
    </style>
</head>

<body>

<!-- =====================
     HERO SECTION
     ===================== -->
<section class="hero text-white">
    <div class="container">
        <h1>Engine Gallery</h1>
        <p class="mt-3">
            Il sistema professionale per la gestione, catalogazione
            e consultazione dei motori.
        </p>
        <div class="mt-4">
            <a href="<%= request.getContextPath() %>/login"
               class="btn btn-engine btn-lg me-3">
                Accedi al sistema
            </a>
            <a href="#features" class="btn btn-outline-light btn-lg">
                Scopri di più
            </a>
        </div>
    </div>
</section>

<!-- =====================
     PROBLEMA
     ===================== -->
<section>
    <div class="container">
        <div class="row">
            <div class="col-md-8">
                <h2 class="section-title">
                    Un magazzino pieno di motori non è un sistema.
                </h2>
                <p>
                    Senza una struttura chiara, la gestione dei motori diventa
                    complessa: codici duplicati, clienti confusi,
                    difficoltà nel recuperare informazioni tecniche.
                </p>
                <p>
                    Engine Gallery nasce per risolvere questi problemi,
                    offrendo un ambiente ordinato, affidabile e progettato
                    per contesti industriali.
                </p>
            </div>
        </div>
    </div>
</section>

<!-- =====================
     FEATURES
     ===================== -->
<section id="features" class="bg-light">
    <div class="container">
        <h2 class="section-title text-center">Funzionalità principali</h2>

        <div class="row g-4">
            <div class="col-md-4">
                <div class="feature-card">
                    <h5>Catalogazione per cliente</h5>
                    <p>
                        Ogni motore è associato al proprio cliente,
                        per una gestione chiara e senza ambiguità.
                    </p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="feature-card">
                    <h5>Codice motore univoco</h5>
                    <p>
                        Identificazione precisa di ogni unità
                        tramite codice motore dedicato.
                    </p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="feature-card">
                    <h5>Galleria fotografica</h5>
                    <p>
                        Archivia immagini tecniche per ogni motore
                        e consultale rapidamente.
                    </p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="feature-card">
                    <h5>Ricerca rapida</h5>
                    <p>
                        Trova immediatamente motori, clienti
                        o codici tramite ricerca avanzata.
                    </p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="feature-card">
                    <h5>Storico e tracciabilità</h5>
                    <p>
                        Mantieni uno storico completo delle modifiche
                        e delle operazioni effettuate.
                    </p>
                </div>
            </div>

            <div class="col-md-4">
                <div class="feature-card">
                    <h5>Accesso multi-dispositivo</h5>
                    <p>
                        Utilizza il sistema da desktop, tablet
                        o smartphone senza limitazioni.
                    </p>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- =====================
     CTA
     ===================== -->
<section class="cta text-center">
    <div class="container">
        <h2 class="section-title">
            Tutto sotto controllo. In un unico posto.
        </h2>
        <p class="mb-4">
            Engine Gallery è progettato per ambienti tecnici,
            dove precisione e ordine fanno la differenza.
        </p>
        <a href="<%= request.getContextPath() %>/login"
           class="btn btn-engine btn-lg">
            Entra in Engine Gallery
        </a>
    </div>
</section>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
