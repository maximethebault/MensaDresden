package org.tud.mensadresden;

import android.app.Application;

public class MensaDresdenApplication extends Application {
    private boolean hasReceivedLocationPermissionPrompt = false;
    private boolean hasReceivedLocationEnablePrompt = false;

    public boolean hasReceivedLocationPermissionPrompt() {
        return hasReceivedLocationPermissionPrompt;
    }

    public void setHasReceivedLocationPermissionPrompt(boolean hasReceivedLocationPermissionPrompt) {
        this.hasReceivedLocationPermissionPrompt = hasReceivedLocationPermissionPrompt;
    }

    public boolean hasReceivedLocationEnablePrompt() {
        return hasReceivedLocationEnablePrompt;
    }

    public void setHasReceivedLocationEnablePrompt(boolean hasReceivedLocationEnablePrompt) {
        this.hasReceivedLocationEnablePrompt = hasReceivedLocationEnablePrompt;
    }
}
