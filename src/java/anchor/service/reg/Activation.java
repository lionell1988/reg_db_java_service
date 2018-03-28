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
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response)
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
            final String queryURI = ServerConfig.DB_ENDPOINT_ADDRESS+"/" + usersTable + "/username/" + username;
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
            System.out.println("Result: " + result.toString());
            jsoResp = (JSONObject) parser.parse(result.toString());
            JSONArray jsArray = (JSONArray) jsoResp.get("res");
            if (token.equals(((JSONObject) jsArray.get(0)).get("token"))) {
                //valid token so...
                int id = Integer.parseInt(((JSONObject) jsArray.get(0)).get("id").toString());
                jsoResp = setUserActive(id);
                final int respCode = Integer.parseInt(jsoResp.get("code").toString());
                final String respTxt = jsoResp.get("txt").toString();
                jsoResp = new JSONObject();
                jsoResp.put("code", respCode);
                jsoResp.put("text", respTxt);
                
//                jsoResp = new JSONObject();
//                jsoResp.put("code", 200);
//                jsoResp.put("text", "user activated");
            } else {
                jsoResp = new JSONObject();
                jsoResp.put("code", 403);
                jsoResp.put("text", "Forbidden, invalid token");
            }

        } catch (Exception e) {
            logger.error(e);
        }
        try (PrintWriter out = response.getWriter()) {
            out.print(jsoResp);
        }
    }

    private JSONObject setUserActive(int id) throws UnsupportedEncodingException, IOException, ParseException {
        final String queryURI = ServerConfig.DB_ENDPOINT_ADDRESS+"/user/" + id;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(queryURI);
        int active = 1;
        String token = "brav";
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("active", active);
        jsonParams.put("token", token);
        StringEntity jsonData = new StringEntity(jsonParams.toJSONString());
        httpPut.setEntity(jsonData);
        System.out.println("PARAMS: " + jsonData);
        HttpResponse httpResp = httpclient.execute(httpPut);
        BufferedReader rd = new BufferedReader(new InputStreamReader(httpResp.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line = new String();
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
//        System.out.println("Update query res: "+result);
        JSONParser parser = new JSONParser();
        JSONObject jsonResp = (JSONObject) parser.parse(result.toString());
//        System.out.println(jsonResp);
        return jsonResp;
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
        processGetRequest(request, response);
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
