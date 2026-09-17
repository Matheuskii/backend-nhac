package br.com.nhac.backend_nhac.infra.email;

import java.util.List;

public record BrevoEmailRequest(
        Sender sender,
        List<Recipient> to,
        String subject,
        String htmlContent
) {
    public record Sender(String email, String name) {}
    public record Recipient(String email) {}
}
