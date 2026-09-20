package com.example.bank.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DatabaseHealthIndicatorTest {

    private DataSource dataSource;
    private Connection connection;

    private DatabaseHealthIndicator databaseHealthIndicator;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        databaseHealthIndicator = new DatabaseHealthIndicator(dataSource);
    }

    @Test
    @DisplayName("health возвращает UP если соединение с БД валидно")
    void health_shouldReturnUp_whenConnectionIsValid() throws Exception {

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        Health health = databaseHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(true, health.getDetails().get("connectionValid"));

        verify(dataSource).getConnection();
        verify(connection).isValid(2);
        verify(connection).close();
    }

    @Test
    @DisplayName("health возвращает DOWN если соединение с БД невалидно")
    void health_shouldReturnDown_whenConnectionIsInvalid() throws Exception {

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(false);

        Health health = databaseHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(false, health.getDetails().get("connectionValid"));

        verify(dataSource).getConnection();
        verify(connection).isValid(2);
        verify(connection).close();
    }

    @Test
    @DisplayName("health возвращает DOWN если не удалось получить соединение")
    void health_shouldReturnDown_whenConnectionThrowsException() throws Exception {

        when(dataSource.getConnection())
                .thenThrow(new RuntimeException("Database unavailable"));

        Health health = databaseHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(
                "Database unavailable",
                health.getDetails().get("error")
        );

        verify(dataSource).getConnection();
        verify(connection, never()).isValid(anyInt());
    }
}