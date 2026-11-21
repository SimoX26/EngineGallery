<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseDepartment">Reparto</label>
    <select id="warehouseDepartment" class="form-select" name="department" required>
        <option value="">Seleziona reparto</option>
        <option value="Alimentazione" ${department == 'Alimentazione' ? 'selected' : ''}>Alimentazione</option>
        <option value="Motori" ${department == 'Motori' ? 'selected' : ''}>Motori</option>
        <option value="Distribuzione" ${department == 'Distribuzione' ? 'selected' : ''}>Distribuzione</option>
        <option value="Altri ricambi" ${department == 'Altri ricambi' ? 'selected' : ''}>Altri ricambi</option>
    </select>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseCategory">Categoria</label>
    <select id="warehouseCategory" class="form-select" name="categoryId" required>
        <option value="">Seleziona categoria</option>
        <c:forEach var="category" items="${categories}">
            <option value="${category.id}" data-department="<c:out value='${category.department}'/>" data-injector="${category.injectorFields}" ${details.categoryId == category.id ? 'selected' : ''}><c:out value="${category.name}"/></option>
        </c:forEach>
    </select>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseManufacturer">Produttore</label>
    <input id="warehouseManufacturer" class="form-control" name="manufacturer" maxlength="120" value="<c:out value='${details.manufacturer}'/>" required>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseOeCode">Codice originale OE</label>
    <input id="warehouseOeCode" class="form-control" name="oeCode" maxlength="120" value="<c:out value='${details.oeCode}'/>" required>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseAlternativeCodes">Codici alternativi</label>
    <textarea id="warehouseAlternativeCodes" class="form-control" name="alternativeCodes" rows="2" required><c:out value="${details.alternativeCodes}"/></textarea>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseCondition">Condizione del componente</label>
    <input id="warehouseCondition" class="form-control" name="condition" maxlength="80" value="<c:out value='${details.condition}'/>" required>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseLocation">Ubicazione</label>
    <input id="warehouseLocation" class="form-control" name="location" maxlength="120" value="<c:out value='${location}'/>" required>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseLotCode">Lotto di provenienza</label>
    <input id="warehouseLotCode" class="form-control" name="lotCode" maxlength="120" value="<c:out value='${details.lotCode}'/>" required>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseSourceEngine">Codice motore di provenienza</label>
    <input id="warehouseSourceEngine" class="form-control" name="sourceEngineCode" maxlength="120" value="<c:out value='${details.sourceEngineCode}'/>" required>
</div>
<div class="mb-3 text-start">
    <label class="form-label fw-semibold" for="warehouseTechnicalNotes">Note tecniche</label>
    <textarea id="warehouseTechnicalNotes" class="form-control" name="technicalNotes" rows="3" required><c:out value="${details.technicalNotes}"/></textarea>
</div>
<fieldset class="mb-3 text-start" id="warehouseInjectorFields" data-injector-fields>
    <legend class="fs-6 fw-semibold">Dati iniettore</legend>
    <div class="mb-2"><label class="form-label" for="warehouseManufacturerCode">Codice produttore</label><input id="warehouseManufacturerCode" class="form-control" name="manufacturerCode" maxlength="120" value="<c:out value='${details.manufacturerCode}'/>"></div>
    <div class="mb-2"><label class="form-label" for="warehouseCalibrationCode">Codice calibrazione</label><input id="warehouseCalibrationCode" class="form-control" name="calibrationCode" maxlength="120" value="<c:out value='${details.calibrationCode}'/>"></div>
    <div class="mb-2"><label class="form-label" for="warehouseSourceCylinder">Cilindro di provenienza</label><input id="warehouseSourceCylinder" class="form-control" name="sourceCylinder" maxlength="40" value="<c:out value='${details.sourceCylinder}'/>"></div>
    <div class="mb-2"><label class="form-label" for="warehouseTestStatus">Stato del collaudo</label><input id="warehouseTestStatus" class="form-control" name="testStatus" maxlength="80" value="<c:out value='${details.testStatus}'/>"></div>
    <div class="mb-2"><label class="form-label" for="warehouseRevisionStatus">Stato della revisione</label><input id="warehouseRevisionStatus" class="form-control" name="revisionStatus" maxlength="80" value="<c:out value='${details.revisionStatus}'/>"></div>
</fieldset>
<fieldset class="mb-3 text-start">
    <legend class="fs-6 fw-semibold">Motori compatibili</legend>
    <div id="warehouseCompatibilityRows">
        <c:choose>
            <c:when test="${not empty details.compatibilities}">
                <c:forEach var="compatibility" items="${details.compatibilities}" varStatus="status">
                    <div class="d-flex flex-wrap gap-2 align-items-center mb-2" data-compatibility-row>
                        <input class="form-control flex-grow-1" style="width: 12rem" name="engineCode" aria-label="Codice motore compatibile" maxlength="120" value="<c:out value='${compatibility.engineCode}'/>" required>
                        <label class="small text-nowrap"><input type="checkbox" name="verifiedIndex" value="${status.index}" ${compatibility.verified ? 'checked' : ''}> Verificata</label>
                        <button type="button" class="btn btn-outline-secondary btn-sm" data-remove-compatibility aria-label="Rimuovi compatibilità">×</button>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="d-flex flex-wrap gap-2 align-items-center mb-2" data-compatibility-row>
                    <input class="form-control flex-grow-1" style="width: 12rem" name="engineCode" aria-label="Codice motore compatibile" maxlength="120" required>
                    <label class="small text-nowrap"><input type="checkbox" name="verifiedIndex" value="0"> Verificata</label>
                    <button type="button" class="btn btn-outline-secondary btn-sm" data-remove-compatibility aria-label="Rimuovi compatibilità">×</button>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    <button type="button" class="btn btn-outline-secondary btn-sm" id="warehouseAddCompatibility">Aggiungi motore</button>
    <div class="form-text">Se non è ancora confermata, lascia deselezionato “Verificata”.</div>
</fieldset>
<script src="${pageContext.request.contextPath}/assets/js/warehouse-form.js"></script>
