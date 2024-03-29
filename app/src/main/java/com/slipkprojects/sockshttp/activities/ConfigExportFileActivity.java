package com.slipkprojects.sockshttp.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.widget.*;
import androidx.core.widget.NestedScrollView;
import com.alespero.expandablecardview.ViewAnimation;
import com.anggrayudi.storage.SimpleStorage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.slipkprojects.sockshttp.R;
import com.slipkprojects.sockshttp.SocksHttpApp;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.util.FileUtils;
import com.slipkprojects.ultrasshservice.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

import com.slipkprojects.ultrasshservice.tunnel.TunnelUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.material.snackbar.Snackbar;

import io.github.tonnyl.light.Light;


public class ConfigExportFileActivity
        extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = ConfigExportFileActivity.class.getSimpleName();

    private Settings mConfig;
    private ToastUtil toastutil;

    private FloatingActionButton exportconfig;

    private CheckBox validadeCheck;

    private CheckBox configPass;

    private LinearLayout export_ly;
    private SimpleStorage storage;

    private String android_id;



    // SSH
    private View lyt_expand_input_ssh;
    private NestedScrollView nested_scroll_view_ssh;
    private ImageButton bt_toggle_input_ssh;
    private LinearLayout ly_hide_input_ssh;
    private ImageButton bt_toggle_lock_status_ssh;

    // extra
    private View lyt_expand_input_extra;
    private NestedScrollView nested_scroll_view_extra;
    private ImageButton bt_toggle_input_extra;
    private LinearLayout ly_hide_input_extra;
    private ImageButton bt_toggle_lock_status_extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConfig = new Settings(this);
        toastutil = new ToastUtil(this);

        doLayout();
        storage = new SimpleStorage(this, null);
        // requista permissões ao armazenamento externo
        requestPermissions();
        try {
            File fileDir = new File(Environment.getExternalStorageDirectory(), "/Dayax");

            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    /**
     * Main Views
     */

    private TextView CountSSHCheckText;
    private TextView CountExtraCheckText;
    private TextView validadeText;
    private EditText nomeEdit;
    private EditText mensagemEdit;
    private AdView adsBannerView;

    private boolean mConfigPass = false;
    private boolean mIsProteger = false;
    private String mMessage = "";
    private boolean mRequestPassword = false;
    private boolean mblockRoot = false;


    private int countSSH =0;
    private int countExtra =0;

    @SuppressLint("HardwareIds")
    private void doLayout() {
        setContentView(R.layout.activity_config_export);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // impede autoinicio dos editText
        findViewById(R.id.activity_config_exportMainLinearLayout)
                .requestFocus();

        export_ly = (LinearLayout) findViewById(R.id.exportLinearLayout);
        nomeEdit = (EditText) findViewById(R.id.activity_config_exportNomeEdit);
        CheckBox protegerCheck = (CheckBox) findViewById(R.id.activity_config_exportProtegerCheck);
        validadeCheck = (CheckBox) findViewById(R.id.activity_config_exportValidadeCheck);
        validadeText = (TextView) findViewById(R.id.activity_config_exportValidadeText);
        mensagemEdit = (EditText) findViewById(R.id.activity_config_exportMensagemEdit);
        CheckBox showLoginCheck = (CheckBox) findViewById(R.id.activity_config_exportShowLoginScreenCheck);
        CheckBox blockRootCheck = (CheckBox) findViewById(R.id.activity_config_exportBlockRootCheck);
        configPass = (CheckBox) findViewById(R.id.config_pass);


        // Count SSH block
        bt_toggle_lock_status_ssh = (ImageButton) findViewById(R.id.bt_toggle_lock_status_ssh);
        CountSSHCheckText = (TextView) findViewById(R.id.txt_lock_status_text_ssh);


        // Count Extra block
        bt_toggle_lock_status_extra = (ImageButton) findViewById(R.id.bt_toggle_lock_status_extra);
        CountExtraCheckText = (TextView) findViewById(R.id.txt_lock_status_text_extra);



        showSegurancaLayout(false);
        mensagemEdit.setText(mConfig.getMensagemConfigExportar());

        nested_scroll_view_extra = (NestedScrollView) findViewById(R.id.nested_scroll_view_export);
        bt_toggle_input_extra = (ImageButton) findViewById(R.id.bt_toggle_input_export_extra);
        ly_hide_input_extra = (LinearLayout) findViewById(R.id.export_extra_header);
        lyt_expand_input_extra = (View) findViewById(R.id.lyt_expand_input_extra_export);
        lyt_expand_input_extra.setVisibility(View.GONE);



        bt_toggle_input_extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSectionInputExtra(bt_toggle_input_extra);
            }
        });

        ly_hide_input_extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSectionInputExtra(bt_toggle_input_extra);
            }
        });


        nested_scroll_view_ssh = (NestedScrollView) findViewById(R.id.nested_scroll_view_export_ssh);
        bt_toggle_input_ssh = (ImageButton) findViewById(R.id.bt_toggle_input_export_ssh);
        ly_hide_input_ssh = (LinearLayout) findViewById(R.id.export_ssh_header);
        lyt_expand_input_ssh = (View) findViewById(R.id.lyt_expand_input_ssh_export);
        lyt_expand_input_ssh.setVisibility(View.GONE);


        bt_toggle_input_ssh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSectionInputSSH(bt_toggle_input_ssh);
            }
        });

        ly_hide_input_ssh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSectionInputSSH(bt_toggle_input_ssh);
            }
        });

        validadeCheck.setOnCheckedChangeListener(this);
        protegerCheck.setOnCheckedChangeListener(this);
        configPass.setOnCheckedChangeListener(this);
        showLoginCheck.setOnCheckedChangeListener(this);
        blockRootCheck.setOnCheckedChangeListener(this);
        exportconfig = (FloatingActionButton) findViewById(R.id.export_config);
        exportconfig.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View p1) {
                exportconfig();
                //SkStatus.logInfo("<font color='red'>Log Cleared!</font>");
                // TODO: Implement this method
            }

            private void exportconfig() {

                String nomeConfig = nomeEdit.getText().toString();
                mMessage = mIsProteger ? mensagemEdit.getText().toString() : "";

                if (nomeConfig.isEmpty()) {

                    Light.error(export_ly, getString(R.string.error_empty_name_file), Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (mIsProteger == false || mValidation < 0) {
                    mValidation = 0;
                }

                try {
                    exportConfiguracao(nomeConfig);

                    Light.success(export_ly, getString(R.string.success_export_settings), Snackbar.LENGTH_LONG).show();

                } catch (IOException e) {

                    Light.error(export_ly, getString(R.string.error_export_settings), Snackbar.LENGTH_LONG).show();

                    Toast.makeText(ConfigExportFileActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }

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

    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }

    private void toggleSectionInputSSH(View view) {
        boolean show = toggleArrow(view);
        if (show) {
            ViewAnimation.expand(lyt_expand_input_ssh, new ViewAnimation.AnimListener() {
                @Override
                public void onFinish() {
                    ViewAnimation.nestedScrollTo(nested_scroll_view_ssh, lyt_expand_input_ssh);
                }
            });
        } else {
            ViewAnimation.collapse(lyt_expand_input_ssh);
        }
    }

    private void toggleSectionInputExtra(View view) {
        boolean show = toggleArrow(view);
        if (show) {
            ViewAnimation.expand(lyt_expand_input_extra, new ViewAnimation.AnimListener() {
                @Override
                public void onFinish() {
                    ViewAnimation.nestedScrollTo(nested_scroll_view_extra, lyt_expand_input_extra);
                }
            });
        } else {
            ViewAnimation.collapse(lyt_expand_input_extra);
        }
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

        File fileExport = new File(fileDir, String.format("%s.%s", nome, ConfigParser.FILE_EXTENSIONS));
        if (!fileExport.exists()) {
            try {
                if (fileExport.canWrite())
                    fileExport.createNewFile();

            } catch (IOException e) {
                Log.e("Error at config save", e.getMessage());
                throw new IOException(getString(R.string.error_save_settings));
            }
        }

        // salva mensagem para ser reutilizada
        if (mIsProteger) {
            mConfig.setMensagemConfigExportar(mMessage);
        }

        try {
            ConfigParser.convertDataToFile(new FileOutputStream(fileExport), this, mIsProteger, mRequestPassword, mblockRoot, mMessage, mValidation);
        } catch (IOException e) {
            fileExport.delete();
            throw e;
        }
    }


    /**
     * Validade
     */

    private long mValidation = 0;

    private void setValidadeDate() {

        // Get Current Date
        Calendar c = Calendar.getInstance();
        final long time_hoje = c.getTimeInMillis();

        c.setTimeInMillis(time_hoje + (1000 * 60 * 60 * 24));

        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        mValidation = c.getTimeInMillis();

        final DatePickerDialog dialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker p1, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);

                        mValidation = c.getTimeInMillis();
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

                        mValidation = c.getTimeInMillis();

                        if (mValidation < time_hoje) {
                            mValidation = 0;


                            Light.error(export_ly, getString(R.string.error_date_selected_invalid), Snackbar.LENGTH_LONG).show();

                            if (validadeCheck != null)
                                validadeCheck.setChecked(false);
                        } else {
                            long dias = ((mValidation - time_hoje) / 1000 / 60 / 60 / 24);

                            if (validadeText != null) {
                                validadeText.setVisibility(View.VISIBLE);
                                validadeText.setText(String.format("%s (%s)", dias, df.format(mValidation)));
                            }
                        }
                    }
                }
        );

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mValidation = 0;

                        if (validadeCheck != null) {
                            validadeCheck.setChecked(false);
                        }
                    }
                }
        );

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface v1) {
                mValidation = 0;
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
            R.id.activity_config_exportShowLoginScreenCheck,
            R.id.activity_config_exportBlockHWID,
            R.id.activity_config_exportTerminal,
            R.id.activity_config_exportTorrent,
            R.id.activity_config_export_ssh_host_port,
            R.id.activity_config_export_ssh_username_password
    };

    private int[] idsProtegerChecksView = {
            R.id.mobile_data,
            R.id.config_pass,
            R.id.activity_config_exportValidadeCheck,
            R.id.activity_config_exportBlockRootCheck,
            R.id.activity_config_exportShowLoginScreenCheck,
            R.id.activity_config_exportBlockHWID,
            R.id.activity_config_exportTerminal,
            R.id.activity_config_exportTorrent,
            R.id.activity_config_export_ssh_host_port,
            R.id.activity_config_export_ssh_username_password
    };

    private void showSegurancaLayout(boolean is) {
        if (is) {
            Light.error(export_ly, getString(R.string.alert_block_settings), Snackbar.LENGTH_LONG).show();
            ((CheckBox) findViewById(R.id.mobile_data)).setChecked(true);
            ((CheckBox) findViewById(R.id.activity_config_export_ssh_host_port)).setChecked(true);
            ((CheckBox) findViewById(R.id.activity_config_export_ssh_username_password)).setChecked(true);
        } else {
            for (int id : idsProtegerChecksView) {
                ((CheckBox) findViewById(id)).setChecked(false);
            }

        }

        for (int id : idsProtegerViews) {
            findViewById(id).setEnabled(is);
        }
    }

    private void setCountExtra(boolean type){
         countExtra  = (type)? countExtra++: countExtra--;
         if(countExtra>=1) {
             CountExtraCheckText.setVisibility(View.VISIBLE);
             bt_toggle_lock_status_extra.setVisibility(View.VISIBLE);
             CountExtraCheckText.setText(String.format("%s ", countExtra));
         }else{
             CountExtraCheckText.setVisibility(View.GONE);
             bt_toggle_lock_status_extra.setVisibility(View.GONE);
         }
    }

    @Override
    public void onCheckedChanged(CompoundButton p1, boolean is) {

        switch (p1.getId()) {
            case R.id.activity_config_exportValidadeCheck:
                setCountExtra(is);
                if (is) {
                    setValidadeDate();
                } else {
                    mValidation = 0;
                    if (validadeText != null) {
                        validadeText.setVisibility(View.INVISIBLE);
                        validadeText.setText("");
                    }
                }
                break;

            case R.id.config_pass:
                setCountExtra(is);
                if (configPass.isChecked()) {
                    pass_confirm();
                } else {
                    if (configPass != null) {
                        configPass.setChecked(false);
                    }
                }
                break;


            case R.id.activity_config_exportProtegerCheck:
                mIsProteger = is;
                setCountExtra(is);
                showSegurancaLayout(is);
                break;

            case R.id.activity_config_exportShowLoginScreenCheck:
                setCountExtra(is);
                mRequestPassword = is;
                break;

            case R.id.activity_config_exportBlockRootCheck:
                mblockRoot = is;
                setCountExtra(is);
                break;
        }
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
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent2 = new Intent(ConfigExportFileActivity.this, ConfigGeralActivity.class);
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
                        if (mConfig.getPrivString(Settings.USUARIO_KEY).isEmpty() || (mConfig.getPrivString(Settings.PASS_KEY).isEmpty())) {
                            configPass.setChecked(true);
                        } else if (configPass != null) {
                            configPass.setChecked(false);
                        }
                    }
                }
        );

        dialog.show();
    }

    @Override
    public void onClick(View p1) {
        switch (p1.getId()) {
            case R.id.activity_config_exportButton:
                String nomeConfig = nomeEdit.getText().toString();
                mMessage = mIsProteger ? mensagemEdit.getText().toString() : "";

                if (nomeConfig.isEmpty()) {

                    Light.error(export_ly, getString(R.string.error_empty_name_file), Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (mIsProteger == false || mValidation < 0) {
                    mValidation = 0;
                }

                try {
                    exportConfiguracao(nomeConfig);


                    Light.success(export_ly, getString(R.string.success_export_settings), Snackbar.LENGTH_LONG).show();
                } catch (IOException e) {

                    Light.error(export_ly, getString(R.string.error_export_settings), Snackbar.LENGTH_LONG).show();

                    Toast.makeText(ConfigExportFileActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }

                onBackPressed();
                break;
        }
    }

}
