package com.freightos.fms.domain.housebl.enums;

public enum HandlingInfoCode {
    A("A", "ATTACHED : COMM INV & P/LIST"),
    D("D", "PLS CTC CNEE IMMY UPON ARRVL"),
    E("E", "DOC'S ATTACHED WITH MAWB"),
    F("F", "ATTACHED : COMM INV & PACKING LIST & C/O"),
    G("G", "ATTACHED : COMM INV"),
    H("H", "ATTACHED : COMM INV & PACKING LIST & C/O & E/P"),
    I("I", "ATTACHED : POUCH"),
    J("J", "INVOICE, PACKING LIST ATTD"),
    K("K", "TOTAL : (  ) PACKAGES ONLY. ONE POUCH OF DOCUMENT ATTACHED."),
    M("M", "ATT:INVOICE & P/LIST."),
    N("N", "ATT:ENVLP"),
    O("O", "ATT:INVOICE  P/LIST DANGEROUS GOODS AS PER ATTACHED SHIPPER S DECLARATION"),
    P("P", "ATT:INVOICE  P/LIST DANGEROUS GOODS AS PER ATTACHED DGD - CARGO AIRCRAFT ONLY."),
    C("C", "ATT.(1)ENV.,NO PARTS OF THE PACKAGE CONTENTS IS DANGEROUS"),
    B("B", "NO ATTACHED DOCS");

    private final String code;
    private final String description;

    HandlingInfoCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static HandlingInfoCode fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (HandlingInfoCode h : values()) if (h.code.equals(code)) return h;
        throw new IllegalArgumentException("Unknown HandlingInfoCode: " + code);
    }
}
