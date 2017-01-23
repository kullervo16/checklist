package kullervo16.checklist.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kullervo16.checklist.repository.ChecklistRepository;
import kullervo16.checklist.repository.TemplateRepository;

/**
 * This servlet allows us to reset the data from functional tests. In order to make
 * sure it only works during tests, we check a passed secret that must be present on disk.
 * 
 * @author jef
 */
@WebServlet(name = "FunctionalTestAccesServlet", urlPatterns = {"/reset"})
public class FunctionalTestAccesServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            
            String secret = request.getQueryString();
            
            if(new File("/opt/checklist/"+secret).exists()) {
                // ok, the secret is present... now send a reset to the repositories
                ChecklistRepository.INSTANCE.clearAndLoad();
                TemplateRepository.INSTANCE.clearAndLoad();
                
                out.println("<HTML><HEAD><TITLE>RESET</TITLE><BODY><H1>RESET OK</H1></BODY></HTML>");
            } else {
                out.println("<HTML><HEAD><TITLE>NICE TRY</TITLE><BODY><H1>NICE TRY</H1></BODY></HTML>");
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
