package io.github.altkat.advancementannouncer.Handlers;

/**
 * A simple data class to hold the result from CustomModelDataResolver.
 */
public class ResolvedIconData {
    private final int value;
    private final String rawValue;
    private final Source source;

    public ResolvedIconData(int value, String rawValue, Source source) {
        this.value = value;
        this.rawValue = rawValue;
        this.source = source;
    }

    public int getValue() {
        return value;
    }

    public String getRawValue() {
        return rawValue;
    }

    public Source getSource() {
        return source;
    }

    public enum Source {
        DIRECT,
        ITEMSADDER,
        NEXO
    }
}