package com.oceanview.service;

import com.oceanview.dao.GuestDao;
import com.oceanview.model.Guest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GuestServiceTest {

    @Test
    void registerGuest_existingEmail_throws() throws SQLException {
        GuestDao dao = mock(GuestDao.class);
        when(dao.findByEmail("a@example.com")).thenReturn(Optional.of(new Guest()));

        GuestService svc = new GuestService(dao);
        Guest g = new Guest();
        g.setFirstName("A");
        g.setLastName("B");
        g.setEmail("a@example.com");
        g.setContactNumber("1234567890");
        g.setAddress("X");
        g.setIdNumber("ID");
        g.setPasswordHash("secret");

        assertThrows(IllegalArgumentException.class, () -> svc.registerGuest(g));
    }

    @Test
    void registerGuest_hashesPasswordAndInserts() throws SQLException {
        GuestDao dao = mock(GuestDao.class);
        when(dao.findByEmail(anyString())).thenReturn(Optional.empty());
        when(dao.insert(any())).thenAnswer(i -> i.getArguments()[0]);

        GuestService svc = new GuestService(dao);
        Guest g = new Guest();
        g.setFirstName("John");
        g.setLastName("Doe");
        g.setEmail("JOHN@EXAMPLE.COM ");
        g.setContactNumber("1234567890");
        g.setAddress("Addr");
        g.setIdNumber("ID123");
        g.setPasswordHash("plainPass");

        Guest inserted = svc.registerGuest(g);
        assertEquals("john@example.com", inserted.getEmail());
        assertNotNull(inserted.getPasswordHash());
        assertTrue(inserted.getPasswordHash().startsWith("$2a$") || inserted.getPasswordHash().startsWith("$2y$"));
        verify(dao).insert(any());
    }
}
