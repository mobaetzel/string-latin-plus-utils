package de.btzl.stringlatinplusutils;

import de.btzl.stringlatinplusutils.generated.CharClass;
import de.btzl.stringlatinplusutils.generated.CharNodes;

import java.util.*;

public class StringLatinPlusUtils {
    private char invalidChar = '#';
    private final Set<CharClass> allowedCharClasses = new HashSet<>();

    private StringLatinPlusUtils() {
    }

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
        var codePoints = content.codePoints().toArray();
        var sb = new StringBuilder();

        for (var currentIndex = 0; currentIndex < codePoints.length; currentIndex++) {
            var currentCodePoint = codePoints[currentIndex];
            var currentCharNode = CharNodes.root.findFollowup(currentCodePoint);

            if (currentCharNode != null && allowedCharClasses.contains(currentCharNode.getCharClass())) {
                while (currentIndex + 1 < codePoints.length && currentCharNode.hasFollowup(codePoints[currentIndex + 1])) {
                    currentIndex++;
                    currentCharNode = currentCharNode.findFollowup(codePoints[currentIndex]);
                }

                if (allowedCharClasses.contains(currentCharNode.getCharClass())) {
                    for (var tcp : currentCharNode.getTransliteration()) {
                        sb.appendCodePoint(tcp);
                    }
                } else {
                    sb.append(invalidChar);
                }
            } else {
                sb.append(invalidChar);
            }
        }

        return sb.toString();
    }
}