package consulo.spring.impl.web.endpoint;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringUriTemplateVariable {
    private final String myName;
    private final int myStartOffset;
    private final int myEndOffset;

    public SpringUriTemplateVariable(String name, int startOffset, int endOffset) {
        myName = name;
        myStartOffset = startOffset;
        myEndOffset = endOffset;
    }

    public String getName() {
        return myName;
    }

    public int getStartOffset() {
        return myStartOffset;
    }

    public int getEndOffset() {
        return myEndOffset;
    }

    @Override
    public String toString() {
        return "{" + myName + "}@" + myStartOffset;
    }
}
