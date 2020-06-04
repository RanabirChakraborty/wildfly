package org.wildfly.iiop.openjdk.naming;

public interface ManagedReferenceFactory {

    /**
     * Get a new managed instance reference.
     *
     * @return a reference to a managed object
     */
    ManagedReference getReference();
}
