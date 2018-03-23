package anchor.service.reg;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.Transport;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author leonardo
 */
public class Mailer {
    private final org.apache.logging.log4j.Logger logger;
    private final String smtpServer;
    private final int port;
    private final String user;
    private final String password;

    public Mailer() {
        this.smtpServer = EmailConfig.SMTP_SERVER;
        this.port = EmailConfig.SSL_PORT;
        this.user = EmailConfig.USER;
        this.password = EmailConfig.PASSWORD;
        this.logger = LogManager.getLogger(Mailer.class);
    }

    public void send(String recipient, String subject, String text) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", this.smtpServer);
            props.put("mail.smtp.socketFactory.port", this.port);
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
                
            };
            Session session = Session.getInstance(props, auth);
            
            MimeMessage msg = new MimeMessage(session);
            
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            //msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setFrom(new InternetAddress(this.user,"no-reply-Anchor-Services"));
            msg.setSubject(subject);
            msg.setContent(text,"text/html; charset=utf-8");
            msg.setSentDate(new Date());
            
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
            
            Transport.send(msg);
            
            System.out.println("email sent to "+recipient);
            
        } catch (MessagingException | UnsupportedEncodingException ex) {
            logger.warn(ex.getMessage()+" mail to "+recipient);
        }

    }

}
