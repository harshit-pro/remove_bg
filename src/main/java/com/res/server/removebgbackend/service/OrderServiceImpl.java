package com.res.server.removebgbackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.res.server.removebgbackend.entity.OrderEntity;
import com.res.server.removebgbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {
    private final RazorPayService razorPayService;

    private final OrderRepository orderRepository;

    private static  final Map<String,PlanDetails> planDetailsMap = Map.of(
            "Basic",new PlanDetails("Basic",100,499.0),
            "Premium",new PlanDetails("Premium",250,899.0),
            "Ultimate",new PlanDetails("Ultimate",1000,1499.0)
    );

    private record PlanDetails(String name,int  credits,double amount) {

    }
    @Override
    public Order createOrder(String planId, String clerkId) throws RazorpayException {
        ///  yaha par humne plan details ko map me store kiya hai
        // BECAUSE OF THIS WE CAN EASILY ADD OR REMOVE PLANS IN FUTURE
        // agar hume koi plan add karna hai to hume sirf yaha par add karna padega
        // aur agar hume koi plan remove karna hai to hume sirf yaha par remove karna padega
        // aur hume kisi bhi jagah par change nahi karna padega
        PlanDetails planDetails= planDetailsMap.get(planId);
        if (planDetails == null) {
            System.out.println("Invalid plan ID: " + planId);
            throw new IllegalArgumentException("Invalid plan ID: " + planId);
        }
       try{

           // ho yeh rha hai yaha par ki jab bhi koi user order create karega to uska plan details ko update karna padega
              // aur uske credits ko bhi update karna padega
        Order razorPayOrder=  razorPayService.createOrder(planDetails.amount,"INR");
           System.out.println("Creating order with plan: " + planDetails.name + ", amount: " + planDetails.amount + ", credits: " + planDetails.credits + ", clerkId: " + clerkId);

          OrderEntity newOrderEntity= OrderEntity.builder()
                .orderId(razorPayOrder.get("id").toString())
                .amount(planDetails.amount)
                   .plan(planDetails.name)
                .clerkId(clerkId)
                .credits(planDetails.credits)
                .build();
          orderRepository.save(newOrderEntity);
          return razorPayOrder;
       }catch (RazorpayException e){
           System.out.println("Error while creating Razorpay order: " + e.getMessage());
              e.printStackTrace();
              throw new RazorpayException("RazorPay Error: " + e.getMessage());
       }



    }
}
