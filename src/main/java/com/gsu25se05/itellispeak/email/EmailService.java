//package com.gsu25se05.itellispeak.email;
//
//import com.gsu25se05.itellispeak.jwt.JWTService;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import java.util.Optional;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private TemplateEngine templateEngine;
//
//    @Autowired
//    private JavaMailSender javaMailSender;
//
//    @Autowired
//    private JWTService jwtService;
//
//    @Value("${URL}")
//    private String url;
//
//    @Async
//    public void sendVerifyEmail(EmailDetail emailDetail) {
//        try {
//            Context context = new Context();
//
//            context.setVariable("name", emailDetail.getName());
//            String token = jwtService.generateToken(emailDetail.getRecipient());
//
//            String link = url + "/auth/verify/" + token;
//            context.setVariable("link", link);
//
//            context.setVariable("button", "Verify");
//
//            proceedToSendMail(emailDetail, context, "VerifyAccount");
//        } catch (MessagingException messagingException) {
//            messagingException.printStackTrace();
//        }
//    }
//
//
//
//    private void proceedToSendMail(EmailDetail emailDetail, Context context, String template) throws MessagingException {
//        String text = templateEngine.process(template, context);
//
//        // Creating a simple mail message
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
//
//        // Setting up necessary details
//        mimeMessageHelper.setFrom("dungnlmse170490@fpt.edu.vn");
//        mimeMessageHelper.setTo(emailDetail.getRecipient());
//        mimeMessageHelper.setText(text, true);
//        mimeMessageHelper.setSubject(emailDetail.getSubject());
//        javaMailSender.send(mimeMessage);
//    }
//}
//
