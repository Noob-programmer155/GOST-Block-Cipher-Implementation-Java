import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.*;

public class Gost {
    private int[][] SBox = {
            {4 ,10 ,9 ,2 ,13 ,8 ,0 ,14 ,6 ,11 ,1 ,12 ,7 ,15 ,5 ,3},
            {14, 11, 4, 12, 6 ,13 ,15 ,10 ,2 ,3 ,8 ,1 ,0 ,7 ,5 ,9},
            {5, 8, 1, 13, 10, 3, 4, 2, 14, 15, 12, 7, 6, 0, 9, 11},
            {7, 13, 10, 1, 0, 8, 9, 15, 14, 4, 6, 12, 11, 2, 5, 3},
            {6, 12, 7, 1, 5, 15, 13, 8, 4, 10, 9, 14, 0, 3, 11, 2},
            {4, 11, 10, 0, 7, 2, 1, 13, 3, 6, 8, 5, 9, 12, 15, 14},
            {13, 11, 4, 1, 3, 15, 5, 9, 0, 10, 14, 7, 6, 8, 2, 12},
            {1, 15, 13, 0, 5, 7, 10, 4, 9, 2, 3, 14, 6, 11, 8, 12}
    };
    int LEN = 256;
    Gost(int[][] SBox) {
        if(SBox != null){
            this.SBox = SBox;
        }
    }

