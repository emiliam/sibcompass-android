package fi.yle.sibkompassi.extension;

public class DownloaderService extends com.google.android.vending.expansion.downloader.impl.DownloaderService {  
    public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj6MomLkhtH1J7cuh3VfqdrCS3BIFmvS/UESekdlI5osGxXh8BwquSPu0F2GbScg9VdWgj+VP6VR+6E9QjvlmQNjz99HUwsXhhsn2lduPAVkN0/WUDXVYhbcOWePF9BECbzAUcWMVgrf7DHDuHHoNvO8chasDvY7Rl2mLCWubYbUTB9AakrcJFTS9HcCNX1Hiqhw9QTZn8XrMPW64hvhztEyMP9wTy2wCWgsySbhY4BuSkrn5HOuBKIOl8OUsiBC8Mrm1GBkYWm/JAjOsHQtw/lMTo8R0zm0SEqmk9c62sufjX62yo29tNX23mLHHY125blJN2yy00+1DvFn8zYlExwIDAQAB"; 
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