package org.wildfly.iiop.openjdk.naming.management;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.naming.NamingContext;
import org.jboss.as.naming.NamingStore;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.logging.NamingLogger;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;
import org.wildfly.iiop.openjdk.naming.IIOPViewManagedReferenceFactory;
import org.wildfly.iiop.openjdk.naming.ManagedReferenceFactory;

public class IIOPViewOperation implements OperationStepHandler {
    public static final IIOPViewOperation INSTANCE = new IIOPViewOperation();
    public static final String OPERATION_NAME = "iiop-view";

    @Override
    public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
        final ModelNode resultNode = context.getResult();

        if (context.isNormalServer()) {
            context.addStep(new OperationStepHandler() {
                @Override
                public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
                    final ServiceRegistry serviceRegistry = context.getServiceRegistry(false);

                    final ModelNode contextsNode = resultNode.get("java: contexts");

                    final ServiceController<?> javaContextService = serviceRegistry.getService(ContextNames.JAVA_CONTEXT_SERVICE_NAME);
                    final NamingStore javaContextNamingStore = NamingStore.class.cast(javaContextService.getValue());
                    try {
                        addEntries(contextsNode.get("java:"), new NamingContext(javaContextNamingStore, null));
                    } catch (NamingException e) {
                        throw new OperationFailedException(e, new ModelNode().set(NamingLogger.ROOT_LOGGER.failedToReadContextEntries("java:")));
                    }

                    final ServiceController<?> jbossContextService = serviceRegistry.getService(ContextNames.JBOSS_CONTEXT_SERVICE_NAME);
                    final NamingStore jbossContextNamingStore = NamingStore.class.cast(jbossContextService.getValue());
                    try {
                        addEntries(contextsNode.get("java:jboss"), new NamingContext(jbossContextNamingStore, null));
                    } catch (NamingException e) {
                        throw new OperationFailedException(e, new ModelNode().set(NamingLogger.ROOT_LOGGER.failedToReadContextEntries("java:jboss")));
                    }

                    final ServiceController<?> exportedContextService = serviceRegistry.getService(ContextNames.EXPORTED_CONTEXT_SERVICE_NAME);
                    final NamingStore exportedContextNamingStore = NamingStore.class.cast(exportedContextService.getValue());
                    try {
                        addEntries(contextsNode.get("java:jboss/exported"), new NamingContext(exportedContextNamingStore, null));
                    } catch (NamingException e) {
                        throw new OperationFailedException(e, new ModelNode().set(NamingLogger.ROOT_LOGGER.failedToReadContextEntries("java:jboss/exported")));
                    }

                    final ServiceController<?> globalContextService = serviceRegistry.getService(ContextNames.GLOBAL_CONTEXT_SERVICE_NAME);
                    final NamingStore globalContextNamingStore = NamingStore.class.cast(globalContextService.getValue());
                    try {
                        addEntries(contextsNode.get("java:global"), new NamingContext(globalContextNamingStore, null));
                    } catch (NamingException e) {
                        throw new OperationFailedException(e, new ModelNode().set(NamingLogger.ROOT_LOGGER.failedToReadContextEntries("java:global")));
                    }

                    final ServiceController<?> extensionRegistryController = serviceRegistry
                            .getService(IIOPViewExtensionRegistry.SERVICE_NAME);
                    if(extensionRegistryController != null) {
                        final IIOPViewExtensionRegistry extensionRegistry = IIOPViewExtensionRegistry.class
                                .cast(extensionRegistryController.getValue());

                        for (IIOPViewExtension extension : extensionRegistry.getExtensions()) {
                            extension.execute(new IIOPViewExtensionContext() {
                                @Override
                                public OperationContext getOperationContext() {
                                    return context;
                                }

                                @Override
                                public ModelNode getResult() {
                                    return resultNode;
                                }

                                @Override
                                public void addEntries(ModelNode current, Context context) throws NamingException{
                                    IIOPViewOperation.this.addEntries(current, context);
                                }
                            });
                        }
                    }
                    context.completeStep(OperationContext.RollbackHandler.NOOP_ROLLBACK_HANDLER);
                }
            }, OperationContext.Stage.RUNTIME);
        } else {
            throw new OperationFailedException(NamingLogger.ROOT_LOGGER.IIOPViewNotAvailable());
        }
    }

    private void addEntries(final ModelNode current, final Context context) throws NamingException {
        final NamingEnumeration<NameClassPair> entries = context.list("");
        while (entries.hasMore()) {
            final NameClassPair pair = entries.next();

            final ModelNode node = current.get(pair.getName());
            node.get("class-name").set(pair.getClassName());
            try {
                final Object value;
                if(context instanceof NamingContext) {
                    value = ((NamingContext)context).lookup(pair.getName(), false);
                } else {
                    value = context.lookup(pair.getName());
                }
                if (value instanceof Context) {
                    addEntries(node.get("children"), Context.class.cast(value));
                } else if (value instanceof Reference) {
                    //node.get("value").set(value.toString());
                } else {
                    String IIOPViewValue = IIOPViewManagedReferenceFactory.DEFAULT_IIOP_VIEW_INSTANCE_VALUE;
                    if (value instanceof IIOPViewManagedReferenceFactory) {
                       try {
                           IIOPViewValue = IIOPViewManagedReferenceFactory.class.cast(value)
                                   .getIIOPViewInstanceValue();
                       }
                       catch (Throwable e) {
                           // just log, don't stop the operation
                           NamingLogger.ROOT_LOGGER.failedToLookupIIOPViewValue(pair.getName(),e);
                       }
                    } else if (!(value instanceof ManagedReferenceFactory)) {
                       IIOPViewValue = String.valueOf(value);
                    }
                    node.get("value").set(IIOPViewValue);
                }
            } catch (NamingException e) {
                // just log, don't stop the operation
                NamingLogger.ROOT_LOGGER.failedToLookupIIOPViewValue(pair.getName(),e);
            }
        }
    }
}
