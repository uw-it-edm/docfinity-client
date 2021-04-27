package edu.uw.edm.docfinity.cli;

public enum ActionEnum {
    create,
    update;

    public static ActionEnum fromString(String code) {
        for (ActionEnum output : ActionEnum.values()) {
            if (output.toString().equalsIgnoreCase(code)) {
                return output;
            }
        }

        return null;
    }
}
