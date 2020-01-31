package com.liveperson.faas.client.types;

import java.time.LocalDateTime;

public class FaaSEventImplementedExpiry {
    private boolean isImplemented;
    private LocalDateTime expirationDate;

    public boolean isImplemented() {
        return isImplemented;
    }

    public void setImplemented(boolean implemented) {
        isImplemented = implemented;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

}
