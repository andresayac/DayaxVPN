package com.slipkprojects.ultrasshservice.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.kimchangyoun.rootbeerFresh.RootBeer;
import com.slipkprojects.ultrasshservice.R;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.util.Cripto;
import com.slipkprojects.ultrasshservice.util.FileUtils;
import com.slipkprojects.ultrasshservice.util.securepreferences.crypto.Cryptor;
import com.slipkprojects.ultrasshservice.util.securepreferences.model.SecurityConfig;

import java.io.*;
import java.util.Calendar;
import java.util.Properties;

/**
 * @author SlipkHunter
 */
public class ConfigParser {
    private static final String TAG = ConfigParser.class.getSimpleName();


    public static final String FILE_EXTENSIONS = "dax";
    private static final String KEY_PASSWORD = "cinbdf665$4";

    private static final String
            SETTING_VERSION = "file.appVersionCode",
            SETTING_VALIDATE = "file.validate",
            SETTING_PROTEGE = "file.protege",
            SETTING_AUTHOR_MSG = "file.msg";


    public static boolean convertRemoteAndSave(String input, Context mContext) {
        Properties mConfigFile = new Properties();
        Settings settings = new Settings(mContext);
        SharedPreferences.Editor prefsEdit = settings.getPrefsPrivate().edit();


        return false;
    }

