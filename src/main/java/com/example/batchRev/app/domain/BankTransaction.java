package com.example.batchRev.app.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.jdbc.core.RowMapper;

import java.io.Serializable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "customer_transactions")

public class BankTransaction implements Serializable {
    private static final long serialVersionUID = 1L; // Add a serialVersionUID

    public static final String SELECT_ALL_QUERY = "select id, month, day, hour, minute, amount, merchant from customer_transactions";
    public static final RowMapper<BankTransaction> ROW_MAPPER = (rs, rowNum) -> new BankTransaction(
            rs.getInt("id"),
            rs.getInt("month"),
            rs.getInt("day"),
            rs.getInt("hour"),
            rs.getInt("minute"),
            rs.getDouble("amount"),
            rs.getString("merchant")
    );





@Id
    private Integer id;

    private int month;  // Month of transaction (1-12)
    private int day;    // Day of transaction (1-31)
    private int hour;   // Hour of transaction (0-23)
    private int minute; // Minute of transaction (0-59)

    private Double amount; // Transaction amount
    private String merchant;   // Merchant name
}
