package com.banque.abc.tpe.entity.enums;

public enum TypeTPE {
    PHYSIQUE,
    ECOMMERCE,
    TPE,
    MOBILE;

    public TypeTPE canonical() {
        return switch (this) {
            case PHYSIQUE -> TPE;
            case ECOMMERCE -> MOBILE;
            default -> this;
        };
    }
}
