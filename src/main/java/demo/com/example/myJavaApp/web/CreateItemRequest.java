package demo.com.example.myJavaApp.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateItemRequest(
        @NotBlank(message = "name is required")
        String name,

        @Positive(message = "price must be greater than 0")
        BigDecimal price
) {}