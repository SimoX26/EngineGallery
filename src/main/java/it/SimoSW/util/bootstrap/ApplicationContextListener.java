package it.SimoSW.util.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class ApplicationContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ApplicationContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        try {
            ApplicationInitializer initializer = new ApplicationInitializer();

            context.setAttribute("appInitializer", initializer);

            LOGGER.info("[EngineGallery] Application initialized successfully");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[EngineGallery] FATAL ERROR during startup", e);

            // blocca l'avvio dell'app se il bootstrap fallisce
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("[EngineGallery] Application shutdown completed");
    }
}
