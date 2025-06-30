package com.res.server.removebgbackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.res.server.removebgbackend.dto.UserDto;
import com.res.server.removebgbackend.entity.OrderEntity;
import com.res.server.removebgbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RazorPayServiceImpl implements RazorPayService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    @Override
    public Order createOrder(Double amount, String currency) throws RazorpayException {
        ///  ism humne RazorpayClient ka object banaya hai
        /// aur usme humne key id aur secret pass kiya hai
        /// aur uske baad humne order create kiya hai
        /// aur usme humne amount, currency, receipt aur payment_capture pass kiya hai
        /// amount ko 100 se multiply kiya hai kyuki razorpay me amount paise me hota hai
        /// aur currency ko INR set kiya hai
        /// receipt ko unique id set kiya hai
        /// aur payment_capture ko 1 set kiya hai kyuki hum auto capture payment karna chahte hain
        // auto capture payment ka matlab hai ki jab user payment karega to uska payment automatically capture ho jayega
        // then
        try {
            System.out.println("Creating Razorpay order with keyId: " + razorpayKeyId + " and secret: " + razorpayKeySecret);
            RazorpayClient razorpayClient= new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",amount * 100);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "order_rcptid"+ System.currentTimeMillis()); // Unique receipt ID
            orderRequest.put("payment_capture", 1); // Auto capture payment || 0 for manual capture
            System.out.println("Creating order with amount: " + amount + " and currency: " + currency);
            return razorpayClient.orders.create(orderRequest);

        } catch (RazorpayException e) {
            System.out.println("Error while creating Razorpay order: " + e.getMessage());
            e.printStackTrace();
            throw new RazorpayException("RazorPay Error"+e.getMessage());

        }
    }

    @Override
    public Map<String, Object> verifyPayment(String razorpayOrderId) throws RazorpayException {
        Map<String,Object> returnValue = new HashMap<>();
      try {
          RazorpayClient razorpayClient =new RazorpayClient(razorpayKeyId,razorpayKeySecret);
          Order order_info= razorpayClient.orders.fetch(razorpayOrderId);
          if (order_info.get("status").toString().equalsIgnoreCase("paid")) {
              OrderEntity existingOrder = orderRepository.findByOrderId(razorpayOrderId).orElseThrow(()->
              new RuntimeException("Order not found"+ razorpayOrderId));
              if (existingOrder.getPayment()) {
                  returnValue.put("message", "Payment failed for this order.");
                    returnValue.put("status", false);
                    return returnValue;
              }
              UserDto userDto = userService.getUserByClerkId(existingOrder.getClerkId());
              userDto.setCredits(userDto.getCredits() + existingOrder.getCredits());
              userService.saveUser(userDto);
              existingOrder.setPayment(false);
                orderRepository.save(existingOrder);
                returnValue.put("success", true);
                returnValue.put("message", "Credits added successfully.");
          }

      }catch(RazorpayException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while verifying payment: " + e.getMessage());
      }
return returnValue;

    }
}
