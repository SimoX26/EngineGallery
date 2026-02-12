package it.SimoSW.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Customer {

    /* =========================
       Identità tecnica (DB)
       ========================= */
    private Long id;

    /* =========================
       Dati anagrafici
       ========================= */
    private final String name;
    private final String companyName;
    private final String phone;
    private final String email;
    private final String notes;

    /* =========================
       Audit
       ========================= */
    private LocalDateTime createdAt;

    /* =========================
       Costruttore per nuovo Customer
       ========================= */
    public Customer(String name,
                    String companyName,
                    String phone,
                    String email,
                    String notes) {

        this.name = Objects.requireNonNull(name, "Il nome è obbligatorio");
        this.companyName = companyName;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    /* =========================
       Costruttore per Customer persistito
       ========================= */
    public Customer(Long id,
                    String name,
                    String companyName,
                    String phone,
                    String email,
                    String notes,
                    LocalDateTime createdAt) {

        this(name, companyName, phone, email, notes);
        this.id = id;
        this.createdAt = createdAt;
    }

    public Customer(String name) {
        this.name = Objects.requireNonNull(name, "Il nome è obbligatorio");
        this.companyName = null;
        this.phone = null;
        this.email = null;
        this.notes = null;
        this.createdAt = LocalDateTime.now();
    }

    /* =========================
       Getter
       ========================= */

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /* =========================
       Identity methods
       ========================= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer that = (Customer) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}