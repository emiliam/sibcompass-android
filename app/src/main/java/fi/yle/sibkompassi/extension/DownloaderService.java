package fi.yle.sibkompassi.extension;

public class DownloaderService extends com.google.android.vending.expansion.downloader.impl.DownloaderService {  
    public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvLgeR6ItZ2FLUEZ9DHl5IGgD5H14kjs3HDo9TjO0+ks8i8eMeIFuhenx2g4xy8HCSdGL5ZF4CMaVHEywfDNhPZ138w4gGAPrkSrgzHJJqTSKIzmJ/HB0RzPSsKeD4N5yAbQf5FTvKCQbOsH6uYzXEcll6AbJ7ROnjlqYU1wcSpxThVgsHpXcH4FMXEcNttelc4m1FEkdP3e49HdCzEZUDlpr0iVdQid++sAMEiBsc6rK6BymVGaNCKWc9phG+FWrPmU7CJJZxEZj+mUqJDTlJWdkqR8b6zO5sSLa7Hu9uUbs4VaAfOci7yEZ6SRGtkgbBAKjvefhaAXrSuzX5z/AuQIDAQAB";
    private static final byte[] SALT = new byte[]{64, -2, 33, 20,-14,-17,-29,10,-17,92,24,-23,-20,24,14,-53,22,-44,-27,16};

    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    @Override
    public byte[] getSALT() {
        return SALT;
    }

    @Override
    public String getAlarmReceiverClassName() {
        return DownloaderServiceBroadcastReceiver.class.getName();
    }
}