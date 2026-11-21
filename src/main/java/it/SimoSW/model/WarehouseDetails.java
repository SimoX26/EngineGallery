package it.SimoSW.model;

import java.util.List;

public class WarehouseDetails {
    private final Long categoryId;
    private final String manufacturer;
    private final String oeCode;
    private final String alternativeCodes;
    private final String condition;
    private final String lotCode;
    private final String sourceEngineCode;
    private final String technicalNotes;
    private final String manufacturerCode;
    private final String calibrationCode;
    private final String sourceCylinder;
    private final String testStatus;
    private final String revisionStatus;
    private final List<WarehouseCompatibility> compatibilities;
    private String department;
    private String categoryName;
    private boolean injectorCategory;

    public WarehouseDetails(Long categoryId, String manufacturer, String oeCode, String alternativeCodes,
                            String condition, String lotCode, String sourceEngineCode, String technicalNotes,
                            String manufacturerCode, String calibrationCode, String sourceCylinder,
                            String testStatus, String revisionStatus, List<WarehouseCompatibility> compatibilities) {
        this.categoryId = categoryId;
        this.manufacturer = manufacturer;
        this.oeCode = oeCode;
        this.alternativeCodes = alternativeCodes;
        this.condition = condition;
        this.lotCode = lotCode;
        this.sourceEngineCode = sourceEngineCode;
        this.technicalNotes = technicalNotes;
        this.manufacturerCode = manufacturerCode;
        this.calibrationCode = calibrationCode;
        this.sourceCylinder = sourceCylinder;
        this.testStatus = testStatus;
        this.revisionStatus = revisionStatus;
        this.compatibilities = compatibilities == null ? List.of() : List.copyOf(compatibilities);
    }

    public Long getCategoryId() { return categoryId; }
    public String getManufacturer() { return manufacturer; }
    public String getOeCode() { return oeCode; }
    public String getAlternativeCodes() { return alternativeCodes; }
    public String getCondition() { return condition; }
    public String getLotCode() { return lotCode; }
    public String getSourceEngineCode() { return sourceEngineCode; }
    public String getTechnicalNotes() { return technicalNotes; }
    public String getManufacturerCode() { return manufacturerCode; }
    public String getCalibrationCode() { return calibrationCode; }
    public String getSourceCylinder() { return sourceCylinder; }
    public String getTestStatus() { return testStatus; }
    public String getRevisionStatus() { return revisionStatus; }
    public List<WarehouseCompatibility> getCompatibilities() { return compatibilities; }
    public String getCompatibilitySearch() {
        return compatibilities.stream().map(WarehouseCompatibility::getEngineCode)
                .collect(java.util.stream.Collectors.joining(" "));
    }
    public String getSearchText() {
        return java.util.stream.Stream.of(department, categoryName, manufacturer, oeCode,
                        alternativeCodes, condition, lotCode, sourceEngineCode, technicalNotes,
                        manufacturerCode, calibrationCode, sourceCylinder, testStatus, revisionStatus,
                        getCompatibilitySearch())
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(" "));
    }
    public String getDepartment() { return department; }
    public String getCategoryName() { return categoryName; }
    public boolean isInjectorCategory() { return injectorCategory; }

    public void setCategoryLabels(String department, String categoryName, boolean injectorCategory) {
        this.department = department;
        this.categoryName = categoryName;
        this.injectorCategory = injectorCategory;
    }
}
