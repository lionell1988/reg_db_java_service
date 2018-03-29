package anchor.service.reg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author root
 */
@WebServlet(name = "RegServlet", urlPatterns = {"/RegServlet"})
public class RegServlet extends HttpServlet {

    private final Logger logger = LogManager.getLogger(RegServlet.class);

    /**
     * Processes requests for both HTTP <code>GET</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // response.setContentType("application/json;charset=UTF-8");
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {

            logger.info("Service requested");
            /* TODO output your page here. You may use following sample code. */
            // response.setHeader("Content-Type", "application/json");
            HashMap<String, Object> msg = new HashMap<>();
            msg.put("code", 501);
            msg.put("text", "Not implemented");
            JSONObject jso = new JSONObject(msg);
            System.out.println(jso);
            out.print(jso);
//            out.println(jso.get("code"));
            System.out.println("running");
            // out.flush();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Processes requests for <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
//        System.out.println(request.getHeader("Content-Type"));
//        response.setContentType("text/html");
        HashMap<String, Object> user = new HashMap<>();
        //try to get user attributes
        try {
            final String DBEndPointURL = ServerConfig.DB_ENDPOINT_ADDRESS+"/user/";
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(DBEndPointURL);
            List<NameValuePair> params = new ArrayList<>(2);
//            String name = request.getParameterValues("name")[0]; 
            String name = request.getParameter("name");
            System.out.println(name);
            String surname = request.getParameterValues("surname")[0];
            String username = request.getParameterValues("username")[0];
            String email = request.getParameterValues("email")[0];
            String token = generateToken();
            String password = request.getParameterValues("password")[0];
            int active = 0;
            //setting POST params
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("surname", surname));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("token", token));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("active", Integer.toString(active)));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResp = httpclient.execute(httpPost);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(httpResp.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = new String();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonResp = (JSONObject) parser.parse(result.toString());

            long respCode = (long) jsonResp.get("code");
            System.out.println(jsonResp.get("code"));
            if (respCode == 200) {
                sendEmail(username, name, email, token);
            }

            //sendEmail(name, email, token);
            try (PrintWriter out = response.getWriter()) {
                System.out.println(name);
                System.out.println(result);
                out.println(result);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void sendEmail(String username, String name, String address, String token) throws URISyntaxException {
        final String subject = "ANCHOR reg Service";
        NameValuePair usernameParam = new BasicNameValuePair("username", username);
        NameValuePair tokenParam = new BasicNameValuePair("token", token);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(usernameParam);
        nvps.add(tokenParam);
        final String serviceURI = ServerConfig.ANCHOR_PUBLIC_API_URL+"/activation";
        URIBuilder activationURI = new URIBuilder(serviceURI).setParameters(nvps);
        final String text = "Dear " + name + ",\n"
                + "Thankyou for your registration to our services. This is your activation mail to use Anchor services. Please, follow the link below or use this token activator.<br>"
                + "TOKEN:"
                + token + ""
                + "URL:\n"
                + "< href =\"" + activationURI + "\">" + activationURI + "</a>";

        final String HTMLText = "<html><body>"
                + "<p>Dear " + name + ",</p>"
                + "<p>Thankyou for your registration to our services. This is your activation mail to use Anchor services. Please, follow the link below or use this token activator.<br>"
                + "TOKEN:"
                + token + "<br>"
                + "URL:<br>"
                + "<a href =\"" + activationURI + "\">" + activationURI + "</a>"
                + "</p>"
                + "</body>"
                + "</html>";

        Mailer mailer = new Mailer();
        mailer.send(address, subject, HTMLText);
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
        processPostRequest(request, response);
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
