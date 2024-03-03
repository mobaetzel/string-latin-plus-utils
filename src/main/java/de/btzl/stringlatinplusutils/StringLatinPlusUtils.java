package de.btzl.stringlatinplusutils;

import de.btzl.stringlatinplusutils.generated.CharClass;
import de.btzl.stringlatinplusutils.generated.CharNodes;
import de.btzl.stringlatinplusutils.models.CharNode;

import java.util.*;

public class StringLatinPlusUtils {
    private int[] codePoints;
    private int currentIndex = 0;
    private char invalidChar = '#';
    private final Set<CharClass> allowedCharClasses = new HashSet<>();
    private StringBuilder stringBuilder = new StringBuilder();

    private StringLatinPlusUtils() {}

    public static StringLatinPlusUtils build() {
        return new StringLatinPlusUtils();
    }

    public StringLatinPlusUtils withCharClasses(CharClass... charClass) {
        allowedCharClasses.addAll(Arrays.asList(charClass));
        return this;
    }

    public StringLatinPlusUtils withAllCharClasses() {
        return withCharClasses(CharClass.values());
    }

    public StringLatinPlusUtils withInvalidChar(char invalidChar) {
        this.invalidChar = invalidChar;
        return this;
    }

    public String transform(String content) {
        this.codePoints = content.codePoints().toArray();
        this.stringBuilder = new StringBuilder();

        while (currentIndex < codePoints.length) {
            var charNode = getCharNodeAt(currentIndex);
            if (charNode.isPresent()) {
                for (var cp : charNode.get().getMappedCodePoints()) {
                    stringBuilder.appendCodePoint(cp);
                }
            } else {
                stringBuilder.append(invalidChar);
            }
            currentIndex++;
        }

        return this.stringBuilder.toString();
    }



    private Optional<CharNode> getCharNodeAt(int index) {
        if (index < 0 || index + 1 > codePoints.length) {
            return Optional.empty();
        }

        var codePoint = codePoints[index];
        if (CharNodes.root.hasFollowupNode(codePoint)) {
            var currentCharNode = CharNodes.root.getFollowupNode(codePoint);
            if (allowedCharClasses.contains(currentCharNode.getCharClass())) {
                return Optional.of(currentCharNode);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}