package it.SimoSW.model.dao.mock;

import it.SimoSW.model.Image;
import it.SimoSW.model.dao.ImageDAO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MockImageDAO implements ImageDAO {

    private final List<Image> images = new ArrayList<>();
    private long nextId = 1;

    public MockImageDAO() {
        // Dati di test (engine_id 1..8 come nel tuo seed)
        images.add(new Image(nextId++, 1, "n47_front.jpg", 3L, LocalDateTime.of(2026, 1, 3, 10, 15)));
        images.add(new Image(nextId++, 1, "n47_chain.jpg", 4L, LocalDateTime.of(2026, 1, 3, 10, 20)));
        images.add(new Image(nextId++, 2, "n47_block.jpg", 3L, LocalDateTime.of(2026, 1, 4, 9, 5)));

        images.add(new Image(nextId++, 3, "k9k_before.jpg", 4L, LocalDateTime.of(2026, 1, 5, 14, 0)));
        images.add(new Image(nextId++, 3, "k9k_open.jpg", 4L, LocalDateTime.of(2026, 1, 5, 14, 10)));

        images.add(new Image(nextId++, 4, "k9k_ready.jpg", 5L, LocalDateTime.of(2026, 1, 6, 18, 30)));

        images.add(new Image(nextId++, 6, "m9r_final.jpg", 5L, LocalDateTime.of(2026, 1, 10, 11, 0)));

        images.add(new Image(nextId++, 8, "d998_overview.jpg", 6L, LocalDateTime.of(2026, 1, 9, 16, 40)));
        images.add(new Image(nextId++, 8, "d998_detail.jpg", 6L, LocalDateTime.of(2026, 1, 9, 16, 45)));

        // Upload anonimo (uploaded_by NULL)
        images.add(new Image(nextId++, 1, "n47_old_damage.jpg", null, LocalDateTime.of(2026, 1, 1, 8, 0)));
    }

    @Override
    public Image save(Image image) {
        Image saved = new Image(
                nextId++,
                image.getEngineId(),
                image.getFilename(),
                image.getUploadedBy(),
                // simuliamo l’upload_date come “ora”
                LocalDateTime.now()
        );
        images.add(saved);
        return saved;
    }

    @Override
    public boolean delete(long imageId) {
        return images.removeIf(i -> i.getId() == imageId);
    }

    @Override
    public Optional<Image> findById(long imageId) {
        return images.stream()
                .filter(i -> i.getId() == imageId)
                .findFirst();
    }

    @Override
    public List<Image> findAllByEngineId(long engineId) {
        // coerente con DatabaseImageDAO: ORDER BY upload_date DESC
        return images.stream()
                .filter(i -> i.getEngineId() == engineId)
                .sorted(Comparator.comparing(Image::getUploadDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Image> findCoverByEngineId(long engineId) {
        // coerente con DatabaseImageDAO: "prima immagine" = più vecchia (ASC)
        return images.stream()
                .filter(i -> i.getEngineId() == engineId)
                .sorted(Comparator.comparing(Image::getUploadDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .findFirst();
    }
}