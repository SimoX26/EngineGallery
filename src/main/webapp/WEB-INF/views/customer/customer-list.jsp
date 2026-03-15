<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/ico/ICONA.png">
    <title>Engine Gallery • Rubrica Clienti</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>

<body>

<!-- NAVBAR (sempre fuori dal container) -->
<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<!-- FAB -->
<jsp:include page="/WEB-INF/views/includes/FAB.jsp"/>

<div class="container">

    <!-- HEADER -->
    <div class="page-header">
        <h1>Rubrica Clienti</h1>
        <p>Elenco completo dei clienti</p>
    </div>

    <!-- ERROR -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger">
            ${error}
        </div>
    </c:if>

    <div class="card-base mb-4">
        <label for="customerKeywordSearch" class="form-label fw-semibold mb-2">Ricerca per parola chiave</label>
        <input type="search"
               id="customerKeywordSearch"
               class="form-control"
               placeholder="Cerca per nome, telefono, azienda, email o note...">
        <div id="customerKeywordEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
            Nessun cliente corrisponde alla ricerca.
        </div>
    </div>

    <!-- LISTA CLIENTI -->
    <div class="customer-list" id="customerListGrid">

        <c:forEach var="customer" items="${customers}">
            <a href="<%= request.getContextPath() %>/customer/detail?id=${customer.id}"
               class="customer-card-link customer-card-item"
               data-search="${fn:escapeXml(customer.name)} ${fn:escapeXml(customer.phone)} ${fn:escapeXml(customer.companyName)} ${fn:escapeXml(customer.email)} ${fn:escapeXml(customer.notes)}">
                <div class="card-base customer-card">

                    <div class="customer-row">

                        <div class="customer-field">
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

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const searchInput = document.getElementById('customerKeywordSearch');
    const customerCards = document.querySelectorAll('.customer-card-item');
    const emptyState = document.getElementById('customerKeywordEmptyState');

    const normalizeText = (value) => (value || '')
        .toString()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();

    const applyCustomerFilter = () => {
        const keyword = normalizeText(searchInput.value);
        let visibleCount = 0;

        customerCards.forEach((card) => {
            const haystack = normalizeText(card.dataset.search);
            const isVisible = keyword.length === 0 || haystack.includes(keyword);
            card.classList.toggle('d-none', !isVisible);
            if (isVisible) {
                visibleCount += 1;
            }
        });

        emptyState.classList.toggle('d-none', visibleCount > 0);
    };

    searchInput.addEventListener('input', applyCustomerFilter);
</script>

</body>
</html>
