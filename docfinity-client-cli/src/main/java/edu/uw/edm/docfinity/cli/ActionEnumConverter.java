package edu.uw.edm.docfinity.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class ActionEnumConverter implements IStringConverter<ActionEnum> {

    @Override
    public ActionEnum convert(String value) {
        ActionEnum convertedValue = ActionEnum.fromString(value);

        if (convertedValue == null) {
            throw new ParameterException(
                    "Value "
                            + value
                            + "can not be converted to ActionEnum. "
                            + "Available values are: create, update.");
        }
        return convertedValue;
    }
}
