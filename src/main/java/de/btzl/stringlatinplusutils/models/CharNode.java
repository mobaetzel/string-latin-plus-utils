package de.btzl.stringlatinplusutils.models;

import de.btzl.stringlatinplusutils.generated.CharClass;

import java.util.HashMap;
import java.util.Map;

public class CharNode {
    private final int codePoint;
    private final CharClass charClass;
    private final int[] mappedCodePoints;
    private final CharNode[] followupNodes;
    private final Map<Integer, Integer> mappedFollowupCodePointsMap;

    public CharNode(int codePoint, CharClass charClass, int[] mappedCodePoints, CharNode... followupNodes) {
        this.codePoint = codePoint;
        this.charClass = charClass;
        this.mappedCodePoints = mappedCodePoints;
        this.followupNodes = followupNodes;
        this.mappedFollowupCodePointsMap = new HashMap<>();
        for (int i = 0; i < followupNodes.length; i++) {
            mappedFollowupCodePointsMap.put(followupNodes[i].codePoint, i);
        }
    }

    public CharNode getFollowupNode(int codePoint) {
        return followupNodes[mappedFollowupCodePointsMap.get(codePoint)];
    }

    public boolean hasFollowupNode(int codePoint) {
        return mappedFollowupCodePointsMap.containsKey(codePoint);
    }

    public boolean hasFollowupNodes() {
        return followupNodes.length > 0;
    }

    public int getCodePoint() {
        return codePoint;
    }

    public CharClass getCharClass() {
        return charClass;
    }

    public int[] getMappedCodePoints() {
        return mappedCodePoints;
    }
}
