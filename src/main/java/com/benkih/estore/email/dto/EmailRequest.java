package com.benkih.estore.email.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    private String to;
    private String from;
    private String fromName;
    private String subject;
    private String html;
    private String text;
    private List<EmailAttachment> attachments;
}
