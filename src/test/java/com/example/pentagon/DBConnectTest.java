package com.example.pentagon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DbConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void DB_연결_SELECT_1_성공() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            assertTrue(conn.isValid(2), "Connection is not valid");
            assertTrue(rs.next(), "No result row");
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void DB_정보_확인() throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT DATABASE(), USER(), VERSION()");
             ResultSet rs = ps.executeQuery()) {

            assertTrue(rs.next());
            System.out.println("DATABASE = " + rs.getString(1));
            System.out.println("USER     = " + rs.getString(2));
            System.out.println("VERSION  = " + rs.getString(3));
        }
    }
}
