package org.example;

public class UserRegistrationService {
    private final DatabaseCommunicator databaseCommunicator;

    private final DiskService diskService;

    private final LoadBalancer loadBalancer;

    private int idCounter;

    private final String location;

    public UserRegistrationService(DiskService diskService, DatabaseCommunicator databaseCommunicator, LoadBalancer loadBalancer) {
        location = BootstrapProperties.INSTANCE.getUserDataLocation();
        if (!diskService.doesFileExist(location)) {
            diskService.createDirectory(location);
        }

        if (diskService.doesFileExist(location + "idCounter.json")) {
            idCounter = diskService.read(location + "idCounter.json", Integer.class);
        } else {
            idCounter = 0;
        }
        this.diskService = diskService;
        this.databaseCommunicator = databaseCommunicator;
        this.loadBalancer = loadBalancer;
    }

    public User register(String password) {
        idCounter++;
        String node = loadBalancer.getNextNodeName();
        User user = new User();
        user.setUserId(idCounter);
        user.setPassword(password);
        user.setHostName(node);

        diskService.write(location + user.getUserId() + ".json", user);
        diskService.write(location + "idCounter.json", idCounter);

        databaseCommunicator.register(user);
        return user;
    }
}
