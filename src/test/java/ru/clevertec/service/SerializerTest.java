package ru.clevertec.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializerTest {
    private final Serializer serializer = new Serializer();
    private static Customer customer;

    @BeforeAll
    public static void init() {
        Map<UUID, String> map = new HashMap<>();
        map.put(UUID.randomUUID(), "a");
        map.put(UUID.randomUUID(), "b");
        map.put(UUID.randomUUID(), "c");
        map.put(UUID.randomUUID(), "d");
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .count(map)
                .price(2.2)
                .build();
        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .price(3.3)
                .build();
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .createDate(OffsetDateTime.now())
                .products(List.of(product1, product))
                .build();
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .dateBirth(LocalDate.now())
                .firstName("firstCust")
                .lastName("lastCust")
                .orders(List.of(order))
                .build();
    }

    @Test
    void toJsonTest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
        String expected = objectMapper.writeValueAsString(customer);

        String result = serializer.toJson(customer);

        assertEquals(expected, result);
    }
}