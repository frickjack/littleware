package littleware.asset.validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import littleware.asset.AssetBuilder;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.validate.AbstractValidator;
import littleware.base.validate.Validator;

/**
 * Validate an AssetBuilder's link, data, and attribute maps
 * have legal keys and values
 */
public class AssetAttrValidator {

    public Validator build(final AssetBuilder builderIn) {
        final AbstractAssetBuilder<? extends AssetBuilder> builder = (AbstractAssetBuilder) builderIn;
        return new AbstractValidator() {

            @Override
            public Collection<String> checkIfValid() {
                for (Map<String, ?> index : Arrays.asList(builder.getLinkMap(),
                        builder.getDateMap(), builder.getAttributeMap())) {
                    if (index.size() > 15) {
                        return Collections.singletonList("Attr map exceeds 15 entries");
                    }
                    for (String key : index.keySet()) {
                        if (key.length() > 20) {
                            return Collections.singletonList("Illegal key - 20 char limit: " + key);
                        }
                    }
                }
                for (String value : builder.getAttributeMap().values()) {
                    if (value.length() > 128) {
                        return Collections.singletonList("Illegal value - 128 char limit: " + value);
                    }
                }
                return Collections.emptyList();
            }
        };
    }
}
