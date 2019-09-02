package ServerPart;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

class JavaMail {
    private static final String ENCODING = "UTF-8";
    
    String registration(String email) {
        String password = generatePassword();
        String subject = "Confirm registration";
        String content = "Введите код: " + password;
        String smtpHost = "mail.99cows.com";
        String from = "Katusha";
        String login = "katya";
        String password1 = "katya";
        String smtpPort = "25";
        try {
            sendSimpleMessage(login, password1, from, email, content, subject, smtpPort, smtpHost);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return password;
    }
    
    private String generatePassword() {
        StringBuilder stringBuilder = new StringBuilder();
        String s = "abcdefghijklmnopkrstuvexyzABCDEFJHIJKLMNOPQRSTUVWXYZ1234567890";
        for (int k = 0; k < 10; k++) {
            stringBuilder.append(s.toCharArray()[(int) (Math.random() * 61)]);
        }
        return stringBuilder.toString();
    }
    
    private static void sendSimpleMessage(String login, String password, String from, String to, String content, String subject, String smtpPort, String smtpHost)
            throws MessagingException {
        Authenticator auth = new MyAuthenticator(login, password);
    
        Properties props = System.getProperties();
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.mime.charset", ENCODING);
        Session session = Session.getDefaultInstance(props, auth);
    
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(content);
        Transport.send(msg);
    }
    
    static class MyAuthenticator extends Authenticator {
        private String user;
        private String password;
        
        MyAuthenticator(String user, String password) {
            
            this.user = user;
            this.password = password;
        }
        
        public PasswordAuthentication getPasswordAuthentication() {
            String user = this.user;
            String password = this.password;
            return new PasswordAuthentication(user, password);
        }
        
    }
    
    
}