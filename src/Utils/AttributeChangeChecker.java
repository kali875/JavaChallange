package Utils;

import challenge.game.event.attibute.AttributeChange;

import java.util.List;

public class AttributeChangeChecker {
    private List<AttributeChange> attributeChanges;
    public AttributeChangeChecker(List<AttributeChange> attributeChanges) {
        this.attributeChanges = attributeChanges;
    }
    public boolean contains(String attribute_name, String attribute_value) {
        for (AttributeChange attributeChange : attributeChanges) {
            if (attributeChange.getName().equals(attribute_name) && attributeChange.getValue().equals(attribute_value)) return true;
        }
        return false;
    }

    public String contains(String attribute_name) {
        for (AttributeChange attributeChange : attributeChanges) {
            if (attributeChange.getName().equals(attribute_name)) return attributeChange.getValue();
        }
        return "false";
    }
}
