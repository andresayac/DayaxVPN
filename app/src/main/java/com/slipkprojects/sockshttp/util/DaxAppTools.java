package com.slipkprojects.sockshttp.util;

import android.content.Context;

public class DaxAppTools {

    private static DaxAppTools mInstance;

    private Context mContext;

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new DaxAppTools(context);

            // This method will print your certificate signature to the logcat.
            //AndroidTamperingProtectionUtils.getCertificateSignature(context);
        }
    }

    private DaxAppTools(Context context) {
        mContext = context;
    }

	/*public void tamperProtect() {
		AndroidTamperingProtection androidTamperingProtection = new AndroidTamperingProtection.Builder(mContext, APP_SIGNATURE)
			.installOnlyFromPlayStore(false) // By default is set to false.
			.build();

		if (!androidTamperingProtection.validate()) {
			throw new RuntimeException();
		}
	}*/

    private String llz() {
        return mContext.getString(com.slipkprojects.ultrasshservice.R.string.app_name).toLowerCase();
    }

    private String Xja() {
        return mContext.getPackageName().toLowerCase();
    }

    private void xAZ() {
        if (!daxlib.Daxlib.getJSNC(this.llz(), Xja())) {
            throw new RuntimeException();
        }
    }

    public static void checkApp() {
        if (mInstance == null) return;

        mInstance.xAZ();

        //mInstance.tamperProtect();
    }
}
