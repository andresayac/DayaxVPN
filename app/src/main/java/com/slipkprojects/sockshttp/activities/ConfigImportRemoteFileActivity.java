package com.slipkprojects.sockshttp.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.anggrayudi.storage.SimpleStorage;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.slipkprojects.sockshttp.R;
import com.slipkprojects.sockshttp.SocksHttpApp;
import com.slipkprojects.sockshttp.SocksHttpMainActivity;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import com.slipkprojects.ultrasshservice.config.ConfigRemote;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.tunnel.TunnelUtils;
import com.slipkprojects.ultrasshservice.util.FileUtils;
import com.slipkprojects.ultrasshservice.util.ToastUtil;
import io.github.tonnyl.light.Light;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Calendar;

public class ConfigImportRemoteFileActivity
extends BaseActivity
implements CompoundButton.OnCheckedChangeListener, OnClickListener
{
	private static final String TAG = ConfigImportRemoteFileActivity.class.getSimpleName();

	private Settings mConfig;
	private ToastUtil toastutil;

	private FloatingActionButton importconfig;

	private CheckBox validadeCheck;

	private CheckBox configPass;

	private LinearLayout export_ly;
	private SimpleStorage storage;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mConfig = new Settings(this);
		toastutil = new ToastUtil(this);

		doLayout();
		storage = new SimpleStorage(this,null);
		// requista permissões ao armazenamento externo
		requestPermissions();
		try {
			File fileDir = new File(Environment.getExternalStorageDirectory(), "/Dayax");

			if (!fileDir.exists()) {
				fileDir.mkdir();
			}
		}catch (Exception e){
			Log.e(TAG,e.getMessage());
			Light.error(export_ly, getString(R.string.error_permission_writer_required), Snackbar.LENGTH_LONG).show();
			finish();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(com.slipkprojects.ultrasshservice.R.string.error_request_permission))
					.setPositiveButton("Allow", (dialog, which) -> storage.requestFullStorageAccess())
					.setNegativeButton("Cancel", null)
					.create()
					.show();

		}
	}

	@Override
	public boolean onSupportNavigateUp()
	{
		super.onBackPressed();
		return true;
	}

	/**
	 * Main Views
	 */

	private TextView validadeText;
	private EditText urlServer;
	private EditText mensagemEdit;
	private AdView adsBannerView;

	private boolean mConfigPass = false;
	private boolean mIsProteger = false;
	private String mMensagem = "";
	private String file_remote;
	private boolean mPedirSenha = false;
	private boolean mBloquearRoot = false;

	private void doLayout() {
		setContentView(R.layout.import_remote);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// impede autoinicio dos editText
		findViewById(R.id.activity_config_exportMainLinearLayout).requestFocus();
 
		export_ly = (LinearLayout) findViewById(R.id.exportLinearLayout);
		urlServer = (EditText) findViewById(R.id.activity_config_url_remote);

		importconfig = (FloatingActionButton) findViewById(R.id.export_config);
		importconfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View p1)
			{
				String result = urlServer.getText().toString();

				if(isFirstnameValid(result)){
					importRemoteFile(result);
				}else{
					Light.error(export_ly, getString(R.string.error_file_invalid), Snackbar.LENGTH_LONG).show();
				}
			}

			private boolean isFirstnameValid(String text){
				return text.matches("^https:\\/\\/afaya.com.co\\/Apps\\/.*");

			}

			private String importRemoteFile(String url_servers) {
				Log.d(TAG, "importRemoteFile");

				new ConfigRemote(ConfigImportRemoteFileActivity.this, new ConfigRemote.OnUpdateListener() {
					@Override
					public void onUpdateListener(String result) {

						try {
							if (!result.contains("Error on getting data")) {
								InputStream stream = new  ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
								importarConfigInputFile(stream);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				},url_servers).start(true);
				return "";
			}


		});

		adsBannerView = (AdView) findViewById(R.id.load_export);
        if (!SocksHttpApp.DEBUG) {
            adsBannerView.setAdUnitId(SocksHttpApp.ADS_UNITID_BANNER_TEST);
		}
        if (TunnelUtils.isNetworkOnline(this)) {

            adsBannerView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        if (adsBannerView != null) {
                            adsBannerView.setVisibility(View.VISIBLE);
                        }
                    }
                });

            adsBannerView.loadAd(new AdRequest.Builder()
                                 .build());

		}

	}


	public void importarConfigInputFile(InputStream inputFile) {
		try {

			if (!ConfigParser.convertInputAndSave(inputFile, this)) {

				throw new IOException(getString(R.string.error_save_settings));
			}

			long mValidade = new Settings(this)
					.getPrefsPrivate().getLong(Settings.CONFIG_VALIDADE_KEY, 0);

			if (mValidade > 0) {
				SkStatus.logInfo(R.string.log_settings_valid,
						android.text.format.DateFormat.getDateFormat(this).format(mValidade));
			}




			// atualiza views
			SocksHttpMainActivity.updateMainViews(this);

		} catch(IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		Intent intent = new Intent(this,SocksHttpMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}


	private void exportConfiguracao(String nome)
	throws IOException {
		if (!FileUtils.isExternalStorageWritable()) {
			throw new IOException(getString(R.string.error_permission_writer_required));
		}

		File fileDir = new File(Environment.getExternalStorageDirectory(), "/Dayax");

		if (!fileDir.exists()) {
			fileDir.mkdir();
		}

		File fileExport = new File(fileDir, String.format("%s.%s", nome, ConfigParser.FILE_EXTENSAO));
		if (!fileExport.exists()) {
			try {
				if(fileExport.canWrite())
				fileExport.createNewFile();

			} catch(IOException e) {
				Log.e("Error at config save",e.getMessage());
				throw new IOException(getString(R.string.error_save_settings));
			}
		}

		// salva mensagem para ser reutilizada
		if (mIsProteger) {
			mConfig.setMensagemConfigExportar(mMensagem);
		}

		try {
			ConfigParser.convertDataToFile(new FileOutputStream(fileExport), this,
										   mIsProteger, mPedirSenha, mBloquearRoot, mMensagem, mValidade);
		} catch(IOException e) {
			fileExport.delete();
			throw e;
		}
	}


	/**
	 * Validade
	 */

	private long mValidade = 0;

	private void setValidadeDate() {

		// Get Current Date
		Calendar c = Calendar.getInstance();
		final long time_hoje = c.getTimeInMillis();

		c.setTimeInMillis(time_hoje+(1000*60*60*24));

		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);

		mValidade = c.getTimeInMillis();

		final DatePickerDialog dialog = new DatePickerDialog(this,
			new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker p1, int year, int monthOfYear, int dayOfMonth) {
					Calendar c = Calendar.getInstance();
					c.set(year, monthOfYear, dayOfMonth);

					mValidade = c.getTimeInMillis();
				}
			},
			mYear, mMonth, mDay);

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog2, int which) {
					DateFormat df = DateFormat.getDateInstance();
					DatePicker date = dialog.getDatePicker();

					Calendar c = Calendar.getInstance();
					c.set(date.getYear(), date.getMonth(), date.getDayOfMonth());

					mValidade = c.getTimeInMillis();

					if (mValidade < time_hoje) {
						mValidade = 0;


						Light.error(export_ly, getString(R.string.error_date_selected_invalid), Snackbar.LENGTH_LONG).show();

						if (validadeCheck != null)
							validadeCheck.setChecked(false);
					}
					else {
						long dias = ((mValidade-time_hoje)/1000/60/60/24);

						if (validadeText != null) {
							validadeText.setVisibility(View.VISIBLE);
							validadeText.setText(String.format("%s (%s)", dias, df.format(mValidade)));
						}
					}
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mValidade = 0;

					if (validadeCheck != null) {
						validadeCheck.setChecked(false);
					}
				}
			}
		);

		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface v1) {
					mValidade = 0;
					if (validadeCheck != null) {
						validadeCheck.setChecked(false);
					}
				}
			});

		dialog.show();
	}

	private void requestPermissions() {
		FileUtils.requestForPermissionExternalStorage(this);
	}


	/**
	 * Oculta/Mostra layout com opções
	 */

	private int[] idsProtegerViews = {
		R.id.activity_config_exportValidadeCheck,
		R.id.mobile_data,
		R.id.config_pass,
		R.id.activity_config_exportValidadeText,
		R.id.activity_config_exportMensagemEdit,
		R.id.activity_config_exportLayoutMensagemEdit,
		R.id.activity_config_exportBlockRootCheck,
		R.id.activity_config_exportShowLoginScreenCheck
	};

	private int[] idsProtegerChecksView = {
		R.id.mobile_data,
		R.id.config_pass,
		R.id.activity_config_exportValidadeCheck,
		R.id.activity_config_exportBlockRootCheck,
		R.id.activity_config_exportShowLoginScreenCheck
	};

	private void showSegurancaLayout(boolean is) {
		if (is) {

			Light.error(export_ly, getString(R.string.alert_block_settings), Snackbar.LENGTH_LONG).show();
		}
		else {
			for (int id : idsProtegerChecksView) {
				((CheckBox) findViewById(id)).setChecked(false);
			}
		}

		for (int id : idsProtegerViews) {
			findViewById(id).setEnabled(is);
		}
	}


	@Override
	public void onCheckedChanged(CompoundButton p1, boolean is)
	{
		/*switch (p1.getId()) {
			case R.id.activity_config_exportValidadeCheck:
				if (is) {
					setValidadeDate();
				}
				else {
					mValidade = 0;
					if (validadeText != null) {
						validadeText.setVisibility(View.INVISIBLE);
						validadeText.setText("");
					}
				}
				break;

			case R.id.config_pass:
				if (configPass.isChecked()) {
					pass_confirm();
				} else {
					if (configPass != null) {
						configPass.setChecked(false);
					}}
				break;



			case R.id.activity_config_exportProtegerCheck:
				mIsProteger = is;
				showSegurancaLayout(is);
				break;

			case R.id.activity_config_exportShowLoginScreenCheck:
				mPedirSenha = is;
				break;

			case R.id.activity_config_exportBlockRootCheck:
				mBloquearRoot = is;
				break;
		}

		 */
	}


	public void pass_confirm() {
		AlertDialog dialog = new AlertDialog.Builder(this).
			create();
		dialog.setIcon(R.drawable.app_launch);
		dialog.setTitle("Are you sure you want to set password?");
		dialog.setMessage("\n1.Make sure you keep this box checked.\n\n2.Go to SSH Settings and remove password.\n\n3.The user will need to enter the SSH password you removed...  ");
        dialog.setCancelable(false);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.
																	string.ok),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Intent intent2 = new Intent(ConfigImportRemoteFileActivity.this, ConfigGeralActivity.class);
					intent2.setAction(ConfigGeralActivity.OPEN_SETTINGS_SSH);
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent2);
				}
			}
		);

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.
																	string.cancel),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// minimiza app
					dialog.dismiss();
					if (mConfig.getPrivString(Settings.USUARIO_KEY).isEmpty() || (mConfig.getPrivString(Settings.SENHA_KEY).isEmpty())) {
						configPass.setChecked(true);
						}
						else 
					if (configPass != null) {
						configPass.setChecked(false);
					}}
			}
		);

		dialog.show();
	}
	
	@Override
	public void onClick(View p1) {

	}

}
