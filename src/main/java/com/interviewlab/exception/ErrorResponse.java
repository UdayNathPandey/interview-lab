package com.interviewlab.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

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
    private Map<String,String>  validationErrors;
    private String path;
}
