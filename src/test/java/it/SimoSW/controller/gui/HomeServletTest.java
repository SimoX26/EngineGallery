package it.SimoSW.controller.gui;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeServletTest {

    @Test
    void returnsBuongiornoAt1559() {
        assertEquals("Buongiorno", HomeServlet.buildGreeting(LocalTime.of(15, 59)));
    }

    @Test
    void returnsBuonaseraAt1600() {
        assertEquals("Buonasera", HomeServlet.buildGreeting(LocalTime.of(16, 0)));
    }

    @Test
    void returnsBuonaseraAfter1600() {
        assertEquals("Buonasera", HomeServlet.buildGreeting(LocalTime.of(20, 30)));
    }

    @Test
    void capitalizesSingleWordDisplayName() {
        assertEquals("Simone", HomeServlet.formatDisplayName("simone"));
    }

    @Test
    void keepsAlreadyCapitalizedDisplayName() {
        assertEquals("Simone", HomeServlet.formatDisplayName("Simone"));
    }

    @Test
    void capitalizesEachWordInDisplayName() {
        assertEquals("Mario Rossi", HomeServlet.formatDisplayName("mario rossi"));
    }

    @Test
    void capitalizesAfterSpecialCharacters() {
        assertEquals("D'Angelo", HomeServlet.formatDisplayName("d'angelo"));
    }
}
