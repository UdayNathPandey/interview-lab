package com.interviewlab.exception;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ErrorResponse {

    // TSEMP
    private LocalDateTime timestamp;
    private int status; // made it string
    private String error;
    private String message;
    private String path;
}
