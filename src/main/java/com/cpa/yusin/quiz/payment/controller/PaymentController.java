package com.cpa.yusin.quiz.payment.controller;

import com.cpa.yusin.quiz.payment.controller.dto.request.PaymentWebHookDTO;
import com.cpa.yusin.quiz.payment.controller.port.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/payment")
@Slf4j
@RequiredArgsConstructor
@RestController
public class PaymentController
{
    private final PaymentService paymentService;
    @GetMapping
    public String verifyPayment(@RequestBody PaymentWebHookDTO webHookDTO)
    {


        return "";
    }
}
