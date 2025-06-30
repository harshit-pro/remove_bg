package com.res.server.removebgbackend.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.res.server.removebgbackend.dto.RazorPayOrderDTO;
import com.res.server.removebgbackend.response.RemoveBgResponse;
import com.res.server.removebgbackend.service.OrderService;
import com.res.server.removebgbackend.service.RazorPayService;
import com.res.server.removebgbackend.service.RemoveBGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final RazorPayService razorPayService;
    private final RemoveBGService removeBGService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam String planId, Authentication auth) throws RazorpayException {
        System.out.println("Creating order with planId: " + planId + " for user: " + auth.getName());
        Map<String, Object> responseMap = new HashMap<>();
        RemoveBgResponse response = null;

        if (auth.getName().isEmpty() || auth.getName() == null) {
            System.out.println("Unauthorized access: User not authenticated.");
            response = RemoveBgResponse.builder()
                    .statusCode(HttpStatus.FORBIDDEN)
                    .success(false)

                    .data("Unauthorized access: User not authenticated.")
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
            try {
                Order order = orderService.createOrder(planId, auth.getName());
//                System.out.println("Order created successfully: " + order);
                log.info("Order created successfully: " + order);
                RazorPayOrderDTO responseDTO = convertToDTO(order);
                System.out.println("Converted order to DTO: " + responseDTO.getAmount());
                log.info("Converted order to DTO: " + responseDTO.getAmount());
                response = RemoveBgResponse.builder()
                        .success(true)
                        .data(responseDTO)
                        .statusCode(HttpStatus.CREATED)
                        .build();

                System.out.println("Order response: " + response);
                return ResponseEntity.ok(response);
            } catch (RazorpayException e) {
                System.out.println("Error creating order: " + e.getMessage());
                response = RemoveBgResponse.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                        .data("Error creating order: " + e.getMessage())
                        .success(false)


                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        }

    private RazorPayOrderDTO convertToDTO(Order order) {

      RazorPayOrderDTO dto=   RazorPayOrderDTO.builder()
                .id(order.get("id"))
                .entity(order.get("entity"))
                .amount(order.get("amount"))
                .currency(order.get("currency"))
              .receipt(order.get("receipt"))
                .status(order.get("status"))
                .created_at(order.get("created_at"))

                .build();
        System.out.println("DTO created: " + dto);
        return dto;

    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOrder(@RequestBody Map<String, Object> request) throws RazorpayException {

        try {
           String razorpayOrderId=request.get("razorpay_order_id").toString();
           Map<String,Object> returnValue =razorPayService.verifyPayment(razorpayOrderId);

              return  ResponseEntity.ok(returnValue);
       }catch (RazorpayException e) {
              e.printStackTrace();
             Map<String,Object> errorResponse = new HashMap<>();
             errorResponse.put("message","Error while verifying payment: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
       }
    }
    }
