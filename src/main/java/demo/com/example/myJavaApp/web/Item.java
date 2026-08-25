package demo.com.example.myJavaApp.web;

import java.math.BigDecimal;

public record Item(Long id, String name, BigDecimal price) {}