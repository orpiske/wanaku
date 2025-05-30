package ai.wanaku.core.services.discovery;

import ai.wanaku.api.exceptions.WanakuException;
import ai.wanaku.api.types.WanakuResponse;
import ai.wanaku.api.types.providers.ServiceTarget;
import ai.wanaku.core.service.discovery.client.DiscoveryService;
import ai.wanaku.core.services.io.InstanceDataManager;
import ai.wanaku.core.services.io.ServiceEntry;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import static ai.wanaku.core.services.common.ServicesHelper.waitAndRetry;

public class DefaultRegistrationManager implements RegistrationManager {
    private static final Logger LOG = Logger.getLogger(DefaultRegistrationManager.class);

    private final DiscoveryService service;
    private ServiceTarget target;
    private int retries;
    private int waitSeconds;
    private final InstanceDataManager instanceDataManager;
    private volatile boolean registered;
    private final ReentrantLock lock = new ReentrantLock();


    public DefaultRegistrationManager(DiscoveryService service, ServiceTarget target,
            int retries, int waitSeconds, String dataDir) {
        this.service = service;
        this.target = target;

        this.retries = retries;
        this.waitSeconds = waitSeconds;

        instanceDataManager = new InstanceDataManager(dataDir, target.getService());

        if (instanceDataManager.dataFileExists()) {
            final ServiceEntry serviceEntry = instanceDataManager.readEntry();
            if (serviceEntry == null) {
                registered = false;
            } else {
                registered = true;
            }
        } else {
            registered = false;
            try {
                instanceDataManager.createDataDirectory();
            } catch (IOException e) {
                throw new WanakuException(e);
            }
        }
    }

    private void tryRegistering() {
        do {
            try {
                final RestResponse<WanakuResponse<ServiceTarget>> response = service.register(target);
                final WanakuResponse<ServiceTarget> entity = response.getEntity();
                target = entity.data();
                registered = true;
            } catch (Exception e) {
                retries = waitAndRetry(target.getService(), e, retries, waitSeconds);
            }
        } while (!registered && (retries > 0));
    }

    private boolean isRegistered() {
        return registered;
    }

    @Override
    public void register() {
        if (isRegistered()) {
            ping();
        } else {
            LOG.debugf("Registering %s service %s with address %s", target.getServiceType().asValue(), target.getService(), target.toAddress());
            try {
                if (!lock.tryLock(1, TimeUnit.SECONDS)) {
                    LOG.warnf("Could not obtain a registration lock in 1 second. Giving up ...");
                }
                tryRegistering();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }

            instanceDataManager.writeEntry(target);
        }
    }

    @Override
    public void deregister() {
        service.deregister(target);
    }

    @Override
    public void ping() {
        LOG.infof("Should ping service ...");
    }

    @Override
    public void lastAsFail(String reason) {

    }

    @Override
    public void lastAsSuccessful() {

    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }
}
