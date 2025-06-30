package com.res.server.removebgbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;


@Entity
@Table(name = "tbl_orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String orderId;
    private String clerkId;
    private String plan;
    private Double amount;
    private Integer credits;
    private Boolean payment;
    @CreationTimestamp
    @Column(updatable = false,nullable = false)
    private Timestamp created_at;

    @PrePersist
    public void prePersist() {

        if (payment == null) {
            payment = false; // Default value for payment status
        }
    }


}
