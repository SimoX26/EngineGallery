package it.SimoSW.bootstrap;

import it.SimoSW.controller.app.*;
import it.SimoSW.model.dao.*;
import it.SimoSW.model.dao.database.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ApplicationInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        /* =========================
           DAO
           ========================= */

        EngineDAO engineDAO = new DatabaseEngineDAO();
        ImageDAO imageDAO = new DatabaseImageDAO();
        GalleryDAO galleryDAO = new DatabaseGalleryDAO();
        UserDAO userDAO = new DatabaseUserDAO();

        /* =========================
           Controller applicativi
           ========================= */

        GalleryController galleryController = new GalleryController(engineDAO, imageDAO);

        ImageController imageController = new ImageController(imageDAO);

        FolderController folderController = new FolderController(galleryDAO);

        AuthenticationController authenticationController = new AuthenticationController(userDAO);

        /* =========================
           Registrazione nel contesto
           ========================= */

        context.setAttribute("galleryController", galleryController);
        context.setAttribute("imageController", imageController);
        context.setAttribute("folderController", folderController);
        context.setAttribute("authenticationController", authenticationController);
    }
}
