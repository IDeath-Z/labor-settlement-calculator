package converters;

import java.math.BigDecimal;

import org.junit.jupiter.params.converter.SimpleArgumentConverter;

public class BigDecimalConverter extends SimpleArgumentConverter {
    
    @Override
    protected Object convert(Object source, Class<?> targetType) {
        if (source == null)
            return null;
        
        return new BigDecimal((String) source);
    }
}
