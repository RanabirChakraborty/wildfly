package org.wildfly.iiop.openjdk.naming.subsystem;

import java.util.EnumSet;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleOperationDefinition;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.naming.NamingStore;
import org.wildfly.iiop.openjdk.naming.management.IIOPViewOperation;
import org.jboss.as.naming.service.NamingService;
import org.jboss.as.naming.subsystem.NamingSubsystemAdd;
import org.jboss.as.naming.subsystem.NamingSubsystemRemove;
import org.jboss.dmr.ModelType;

public class NamingSubsystemRootResourceDefinition extends SimpleResourceDefinition {

    enum Capability {
        NAMING_STORE(NamingService.CAPABILITY_NAME, NamingStore.class),;

        private final RuntimeCapability<?> definition;

        Capability(String name, Class<?> type) {
            this.definition = RuntimeCapability.Builder.of(name, type).build();
        }

        RuntimeCapability<?> getDefinition() {
            return this.definition;
        }
    }

    static final SimpleOperationDefinition IIOP_VIEW = new SimpleOperationDefinitionBuilder(IIOPViewOperation.OPERATION_NAME,NamingExtension.getResourceDescriptionResolver(NamingExtension.SUBSYSTEM_NAME))
                    .addAccessConstraint(NamingExtension.IIOP_VIEW_CONSTRAINT)
                    .setReadOnly()
                    .setRuntimeOnly()
                    .setReplyType(ModelType.LIST)
                    .setReplyValueType(ModelType.STRING)
                    .build();

    NamingSubsystemRootResourceDefinition() {
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, NamingExtension.SUBSYSTEM_NAME),
                NamingExtension.getResourceDescriptionResolver(NamingExtension.SUBSYSTEM_NAME),
                new NamingSubsystemAdd(), new NamingSubsystemRemove());
    }

    @Override
    public void registerCapabilities(ManagementResourceRegistration registration) {
        super.registerCapabilities(registration);

        for (Capability capability : EnumSet.allOf(Capability.class)) {
            RuntimeCapability<?> definition = capability.getDefinition();
            registration.registerCapability(definition);
        }
    }
}
