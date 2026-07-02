<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <link rel="apple-touch-icon" sizes="180x180" href="${pageContext.request.contextPath}/assets/img/apple-touch-icon.png">
    <title>Engine Gallery • Rubrica Clienti</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=12">
</head>

<body>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="engine-gallery-page">

<!-- NAVBAR (sempre fuori dal container) -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="container">

    <div class="page-header-with-search">
        <!-- HEADER -->
        <div class="page-header">
            <h1>Rubrica Clienti</h1>
        </div>

        <div class="search-panel-compact">
            <label for="customerKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
            <input type="search"
                   id="customerKeywordSearch"
                   class="form-control"
                   placeholder="cerca...">
            <div id="customerKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
                Nessun cliente corrisponde alla ricerca.
            </div>
        </div>
    </div>

    <!-- ERROR -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
            ${error}
        </div>
    </c:if>

    <!-- LISTA CLIENTI -->
    <div class="customer-list" id="customerListGrid">

        <c:forEach var="customer" items="${customers}">
            <a href="<%= request.getContextPath() %>/customer/detail?id=${customer.id}"
               class="customer-card-link customer-card-item"
               data-customer-id="${customer.id}"
               data-customer-name="${fn:escapeXml(customer.name)}"
               data-customer-company="${fn:escapeXml(customer.companyName)}"
               data-customer-phone="${fn:escapeXml(customer.phone)}"
               data-customer-email="${fn:escapeXml(customer.email)}"
               data-customer-notes="${fn:escapeXml(customer.notes)}"
               data-search="${fn:escapeXml(customer.name)} ${fn:escapeXml(customer.phone)} ${fn:escapeXml(customer.companyName)} ${fn:escapeXml(customer.email)} ${fn:escapeXml(customer.notes)}">
                <div class="card-base customer-card customer-directory-card">

                    <div class="customer-row">

                        <div class="customer-field customer-field--name">
                            <div class="customer-main">
                                ${customer.name}
                            </div>
                        </div>

                        <div class="customer-field">
                            <div class="customer-meta">Telefono</div>
                            <div><c:out value="${customer.phone}" default="—" /></div>
                        </div>

                        <div class="customer-field">
                            <div class="customer-meta">Email</div>
                            <div><c:out value="${customer.email}" default="—" /></div>
                        </div>

                    </div>
                </div>
            </a>

        </c:forEach>

    </div>

</div>

</div>

<div class="modal fade" id="customerDetailOverlay" tabindex="-1" aria-labelledby="customerDetailOverlayLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-semibold" id="customerDetailOverlayLabel">Dettaglio cliente</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Chiudi"></button>
            </div>
            <div class="modal-body">
                <dl class="engine-detail-list mb-0">
                    <dt>Nome:</dt>
                    <dd id="customerOverlayName">—</dd>

                    <dt>Azienda:</dt>
                    <dd id="customerOverlayCompany">—</dd>

                    <dt>Telefono:</dt>
                    <dd id="customerOverlayPhone">—</dd>

                    <dt>Email:</dt>
                    <dd id="customerOverlayEmail">—</dd>

                    <dt>Note:</dt>
                    <dd id="customerOverlayNotes">—</dd>
                </dl>
            </div>
            <div class="modal-footer justify-content-center justify-content-md-end gap-2">
                <a id="customerOverlayEditLink" href="#" class="btn btn-detail-edit px-4">Modifica</a>
                <a id="customerOverlayDeleteLink" href="#" class="btn btn-detail-delete px-4">Elimina</a>
                <a id="customerOverlayDetailLink" href="#" class="btn btn-outline-secondary px-4">Apri pagina</a>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/live-search.js"></script>
<script>
    const searchInput = document.getElementById('customerKeywordSearch');
    const customerCards = document.querySelectorAll('.customer-card-item');
    const emptyState = document.getElementById('customerKeywordEmptyState');
    const customerDetailOverlayEl = document.getElementById('customerDetailOverlay');
    const customerDetailOverlay = bootstrap.Modal.getOrCreateInstance(customerDetailOverlayEl);
    const customerOverlayName = document.getElementById('customerOverlayName');
    const customerOverlayCompany = document.getElementById('customerOverlayCompany');
    const customerOverlayPhone = document.getElementById('customerOverlayPhone');
    const customerOverlayEmail = document.getElementById('customerOverlayEmail');
    const customerOverlayNotes = document.getElementById('customerOverlayNotes');
    const customerOverlayEditLink = document.getElementById('customerOverlayEditLink');
    const customerOverlayDeleteLink = document.getElementById('customerOverlayDeleteLink');
    const customerOverlayDetailLink = document.getElementById('customerOverlayDetailLink');
    const contextPath = '<%= request.getContextPath() %>';

    const filterController = window.EngineGalleryLiveSearch && searchInput
        ? window.EngineGalleryLiveSearch.init({
            input: searchInput,
            groups: [{ elements: customerCards }],
            emptyState,
            debounceMs: 180
        })
        : null;

    const withFallback = (value) => {
        const normalized = (value || '').trim();
        return normalized.length > 0 ? normalized : '—';
    };

    customerCards.forEach((card) => {
        card.addEventListener('click', (event) => {
            event.preventDefault();

            const customerId = card.dataset.customerId;
            if (!customerId) {
                return;
            }

            customerOverlayName.textContent = withFallback(card.dataset.customerName);
            customerOverlayCompany.textContent = withFallback(card.dataset.customerCompany);
            customerOverlayPhone.textContent = withFallback(card.dataset.customerPhone);
            customerOverlayEmail.textContent = withFallback(card.dataset.customerEmail);
            customerOverlayNotes.textContent = withFallback(card.dataset.customerNotes);

            customerOverlayEditLink.href = `${contextPath}/customer/edit?id=${customerId}`;
            customerOverlayDeleteLink.href = `${contextPath}/customer/delete?id=${customerId}`;
            customerOverlayDetailLink.href = `${contextPath}/customer/detail?id=${customerId}`;

            customerDetailOverlay.show();
        });
    });
    if (filterController) {
        filterController.apply();
    }
</script>

</body>
</html>
