/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package anchor.service.reg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author root
 */
@WebServlet(name = "Activation", urlPatterns = {"/Activation"})
public class Activation extends HttpServlet {

    private final Logger logger = LogManager.getLogger(Activation.class);

    /**
     * Processes requests for both HTTP <code>GET</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        final String usersTable = "user";
        final int defaultCode = 500;
        final String defaultResp = "Error";
        JSONObject jsoResp = new JSONObject();
        jsoResp.put("code", defaultCode);
        jsoResp.put("text", defaultResp);
        try {
            final String token = request.getParameter("token");
            final String username = request.getParameter("username");
            final String queryURI = "http://localhost/db/" + usersTable + "/username/" + username;
//            final String queryURI ="http://localhost/db/user/username/lionell":
            System.out.println(queryURI);

            //connection to DB API REST
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet getRequest = new HttpGet(queryURI);
            HttpResponse getResponse = client.execute(getRequest);
//            
//            Header[] respHeader = getResponse.getHeaders("Content-Type");
//            for(Header headerLine:respHeader){
//                System.out.println(headerLine.getValue());
//            }
            int statusCode = getResponse.getStatusLine().getStatusCode();
            String statusMsg = getResponse.getStatusLine().getReasonPhrase();
            jsoResp = new JSONObject();
            jsoResp.put("code", statusCode);
            jsoResp.put("text", statusMsg);

            BufferedReader rd = new BufferedReader(new InputStreamReader(getResponse.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = new String();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            //BODY JSON RESP
            JSONParser parser = new JSONParser();
            jsoResp = (JSONObject) parser.parse(result.toString());
            JSONArray jsArray = (JSONArray)jsoResp.get("res");
//            System.out.println(jsoResp.size());
            if (token.equals( ((JSONObject)jsArray.get(0)).get("token"))) {
                //valid token so...
                
                jsoResp = new JSONObject();
                jsoResp.put("code", 200);
                jsoResp.put("text", "user activated");
            }else{
                jsoResp = new JSONObject();
                jsoResp.put("code", 403);
                jsoResp.put("text", "Forbidden, invalid token");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        try (PrintWriter out = response.getWriter()) {
            out.print(jsoResp);
        }
    }
    
    private void setUserActive(){
        //Edit user field with a PUT call to DB endpoint
        
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "User Activation service";
    }// </editor-fold>

}
