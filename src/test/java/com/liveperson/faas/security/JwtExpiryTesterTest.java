package com.liveperson.faas.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JwtExpiryTesterTest {

    private AuthExpiryTester authExpiryTester = new JwtExpiryTester();

    @Test
    public void isExpiredTrue() throws JWTCreationException {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create()
                .withIssuer("auth0")
                .withExpiresAt(new Date(System.currentTimeMillis() - 10000000))
                .sign(algorithm);

        assertTrue(authExpiryTester.isExpired(token));
    }

    @Test
    public void isExpiredFalse() throws JWTCreationException {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create()
                .withIssuer("auth0")
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000000))
                .sign(algorithm);

        assertFalse(authExpiryTester.isExpired(token));
    }

    @Test
    public void isAboutToExpireTrue() throws JWTCreationException {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create()
                .withIssuer("auth0")
                .withExpiresAt(new Date(System.currentTimeMillis() + 100000))
                .sign(algorithm);

        assertTrue(authExpiryTester.isAboutToExpire(token));
    }

    @Test
    public void isAboutToExpireFalse() {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        String token = JWT.create()
                .withIssuer("auth0")
                .withExpiresAt(new Date(System.currentTimeMillis() + 10000000))
                .sign(algorithm);

        assertFalse(authExpiryTester.isAboutToExpire(token));
    }
}