package littleware.asset.pickle;

/**
 * Factory for PickleHuman that supports AssetType based
 * specialization of serialization - tries to implement a flywheel pattern.
 * OSGi activators should register custom picklers at startup time.
 * Concurrent calls to get() are thread safe, but registerSpecializer
 * is not safe.
 */
public interface HumanPicklerProvider extends PicklerRegistry<AssetHumanPickler> {}
