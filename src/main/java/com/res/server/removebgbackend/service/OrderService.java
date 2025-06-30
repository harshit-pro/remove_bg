package com.res.server.removebgbackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;

public interface OrderService {

   Order createOrder(String planId,String clerkId) throws RazorpayException;
}
