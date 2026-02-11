package it.SimoSW.model.dao;

public interface CustomerDAO {
    int countClientiConMotoriInOfficina();
    public Long findIdByName(String name);
}
