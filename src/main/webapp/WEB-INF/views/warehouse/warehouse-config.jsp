<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<details id="warehouse-config" class="card-base mt-4 mb-4">
    <summary class="fw-semibold">Configura categorie e ubicazioni</summary>
    <c:if test="${not empty catalogMessage}"><div class="alert alert-success mt-3"><c:out value="${catalogMessage}"/></div></c:if>
    <c:if test="${not empty catalogError}"><div class="alert alert-danger mt-3"><c:out value="${catalogError}"/></div></c:if>
    <div class="row g-4 mt-1">
        <section class="col-12 col-lg-6" aria-labelledby="warehouse-categories-title">
            <h2 class="h5" id="warehouse-categories-title">Categorie</h2>
            <form method="post" action="${pageContext.request.contextPath}/warehouse/catalog" class="row g-2 align-items-end mb-3">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                <input type="hidden" name="action" value="addCategory">
                <div class="col-12 col-sm-5"><label class="form-label small" for="newCategoryDepartment">Reparto</label><select id="newCategoryDepartment" class="form-select" name="department" required>
                    <option value="Alimentazione">Alimentazione</option><option value="Motori">Motori</option><option value="Distribuzione">Distribuzione</option><option value="Altri ricambi">Altri ricambi</option>
                </select></div>
                <div class="col-8 col-sm-5"><label class="form-label small" for="newCategoryName">Nome categoria</label><input id="newCategoryName" class="form-control" name="name" maxlength="100" required></div>
                <div class="col-4 col-sm-2"><button class="btn btn-outline-secondary w-100" type="submit">Aggiungi</button></div>
                <div class="col-12"><label class="small"><input type="checkbox" name="injectorFields"> Mostra i campi specifici degli iniettori</label></div>
            </form>
            <div class="d-grid gap-2">
                <c:forEach var="category" items="${categories}">
                    <div class="border rounded p-2">
                        <div class="small text-muted"><c:out value="${category.department}"/></div>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <form method="post" action="${pageContext.request.contextPath}/warehouse/catalog" class="d-flex flex-grow-1 gap-2">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                <input type="hidden" name="action" value="renameCategory">
                                <input type="hidden" name="id" value="${category.id}">
                                <input class="form-control form-control-sm" name="name" aria-label="Nome categoria" maxlength="100" value="<c:out value='${category.name}'/>" required>
                                <button class="btn btn-outline-secondary btn-sm" type="submit">Salva</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/warehouse/catalog">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                <input type="hidden" name="action" value="deleteCategory">
                                <input type="hidden" name="id" value="${category.id}">
                                <button class="btn btn-outline-secondary btn-sm" type="submit" onclick="return confirm('Eliminare questa categoria?')">Elimina</button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </section>
        <section class="col-12 col-lg-6" aria-labelledby="warehouse-locations-title">
            <h2 class="h5" id="warehouse-locations-title">Ubicazioni</h2>
            <form method="post" action="${pageContext.request.contextPath}/warehouse/catalog" class="row g-2 align-items-end mb-3">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                <input type="hidden" name="action" value="addLocation">
                <div class="col-8 col-sm-9"><label class="form-label small" for="newLocation">Ubicazione</label><input id="newLocation" class="form-control" name="code" maxlength="120" placeholder="Es. A-02-03" required></div>
                <div class="col-4 col-sm-3"><button class="btn btn-outline-secondary w-100" type="submit">Aggiungi</button></div>
            </form>
            <c:if test="${empty locations}"><p class="small text-muted">Nessuna ubicazione configurata.</p></c:if>
            <div class="d-grid gap-2">
                <c:forEach var="configuredLocation" items="${locations}">
                    <div class="border rounded p-2 d-flex justify-content-between align-items-center gap-2">
                        <span><c:out value="${configuredLocation.code}"/></span>
                        <form method="post" action="${pageContext.request.contextPath}/warehouse/catalog">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                            <input type="hidden" name="action" value="deleteLocation">
                            <input type="hidden" name="code" value="<c:out value='${configuredLocation.code}'/>">
                            <button class="btn btn-outline-secondary btn-sm" type="submit" onclick="return confirm('Eliminare questa ubicazione?')">Elimina</button>
                        </form>
                    </div>
                </c:forEach>
            </div>
        </section>
    </div>
</details>
