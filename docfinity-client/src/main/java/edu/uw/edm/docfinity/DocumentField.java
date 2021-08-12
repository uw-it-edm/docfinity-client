package edu.uw.edm.docfinity;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentField {
    /** The name of the document field to index. */
    private String name;

    /** The value of the document field to index. */
    private List<Object> values;

    /**
    * TODO: comments.
    *
    * @param name
    * @param value
    */
    public DocumentField(String name, Object value) {
        this.name = name;
        this.values = Arrays.asList(value);
    }
}
