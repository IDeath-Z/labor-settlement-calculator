package converters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.params.converter.SimpleArgumentConverter;

public class LocalDateConverter extends SimpleArgumentConverter {
    
    @Override
    protected Object convert(Object source, Class<?> targetType) {
        if (source == null)
            return null;
        
        return LocalDate.parse(source.toString(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
