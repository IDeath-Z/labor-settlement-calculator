package com.deathz.laborcalc.domain.enums;

public enum ReportFormat {

    SPREADSHEET("SPREADSHEET"),
    PDF("PDF"),
    UNSUPPORTED("UNSUPPORTED"); // Only for testing purposes

        private final String code;

    ReportFormat(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReportFormat fromCode(String code) {
        for (ReportFormat format : ReportFormat.values()) {
            if (format.code.equals(code)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown ReportFormat code: " + code);
    }
}
