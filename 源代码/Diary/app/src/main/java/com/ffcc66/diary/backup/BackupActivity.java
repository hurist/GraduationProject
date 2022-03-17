package com.ffcc66.diary.backup;

import android.Manifest;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.box.sdk.BoxAPIConnection;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.BaseDisposableObserver;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.bean.Tag;
import com.ffcc66.diary.export.ExportActivity;
import com.ffcc66.diary.util.FileUtils;


import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;

public class BackupActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.backup_to_local)
    TextView tvBackupToLocal;
//    @BindView(R.id.backup_to_cloud)
//    TextView tvBackupToCloud;
    @BindView(R.id.restore_local)
    TextView tvRestoreLocal;
//    @BindView(R.id.restore_cloud)
//    TextView tvRestoreCloud;

    private static final String TAG = "BackupActivity";
    private final String[] perems = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};
    private boolean hasPermission = false;

    final String DB_PATH = "/data/data/com.ffcc66.diary/databases/";
    final String BACKUP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Diary/backup/";
    final String BACKUP_IMAGE_PATH = BACKUP_PATH + "image/";
    final String DB_NAME = "diary.db";
    final String DB_SHM = "-shm";
    final String DB_WAL = "-wal";
    final String DB_BACKUP_NAME = "diary_backup.db";

    private final int BACKUP_TO_LOCAL = 0;
    private final int RESTORE_LOCAL = 1;
    private final int BACKUP_TO_CLOUND = 2;
    private final int RESTORE_CLOUND = 3;

    private MaterialDialog waitDialog = null;




    @Override
    public int initLayout() {
        return R.layout.activity_backup;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(view -> finish());

        tvBackupToLocal.setOnClickListener(v -> startTask(BACKUP_TO_LOCAL));
        tvRestoreLocal.setOnClickListener(v -> startTask(RESTORE_LOCAL));
       // tvBackupToCloud.setOnClickListener(v -> startTask(BACKUP_TO_CLOUND));

        waitDialog = new MaterialDialog.Builder(this)
                .title("请稍等")
                .content("正在导出...")
                .progress(true, 0)
                .progressIndeterminateStyle(true).build();
    }

    @Override
    public void initData() {

    }


    private void startTask(int taskNO) {
        checkPermission();
        if (hasPermission) {
            waitDialog.show();
        }
        Observable backup = Observable.create(emitter -> {
            boolean isSuccess = false;
            switch (taskNO) {
                case BACKUP_TO_LOCAL:
                    isSuccess = backToLocal(); break;
                case RESTORE_LOCAL:
                    isSuccess = restoreLocal(); break;
            }
            if (!isSuccess) {
                emitter.onNext(false);
            } else {
                emitter.onComplete();
            }
        });

        //创建一个观察者
        BaseDisposableObserver observer = new BaseDisposableObserver() {
            @Override
            public void onComplete() {
                waitDialog.dismiss();
                String msg = "完成";
                Toast.makeText(BackupActivity.this, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(Object o) {
                Toast.makeText(BackupActivity.this, "缺少权限",Toast.LENGTH_LONG).show();
            }
        };

        backup.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
        register(observer);

    }

    private boolean backToLocal(){
        if (!hasPermission) {
            return false;
        }
        //将数据库文件复制到备份文件夹中
        FileUtils.copyFile(new File(DB_PATH+DB_NAME), BACKUP_PATH, DB_BACKUP_NAME);
        FileUtils.copyFile(new File(DB_PATH+DB_NAME+DB_SHM), BACKUP_PATH, DB_BACKUP_NAME+DB_SHM);
        FileUtils.copyFile(new File(DB_PATH+DB_NAME+DB_WAL), BACKUP_PATH, DB_BACKUP_NAME+DB_WAL);
        //将图片文件复制到备份文件夹中
        File imageFolder = getExternalFilesDir("image");
        if (imageFolder.exists()) {
            File[] images = imageFolder.listFiles();
            for (File file: images) {
                FileUtils.copyFile(file, BACKUP_IMAGE_PATH, file.getName());
            }
        }
        return true;

    }

    private boolean restoreLocal() {
        if (!hasPermission) {
            return false;
        }
        File DBFile = new File(BACKUP_PATH +DB_BACKUP_NAME);
        if (!DBFile.exists()) {
            Toast.makeText(this,"本地备份不存在",Toast.LENGTH_LONG).show();
        } else {
            //将数据库文件移动到放到databases目录中
            FileUtils.copyFile(new File(BACKUP_PATH +DB_BACKUP_NAME), DB_PATH, DB_BACKUP_NAME);
            FileUtils.copyFile(new File(BACKUP_PATH +DB_BACKUP_NAME+DB_SHM), DB_PATH, DB_BACKUP_NAME+DB_SHM);
            FileUtils.copyFile(new File(BACKUP_PATH +DB_BACKUP_NAME+DB_WAL), DB_PATH, DB_BACKUP_NAME+DB_WAL);
            //将图片文件复制到数据文件夹中
            LitePalDB db = LitePalDB.fromDefault(DB_BACKUP_NAME);
            LitePal.use(db);    //通过Litepal读取备份数据库里的数据
            List<Diary> backupDiaryList = LitePal.findAll(Diary.class, true);
            LitePal.useDefault(); //切换回程序数据库
            List<Diary> diaries = LitePal.findAll(Diary.class, true);
            List<Tag> tags = LitePal.findAll(Tag.class, false);
            new File(DB_PATH+DB_BACKUP_NAME).delete();

            List<String> images = new ArrayList<>();
            for (Diary backupDiary: backupDiaryList) {
                boolean exist = false;
                for (Diary diary: diaries) {
                   if (diary.getData() == backupDiary.getData()) { exist = true; break; }
                }
                if (!exist) {
                    boolean tagExist = false;
                    for (Tag tag:tags) {    //判断tag是否存在
                        if (tag.getName().equals(backupDiary.getTag().getName())) {
                            tagExist = true;
                        }
                    }
                    if (!tagExist) {
                        backupDiary.getTag().clearSavedState();
                        backupDiary.getTag().save();
                    }
                    backupDiary.clearSavedState();
                    backupDiary.save();
                    if (!backupDiary.getImg().equals("")) {
                        images.add(backupDiary.getImg());
                    }
                }
            }
            for (String img: images) {  //将备份文件夹的图片复制回程序文件夹中
                File image = new File(BACKUP_IMAGE_PATH + img);
                FileUtils.copyFile(image, getExternalFilesDir("image").getAbsolutePath()+"/", img);
            }

        }
        return true;
    }

//    private boolean backupToCloud() {
//
//
//        if (!hasPermission) {
//            return false;
//        }
//
//        String url = "https://dav.jianguoyun.com/dav/Diary/";
//        String account = "519648226@qq.com";
//        String password = "amfcrjse4knwvcpa";
//
//
//        Sardine sardine = new OkHttpSardine();
//        sardine.setCredentials(account, password, true);
//        try {
//            if (!sardine.exists(url)) {
//                sardine.createDirectory(url);
//            }
//            File file = new File(DB_PATH+DB_NAME);
//            sardine.put(url,file,"");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return true;
//
//    }

    private void checkPermission() {

        if (!EasyPermissions.hasPermissions(this, perems)) {
            EasyPermissions.requestPermissions(this, "备份需要必要的权限，否则无法正常运行", 0, perems);
        } else {
            hasPermission = true;
        }

    }



    /**
     * 权限请求结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        hasPermission = true;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        hasPermission = false;
    }
}
