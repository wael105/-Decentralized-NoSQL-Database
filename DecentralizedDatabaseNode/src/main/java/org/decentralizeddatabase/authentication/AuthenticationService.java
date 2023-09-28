package org.decentralizeddatabase.authentication;

import org.decentralizeddatabase.constants.NodeProperties;
import org.decentralizeddatabase.models.User;
import org.decentralizeddatabase.services.DiskService;

public enum AuthenticationService {

    INSTANCE;

    private final DiskService diskService;

    private final String userDataLocation;

    private final String hostName;

    AuthenticationService() {
        diskService = DiskService.INSTANCE;
        this.userDataLocation = NodeProperties.INSTANCE.getUserDataLocation();
        this.hostName = NodeProperties.INSTANCE.getHostName();
    }

    public void registerUser(User user) {
        diskService.write(userDataLocation + user.getUserId() + ".json", user);
    }

    public boolean isUserRegisteredToUseThisNode(int username, String password) {
        if (!diskService.doesFileExist(userDataLocation + username + ".json"))
            return false;

        User user = diskService.read(userDataLocation + username + ".json", User.class);
        return password.equals(user.getPassword()) && hostName.equals(user.getHostName());
    }
}
