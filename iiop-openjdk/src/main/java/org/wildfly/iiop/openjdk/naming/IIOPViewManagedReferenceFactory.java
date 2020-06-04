package org.wildfly.iiop.openjdk.naming;

public interface IIOPViewManagedReferenceFactory extends ManagedReferenceFactory {

    String DEFAULT_IIOP_VIEW_INSTANCE_VALUE = "?";

    /**
     * Retrieves the reference's object instance JNDI View value.
     *
     * If it's not possible to obtain such data, the factory should return the
     * static attribute DEFAULT_JNDI_VIEW_INSTANCE_VALUE, exposed by this interface.
     *
     * @return
     */

    String getIIOPViewInstanceValue();

}
