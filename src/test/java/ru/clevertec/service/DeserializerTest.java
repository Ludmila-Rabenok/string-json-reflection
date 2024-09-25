package ru.clevertec.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.clevertec.exception.JsonParserException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeserializerTest {
    private final Deserializer deserializer = new Deserializer();
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
                .createDate(OffsetDateTime.parse("2018-12-30T06:00:00Z"))
                .products(List.of(product1, product))
                .build();
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .dateBirth(LocalDate.parse("2024-09-25"))
                .firstName("firstCust")
                .lastName("lastCust")
                .orders(List.of(order))
                .build();
    }

    @Test
    void shouldJsonToObjectTest() throws JsonProcessingException {
        //given
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
        String json = objectMapper.writeValueAsString(customer);
        Customer expected = objectMapper.readValue(json, customer.getClass());

        //when
        Customer result = deserializer.toObject(json, customer.getClass());

        //then
        assertEquals(expected, result);
    }

    @Test
    void shouldReturnNull_whenJsonNull() {
        //given,when
        Customer result = deserializer.toObject(null, customer.getClass());

        //then
        assertNull(result);
    }

    @Test
    void shouldThrowJsonParserException_whenInvalidJson() {
        //given,when,then
        assertThrows(JsonParserException.class,
                () -> deserializer.toObject("{Invalid:,}", customer.getClass()));
    }
}
