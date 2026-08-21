package com.interviewlab.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ErrorResponse {

    // TSEMP
    private LocalDateTime timeStamp;
    private int status; // made it string
    private String error;
    private String message;
    private String path;
}