    public static boolean convertInputAndSave(InputStream input, Context mContext)
            throws IOException {
        Properties mConfigFile = new Properties();

        Settings settings = new Settings(mContext);
        SharedPreferences.Editor prefsEdit = settings.getPrefsPrivate()
                .edit();

        try {

            InputStream decodedInput = decodeInput(input);
            Log.d(TAG, "decodedInput" + decodedInput);

            try {
                mConfigFile.loadFromXML(decodedInput);
            } catch (FileNotFoundException e) {
                throw new IOException("File Not Found");
            } catch (IOException e) {
                throw new Exception("Error Unknown", e);
            }

            //  check version
            int versionCode = Integer.parseInt(mConfigFile.getProperty(SETTING_VERSION));

            // if (versionCode > getBuildId(mContext)) {
            // 	throw new IOException(mContext.getString(R.string.alert_update_app));
            // }

            // mIsProtege check
            String msg = mConfigFile.getProperty(SETTING_AUTHOR_MSG);
            boolean mIsProtege = mConfigFile.getProperty(SETTING_PROTEGE).equals("1") ? true : false;
            long mValidate = 0;

            try {
                mValidate = Long.parseLong(mConfigFile.getProperty(SETTING_VALIDATE));
            } catch (Exception e) {
                // throw new IOException(mContext.getString(R.string.alert_update_app));
            }

            if (!mIsProtege || mValidate < 0) {
                mValidate = 0;
            } else if (mValidate > 0 && isValidityExpired(mValidate)) {
                throw new IOException(mContext.getString(R.string.error_settings_expired));
            }

            // block root
            boolean isBlockRoot = false;
            String _blockRoot = mConfigFile.getProperty("blockRoot");
            if (_blockRoot != null) {
                isBlockRoot = _blockRoot.equals("1") ? true : false;
                if (isBlockRoot) {
                    if (isDeviceRooted(mContext)) {
                        throw new IOException(mContext.getString(R.string.error_root_detected));
                    }
                }
            }
            try {
                String mServers = mConfigFile.getProperty(Settings.SERVER_KEY);
                String mServersPort = mConfigFile.getProperty(Settings.SERVERS_PORT_KEY);
                String mUser = mConfigFile.getProperty(Settings.USUARIO_KEY);
                String mPass = mConfigFile.getProperty(Settings.PASS_KEY);

                int mPortLocal = Integer.parseInt(mConfigFile.getProperty(Settings.PORT_LOCAL_KEY));
                int mTunnelType;

                String key_code = mConfigFile.getProperty(Settings.CODE_KEY);
                String nameserver = mConfigFile.getProperty(Settings.NAMESERVER_KEY);
                String dns = mConfigFile.getProperty(Settings.DNS_KEY);

                String _tunnelType = mConfigFile.getProperty(Settings.TUNNELTYPE_KEY);
                // Maintain compatibility
                switch (_tunnelType) {
                    case Settings.TUNNEL_TYPE_SSH_PROXY:
                        mTunnelType = Integer.parseInt(_tunnelType);
                        break;
                    case Settings.TUNNEL_TYPE_SSH_DIRECT:
                        mTunnelType = Settings.bTUNNEL_TYPE_SSH_DIRECT;
                        break;
                    case Settings.TUNNEL_TYPE_PAY_SSL:
                        mTunnelType = Settings.bTUNNEL_TYPE_PAY_SSL;
                        break;
                    case Settings.TUNNEL_TYPE_SSH_SSLTUNNEL:
                        mTunnelType = Settings.bTUNNEL_TYPE_SSL_TLS;
                        break;
                    default:
                        mTunnelType = Settings.bTUNNEL_TYPE_SSH_DIRECT;

                }

                if (mServers == null) {
                    throw new Exception();
                }

                String _proxyIp = mConfigFile.getProperty(Settings.PROXY_IP_KEY);
                String _proxyPort = mConfigFile.getProperty(Settings.PROXY_PORTA_KEY);
                prefsEdit.putString(Settings.PROXY_IP_KEY, _proxyIp != null ? _proxyIp : "");
                prefsEdit.putString(Settings.PROXY_PORTA_KEY, _proxyPort != null ? _proxyPort : "");
                prefsEdit.putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, !mConfigFile.getProperty(Settings.PROXY_USAR_DEFAULT_PAYLOAD).equals("1") ? false : true);
                String ssl = mConfigFile.getProperty(Settings.CUSTOM_SNI);
                prefsEdit.putString(Settings.CUSTOM_SNI, ssl != null ? ssl : "");
                String _customPayload = mConfigFile.getProperty(Settings.CUSTOM_PAYLOAD_KEY);
                prefsEdit.putString(Settings.CUSTOM_PAYLOAD_KEY, _customPayload != null ? _customPayload : "");

                if (mIsProtege) {
                    prefsEdit.putString(Settings.CONFIG_MENSAGEM_KEY, msg != null ? msg : "");

                    new Settings(mContext)
                            .setModoDebug(false);

                    String request_login = mConfigFile.getProperty("file.request_login");
                    if (request_login != null)
                        prefsEdit.putBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, request_login.equals("1") ? true : false);
                    else
                        prefsEdit.putBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false);
                } else {
                    prefsEdit.putString(Settings.CONFIG_MENSAGEM_KEY, "");
                    prefsEdit.putBoolean(Settings.CONFIG_INPUT_PASSWORD_KEY, false);
                }

                prefsEdit.putString(Settings.CODE_KEY, key_code);
                prefsEdit.putString(Settings.NAMESERVER_KEY, nameserver);
                prefsEdit.putString(Settings.DNS_KEY, dns);

                prefsEdit.putString(Settings.SERVER_KEY, mServers);
                prefsEdit.putString(Settings.SERVERS_PORT_KEY, mServersPort);
                prefsEdit.putString(Settings.USUARIO_KEY, mUser);
                prefsEdit.putString(Settings.PASS_KEY, mPass);
                prefsEdit.putString(Settings.PORT_LOCAL_KEY, Integer.toString(mPortLocal));
                prefsEdit.putInt(Settings.TUNNELTYPE_KEY, mTunnelType);
                prefsEdit.putBoolean(Settings.CONFIG_PROTEGER_KEY, mIsProtege);
                prefsEdit.putLong(Settings.CONFIG_VALIDADE_KEY, mValidate);
                prefsEdit.putBoolean(Settings.BLOQUEAR_ROOT_KEY, isBlockRoot);
                String _isDnsForward = mConfigFile.getProperty(Settings.DNSFORWARD_KEY);
                boolean isDnsForward = _isDnsForward != null && _isDnsForward.equals("0") ? false : true;
                String dnsResolver = mConfigFile.getProperty(Settings.DNSRESOLVER_KEY);
                settings.setVpnDnsForward(isDnsForward);
                //settings.setVpnDnsResolver(dnsResolver);