    private byte[] toByte(int[] data){
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) data[i];
        }
        return bytes;
    }

    private int[] toInt(byte[] data) {
        int intArr[] = new int[data.length];
        for(int i = 0; i < intArr.length; i++) {
            int ye = data[i];
            if(ye < 0) {
                ye = 256 + ye;
            }
            intArr[i] = ye;
        }
        return intArr;
    }

    private String reverse(byte[] text) {
        byte[] reverse = new byte[text.length];
        for (int i=0;i<text.length;i++){
            reverse[text.length - i - 1] = text[i];
        }
        return new String(reverse);
    }

    private String[] passInit(String key) {
        String[] keys = new String[8];
        if(key.length() != 32) {
            System.err.println("Must 32 length of character");
            return null;
        }
        String bin = "";
        for (char i : key.toCharArray()){
            bin += String.format("%8s",Integer.toBinaryString(i)).replaceAll(" ","0");
        }
        int start = 0;
        for (int i = 0;i < 8;i++) {
            keys[i] = reverse(bin.substring(start,start+32).getBytes());
            start += 32;
        }
        return keys;
    }

    private byte[] encryptMain(String msg, String password) {
        String[] keys = passInit(password);
        // L0 + R0
        String all = "";
        for (char j:msg.toCharArray()) {
            all += String.format("%8s",Long.toBinaryString(j)).replaceAll(" ","0");
        }
        all = reverse(all.getBytes());
        String L = all.substring(0,32);
        String R = all.substring(32);
        int p = 0;
        for (int u = 0;u < 32;u++) {
            if (u == 24) {
                p = 7;
            }
            if (p > 7) {
                p = 0;
            }
            long res = Long.sum(Long.parseLong(R,2), Long.parseLong(keys[p],2)) % (long) Math.pow(2,32);
            String bn = String.format("%32s",Long.toBinaryString(res)).replaceAll(" ","0");
            String h = "";
            int r = 0;
            for (int i = 0;i < 8;i++) {
                h += String.format("%4s",Long.toBinaryString(SBox[i][Integer.parseInt(bn.substring(r, 4*(i+1)),2)])).replaceAll(" ","0");
                r += 4;
            }
            String kjw = (h + h.substring(0,11)).substring(11);
            long lk = Long.parseLong(kjw,2) ^ Long.parseLong(L,2);
            if (u >= 31) {
                L = String.format("%32s",Long.toBinaryString(lk)).replaceAll(" ","0");
            } else {
                L = R;
                R = String.format("%32s",Long.toBinaryString(lk)).replaceAll(" ","0");
            }
            if(u >= 24) {
                p--;
            } else {
                p++;
            }
        }
        String u = reverse((L+R).getBytes());
        int[] data = new int[8];
        int f = 0;
        for (int v = 0;v < 8;v++) {
            data[v] = Integer.parseInt(u.substring(f,8*(v+1)),2);
            f += 8;
        }
        return toByte(data);
    }

    private String decryptMain(byte[] enc, String password) {
        String[] keys = passInit(password);
        int[] encrypted = toInt(enc);
        // L0 + R0
        String all = "";
        for (int j:encrypted) {
            all += String.format("%8s",Long.toBinaryString(j)).replaceAll(" ","0");
        }
        all = reverse(all.getBytes());
        String L = all.substring(0,32);
        String R = all.substring(32);
        int p = 0;
        for (int u = 0;u < 32;u++) {
            if (u == 8) {
                p = 7;
            }
            if (p < 0) {
                p = 7;
            }
            long res = Long.sum(Long.parseLong(R,2), Long.parseLong(keys[p],2)) % (long) Math.pow(2,32);
            String bn = String.format("%32s",Long.toBinaryString(res)).replaceAll(" ","0");
            String h = "";
            int r = 0;
            for (int i = 0;i < 8;i++) {
                h += String.format("%4s",Long.toBinaryString(SBox[i][Integer.parseInt(bn.substring(r, 4*(i+1)),2)])).replaceAll(" ","0");
                r += 4;
            }
            String kjw = (h + h.substring(0,11)).substring(11);
            long lk = Long.parseLong(kjw,2) ^ Long.parseLong(L,2);
            if (u >= 31) {
                L = String.format("%32s",Long.toBinaryString(lk)).replaceAll(" ","0");
            } else {
                L = R;
                R = String.format("%32s",Long.toBinaryString(lk)).replaceAll(" ","0");
            }
            if(u >= 8) {
                p--;
            } else {
                p++;
            }
        }
        String u = reverse((L+R).getBytes());
        all = "";
        int f = 0;
        for (int v = 0;v < 8;v++) {
            all += (char) Long.parseLong(u.substring(f,8*(v+1)),2);
            f += 8;
        }
        return all;
    }

    public byte[] encrypt(String msg, String key) throws ExecutionException, InterruptedException {
        var dtmsg = new Object(){ String message = "";};
        int g = 0;
        var data = new Object() { byte[] dt = new byte[msg.length()];};
        if (msg.length() % 8 != 0) {
            g = (int) Math.floor(msg.length() / 8)+1;
            data.dt = new byte[g*8];
            String mn = "";
            for (int w=0;w<g*8-msg.length();w++) {
                mn += " ";
            }
            dtmsg.message = msg + mn;
        }
        ExecutorService thp = Executors.newFixedThreadPool(1);
        for (int h=0;h < g;h++) {
            var wr = new Integer(h);
            Future<byte[]> dt = thp.submit(() -> {
                return encryptMain(dtmsg.message.substring(wr*8,(wr+1)*8),key);
            });
            System.arraycopy(dt.get(),0,data.dt,wr*8,dt.get().length);
        }
        if (msg.length() % 8 != 0) {
            byte[] dt = encryptMain(dtmsg.message.substring(8*(g-1)),key);
            System.arraycopy(dt,0,data.dt,8*(g-1),dt.length);
        }
        thp.shutdown();
        return data.dt;
    }

    public String decrypt(byte[] enc, String key) throws ExecutionException, InterruptedException {
        var data = new Object(){String text = "";};
        byte[] encdata = enc;
        if (enc.length % 8 != 0) {
            throw new RuntimeException("Not Permited !!!");
        }
        ExecutorService thp = Executors.newFixedThreadPool(1);
        for (int h=0;h < Math.ceil(enc.length/8);h++) {
            var wr = new Integer(h);
            Future<String> tx = thp.submit(() -> {
                return decryptMain(Arrays.copyOfRange(enc,wr*8,(wr+1)*8),key);
            });
            data.text += tx.get();
        }
        thp.shutdown();
        return data.text;
    }
}
