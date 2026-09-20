package com.example.bank.actuator;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {


    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
        boolean valid = connection.isValid(2);
             if(valid) {
                 return Health.up()
                        .withDetail("connectionValid", valid)
                        .build();
            } else {
                 return Health.down()
                         .withDetail("connectionValid", valid)
                         .build();
             }

        } catch (Exception e) {

            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

}
