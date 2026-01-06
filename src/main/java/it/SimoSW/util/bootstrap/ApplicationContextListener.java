package it.SimoSW.util.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        try {
            ApplicationInitializer initializer = new ApplicationInitializer();

            context.setAttribute("appInitializer", initializer);

            System.out.println("[EngineGallery] Application initialized successfully");

        } catch (Exception e) {
            System.err.println("[EngineGallery] FATAL ERROR during startup");
            e.printStackTrace();

            // blocca l'avvio dell'app se il bootstrap fallisce
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[EngineGallery] Application shutdown completed");
    }
}
