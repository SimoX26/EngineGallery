package it.SimoSW.controller.app;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.util.bean.EngineBean;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineDeliveryNotificationTest {
    private static final String REF = "RML-2026-00001";

    @Test
    void notifiesOnlyAfterFirstSuccessfulTransitionToDelivered() {
        AtomicReference<Engine> stored = new AtomicReference<>(engine(EngineStatus.READY));
        List<String> notifications = new ArrayList<>();
        EngineController controller = new EngineController(dao(stored, false), null, null,
                null, notifications::add);

        controller.updateEngine(bean(EngineStatus.DELIVERED));
        controller.updateEngine(bean(EngineStatus.DELIVERED));

        assertEquals(List.of(REF), notifications);
    }

    @Test
    void doesNotNotifyIfSaveFails() {
        AtomicReference<Engine> stored = new AtomicReference<>(engine(EngineStatus.READY));
        List<String> notifications = new ArrayList<>();
        EngineController controller = new EngineController(dao(stored, true), null, null,
                null, notifications::add);

        assertThrows(IllegalStateException.class,
                () -> controller.updateEngine(bean(EngineStatus.DELIVERED)));
        assertEquals(List.of(), notifications);
    }

    private static EngineDAO dao(AtomicReference<Engine> stored, boolean failUpdate) {
        return (EngineDAO) Proxy.newProxyInstance(EngineDAO.class.getClassLoader(),
                new Class<?>[]{EngineDAO.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findByEngineRef")) {
                        return Optional.of(stored.get());
                    }
                    if (method.getName().equals("update")) {
                        if (failUpdate) throw new IllegalStateException("Salvataggio fallito");
                        stored.set((Engine) args[0]);
                        return stored.get();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static Engine engine(EngineStatus status) {
        return new Engine(1L, REF, "N47", 1L, LocalDate.of(2026, 10, 1),
                status, null, null);
    }

    private static EngineBean bean(EngineStatus status) {
        EngineBean bean = new EngineBean();
        bean.setEngineRef(REF);
        bean.setEngineCode("N47");
        bean.setCustomerId(1L);
        bean.setStatus(status.name());
        bean.setIntakeDate("2026-10-01");
        return bean;
    }
}
