package org.tud.mensadresden.finding.offers.service;

import org.tud.mensadresden.finding.offers.model.Mensa;

import java.util.List;

public interface FetchMensaListener {
    void onStarted();

    void onSuccess(boolean wasMensaListUpdated, List<Mensa> mensas);

    void onFail(ErrorType errorType, String error);
}