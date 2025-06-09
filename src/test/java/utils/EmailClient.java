package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class EmailClient {


    public static void sendEmailWithReport(String htmlBody, String reportPath) throws IOException, FileNotFoundException{

        final Properties props = new Properties();
        props.load(new FileInputStream(new File("src/test/resources/smtp.properties")));

        // Email config
        String to = props.getProperty("username");
        String from = props.getProperty("username");
        String host = props.getProperty("host");

        // Get system properties
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", props.getProperty("port"));
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");


        // Authenticate
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    props.getProperty("username"), 
                    props.getProperty("password")
                    );
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Mocker Test Report");

            Multipart multipart = new MimeMultipart();

            // HTML body
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Attach Extent Report
            MimeBodyPart attachmentPart = new MimeBodyPart();
            File reportFile = new File(reportPath);
            if (reportFile.exists()) {
                DataSource source = new FileDataSource(reportFile);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(reportFile.getName());
                multipart.addBodyPart(attachmentPart);
            } else {
                System.out.println("Report file not found: " + reportPath);
            }

            // Final message
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email sent successfully.");

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    private static void dumpHtmlToFile(String html, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(html);
            System.out.println("✅ Email body saved to: " + filePath);
        } catch (IOException ex) {
            System.err.println("❌ Failed to write HTML body to file.");
            ex.printStackTrace();
        }
    }
}
