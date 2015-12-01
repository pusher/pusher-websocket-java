package com.pusher.client.channel.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Locale;


@RunWith(MockitoJUnitRunner.class)
public class PresenceChannelImplTurkeyTest extends PresenceChannelImplTest {

    private static Locale defaultLocale;

    @BeforeClass
    public static void overrideLocale() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
    }

    @AfterClass
    public static void resetLocale() {
        Locale.setDefault(defaultLocale);
    }
}
