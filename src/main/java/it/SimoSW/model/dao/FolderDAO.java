package it.SimoSW.model.dao;

import it.SimoSW.model.Folder;

import java.util.List;

public interface FolderDAO {

    void save(Folder folder);

    void delete(long folderId);

    Folder findById(long folderId);

    List<Folder> findSubfolders(long parentFolderId);
}
