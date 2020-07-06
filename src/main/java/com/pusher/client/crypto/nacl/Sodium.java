package com.pusher.client.crypto.nacl;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;

public abstract class Sodium {

    private static LazySodiumJava lazySodium = null;

    public static synchronized LazySodiumJava getInstance() {
        if (lazySodium == null) {
            lazySodium = new LazySodiumJava(new SodiumJava());
        }

        return lazySodium;
    }

}
