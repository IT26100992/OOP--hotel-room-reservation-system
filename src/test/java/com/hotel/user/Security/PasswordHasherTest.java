package com.hotel.user.Security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void correctPasswordShouldVerify() {

        String password = "TestStrongPassword123!";

        String hash = PasswordHasher.hashPassword(password);

        assertTrue(
                PasswordHasher.verifyPassword(password, hash)
        );
    }

    @Test
    void wrongPasswordShouldFail() {

        String hash = PasswordHasher.hashPassword(
                "CorrectPassword123!"
        );

        assertFalse(
                PasswordHasher.verifyPassword(
                        "WrongPassword123!",
                        hash
                )
        );
    }

    @Test
    void samePasswordShouldGenerateDifferentHashes() {

        String password = "TestStrongPassword123!";

        String first = PasswordHasher.hashPassword(password);
        String second = PasswordHasher.hashPassword(password);

        assertNotEquals(first, second);

        assertTrue(
                PasswordHasher.verifyPassword(password, first)
        );

        assertTrue(
                PasswordHasher.verifyPassword(password, second)
        );
    }
}
