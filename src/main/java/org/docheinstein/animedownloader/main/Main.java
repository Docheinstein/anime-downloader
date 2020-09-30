package org.docheinstein.animedownloader.main;

import org.docheinstein.animedownloader.ui.main.MainWindow;
import org.docheinstein.commons.http.HttpRequester;

public class Main {
    public static void main(String[] args) {
        HttpRequester.enableTrustAllSocketFactory();
        MainWindow.main(args);
    }
}
