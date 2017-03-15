package mr.x.commons.utils;

import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Reilost on 4/25/14.
 */
class HtmlCharacterEntityReferences {

    private static final String PROPERTIES_FILE = "HtmlCharacterEntityReferences.properties";

    static final char REFERENCE_START = '&';

    static final String DECIMAL_REFERENCE_START = "&#";

    static final String HEX_REFERENCE_START = "&#x";

    static final char REFERENCE_END = ';';

    static final char CHAR_NULL = (char) -1;


    private final String[] characterToEntityReferenceMap = new String[3000];

    private final Map<String, Character> entityReferenceToCharacterMap = new HashMap<String, Character>(252);


    /**
     * Returns a new set of character entity references reflecting the HTML 4.0 character set.
     */
    public HtmlCharacterEntityReferences() {
        Properties entityReferences = new Properties();

        // Load reference definition file
        InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if (is == null) {
            throw new IllegalStateException(
                    "Cannot find reference definition file [HtmlCharacterEntityReferences.properties] as class path resource");
        }
        try {
            try {
                entityReferences.load(is);
            }
            finally {
                is.close();
            }
        }
        catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to parse reference definition file [HtmlCharacterEntityReferences.properties]: " +  ex.getMessage());
        }

        // Parse reference definition properties
        Enumeration keys = entityReferences.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            int referredChar = Integer.parseInt(key);
            Assert.isTrue((referredChar < 1000 || (referredChar >= 8000 && referredChar < 10000)),
                    "Invalid reference to special HTML entity: " + referredChar);
            int index = (referredChar < 1000 ? referredChar : referredChar - 7000);
            String reference = entityReferences.getProperty(key);
            this.characterToEntityReferenceMap[index] = REFERENCE_START + reference + REFERENCE_END;
            this.entityReferenceToCharacterMap.put(reference, new Character((char) referredChar));
        }
    }


    /**
     * Return the number of supported entity references.
     */
    public int getSupportedReferenceCount() {
        return this.entityReferenceToCharacterMap.size();
    }

    /**
     * Return true if the given character is mapped to a supported entity reference.
     */
    public boolean isMappedToReference(char character) {
        return (convertToReference(character) != null);
    }

    /**
     * Return the reference mapped to the given character or <code>null</code>.
     */
    public String convertToReference(char character) {
        if (character < 1000 || (character >= 8000 && character < 10000)) {
            int index = (character < 1000 ? character : character - 7000);
            String entityReference = this.characterToEntityReferenceMap[index];
            if (entityReference != null) {
                return entityReference;
            }
        }
        return null;
    }

    /**
     * Return the char mapped to the given entityReference or -1.
     */
    public char convertToCharacter(String entityReference) {
        Character referredCharacter = this.entityReferenceToCharacterMap.get(entityReference);
        if (referredCharacter != null) {
            return referredCharacter.charValue();
        }
        return CHAR_NULL;
    }

}
