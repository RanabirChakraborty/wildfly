package org.wildfly.iiop.openjdk.naming;

public interface ManagedReference {
    /**
     * Release the reference. Depending on the implementation this may destroy the
     * underlying object.
     * <p/>
     * Implementers should take care to make this method idempotent, as it may be
     * called multiple times.
     */
    void release();

    /**
     * Get the object instance.
     *
     * @return the object this reference refers to
     */
    Object getInstance();
}
