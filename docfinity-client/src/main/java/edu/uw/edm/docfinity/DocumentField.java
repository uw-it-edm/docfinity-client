package edu.uw.edm.docfinity;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents a metadata name and value exposed to user. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentField {
    /** The name of the document field. */
    private String name;

    /** The list of values of the document field. */
    private List<Object> values;

    /** Creates a new DocumentField with the given name and value. */
    public static DocumentField fromSingleValue(String name, Object value) {
        Preconditions.checkNotNull(name, "name is required.");
        Preconditions.checkNotNull(
                value, "value is required. Set the 'values' field to null if fields needs to be cleared.");

        List<Object> values = new ArrayList<>();
        values.add(value);
        return new DocumentField(name, values);
    }
}
