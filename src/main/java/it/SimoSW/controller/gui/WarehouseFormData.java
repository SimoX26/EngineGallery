package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.model.WarehouseCompatibility;
import it.SimoSW.model.WarehouseDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class WarehouseFormData {
    private WarehouseFormData() { }

    static void bindOptions(HttpServletRequest request, WarehouseController controller) {
        request.setAttribute("categories", controller.getCategories());
    }

    static WarehouseDetails read(HttpServletRequest request) {
        Long categoryId = null;
        String rawCategoryId = request.getParameter("categoryId");
        if (rawCategoryId != null && !rawCategoryId.isBlank()) {
            try {
                categoryId = Long.parseLong(rawCategoryId);
                if (categoryId <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Categoria non valida");
            }
        }
        String[] codes = request.getParameterValues("engineCode");
        if (codes != null && codes.length > 50) throw new IllegalArgumentException("Massimo 50 motori compatibili");
        String[] verifiedIndexes = request.getParameterValues("verifiedIndex");
        Set<String> verified = verifiedIndexes == null ? Set.of() : new HashSet<>(List.of(verifiedIndexes));
        List<WarehouseCompatibility> compatibility = new ArrayList<>();
        if (codes != null) {
            for (int i = 0; i < codes.length; i++) {
                String code = trim(codes[i]);
                if (code != null) compatibility.add(new WarehouseCompatibility(code, verified.contains(String.valueOf(i))));
            }
        }
        return new WarehouseDetails(categoryId, request.getParameter("manufacturer"), request.getParameter("oeCode"),
                request.getParameter("alternativeCodes"), request.getParameter("condition"), request.getParameter("lotCode"),
                request.getParameter("sourceEngineCode"), request.getParameter("technicalNotes"),
                request.getParameter("manufacturerCode"), request.getParameter("calibrationCode"),
                request.getParameter("sourceCylinder"), request.getParameter("testStatus"),
                request.getParameter("revisionStatus"), compatibility);
    }

    static void bind(HttpServletRequest request, WarehouseDetails details, WarehouseController controller) {
        request.setAttribute("details", details);
        request.setAttribute("department", request.getParameter("department"));
        if (details != null && details.getCategoryId() != null) {
            controller.getCategories().stream().filter(c -> c.getId().equals(details.getCategoryId())).findFirst()
                    .ifPresent(c -> {
                        details.setCategoryLabels(c.getDepartment(), c.getName(), c.isInjectorFields());
                        request.setAttribute("department", c.getDepartment());
                    });
        }
    }

    static void bind(HttpServletRequest request, WarehouseDetails details) {
        request.setAttribute("details", details);
        if (details != null) request.setAttribute("department", details.getDepartment());
    }

    private static String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
