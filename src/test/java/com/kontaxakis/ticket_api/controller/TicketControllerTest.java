package com.kontaxakis.ticket_api.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.kontaxakis.ticket_api.service.RabbitMQProducer;
import com.kontaxakis.ticket_api.service.RedisStockService;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RedisStockService redisStockService;

    @MockitoBean
    private RabbitMQProducer rabbitMQProducer;

    @Test
    void testBuyTicket_Success() throws Exception {
        when(redisStockService.reserveTickets(anyLong(), anyInt())).thenReturn(true);

        String requestJson = """
                {
                    "userId": 1,
                    "eventId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/tickets/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Order accepted")));
    }

    @Test
    void testBuyTicket_WrongQuantity() throws Exception {
        when(redisStockService.reserveTickets(anyLong(), anyInt())).thenReturn(true);
        String requestJson = """
                {
                    "userId": 1,
                    "eventId": 1,
                    "quantity": 15
                }
                """;

        mockMvc.perform(post("/api/tickets/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("The valid number of tickets is between 1 and 10.")));
    }

    @Test
    void testBuyTicket_OutOfStock() throws Exception {
        when(redisStockService.reserveTickets(anyLong(), anyInt())).thenReturn(false);
        String requestJson = """
                {
                    "userId": 1,
                    "eventId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/tickets/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Sold out.")));
    }
}
