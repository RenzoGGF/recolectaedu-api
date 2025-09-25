package com.recolectaedu.model.enums;

public enum Periodo {
    verano(0),
    primer(1),
    segundo(2);

    private final int valor;

    Periodo(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
