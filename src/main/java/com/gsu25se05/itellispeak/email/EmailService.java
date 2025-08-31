package com.gsu25se05.itellispeak.email;

import com.gsu25se05.itellispeak.jwt.JWTService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private JWTService jwtService;

    @Value("${BASE_BACKEND_URL}")
    private String backendBaseUrl;

    @Value("${BASE_FRONTEND_URL}")
    private String frontendBaseUrl;

    private String url;

    @Async
    public void sendVerifyEmail(EmailDetail emailDetail) {
        try {
            Context context = new Context();
            context.setVariable("name", emailDetail.getName());

            String token = jwtService.generateEmailVerifyToken(emailDetail.getRecipient());

            String link = String.format("%s/auth/verify/%s", backendBaseUrl, token);
            context.setVariable("link", link);
            context.setVariable("button", "Verify");

            proceedToSendMail(emailDetail, context, "VerifyAccount");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    @Async
    public void sendForgotPasswordEmail(EmailDetail emailDetail) {
        try {
            Context context = new Context();
            context.setVariable("name", emailDetail.getName());
            context.setVariable("link", emailDetail.getAttachment());
            context.setVariable("button", "Reset Password");

            proceedToSendMail(emailDetail, context, "forgot-password");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendHrApprovalEmail(String recipientEmail, String hrName) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", hrName);
            ctx.setVariable("status", "APPROVED");

            EmailDetail detail = new EmailDetail();
            detail.setRecipient(recipientEmail);
            detail.setName(hrName);
            detail.setSubject("Your HR application has been approved");

            proceedToSendMail(detail, ctx, "hr-approval");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendHrRejectionEmail(String recipientEmail, String hrName, String reason) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", hrName);
            ctx.setVariable("status", "REJECTED");
            ctx.setVariable("reason", (reason == null) ? "" : reason);

            EmailDetail detail = new EmailDetail();
            detail.setRecipient(recipientEmail);
            detail.setName(hrName);
            detail.setSubject("Your HR application has been rejected");

            proceedToSendMail(detail, ctx, "hr-rejection");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void handleRejectHandleComplaint(String recipientEmail, String userName, String reason) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", userName);
            ctx.setVariable("status", "REJECTED");
            ctx.setVariable("reason", (reason == null) ? "" : reason);

            EmailDetail detail = new EmailDetail();
            detail.setRecipient(recipientEmail);
            detail.setName(userName);
            detail.setSubject("Your complaint has been rejected.");

            proceedToSendMail(detail, ctx, "handleComplaint");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void handleApproveHandleComplaint(String recipientEmail, String userName) {
        try {
            Context ctx = new Context();
            ctx.setVariable("name", userName);
            ctx.setVariable("status", "APPROVED");

            EmailDetail detail = new EmailDetail();
            detail.setRecipient(recipientEmail);
            detail.setName(userName);
            detail.setSubject("Your complaint has been handled.");

            proceedToSendMail(detail, ctx, "handleComplaint");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    private void proceedToSendMail(EmailDetail emailDetail, Context context, String template) throws MessagingException {
        String text = templateEngine.process(template, context);

        // Creating a simple mail message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        // Setting up necessary details
        mimeMessageHelper.setFrom("dungnlmse170490@fpt.edu.vn");
        mimeMessageHelper.setTo(emailDetail.getRecipient());
        mimeMessageHelper.setText(text, true);
        mimeMessageHelper.setSubject(emailDetail.getSubject());
        javaMailSender.send(mimeMessage);
    }
}

