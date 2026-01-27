package it.SimoSW.controller.app;

import it.SimoSW.model.Image;
import it.SimoSW.model.dao.ImageDAO;

import java.util.Optional;

public class ImageController {

    private final ImageDAO imageDAO;

    public ImageController(ImageDAO imageDAO) {
        this.imageDAO = imageDAO;
    }

    /* =========================
       Operazioni sulle immagini
       ========================= */

    /**
     * Inserisce una nuova immagine associata a un motore.
     *
     * @param image immagine da inserire
     * @return immagine persistita
     */
    public Image addImage(Image image) {
        return imageDAO.save(image);
    }

    /**
     * Elimina un'immagine dato il suo identificativo.
     *
     * @param imageId id dell'immagine
     */
    public void deleteImage(long imageId) {
        imageDAO.delete(imageId);
    }

    /**
     * Restituisce una singola immagine, se presente.
     *
     * @param imageId id dell'immagine
     * @return immagine opzionale
     */
    public Optional<Image> getImageById(long imageId) {
        return imageDAO.findById(imageId);
    }
}
