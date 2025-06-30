package com.res.server.removebgbackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import java.util.Map;

public interface RazorPayService {
    Order createOrder(Double amount,String currency)throws RazorpayException;
     Map<String,Object> verifyPayment(String razorpayOrderId) throws RazorpayException;
     // why take map?
    // because razorpay returns a map with all the details of the payment
    // so we can use that map to verify the payment
}
