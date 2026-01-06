package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.FolderController;
import it.SimoSW.model.Folder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/folders")
public class FolderServlet extends HttpServlet {

    private FolderController folderController;

    @Override
    public void init() {
        this.folderController = (FolderController) getServletContext().getAttribute("folderController");
    }

    /* =========================
       GET: navigazione cartelle
       ========================= */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String parentIdParam = request.getParameter("parentId");
        List<Folder> folders;

        // Caso 1: cartelle root
        if (parentIdParam == null) {
            folders = folderController.getRootFolders();
        }
        // Caso 2: sottocartelle
        else {
            long parentId = Long.parseLong(parentIdParam);
            folders = folderController.getSubFolders(parentId);
            request.setAttribute("parentId", parentId);
        }

        request.setAttribute("folders", folders);

        request.getRequestDispatcher("/WEB-INF/views/folders/folderList.jsp").forward(request, response);
    }
}
