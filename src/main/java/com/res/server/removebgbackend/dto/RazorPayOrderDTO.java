package com.res.server.removebgbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RazorPayOrderDTO {
    private String id;
    private String entity;
    private Integer amount;
    private String currency;
    private String receipt;
    private String status;

    private Date created_at;

}
