package it.SimoSW.controller.app;

@FunctionalInterface
public interface DeliveredEngineNotifier {
    void notifyDelivered(String engineRef);
}
