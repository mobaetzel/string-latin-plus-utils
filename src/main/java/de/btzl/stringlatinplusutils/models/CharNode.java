package de.btzl.stringlatinplusutils.models;

import de.btzl.stringlatinplusutils.generated.CharClass;

import java.util.HashMap;
import java.util.Map;

public class CharNode {
    private final int codePoint;
    private final CharClass charClass;
    private final int[] transliteration;
    private final Map<Integer, CharNode> followups;

    public CharNode(int codePoint, CharClass charClass, int[] transliteration, CharNode... followups) {
        this.codePoint = codePoint;
        this.charClass = charClass;
        this.transliteration = transliteration;
        this.followups = new HashMap<>();
        for (var charNode : followups) {
            this.followups.put(charNode.codePoint, charNode);
        }
    }

    public CharClass getCharClass() {
        return charClass;
    }

    public int[] getTransliteration() {
        return transliteration;
    }

    public boolean hasFollowup(int codePoint) {
        return followups.containsKey(codePoint);
    }

    public CharNode findFollowup(int codePoint) {
        return followups.get(codePoint);
    }
}
