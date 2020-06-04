package org.wildfly.iiop.openjdk.naming.management;

import java.util.ArrayList;
import java.util.List;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class IIOPViewExtensionRegistry implements Service<IIOPViewExtensionRegistry> {
    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("iiop-view", "extension", "registry");
    private List<IIOPViewExtension> extensions;

    @Override
    public synchronized void start(StartContext startContext) throws StartException {
        this.extensions = new ArrayList<IIOPViewExtension>();
    }

    @Override
    public synchronized void stop(StopContext stopContext) {
        this.extensions = null;
    }

    @Override
    public IIOPViewExtensionRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    List<IIOPViewExtension> getExtensions() {
        return this.extensions;
    }

    public void addExtension(final IIOPViewExtension extension) {
        this.extensions.add(extension);
    }

    public void removeExtension(final IIOPViewExtension extension) {
        this.extensions.remove(extension);
    }
}