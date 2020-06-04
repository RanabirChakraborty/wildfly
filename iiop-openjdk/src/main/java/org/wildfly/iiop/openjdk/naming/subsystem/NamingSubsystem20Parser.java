package org.wildfly.iiop.openjdk.naming.subsystem;

import org.jboss.as.naming.subsystem.NamingSubsystemNamespace;

class NamingSubsystem20Parser extends NamingSubsystem14Parser {

    NamingSubsystem20Parser() {
        super(NamingSubsystemNamespace.NAMING_2_0);
    }
}