                String _isUdpForward = mConfigFile.getProperty(Settings.UDPFORWARD_KEY);
                boolean isUdpForward = _isUdpForward != null && _isUdpForward.equals("1") ? true : false;
                String udpResolver = mConfigFile.getProperty(Settings.UDPRESOLVER_KEY);
                settings.setVpnUdpForward(isUdpForward);
                settings.setVpnUdpResolver(udpResolver);
            } catch (Exception e) {
                if (settings.getModoDebug()) {
                    SkStatus.logException("Error Settings", e);
                }
                throw new IOException(mContext.getString(R.string.error_file_settings_invalid));
            }

            return prefsEdit.commit();

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(mContext.getString(R.string.error_file_invalid), e);
        } catch (Throwable e) {
            throw new IOException(mContext.getString(R.string.error_file_invalid));
        }
    }

    public static void convertDataToFile(OutputStream fileOut, Context mContext, boolean mIsProtege, boolean mRequestPassword, boolean isBlockRoot, String mMessage, long mValidation)
            throws IOException {

        Properties mConfigFile = new Properties();
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

        Settings settings = new Settings(mContext);
        SharedPreferences prefs = settings.getPrefsPrivate();
        try {
            int targetId = getBuildId(mContext);
            // for beta versions
            targetId = 24;

            mConfigFile.setProperty(SETTING_VERSION, Integer.toString(targetId));
            mConfigFile.setProperty(SETTING_AUTHOR_MSG, mMessage);
            mConfigFile.setProperty(SETTING_PROTEGE, mIsProtege ? "1" : "0");
            mConfigFile.setProperty("blockRoot", isBlockRoot ? "1" : "0");
            mConfigFile.setProperty(SETTING_VALIDATE, Long.toString(mValidation));
            String server = prefs.getString(Settings.SERVER_KEY, "");
            String server_port = prefs.getString(Settings.SERVERS_PORT_KEY, "");
            if (mIsProtege && (server.isEmpty() || server_port.isEmpty())) {
                throw new Exception();
            }
            mConfigFile.setProperty(Settings.SERVER_KEY, server);
            mConfigFile.setProperty(Settings.SERVERS_PORT_KEY, server_port);
            mConfigFile.setProperty(Settings.USUARIO_KEY, prefs.getString(Settings.USUARIO_KEY, ""));
            mConfigFile.setProperty(Settings.PASS_KEY, prefs.getString(Settings.PASS_KEY, ""));
            mConfigFile.setProperty(Settings.PORT_LOCAL_KEY, prefs.getString(Settings.PORT_LOCAL_KEY, "1080"));
            mConfigFile.setProperty(Settings.TUNNELTYPE_KEY, Integer.toString(prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSL_TLS)));
            mConfigFile.setProperty(Settings.PROXY_IP_KEY, prefs.getString(Settings.PROXY_IP_KEY, ""));
            mConfigFile.setProperty(Settings.PROXY_PORTA_KEY, prefs.getString(Settings.PROXY_PORTA_KEY, ""));
            String isDefaultPayload = prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true) ? "1" : "0";
            String customPayload = prefs.getString(Settings.CUSTOM_PAYLOAD_KEY, "");
            String ssl = prefs.getString(Settings.CUSTOM_SNI, "");

            String key_code = prefs.getString(Settings.CODE_KEY, "");
            String nameserver = prefs.getString(Settings.NAMESERVER_KEY, "");
            String dns = prefs.getString(Settings.DNS_KEY, "");

            if (mIsProtege && isDefaultPayload.equals("0") && customPayload.isEmpty()) {
                throw new IOException();
            }

            int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
            if (tunnelType == Settings.bTUNNEL_TYPE_SSL_TLS) {
                if (mIsProtege && ssl.isEmpty()) {
                    throw new IOException();
                }
            } else if (tunnelType == Settings.bTUNNEL_TYPE_SLOWDNS) {
                if (mIsProtege && (key_code.isEmpty() || nameserver.isEmpty() || dns.isEmpty())) {
                    throw new IOException();
                }
            }

            mConfigFile.setProperty(Settings.PROXY_USAR_DEFAULT_PAYLOAD, isDefaultPayload);
            mConfigFile.setProperty(Settings.CUSTOM_PAYLOAD_KEY, customPayload);
            mConfigFile.setProperty(Settings.CODE_KEY, key_code);
            mConfigFile.setProperty(Settings.NAMESERVER_KEY, nameserver);
            mConfigFile.setProperty(Settings.DNS_KEY, dns);
            mConfigFile.setProperty(Settings.CUSTOM_SNI, ssl);
        } catch (Exception e) {
            throw new IOException(mContext.getString(R.string.error_file_settings_invalid));
        }

        try {
            mConfigFile.storeToXML(tempOut,
                    "Configuration file");
        } catch (FileNotFoundException e) {
            throw new IOException("File Not Found");
        } catch (IOException e) {
            throw new IOException("Error Unknown", e);
        }

        try {
            InputStream input_encoded = encodeInput(new ByteArrayInputStream(tempOut.toByteArray()));

            FileUtils.copiarArquivo(input_encoded, fileOut);
        } catch (Throwable e) {
            throw new IOException(mContext.getString(R.string.error_save_settings));
        }
    }

    private static final Cryptor mCrypto;

    static {
        mCrypto = Cryptor.initWithSecurityConfig(new SecurityConfig.Builder(new String(new byte[]{69, 100, 1,})).build());
    }

    private static InputStream encodeInput(InputStream in) throws Throwable {
        String strBase64 = mCrypto.encryptToBase64(getBytesArrayInputStream(in).toByteArray());
        return new ByteArrayInputStream(strBase64.getBytes());
    }

    private static InputStream decodeInput(InputStream in) throws Throwable {
        byte[] byteDecrypt;

        ByteArrayOutputStream byteArrayOut = getBytesArrayInputStream(in);

        try {
            byteDecrypt = mCrypto.decryptFromBase64(byteArrayOut.toString());
        } catch (IllegalArgumentException e) {
            // decode old config
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Cripto.decrypt(KEY_PASSWORD, new ByteArrayInputStream(byteArrayOut.toByteArray()), out);
            byteDecrypt = out.toByteArray();
        }

        return new ByteArrayInputStream(byteDecrypt);
    }

    public static ByteArrayOutputStream getBytesArrayInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer;
    }


    /**
     * Utils
     */

    public static boolean isValidityExpired(long validateDateMillis) {
        if (validateDateMillis == 0) {
            return false;
        }

        // Get Current Date
        long date_actual = Calendar.getInstance()
                .getTime().getTime();

        if (date_actual >= validateDateMillis) {
            return true;
        }

        return false;
    }

    public static int getBuildId(Context context) throws IOException {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new IOException("Build ID not found");
        }
    }

    public static boolean isDeviceRooted(Context context) {
        /*for (String pathDir : System.getenv("PATH").split(":")){
		 if (new File(pathDir, "su").exists()) {
		 return true;
		 }
		 }

		 Process process = null;
		 try {
		 process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
		 BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
		 if (in.readLine() != null) return true;
		 return false;
		 } catch (Throwable t) {
		 return false;
		 } finally {
		 if (process != null) process.destroy();
		 }*/

        RootBeer rootBeer = new RootBeer(context);

        //boolean experiementalTests = rootBeer.checkForMagiskNative();

        return rootBeer.detectRootManagementApps() || rootBeer.detectPotentiallyDangerousApps() || rootBeer.checkForBinary("su")
                || rootBeer.checkForDangerousProps() || rootBeer.checkForRWPaths()
                || rootBeer.detectTestKeys() || rootBeer.checkSuExists() || rootBeer.checkForRootNative() || rootBeer.checkForMagiskBinary();
    }

}
