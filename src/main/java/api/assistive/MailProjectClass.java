package api.assistive;

import java.util.Properties;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailProjectClass {
    static Session getMailSession;

    public static void main(String[] args) {

        final String username = "akrasilnikov@i-novus.ru";
        final String password = "q21xkr927";

        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        System.out.println("Mail Server Properties have been setup successfully..");


        try {
            getMailSession = Session.getDefaultInstance(props, null);

            MimeMessage message = new MimeMessage(getMailSession);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("akrasilnikov@i-novus.ru"));
            message.setSubject("Пожрал?");
            message.setSubject("Testing Subject");
            message.setText("PFA");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            String file = "C:/IdeaProjects/DriveQuickstart/build/libs";
            String fileName = "11.txt";
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            System.out.println("Sending");

            Transport transport = getMailSession.getTransport("smtp");

            // Enter your correct gmail UserID and Password
            // if you have 2FA enabled then provide App Specific Password
            transport.connect("smtp.gmail.com", "akrasilnikov@i-novus.ru", "q21xkr927");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}