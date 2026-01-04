package it.SimoSW.model.dao;

import it.SimoSW.model.Folder;
import it.SimoSW.model.Image;

import java.util.List;

public interface GalleryDAO {

    List<Folder> loadRootFolders();

    List<Image> loadImagesInFolder(long folderId);
}
