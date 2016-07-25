package littleware.base;

import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ObjectStreamException;
import java.util.stream.Collectors;


/**
 * A sort of dynamic-enum that allows 3rd-party plugins
 * to add unique (UUID based) members to the enum at load time.
 * Subtypes should attempt to maintain themselves as singletons locally,
 * but also tolerate the serialization/deserialization of new instances
 * due to RPC/etc.
 */
public abstract class DynamicEnum<T extends DynamicEnum> implements java.io.Serializable, Comparable<T> {
    private static final long serialVersionUID = 1111142L;
    private static final Logger log = Logger.getLogger(DynamicEnum.class.getName());

    /**
     * Little data-bucket for tracking information for each subtype
     */
    private static class SubtypeData {

        private final Map<UUID, DynamicEnum> idMap = new HashMap<>();
        private final Map<String, DynamicEnum> nameMap = new HashMap<>();
        private final Class<? extends DynamicEnum> clazz;

        public SubtypeData( Class<? extends DynamicEnum> clazz ) {
            this.clazz = clazz;
        }

        /** Return member if it exists, else null */
        public synchronized DynamicEnum getMember(String name) {
            return nameMap.get(name);
        }

        /** Return member if it exists, else null */
        public synchronized DynamicEnum getMember(UUID id) {
            return idMap.get(id);
        }

        /** Return set of all members registered */
        public synchronized ImmutableSet<DynamicEnum> getMembers() {
            final ImmutableSet<DynamicEnum> resultSet = ImmutableSet.copyOf( idMap.values() );
            return resultSet;
        }

        /**
         * Add the given type-object to the typemap if another
         * object hasn't already been registered with the given UUID.
         */
        private synchronized void registerMemberIfNecessary(DynamicEnum member ) {
            if (!idMap.containsKey(member.getObjectId())) {
                if( nameMap.containsKey(member.getName())) {
                    throw new IllegalArgumentException( "Dynamic enum member name not unique" + member.getName() );
                }
                idMap.put(member.getObjectId(), clazz.cast( member ) );
                nameMap.put(member.getName(), clazz.cast( member ) );
            }
        }
    }
    private static final Map<String, SubtypeData> subtypesByName = new HashMap<>();
    private UUID id = null;
    private String name = null;
    private Class<T> clazz = null;

    /**
     * Add the given type-object to the global typemap if another
     * object hasn't already been registered with the given UUID.
     */
    private void registerMemberIfNecessary(Class<T> clazz) {
        SubtypeData data;

        synchronized (subtypesByName) {
            data = subtypesByName.get ( clazz.getName () );

            if (null == data) {
                log.log(Level.FINE, "Registering new new DynamicEnum type: {0}", clazz.getName());
                data = new SubtypeData( clazz );
                subtypesByName.put(clazz.getName(), data);
            }
        }
        data.registerMemberIfNecessary( this );
    }

    /**
     * Lookup the registered enum member by UUID.
     */
    public static <T extends DynamicEnum> Optional<T> getOptMember(UUID id, Class<T> clazz ) {
        throw new UnsupportedOperationException( "not yet implemented" );
    }

    /**
     * Lookup the registered enum member by UUID.
     *
     * @throws NoSuchThingException if type not registered
     */
    public static <T extends DynamicEnum> T getMember(UUID id, Class<T> clazz) throws NoSuchThingException {
        final SubtypeData subTypeData;

        synchronized (subtypesByName) {
            subTypeData = subtypesByName.get ( clazz.getName () );
        }

        final T result;

        if (null != subTypeData) {
            result = clazz.cast( subTypeData.getMember(id) );
        } else { result = null; }
        if (null == result) {
            throw new NoSuchThingException( "No member with id: " + id );
        }
        return result;
    }

    /**
     * Lookup the registered enum meber by UUID.
     */
    public static <T extends DynamicEnum> Optional<T> getOptMember(String name, Class<T> clazz){
        final SubtypeData subtypeData;

        synchronized (subtypesByName) {
            subtypeData = subtypesByName.get ( clazz.getName () );
        }

        final T result;

        if (null != subtypeData) {
            result = clazz.cast( subtypeData.getMember(name) );
        } else { result = null; }
        return Optional.ofNullable(result);
    }
    
    /**
     * Lookup the registered enum meber by UUID.
     *
     * @throws NoSuchThingException if type not registered
     */
    public static <T extends DynamicEnum> T getMember(String name, Class<T> clazz) throws NoSuchThingException {
        final Optional<T> opt = getOptMember( name, clazz );
        if ( ! opt.isPresent() ) {
            throw new NoSuchThingException( "No " + clazz.getName() + " with name: " + name );
        }
        return opt.get();
    }

    /**
     * Get the set of DynamicEnums that have been registered with the engine.
     */
    public static <T extends DynamicEnum> Set<T> getMembers(Class<T> clazz) {
        final SubtypeData subtypeData;
        synchronized (subtypesByName) {
            subtypeData = subtypesByName.get(clazz.getName());
        }
        if (null == subtypeData) {
            return Collections.emptySet();
        }
        return subtypeData.getMembers().stream().map( (entry) -> { return clazz.cast(entry); } ).collect( Collectors.toSet() );
    }

    /**
     * Do-nothing constructor intended for deserialization only.
     */
    protected DynamicEnum() {
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName().
     *
     * @param id of the new member
     * @param name of the new member
     * @param clazz enum-type whose members this object is joining -
     *           this must be an instanceof clazz
     */
    protected DynamicEnum(UUID id, String name, Class<T> clazz) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;

        if ( ! clazz.isInstance(this)) {
            throw new IllegalArgumentException( "Only valid subtypes may join a DynamicEnum set" );
        }
        registerMemberIfNecessary(clazz);
    }

    /**
     * Each new asset-type should have unique UUID
     */
    public UUID getObjectId() {
        return id;
    }

    /**
     * Each new asset-type should have a unique name
     */
    public String getName() {
        return name;
    }

    /**
     * Just return getName()
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Equality is based on UUID comparison
     */
    @Override
    public boolean equals(Object x_other) {
        return (null != x_other) && (x_other instanceof DynamicEnum) && ((DynamicEnum) x_other).getObjectId().equals(this.getObjectId());
    }

    /**
     * Hash on the UUID object id
     */
    @Override
    public int hashCode() {
        return getObjectId().hashCode();
    }

    /**
     * Sort based on getName() first, fall through to getObjectId()
     * if names are equal.
     */
    @Override
    public int compareTo(T n_other) {
        int i_result = getName().compareTo(n_other.getName());

        if (0 != i_result) {
            return i_result;
        }
        return getObjectId().compareTo(n_other.getObjectId());
    }

    /**
     * Attempt to keep us a singleton on deserialization
     */
    public Object readResolve() throws ObjectStreamException {
        try {
            registerMemberIfNecessary(clazz);
            return getMember(this.getObjectId(), clazz);
        } catch (NoSuchThingException ex) {
            log.log(Level.WARNING, "Deserialization of asset-type: " + this, ex);
            return this;
        }
    }
}
