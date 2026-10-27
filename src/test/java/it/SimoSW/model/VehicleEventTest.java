package it.SimoSW.model;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleEventTest {
    @Test
    void pendingEventsShowOverdueAndUpcomingDates() {
        LocalDate today = LocalDate.now();
        VehicleEvent overdue = event(today.minusDays(1), null);
        VehicleEvent upcoming = event(today.plusDays(30), null);
        VehicleEvent later = event(today.plusDays(31), null);
        VehicleEvent completed = event(today.minusDays(1), today);

        assertTrue(overdue.isOverdue());
        assertFalse(overdue.isDueSoon());
        assertTrue(upcoming.isDueSoon());
        assertFalse(later.isDueSoon());
        assertFalse(completed.isOverdue());
        assertFalse(completed.isDueSoon());
    }

    private static VehicleEvent event(LocalDate due, LocalDate completed) {
        return new VehicleEvent(1, 1, "DEADLINE", "Revisione", due, completed, null);
    }
}
