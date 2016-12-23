package kullervo16.checklist.health;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet shows the health of the application by 
 * a) responding :-)
 * b) throwing an error when the persistence layer is not working (anymore)
 * 
 * This pattern can be used by for example an Openshift readiness/liveness probe.
 * For that reason, it is considered to be called often... so the persistence check
 * is handled asynchronously.
 * 
 * It is handled by a separate servlet to be outside of the authorization scope.
 * 
 * @author jef
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/health"})
public class HealthServlet extends HttpServlet {

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
        
        File hb = new File("/opt/checklist/heartbeat");
        if(hb.exists() && System.currentTimeMillis() - hb.lastModified() > 2 * 60 * 1000) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Heartbeat "+(System.currentTimeMillis() - hb.lastModified())+" ms old... persistence down");
        } else {
        
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Checklist health</title>");            
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Checklist health : OK</h1>");

                try(BufferedReader reader = new BufferedReader(new FileReader(hb));) {
                    String line = reader.readLine();
                    while(line != null) {
                        out.println("<p>"+line+"</p>");
                        line = reader.readLine();
                    }
                    reader.close();
                }catch(IOException ioe) {
                    out.println("<h2>No heartbeat found... suspicious</h2>");
                }            
                out.println("</body>");
                out.println("</html>");
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
