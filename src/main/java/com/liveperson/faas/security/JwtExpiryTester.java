package com.liveperson.faas.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.Date;

public class JwtExpiryTester implements AuthExpiryTester {

    @Override
    public boolean isExpired(String token) {
        return JWT.decode(token).getExpiresAt().before(new Date(System.currentTimeMillis()));
    }

    @Override
    public boolean isAboutToExpire(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Date expiresAt = jwt.getExpiresAt();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.add(Calendar.MINUTE, 30);
        Date checkDate = cal.getTime();
        return checkDate.after(expiresAt);
    }
}
