package sk.tuke.gamestudio.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.service.EmailSendService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import sk.tuke.gamestudio.service.exception.EmailException;

import java.util.Properties;

@Component
public class EmailSendServiceJakarta implements EmailSendService {
    @Value("${mail.from}")
    private String fromEmail;

    @Value("${mail.app-password}")
    private String appPassword;

    public void sendCode(String toEmail, int code) {
        Properties props = getProperties();
        Session session = getSession(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail) // give as argument to the function
            );
            String html = String.format("""
               <html>
               <body style="font-family: Arial, sans-serif; background: #f5f5f5; padding: 30px;">
               <div style="max-width: 500px; margin: auto; background: white; padding: 30px; border-radius: 8px;">
                   <h2 style="margin-top: 0;">Verification code</h2>
               
                   <p>Your verification code:</p>
               
                   <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; text-align: center; padding: 15px; margin: 20px 0; background: #f2f2f2; border-radius: 6px;">
                       %d
                   </div>
               
                   <p>This code will expire in 10 minutes.</p>
               
                   <p style="color: #cc0000;">
                       Do not share this code with anyone.
                   </p>
               
                   <hr style="margin: 25px 0;">
               
                   <p style="color: #777; font-size: 12px;">
                       This is an automated message. Please do not reply to this email.
                   </p>
               </div>
               </body>
               </html>""", code
            );
            message.setSubject("Verification");
            message.setContent(html, "text/html; charset=utf-8");


            Transport.send(message);
        }
        catch (MessagingException e) {
            throw new EmailException("can not send email", e);
        }
    }

    private Properties getProperties() {
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return props;
    }

    private Session getSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }
}
