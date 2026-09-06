# Engine Gallery

## Panoramica

Engine Gallery e una web application pensata per organizzare le attivita di officina legate a motori, clienti, contenuti tecnici e articoli di magazzino in un unico ambiente operativo. Nel repository e presente anche un wrapper Android che rende disponibile la stessa esperienza in mobilita.

L'applicazione e adatta a contesti in cui ogni motore attraversa diverse fasi di lavorazione e deve rimanere collegato a cliente, immagini, note e materiali di supporto.

## A Cosa Serve

Engine Gallery riunisce informazioni che spesso sono distribuite tra cartelle, messaggi e liste separate. Aiuta a:

- tracciare i motori e il loro stato di avanzamento;
- collegare ogni lavorazione al cliente corretto;
- raccogliere immagini e contenuti tecnici in modo ordinato;
- mantenere uno storico consultabile delle attivita;
- monitorare articoli di magazzino e relativa documentazione visiva;
- consultare indicatori sintetici e attivita recenti da una dashboard centrale.

## Utenti

L'applicazione e rivolta a personale operativo e amministrativo che deve gestire il lavoro quotidiano di officina e i materiali collegati.

Dal repository emergono due ruoli utente chiaramente riconoscibili:

- `OPERATOR`, per l'uso operativo standard;
- `ADMIN`, con accesso a sezioni aggiuntive come statistiche, manutenzione e archivio motori.

## Sezioni Principali

## Dashboard

La home fornisce una panoramica operativa dell'attivita corrente. Include indicatori sintetici, collegamenti rapidi alle sezioni principali e informazioni recenti su carico di lavoro e attivita.

## Clienti

L'area clienti supporta la gestione delle anagrafiche e delle informazioni di contatto o descrittive. I dati cliente sono collegati ai motori e aiutano a organizzare il lavoro per commessa o referente.

## Motori

E il nucleo dell'applicazione. I motori possono essere elencati, consultati nel dettaglio, aggiornati e seguiti lungo i diversi stati di lavorazione. Il flusso comprende anche una vista dedicata ai motori pronti e, per gli amministratori, una vista di archivio.

Ogni motore puo essere associato a cliente, note, codici identificativi e immagini.

## Prove Idrauliche

L'applicazione include una sezione dedicata alle prove idrauliche, con flussi di elenco, dettaglio e inserimento. In quest'area vengono raccolte informazioni di test e relativi contenuti multimediali.

## Magazzino

La sezione magazzino gestisce articoli, quantita, ubicazioni, note e immagini collegate. Supporta sia la consultazione operativa sia l'aggiornamento delle informazioni di giacenza.

## Altre Aree

La navigazione comprende anche:

- una sezione impostazioni per preferenze di utilizzo;
- una sezione manutenzione riservata agli amministratori;
- una sezione catalogo consultabile e una sezione placeholder per pronta consegna, gia presenti nella struttura applicativa.

## Flusso di Utilizzo

Dal codice emerge un modello d'uso centrato su accesso autenticato e navigazione per aree funzionali.

Un flusso tipico prevede:

1. accesso all'applicazione;
2. consultazione della dashboard per avere una vista d'insieme;
3. ingresso nelle aree clienti, motori, prove idrauliche o magazzino;
4. creazione o aggiornamento dei record;
5. associazione di immagini o contenuti multimediali dove necessario;
6. consultazione di dettagli, stato di avanzamento e storico operativo.

## Interfaccia

L'interfaccia e composta da pagine server-rendered con navigazione chiara tra le diverse sezioni. Dalle view implementate risultano direttamente verificabili queste caratteristiche:

- navigazione responsive con gestione dedicata anche in mobilita;
- dashboard con indicatori e accessi rapidi;
- schermate di elenco, dettaglio, inserimento, modifica e conferma eliminazione per le entita principali;
- supporto al caricamento di immagini e altri contenuti multimediali;
- preferenze di tema chiaro, scuro o automatico;
- disponibilita di un wrapper Android per l'utilizzo da smartphone.

## Tecnologie

Engine Gallery e realizzata come applicazione web Java basata su Servlet e JSP, con packaging Maven e interfaccia sviluppata con HTML, CSS, JavaScript, Bootstrap e JSTL.  
Accanto all'applicazione web principale, il repository include un wrapper Android basato su WebView.

## Documentazione tecnica

- [Architettura e funzionamento](docs/ARCHITETTURA_E_FUNZIONAMENTO.md): componenti, flussi applicativi, dati, upload, configurazione e struttura del repository.
- [Applicazione Android](android-app/README.md): configurazione e build del wrapper mobile.

La documentazione descrive lo stato del codice sorgente e non sostituisce un penetration test dell'ambiente distribuito.

## Stato Del Progetto

Dal repository emerge un'applicazione strutturata e in evoluzione, con piu aree funzionali gia implementate, una navigazione coerente tra le sezioni e una presenza sia web sia mobile.
