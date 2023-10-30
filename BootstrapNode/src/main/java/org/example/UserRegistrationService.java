package org.example;

public class UserRegistrationService {
    private final DatabaseCommunicator databaseCommunicator;

    private final DiskService diskService;

    private final LoadBalancer loadBalancer;

    private int idCounter;

    private final String usersLocation;

    private final String idCounterLocation;

    public UserRegistrationService(DiskService diskService, DatabaseCommunicator databaseCommunicator, LoadBalancer loadBalancer) {
        usersLocation = BootstrapProperties.INSTANCE.getUserDataLocation();
        idCounterLocation = usersLocation + "idCounter.json";
        if (!diskService.doesFileExist(usersLocation)) {
            diskService.createDirectory(usersLocation);
        }

        if (diskService.doesFileExist(idCounterLocation)) {
            idCounter = diskService.read(idCounterLocation, Integer.class);
        } else {
            idCounter = 0;
        }

        this.diskService = diskService;
        this.databaseCommunicator = databaseCommunicator;
        this.loadBalancer = loadBalancer;
    }

    public User register(String password) {
        User user = createNewUser(generateUserId(), password);
        saveUserToDisk(user);
        databaseCommunicator.registerUserToDatabaseNodes(user);
        return user;
    }

    private User createNewUser(int id, String password) {
        String node = loadBalancer.getNextNodeName();
        User user = new User();
        user.setUserId(id);
        user.setPassword(password);
        user.setHostName(node);
        return user;
    }

    private int generateUserId() {
        idCounter++;

        diskService.write(idCounterLocation, idCounter);
        return idCounter;
    }

    private void saveUserToDisk(User user) {
        String userFilePath = usersLocation + user.getUserId() + ".json";
        diskService.write(userFilePath, user);
    }
}
