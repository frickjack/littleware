/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.ObjectStreamException;
import java.security.Permission;


/**
 * A sort of dynamic-enum that allows 3rd-party plugins
 * to added unique (UUID based) members to the enum at load time.
 * We implement our own Enum pattern, since we want
 * to be able to dynamically add members to the Enum.
 * Our enum's are uniquely identified by UUID, not by
 * integer ordinal - so we don't have to worry about 
 * 2 users in different locations assigning the same ID to
 * 2 different types.
 * Subtypes should attempt to maintain themselves as singletons locally,
 * but also tolerate the serialization/deserialization of new instances
 * due to RPC/etc.
 */
public abstract class DynamicEnum<T extends DynamicEnum> implements java.io.Serializable, Comparable<T> {
    private static final long serialVersionUID = 1111142L;
    private static final Logger olog_generic = Logger.getLogger(DynamicEnum.class.getName());

    /**
     * Little data-bucket for tracking information for each subtype
     */
    private static class SubtypeData<T extends DynamicEnum> {

        private Map<UUID, T> ov_id_map = new HashMap<UUID, T>();
        private Map<String, T> ov_name_map = new HashMap<String, T>();

        public SubtypeData() {
        }

        /** Return member if it exists, else null */
        public synchronized T getMember(String s_name) {
            return ov_name_map.get(s_name);
        }

        /** Return member if it exists, else null */
        public synchronized T getMember(UUID u_name) {
            return ov_id_map.get(u_name);
        }

        /** Return set of all members registered */
        public synchronized Set<T> getMembers() {
            Set<T> v_result = new HashSet<T>();
            v_result.addAll(ov_id_map.values());
            return v_result;
        }

        /**
         * Add the given type-object to the typemap if another
         * object hasn't already been registered with the given UUID.
         *
         * @param perm_join AccessController permission that the calling code-base must have
         *              in order to join the c_type enum-set - may be null
         */
        private synchronized void registerMemberIfNecessary(T n_member, Permission perm_join) {
            if (!ov_id_map.containsKey(n_member.getObjectId())) {
                Whatever.check("DynamicEnums have unique names", !ov_name_map.containsKey(n_member.getName()));
                ov_id_map.put(n_member.getObjectId(), n_member);
                ov_name_map.put(n_member.getName(), n_member);
            }
        }
    }
    private static final Map<String, SubtypeData<? extends DynamicEnum>> mv_subtypes = new HashMap<String, SubtypeData<? extends DynamicEnum>>();
    private UUID ou_id = null;
    private String os_name = null;
    private Class<T> oc_enumtype = null;

    /**
     * Add the given type-object to the global typemap if another
     * object hasn't already been registered with the given UUID.
     *
     * @param perm_join AccessController permission that the calling code-base must have
     *              in order to join the c_type enum-set - may be null
     */
    private void registerMemberIfNecessary(Class<T> c_class, Permission perm_join) {
        SubtypeData<T> x_data = null;

        synchronized (mv_subtypes) {
            x_data = (SubtypeData<T>) mv_subtypes.get ( c_class.getName () );

            if (null == x_data) {
                olog_generic.log(Level.FINE, "Registering new new DynamicEnum type: " + c_class.getName());
                x_data = new SubtypeData<T>();
                mv_subtypes.put(c_class.getName(), x_data);
            }
        }
        x_data.registerMemberIfNecessary((T) this, perm_join);
    }

    /**
     * Lookup the registered enum meber by UUID.
     *
     * @exception NoSuchThingException if type not registered
     */
    public static <T extends DynamicEnum> T getMember(UUID u_type_id, Class<T> c_class) throws NoSuchThingException {
        SubtypeData<T> x_data = null;

        synchronized (mv_subtypes) {
            x_data = (SubtypeData<T>) mv_subtypes.get ( c_class.getName () );
        }

        T n_result = null;

        if (null != x_data) {
            n_result = x_data.getMember(u_type_id);
        }
        if (null == n_result) {
            throw new NoSuchThingException();
        }
        return n_result;
    }

    /**
     * Lookup the registered enum meber by UUID.
     *
     * @exception NoSuchThingException if type not registered
     */
    public static <T extends DynamicEnum> T getMember(String s_name, Class<T> c_class) throws NoSuchThingException {
        SubtypeData<T> x_data = null;

        synchronized (mv_subtypes) {
            x_data = (SubtypeData<T>) mv_subtypes.get ( c_class.getName () );
        }

        T n_result = null;

        if (null != x_data) {
            n_result = x_data.getMember(s_name);
        }
        if (null == n_result) {
            throw new NoSuchThingException();
        }
        return n_result;
    }

    /**
     * Get the set of DynamicEnums that have been registered with the engine.
     */
    public static <T extends DynamicEnum> Set<T> getMembers(Class<T> c_class) {
        SubtypeData x_data = null;

        synchronized (mv_subtypes) {
            x_data = mv_subtypes.get(c_class.getName());
        }
        if (null == x_data) {
            return Collections.emptySet();
        }
        return x_data.getMembers();
    }

    /**
     * Do-nothing constructor intended for deserialization only.
     */
    protected DynamicEnum() {
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName().
     * The perm_join is intended to prevent unauthorized code from
     * introducing new enum-types (service types, asset types).
     *
     * @param u_id of the new member
     * @param s_name of the new member
     * @param c_type enum-type whose members this object is joining -
     *           this must be an instanceof c_type
     * @param perm_join AccessController permission that the calling code-base must have
     *              in order to join the c_type enum-set - may be null -
     *              Always ignored for now - security issues with appserver class-loader
     *              and our Class.forName mechanism for registering types.  Ugh!
     */
    protected DynamicEnum(UUID u_id, String s_name, Class<T> c_type, Permission perm_join) {
        ou_id = u_id;
        os_name = s_name;
        oc_enumtype = c_type;

        Whatever.check("Only valid subtypes may join a DynamicEnum set", c_type.isInstance(this));
        registerMemberIfNecessary(oc_enumtype, perm_join);
    }

    /**
     * Each new asset-type should have unique UUID
     */
    public UUID getObjectId() {
        return ou_id;
    }

    /**
     * Each new asset-type should have a unique name
     */
    public String getName() {
        return os_name;
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
    public int compareTo(DynamicEnum n_other) {
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
            registerMemberIfNecessary(oc_enumtype, null);
            return getMember(this.getObjectId(), oc_enumtype);
        } catch (NoSuchThingException e) {
            olog_generic.log(Level.WARNING, "Deserialization of asset-type: " + this + ", caught unexpected: " + e);
            return this;
        }
    }
}
