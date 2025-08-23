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

    private String url;

    @Async
    public void sendVerifyEmail(EmailDetail emailDetail) {
        try {
            Context context = new Context();

            context.setVariable("name", emailDetail.getName());
            String token = jwtService.generateToken(emailDetail.getRecipient());

            String link = url + "/auth/verify/" + token;
            context.setVariable("link", link);

            context.setVariable("button", "Verify");

            proceedToSendMail(emailDetail, context, "VerifyAccount");
        } catch (MessagingException messagingException) {
            messagingException.printStackTrace();
        }
    }

    public void sendForgotPasswordEmail(EmailDetail emailDetail) {
        try {
            Context context = new Context();
            context.setVariable("name", emailDetail.getName());

            // Tạo token để người dùng có thể reset mật khẩu
            String token = jwtService.generateToken(emailDetail.getRecipient());
            String resetLink = "http://localhost:8080/swagger-ui/index.html#/auth-controller/forgotPassword?" + token;

            context.setVariable("link", resetLink);
            context.setVariable("button", "Reset Password");

            emailDetail.setSubject("Yêu cầu đặt lại mật khẩu");
            emailDetail.setMsgBody("Xin chào " + emailDetail.getName() + ",\n\n" +
                    "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu từ bạn. Vui lòng nhấn vào nút bên dưới để thực hiện:\n\n" +
                    "<a href=\"" + resetLink + "\">Đặt lại mật khẩu</a>\n\n" +
                    "Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ.\n\n" +
                    "Trân trọng,\nĐội ngũ hỗ trợ ItelliSpeak");
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

