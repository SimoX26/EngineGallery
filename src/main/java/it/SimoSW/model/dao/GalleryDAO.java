package it.SimoSW.model.dao;

import it.SimoSW.model.Folder;

import java.util.List;

public interface GalleryDAO {
    List<Folder> loadRootFolders();
    List<Folder> loadSubFolders(long parentFolderId);
}
