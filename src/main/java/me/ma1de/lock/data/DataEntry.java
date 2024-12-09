package me.ma1de.lock.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class DataEntry {
    private String id;
    private Object value;

    /**
     * Don't use this method unless you're absolutely sure
     * that the type of the `value` will be what you're
     * trying to parse it as.
     * @return value
     **/
    @SuppressWarnings("unchecked")
    public <T> T getValueAs() {
        return (T) value;
    }
}