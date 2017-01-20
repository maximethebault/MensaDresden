package org.tud.mensaapp.model.service.mensa;

public interface FetchMensaListener<T> {
    void onStarted();

    void onSuccess(boolean wasMensaListUpdated, T results);

    void onFail(ErrorType errorType, String error);
}