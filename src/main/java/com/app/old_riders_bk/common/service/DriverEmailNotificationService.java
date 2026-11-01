package com.app.old_riders_bk.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverEmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:support@oldriders.app}")
    private String fromEmail;

    // Colours taken from the Old Riders driver app
    private static final String INK = "#0A0A0A";
    private static final String CARD = "#1B1B19";
    private static final String LINE = "#4A4A46";
    private static final String LIME = "#A3D314";
    private static final String AMBER = "#FFB84D";
    private static final String ERROR = "#FF7A6B";
    private static final String SOFT = "#CFCFC8";
    private static final String MUTED = "#8E8E88";

    private static final String TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>{{TITLE}}</title>
            </head>
            <body style="margin:0; padding:0; background:#0A0A0A;">
              <div style="display:none; max-height:0; overflow:hidden; opacity:0;">{{PREHEADER}}</div>
              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background:#0A0A0A;">
                <tr>
                  <td align="center" style="padding:32px 16px;">
                    <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px; width:100%;">

                      <!-- Header -->
                      <tr>
                        <td style="padding:0 0 24px 0;">
                          <span style="font-family:Georgia,'Times New Roman',serif; font-size:20px; color:#FFFFFF;">Old Riders</span>
                          <span style="display:inline-block; margin-left:10px; padding:4px 12px; background:#A3D314; color:#0A0A0A; border-radius:20px; font-family:Arial,Helvetica,sans-serif; font-size:12px; font-weight:bold; letter-spacing:1.6px;">DRIVER</span>
                        </td>
                      </tr>

                      <!-- Card -->
                      <tr>
                        <td style="background:#1B1B19; border:1.5px solid #4A4A46; border-radius:28px; padding:28px;">
                          <span style="display:inline-block; padding:6px 14px; border-radius:20px; background:{{ACCENT}}; color:#0A0A0A; font-family:Arial,Helvetica,sans-serif; font-size:12px; font-weight:bold; letter-spacing:1.4px;">{{STATUS}}</span>
                          <h1 style="margin:16px 0 14px 0; font-family:Georgia,'Times New Roman',serif; font-size:32px; line-height:1.1; font-weight:normal; color:#FFFFFF;">{{HEADING}}</h1>
                          {{BODY}}
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="padding:24px 8px 0 8px; font-family:Arial,Helvetica,sans-serif; font-size:13px; line-height:1.5; color:#8E8E88;">
                          You are receiving this because you applied to drive with Old Riders.
                          Need help? Contact <a href="mailto:support@oldriders.app" style="color:#A3D314; text-decoration:none;">support@oldriders.app</a>.
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

    /**
     * 1. Driver Application Approved
     */
    @Async
    public void sendApprovalEmail(String toEmail, String driverName) {
        String subject = "Congratulations! Your Old Riders Driver Account is Approved";

        String plain = "Dear " + driverName + ",\n\n"
                + "Great news! Your driver application and verification documents have been reviewed and APPROVED by our admin team.\n\n"
                + "You can now open the Old Riders app, switch your status to Online, and begin accepting ride requests.\n\n"
                + "Thank you for being part of the Old Riders community.\n\n"
                + "Best regards,\n"
                + "Old Riders Admin Team";

        String body = paragraph("Dear " + esc(driverName) + ",")
                + paragraph("Great news! Your driver application and verification documents have been reviewed and approved by our admin team.")
                + infoCard("Next step", "Open the Old Riders app, switch your status to Online, and start accepting ride requests.", LIME)
                + paragraph("Thank you for being part of the Old Riders community.")
                + signOff("Old Riders Admin Team");

        String html = render(subject, "Your driver account is approved. You can go online now.",
                "APPROVED", LIME, "You're approved!", body);

        send(toEmail, subject, plain, html);
    }

    /**
     * 2. Request Document Re-Upload (e.g. blurry licence, expired disc)
     */
    @Async
    public void sendReuploadDocumentsEmail(String toEmail, String driverName, String reason) {
        String subject = "Action Required: Re-upload Verification Documents - Old Riders";

        String plain = "Dear " + driverName + ",\n\n"
                + "Thank you for submitting your driver documents to Old Riders.\n\n"
                + "During our verification review, our compliance team identified that some documents need to be re-uploaded.\n\n"
                + "Reason / Instructions:\n"
                + "\"" + reason + "\"\n\n"
                + "Please open the Old Riders app, go to Dashboard > My documents, and upload clear, valid replacements so we can finalize your approval.\n\n"
                + "Best regards,\n"
                + "Old Riders Verification Team";

        String body = paragraph("Dear " + esc(driverName) + ",")
                + paragraph("Thank you for submitting your driver documents. During our verification review, we found that some documents need to be uploaded again.")
                + infoCard("Reason", esc(reason), AMBER)
                + infoCard("What to do", "Open the Old Riders app, go to Dashboard > My documents, and upload clear, valid replacements.", LIME)
                + paragraph("We'll finish your approval as soon as the new documents are checked.")
                + signOff("Old Riders Verification Team");

        String html = render(subject, "Some documents need to be uploaded again.",
                "ACTION REQUIRED", AMBER, "Please re-upload your documents", body);

        send(toEmail, subject, plain, html);
    }

    /**
     * 3. Driver Application Rejected
     */
    @Async
    public void sendRejectionEmail(String toEmail, String driverName, String reason) {
        String subject = "Update Regarding Your Old Riders Driver Application";

        String plain = "Dear " + driverName + ",\n\n"
                + "Thank you for your interest in driving with Old Riders.\n\n"
                + "After reviewing your application, we regret to inform you that we are unable to approve your driver profile at this time.\n\n"
                + "Reason for rejection:\n"
                + "\"" + reason + "\"\n\n"
                + "If you believe this is an error or if your circumstances change, please reach out to our support team at support@oldriders.app.\n\n"
                + "Best regards,\n"
                + "Old Riders Compliance Team";

        String body = paragraph("Dear " + esc(driverName) + ",")
                + paragraph("Thank you for your interest in driving with Old Riders. After reviewing your application, we are unable to approve your driver profile at this time.")
                + infoCard("Reason for rejection", esc(reason), ERROR)
                + paragraph("If you believe this is an error, or your circumstances change, please contact our support team and we will take another look.")
                + signOff("Old Riders Compliance Team");

        String html = render(subject, "An update on your driver application.",
                "NOT APPROVED", ERROR, "Your application update", body);

        send(toEmail, subject, plain, html);
    }

    // ---- building blocks -------------------------------------------------

    private String render(String title, String preheader, String status,
                          String accent, String heading, String body) {
        return TEMPLATE
                .replace("{{TITLE}}", esc(title))
                .replace("{{PREHEADER}}", esc(preheader))
                .replace("{{STATUS}}", esc(status))
                .replace("{{ACCENT}}", accent)
                .replace("{{HEADING}}", esc(heading))
                .replace("{{BODY}}", body);
    }

    private String paragraph(String html) {
        return "<p style=\"margin:0 0 16px 0; font-family:Arial,Helvetica,sans-serif; "
                + "font-size:16px; line-height:1.5; color:" + SOFT + ";\">" + html + "</p>";
    }

    private String infoCard(String label, String text, String accent) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"margin:0 0 16px 0; background:" + INK + "; border:1.5px solid " + LINE + "; "
                + "border-left:4px solid " + accent + "; border-radius:20px;\">"
                + "<tr><td style=\"padding:16px 18px;\">"
                + "<div style=\"font-family:Arial,Helvetica,sans-serif; font-size:12px; font-weight:bold; "
                + "letter-spacing:1.4px; color:" + MUTED + "; text-transform:uppercase; margin-bottom:6px;\">"
                + esc(label) + "</div>"
                + "<div style=\"font-family:Arial,Helvetica,sans-serif; font-size:16px; line-height:1.5; "
                + "color:#FFFFFF;\">" + text + "</div>"
                + "</td></tr></table>";
    }

    private String signOff(String teamName) {
        return "<p style=\"margin:24px 0 0 0; font-family:Arial,Helvetica,sans-serif; font-size:16px; "
                + "line-height:1.5; color:" + SOFT + ";\">Best regards,<br>"
                + "<strong style=\"color:#FFFFFF;\">" + esc(teamName) + "</strong></p>";
    }

    private void send(String to, String subject, String plainText, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html); // plain text fallback + HTML
            mailSender.send(message);
            log.info("Email successfully dispatched to {}", to);
        } catch (MessagingException | RuntimeException ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }

    /** Escapes text so names and reasons can't break the HTML. */
    private static String esc(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}